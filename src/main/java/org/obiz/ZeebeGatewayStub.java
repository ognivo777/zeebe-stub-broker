package org.obiz;

import com.google.common.util.concurrent.AtomicDouble;
import io.camunda.zeebe.gateway.protocol.GatewayGrpc;
import io.camunda.zeebe.gateway.protocol.GatewayOuterClass;
import io.grpc.stub.StreamObserver;
import io.micrometer.core.annotation.Counted;
import io.quarkus.grpc.GrpcService;
import jakarta.inject.Inject;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@GrpcService
public class ZeebeGatewayStub extends GatewayGrpc.GatewayImplBase {

    @Inject
    JobsDispatcher queue;

    private final double ALPHA = 0.2;
    private final AtomicDouble rps = new AtomicDouble(0.0);
    private final AtomicLong prevTime = new AtomicLong(System.nanoTime());
    Lock lock = new ReentrantLock();


    @Override
    @Counted(value = "counted.activateJobs")
    public void activateJobs(GatewayOuterClass.ActivateJobsRequest request, StreamObserver<GatewayOuterClass.ActivateJobsResponse> responseObserver) {

        String workerType = request.getType();
        int maxJobsToActivate = request.getMaxJobsToActivate();
        long requestTimeout = request.getRequestTimeout();
//        System.out.printf("Job request workerType = %s (%s) (maxJobsToActivate: %s; requestTimeout: %d )%n", workerType, worker, maxJobsToActivate, requestTimeout);

        //Dirty sliding RPS calculation
        lock.lock();
        long currentTimeNanos = System.nanoTime();
        long elapsedTimeNanos = currentTimeNanos - prevTime.get();

        if(elapsedTimeNanos>0) {
            double instantRps = 1000000000.0 / elapsedTimeNanos;
            rps.set(ALPHA * instantRps + (1 - ALPHA) * rps.doubleValue());
        }
        prevTime.set(currentTimeNanos);
        lock.unlock();

        queue.jobStream(workerType, maxJobsToActivate, Duration.of(requestTimeout, ChronoUnit.MILLIS), jobRequest -> {
            responseObserver.onNext(GatewayOuterClass.ActivateJobsResponse.newBuilder().addJobs(
                    GatewayOuterClass.ActivatedJob.newBuilder()
                            .setType(jobRequest.getWorker())
                            .setKey(jobRequest.getNumber())
                            .setVariables(jobRequest.getVariablesJson())
                            .build()
            ).build());
        }, responseObserver::onCompleted);
    }

    @Override
    public void streamActivatedJobs(GatewayOuterClass.StreamActivatedJobsRequest request, StreamObserver<GatewayOuterClass.ActivatedJob> responseObserver) {
        //same as activateJobs but without send "onComplete"
        super.streamActivatedJobs(request, responseObserver);
    }

    @Override
    @Counted(value = "counted.completedJobs")
    public void completeJob(GatewayOuterClass.CompleteJobRequest request, StreamObserver<GatewayOuterClass.CompleteJobResponse> responseObserver) {
        long jobKey = request.getJobKey();
        String variables = request.getVariables();
//        System.out.println("Completed job: " + jobKey + " with variables: " + variables);
        queue.sendResponse(jobKey, variables);
        responseObserver.onNext(GatewayOuterClass.CompleteJobResponse.newBuilder().build());
        responseObserver.onCompleted();
//        super.completeJob(request, responseObserver);
    }

    @Override
    public void throwError(GatewayOuterClass.ThrowErrorRequest request, StreamObserver<GatewayOuterClass.ThrowErrorResponse> responseObserver) {
        long jobKey = request.getJobKey();
        String variables = request.getVariables();
        String errorCode = request.getErrorCode();
        String errorMessage = request.getErrorMessage();
        queue.sendResponse(jobKey, variables);//Todo extend Job result to send errors
        responseObserver.onNext(GatewayOuterClass.ThrowErrorResponse.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Override
    public void failJob(GatewayOuterClass.FailJobRequest request, StreamObserver<GatewayOuterClass.FailJobResponse> responseObserver) {
        //Todo extend Job result to send job fails
        super.failJob(request, responseObserver);
    }

    @Override
    public void setVariables(GatewayOuterClass.SetVariablesRequest request, StreamObserver<GatewayOuterClass.SetVariablesResponse> responseObserver) {
        super.setVariables(request, responseObserver);
    }

    @Override
    public void topology(GatewayOuterClass.TopologyRequest request, StreamObserver<GatewayOuterClass.TopologyResponse> responseObserver) {
        super.topology(request, responseObserver);
    }

    @Override
    public void updateJobRetries(GatewayOuterClass.UpdateJobRetriesRequest request, StreamObserver<GatewayOuterClass.UpdateJobRetriesResponse> responseObserver) {
        super.updateJobRetries(request, responseObserver);
    }

    @Override
    public void updateJobTimeout(GatewayOuterClass.UpdateJobTimeoutRequest request, StreamObserver<GatewayOuterClass.UpdateJobTimeoutResponse> responseObserver) {
        super.updateJobTimeout(request, responseObserver);
    }

    @Override
    public void broadcastSignal(GatewayOuterClass.BroadcastSignalRequest request, StreamObserver<GatewayOuterClass.BroadcastSignalResponse> responseObserver) {
        super.broadcastSignal(request, responseObserver);
    }
}
