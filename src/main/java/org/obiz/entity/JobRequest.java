package org.obiz.entity;

import java.util.concurrent.atomic.AtomicInteger;

public class JobRequest {
    private final long number;
    private final String worker;
    private final String variablesJson;
    private static final AtomicInteger counter = new AtomicInteger();

    public JobRequest(String worker, String payload) {
        this.worker = worker;
        this.variablesJson = payload;
        this.number = counter.getAndIncrement();
    }

    public long getNumber() {
        return number;
    }

    public String getWorker() {
        return worker;
    }

    public String getVariablesJson() {
        return variablesJson;
    }
}
