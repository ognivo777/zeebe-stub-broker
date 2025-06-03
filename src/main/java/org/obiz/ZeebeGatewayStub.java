package org.obiz;

import io.camunda.zeebe.gateway.protocol.Gateway;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

public class ZeebeGatewayStub implements Gateway {
    @Override
    public Uni<io.camunda.zeebe.gateway.protocol.GatewayOuterClass.CancelProcessInstanceResponse> cancelProcessInstance(io.camunda.zeebe.gateway.protocol.GatewayOuterClass.CancelProcessInstanceRequest request) {
        return null;
    }

    @Override
    public Uni<io.camunda.zeebe.gateway.protocol.GatewayOuterClass.CompleteJobResponse> completeJob(io.camunda.zeebe.gateway.protocol.GatewayOuterClass.CompleteJobRequest request) {
        return null;
    }

    @Override
    public Uni<io.camunda.zeebe.gateway.protocol.GatewayOuterClass.CreateProcessInstanceResponse> createProcessInstance(io.camunda.zeebe.gateway.protocol.GatewayOuterClass.CreateProcessInstanceRequest request) {
        return null;
    }

    @Override
    public Uni<io.camunda.zeebe.gateway.protocol.GatewayOuterClass.CreateProcessInstanceWithResultResponse> createProcessInstanceWithResult(io.camunda.zeebe.gateway.protocol.GatewayOuterClass.CreateProcessInstanceWithResultRequest request) {
        return null;
    }

    @Override
    public Uni<io.camunda.zeebe.gateway.protocol.GatewayOuterClass.EvaluateDecisionResponse> evaluateDecision(io.camunda.zeebe.gateway.protocol.GatewayOuterClass.EvaluateDecisionRequest request) {
        return null;
    }

    @Override
    public Uni<io.camunda.zeebe.gateway.protocol.GatewayOuterClass.DeployProcessResponse> deployProcess(io.camunda.zeebe.gateway.protocol.GatewayOuterClass.DeployProcessRequest request) {
        return null;
    }

    @Override
    public Uni<io.camunda.zeebe.gateway.protocol.GatewayOuterClass.DeployResourceResponse> deployResource(io.camunda.zeebe.gateway.protocol.GatewayOuterClass.DeployResourceRequest request) {
        return null;
    }

    @Override
    public Uni<io.camunda.zeebe.gateway.protocol.GatewayOuterClass.FailJobResponse> failJob(io.camunda.zeebe.gateway.protocol.GatewayOuterClass.FailJobRequest request) {
        return null;
    }

    @Override
    public Uni<io.camunda.zeebe.gateway.protocol.GatewayOuterClass.ThrowErrorResponse> throwError(io.camunda.zeebe.gateway.protocol.GatewayOuterClass.ThrowErrorRequest request) {
        return null;
    }

    @Override
    public Uni<io.camunda.zeebe.gateway.protocol.GatewayOuterClass.PublishMessageResponse> publishMessage(io.camunda.zeebe.gateway.protocol.GatewayOuterClass.PublishMessageRequest request) {
        return null;
    }

    @Override
    public Uni<io.camunda.zeebe.gateway.protocol.GatewayOuterClass.ResolveIncidentResponse> resolveIncident(io.camunda.zeebe.gateway.protocol.GatewayOuterClass.ResolveIncidentRequest request) {
        return null;
    }

    @Override
    public Uni<io.camunda.zeebe.gateway.protocol.GatewayOuterClass.SetVariablesResponse> setVariables(io.camunda.zeebe.gateway.protocol.GatewayOuterClass.SetVariablesRequest request) {
        return null;
    }

    @Override
    public Uni<io.camunda.zeebe.gateway.protocol.GatewayOuterClass.TopologyResponse> topology(io.camunda.zeebe.gateway.protocol.GatewayOuterClass.TopologyRequest request) {
        return null;
    }

    @Override
    public Uni<io.camunda.zeebe.gateway.protocol.GatewayOuterClass.UpdateJobRetriesResponse> updateJobRetries(io.camunda.zeebe.gateway.protocol.GatewayOuterClass.UpdateJobRetriesRequest request) {
        return null;
    }

    @Override
    public Uni<io.camunda.zeebe.gateway.protocol.GatewayOuterClass.ModifyProcessInstanceResponse> modifyProcessInstance(io.camunda.zeebe.gateway.protocol.GatewayOuterClass.ModifyProcessInstanceRequest request) {
        return null;
    }

    @Override
    public Uni<io.camunda.zeebe.gateway.protocol.GatewayOuterClass.MigrateProcessInstanceResponse> migrateProcessInstance(io.camunda.zeebe.gateway.protocol.GatewayOuterClass.MigrateProcessInstanceRequest request) {
        return null;
    }

    @Override
    public Uni<io.camunda.zeebe.gateway.protocol.GatewayOuterClass.UpdateJobTimeoutResponse> updateJobTimeout(io.camunda.zeebe.gateway.protocol.GatewayOuterClass.UpdateJobTimeoutRequest request) {
        return null;
    }

    @Override
    public Uni<io.camunda.zeebe.gateway.protocol.GatewayOuterClass.DeleteResourceResponse> deleteResource(io.camunda.zeebe.gateway.protocol.GatewayOuterClass.DeleteResourceRequest request) {
        return null;
    }

    @Override
    public Uni<io.camunda.zeebe.gateway.protocol.GatewayOuterClass.BroadcastSignalResponse> broadcastSignal(io.camunda.zeebe.gateway.protocol.GatewayOuterClass.BroadcastSignalRequest request) {
        return null;
    }

    @Override
    public Multi<io.camunda.zeebe.gateway.protocol.GatewayOuterClass.ActivateJobsResponse> activateJobs(io.camunda.zeebe.gateway.protocol.GatewayOuterClass.ActivateJobsRequest request) {
        return null;
    }

    @Override
    public Multi<io.camunda.zeebe.gateway.protocol.GatewayOuterClass.ActivatedJob> streamActivatedJobs(io.camunda.zeebe.gateway.protocol.GatewayOuterClass.StreamActivatedJobsRequest request) {
        return null;
    }
}
