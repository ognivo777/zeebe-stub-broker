package org.obiz;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.obiz.entity.JobRequest;

import static org.junit.jupiter.api.Assertions.*;

class JobsQueueTest {

    private JobsQueue jobsQueue;
    @BeforeEach
    void setUp() {
        jobsQueue = new JobsQueue();
    }

    @Test
    void enqueue() {
        assertTrue(jobsQueue.enqueue(new JobRequest("1","", 0)));
        assertTrue(jobsQueue.enqueue(new JobRequest("1","", 0)));
        assertTrue(jobsQueue.enqueue(new JobRequest("1","", 0)));
        assertTrue(jobsQueue.enqueue(new JobRequest("1","", 0)));
        assertTrue(jobsQueue.enqueue(new JobRequest("1","", 0)));
        assertFalse(jobsQueue.enqueue(new JobRequest("1","", 0)));
        try {
            JobRequest jobRequest = jobsQueue.dequeue("1");
            System.out.println("jobRequest.getWorker() = " + jobRequest.getWorker());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void dequeue() {
    }

    @Test
    void jobStream() {
    }

    @Test
    void testJobStream() {
    }
}