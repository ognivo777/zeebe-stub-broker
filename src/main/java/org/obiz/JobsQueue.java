package org.obiz;

import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.subscription.UniEmitter;
import io.vertx.core.Vertx;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.obiz.entity.EnqueueResult;
import org.obiz.entity.JobRequest;
import org.obiz.entity.JobResult;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.function.Consumer;

@ApplicationScoped
public class JobsQueue {

    private final int bufferSize = 500;

    @Inject
    Vertx vertx;

    // Named per worker in-memory job queues
    private final ConcurrentMap<String,WorkerContext> workers = new ConcurrentHashMap<>();

    private final Map<Long, UniEmitter<? super JobResult>> responseEmitters = new ConcurrentHashMap<>();

    public EnqueueResult enqueue(JobRequest jobRequest) {
        WorkerContext workerContext = getWorkerContext(jobRequest.getWorker());
        BlockingQueue<JobRequest> queue = workerContext.getJobsQueue();
        Lock workerLock = workerContext.getWorkerLock();
        workerLock.lock();
        try {
            var workerEmitters = workerContext.getEmittersForWaitingWorkers();
            if (workerEmitters!=null) {
                Lock emitterLock = workerContext.getEmitterLock();
                emitterLock.lock();
                try {
                    UniEmitter<? super JobRequest> multiEmitter = workerEmitters.poll();
                    if (multiEmitter != null) {
                        multiEmitter.complete(jobRequest);
                        return new EnqueueResult(true, false, queue.remainingCapacity(), jobRequest);
                    }
                } finally {
                    emitterLock.unlock();
                }
            }
            if(queue.offer(jobRequest)){
                return new EnqueueResult(true, true, queue.remainingCapacity(), jobRequest);
            } else {
                return new EnqueueResult(false, false, queue.remainingCapacity(), jobRequest);
            }
        } finally {
            workerLock.unlock();
        }
    }

    /**
     * Reactive job stream (up to maxCount or until timeout)
     */
    public void jobStream(String workerName, int maxCount, Duration timeout, Consumer<JobRequest> jobRequestConsumer, Runnable onFinish) {
        WorkerContext workerContext = getWorkerContext(workerName);
        BlockingQueue<JobRequest> queue = workerContext.getJobsQueue();
        Lock workerLock = workerContext.getWorkerLock();

        workerLock.lock();
        //Consume available requests in queue up to maxCount
        AtomicInteger count = new AtomicInteger();
        do {
            var jobRequest = queue.poll();
            if (jobRequest == null)
                break;
            else
                jobRequestConsumer.accept(jobRequest);
        } while (count.incrementAndGet() < maxCount);

        if (count.get() > 0) {
            onFinish.run();
            workerLock.unlock();
        } else {
            AtomicLong timerId = new AtomicLong();
            Uni.createFrom().<JobRequest>emitter(emitter -> {
                        ConcurrentLinkedQueue<UniEmitter<? super JobRequest>> jobEmitter = workerContext.getEmittersForWaitingWorkers();
                        jobEmitter.add(emitter);
                        workerLock.unlock();
                        timerId.set(vertx.setTimer(timeout.toMillis(), event -> {
                            Lock emitterLock = workerContext.getEmitterLock();
                            emitterLock.lock(); //todo use per-emmiters lock
                            try {
                                if(jobEmitter.remove(emitter))
                                    onFinish.run();
                            } finally {
                                emitterLock.unlock();
                            }
                        }));
                    })
//                    .select().first()
//                    .ifNoItem().after(timeout).fail()
                    .subscribe()
                    .with(jobRequest -> {
                        vertx.cancelTimer(timerId.get());
                        if (jobRequest == null) {
                            // TODO never should happen!
                            System.out.println("Happened something wrong!");
                        } else {
                            try {
                                jobRequestConsumer.accept(jobRequest);
                                onFinish.run();
                            } catch (io.grpc.StatusRuntimeException e) {
                                //workerName disconnected
                                enqueue(jobRequest);
                            }
                        }
                    }, e-> {
                        Log.error("Error on waiting jobs", e);
                        vertx.cancelTimer(timerId.get());
                        onFinish.run();
                    });
        }
    }

    private WorkerContext getWorkerContext(String worker) {
        return workers.computeIfAbsent(worker, s -> new WorkerContext(bufferSize));
    }

    public void addResponseEmitterForJob(JobRequest jobRequest, UniEmitter<? super JobResult> uniEmitter) {
        //todo add wait timeout and emit "no response"
        responseEmitters.put(jobRequest.getNumber(), uniEmitter);
    }

    public void sendResponse(long jobKey, String variables) {
        UniEmitter<? super JobResult> emitter = responseEmitters.remove(jobKey);
        if(emitter!=null) {
            emitter.complete(new JobResult(jobKey, variables));
        }
    }
}
