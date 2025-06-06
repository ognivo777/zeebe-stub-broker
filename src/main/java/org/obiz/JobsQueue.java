package org.obiz;

import io.quarkus.micrometer.runtime.binder.websockets.WebSocketMetricsInterceptorProducerImpl;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.subscription.MultiEmitter;
import io.vertx.core.Vertx;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.obiz.entity.JobRequest;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.stream.Stream;

@ApplicationScoped
public class JobsQueue {

    // Named in-memory queues
    private ConcurrentMap<String, BlockingQueue<JobRequest>> jobsStore = new ConcurrentHashMap<>();
    // Named emitter trigger (one per worker)
    private final Map<String, List<MultiEmitter<? super JobRequest>>> emitters = new ConcurrentHashMap<>();

    private Lock triggerLock = new ReentrantLock();
    private Random random = new Random();
    private int bufferSize = 5;

    @Inject
    Vertx vertx;

    public boolean enqueue(JobRequest jobRequest) {
        if (emitters.containsKey(jobRequest.getWorker())) {
            triggerLock.lock();
            var workerEmitters = emitters.get(jobRequest.getWorker());
            int size = workerEmitters.size();
            if (size > 0) {
                int index = random.nextInt(size);
                MultiEmitter<? super JobRequest> multiEmitter = workerEmitters.remove(index);
                multiEmitter.emit(jobRequest);
                triggerLock.unlock();
                return true;
            }
            triggerLock.unlock();
        }
        return getQueue(jobRequest.getWorker()).offer(jobRequest);
    }

    public JobRequest dequeue(String worker) throws InterruptedException {
        return getQueue(worker).take();
    }

    private BlockingQueue<JobRequest> getQueue(String worker) {
        return jobsStore.computeIfAbsent(worker, s -> new LinkedBlockingQueue<>(bufferSize));
    }

    /**
     * Reactive job stream (up to maxCount or until timeout)
     */
    public void jobStream(String worker, int maxCount, Duration timeout, Consumer<JobRequest> jobRequestConsumer, Runnable onFinish) {
        var queue = getQueue(worker);
        AtomicInteger count = new AtomicInteger();
        //send available requests
        while (count.get() < maxCount) {
            var jobRequest = queue.poll();
            if (jobRequest == null) break;
            count.incrementAndGet();
            jobRequestConsumer.accept(jobRequest);
        }
        if (count.get() > 0) {
            onFinish.run();
        } else {

            Multi.createFrom().<JobRequest>emitter(emitter -> {
                        emitters.computeIfAbsent(worker, s -> Collections.synchronizedList(new ArrayList<>())).add(emitter);
//            vertx.setTimer(timeout.toMillis(), l -> {
//                emitters.get(worker).remove(emitter);
//                emitter.complete();
//                onFinish.run();
//            });
                    })
                    .select().first()
                    .ifNoItem().after(timeout).fail()
                    .subscribe()
                    .with(jobRequest -> {
                        if (jobRequest == null) {
                            // TODO never should happen!
                            System.out.println("Happened something wrong!");
                        } else {
                            jobRequestConsumer.accept(jobRequest);
                            onFinish.run();
                        }
                    }, e-> onFinish.run());

        }

    }

    public Stream<JobRequest> jobStream(String worker, int count) {
//        queue.poll(11, TimeUnit.MINUTES);
        ArrayList<JobRequest> jobs = new ArrayList<>();
        getQueue(worker).drainTo(jobs, count);
        return jobs.stream();
    }
}
