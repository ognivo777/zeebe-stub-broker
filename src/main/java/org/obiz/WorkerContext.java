package org.obiz;

import io.smallrye.mutiny.subscription.UniEmitter;
import org.obiz.entity.JobRequest;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class WorkerContext {
    private int bufferSize = 500; //TODO make adjustable
    private BlockingQueue<JobRequest> jobsQueue =  new LinkedBlockingQueue<>(bufferSize);
    private ConcurrentLinkedQueue<UniEmitter<? super JobRequest>> emittersForWaitingWorkers = new ConcurrentLinkedQueue<>();

    private final Lock emitterLock = new ReentrantLock();
    private final Lock lock = new ReentrantLock();


    public WorkerContext(int bufferSize) {
    }

    public BlockingQueue<JobRequest> getJobsQueue() {
        return jobsQueue;
    }

    public ConcurrentLinkedQueue<UniEmitter<? super JobRequest>> getEmittersForWaitingWorkers() {
        return emittersForWaitingWorkers;
    }

    public Lock getEmitterLock() {
        return emitterLock;
    }

    public Lock getWorkerLock() {
        return lock;
    }
}
