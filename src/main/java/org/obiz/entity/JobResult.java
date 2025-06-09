package org.obiz.entity;

public class JobResult {
    private final long jobKey;
    private final String variables;

    public JobResult(long jobKey, String variables) {
        this.jobKey = jobKey;
        this.variables = variables;
    }

    public long getJobKey() {
        return jobKey;
    }

    public String getVariables() {
        return variables;
    }
}
