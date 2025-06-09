package org.obiz.entity;

import java.util.concurrent.atomic.AtomicInteger;

public class JobRequest {
    private final int number;
    private String worker;
    private String variablesJson;
    private int repeatCount;
    private static AtomicInteger counter = new AtomicInteger();

    public JobRequest(String worker, String payload, int repeatCount) {
        this.worker = worker;
        this.variablesJson = payload;
        this.repeatCount = repeatCount;
        this.number = counter.getAndIncrement();
    }

    public int getNumber() {
        return number;
    }

    public String getWorker() {
        return worker;
    }

    public String getVariablesJson() {
        return variablesJson;
    }

    public int getRepeatCount() {
        return repeatCount;
    }
}
