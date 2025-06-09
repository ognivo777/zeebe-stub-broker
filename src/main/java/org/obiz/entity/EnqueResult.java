package org.obiz.entity;

public class EnqueResult {
    private boolean success;
    private boolean queued;
    private int spaceLeft;
    private JobRequest jobRequest;

    public EnqueResult(boolean success, boolean queued, int spaceLeft, JobRequest jobRequest) {
        this.success = success;
        this.queued = queued;
        this.spaceLeft = spaceLeft;
        this.jobRequest = jobRequest;
    }

    public boolean isSuccess() {
        return success;
    }

    public boolean isQueued() {
        return queued;
    }

    public int getSpaceLeft() {
        return spaceLeft;
    }

    public JobRequest getJobRequest() {
        return jobRequest;
    }
}
