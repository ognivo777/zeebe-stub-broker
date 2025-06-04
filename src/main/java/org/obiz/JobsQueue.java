package org.obiz;

import jakarta.enterprise.context.ApplicationScoped;
import org.obiz.entity.JobRequest;

import java.util.ArrayList;
import java.util.concurrent.*;
import java.util.stream.Stream;

@ApplicationScoped
public class JobsQueue {

    private ConcurrentMap<String, BlockingQueue<JobRequest>> jobsStore = new ConcurrentHashMap<>();

    public void enqueue(JobRequest jobRequest) throws InterruptedException {
        getQueue(jobRequest.getWorker()).add(jobRequest);
    }

    public JobRequest dequeue(String worker) throws InterruptedException {
        return getQueue(worker).take();
    }

    private BlockingQueue<JobRequest> getQueue(String worker) {
        return jobsStore.computeIfAbsent(worker, s -> new LinkedBlockingQueue<>(5));
    }

    public Stream<JobRequest> jobStream(String worker, int count) {
//        queue.poll(11, TimeUnit.MINUTES);
        ArrayList<JobRequest> jobs = new ArrayList<>();
        getQueue(worker).drainTo(jobs, count);
        return jobs.stream();
    }
}
