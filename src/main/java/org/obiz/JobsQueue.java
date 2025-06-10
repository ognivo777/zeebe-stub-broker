package org.obiz;

import io.quarkus.logging.Log;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.subscription.MultiEmitter;
import io.smallrye.mutiny.subscription.UniEmitter;
import io.vertx.core.Vertx;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.obiz.entity.EnqueResult;
import org.obiz.entity.JobRequest;
import org.obiz.entity.JobResult;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

@ApplicationScoped
public class JobsQueue {

    private final Lock emitterLock = new ReentrantLock();
    private final Lock lock = new ReentrantLock();
    private final int bufferSize = 500;

    @Inject
    Vertx vertx;

    // Named per worker in-memory job queues
    private ConcurrentMap<String, BlockingQueue<JobRequest>> jobsStore = new ConcurrentHashMap<>();
    // Named per worker emitters list
    private final Map<String, ConcurrentLinkedQueue<MultiEmitter<? super JobRequest>>> emitters = new ConcurrentHashMap<>();//ToDo use UniEmitter
    private final Map<Long, UniEmitter<? super JobResult>> responseEmitters = new ConcurrentHashMap<>();

    public EnqueResult enqueue(JobRequest jobRequest) {
        BlockingQueue<JobRequest> queue = getQueue(jobRequest.getWorker());
        lock.lock();
        try {
            var workerEmitters = emitters.get(jobRequest.getWorker());
            if (workerEmitters!=null) {
                emitterLock.lock(); //todo use per-emmiters / lock
                try {
                    MultiEmitter<? super JobRequest> multiEmitter = workerEmitters.poll();
                    if (multiEmitter != null) {
                        multiEmitter.emit(jobRequest);
                        return new EnqueResult(true, false, queue.remainingCapacity(), jobRequest);
                    }
                } finally {
                    emitterLock.unlock();
                }
            }
            if(queue.offer(jobRequest)){
                return new EnqueResult(true, true, queue.remainingCapacity(), jobRequest);
            } else {
                return new EnqueResult(false, false, queue.remainingCapacity(), jobRequest);
            }
        } finally {
            lock.unlock();
        }

    }

    /**
     * Reactive job stream (up to maxCount or until timeout)
     */
    public void jobStream(String worker, int maxCount, Duration timeout, Consumer<JobRequest> jobRequestConsumer, Runnable onFinish) {
        lock.lock();
        //Consume available requests in queue up to maxCount
        var queue = getQueue(worker);
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
            lock.unlock();
        } else {
            AtomicLong timerId = new AtomicLong();
            Multi.createFrom().<JobRequest>emitter(emitter -> {
                        var multiEmitters = emitters.computeIfAbsent(worker, s -> new ConcurrentLinkedQueue<>());
                        multiEmitters.add(emitter);
                        lock.unlock();
                        timerId.set(vertx.setTimer(timeout.toMillis(), event -> {
                            emitterLock.lock(); //todo use per-emmiters lock
                            try {
                                if(multiEmitters.remove(emitter))
                                    onFinish.run();
                            } finally {
                                emitterLock.unlock();
                            }
                        }));
                    })
                    .select().first()
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
                                //worker disconnected
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

    private BlockingQueue<JobRequest> getQueue(String worker) {
        return jobsStore.computeIfAbsent(worker, s -> new LinkedBlockingQueue<>(bufferSize));
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
