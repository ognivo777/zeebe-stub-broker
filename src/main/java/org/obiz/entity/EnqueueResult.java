package org.obiz.entity;

public class EnqueueResult {
    private boolean success;
    private boolean queued;
    private int spaceLeft;
    private JobRequest jobRequest;

    public EnqueueResult(boolean success, boolean queued, int spaceLeft, JobRequest jobRequest) {
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

    @Override
    public String toString() {
        return "EnqueueResult{" +
                "success=" + success +
                ", queued=" + queued +
                ", spaceLeft=" + spaceLeft +
                ", jobRequest=" + jobRequest +
                '}';
    }
}
