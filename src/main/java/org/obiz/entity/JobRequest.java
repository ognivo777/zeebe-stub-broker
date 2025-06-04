package org.obiz.entity;

public class JobRequest {
    private String worker;
    private String variablesJson;
    private int repeatCount;


    public JobRequest(String worker, String payload, int repeatCount) {
        this.worker = worker;
        this.variablesJson = payload;
        this.repeatCount = repeatCount;
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
