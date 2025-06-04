package org.obiz;

import com.google.common.util.concurrent.AtomicDouble;
import io.camunda.zeebe.gateway.protocol.GatewayGrpc;
import io.camunda.zeebe.gateway.protocol.GatewayOuterClass;
import io.grpc.stub.StreamObserver;
import io.micrometer.core.annotation.Counted;
import io.quarkus.grpc.GrpcService;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@GrpcService
public class ZeebeGatewayStub extends GatewayGrpc.GatewayImplBase {

//    @Inject
//    @Any
//    InMemoryConnector connector;

    @Inject
    JobsQueue queue;

    private AtomicLong jobKeySequence = new AtomicLong();
    private AtomicLong counter = new AtomicLong();
    private final double ALPHA = 0.2;
    private final AtomicDouble rps = new AtomicDouble(0.0);
    private final AtomicLong prevTime = new AtomicLong(System.nanoTime());
    Lock lock = new ReentrantLock();


    @Override
    @Counted(value = "counted.activateJobs")
    public void activateJobs(GatewayOuterClass.ActivateJobsRequest request, StreamObserver<GatewayOuterClass.ActivateJobsResponse> responseObserver) {

        String worker = request.getType();
        int maxJobsToActivate = request.getMaxJobsToActivate();
        long requestTimeout = request.getRequestTimeout();
        System.out.printf("Job request worker = %s (maxJobsToActivate: %s; requestTimeout: %d )%n", worker, maxJobsToActivate, requestTimeout);

        lock.lock();
        long currentTimeNanos = System.nanoTime();
        long elapsedTimeNanos = currentTimeNanos - prevTime.get();

        if(elapsedTimeNanos>0) {
            double instantRps = 1000000000.0 / elapsedTimeNanos;
            rps.set(ALPHA * instantRps + (1 - ALPHA) * rps.doubleValue());
        }
        prevTime.set(currentTimeNanos);
        lock.unlock();

        List<GatewayOuterClass.ActivatedJob> jobs = queue.jobStream(worker, maxJobsToActivate)
                .map(jobRequest -> GatewayOuterClass.ActivatedJob.newBuilder()
                    .setType(jobRequest.getWorker())
                    .setKey(jobKeySequence.incrementAndGet())
                    .setVariables(jobRequest.getVariablesJson())
                    .build())
                .toList();

        responseObserver.onNext(GatewayOuterClass.ActivateJobsResponse.newBuilder()
                        .addAllJobs(jobs)
                .build());

        if(!jobs.isEmpty()) {
            responseObserver.onCompleted();
        } else {
            Uni.createFrom().emitter(emitter -> {
                emitter.complete("");
            }).onItem().delayIt().by(Duration.ofMillis(requestTimeout)).subscribe().with(o -> {
                responseObserver.onCompleted();
                System.out.println("completed");
            });
        }
    }

    @Override
    public void streamActivatedJobs(GatewayOuterClass.StreamActivatedJobsRequest request, StreamObserver<GatewayOuterClass.ActivatedJob> responseObserver) {
        super.streamActivatedJobs(request, responseObserver);
    }

    @Override
    public void completeJob(GatewayOuterClass.CompleteJobRequest request, StreamObserver<GatewayOuterClass.CompleteJobResponse> responseObserver) {
        long jobKey = request.getJobKey();
        String variables = request.getVariables();
        System.out.println("Completed job: " + jobKey + " with variables: " + variables);
        responseObserver.onNext(GatewayOuterClass.CompleteJobResponse.newBuilder().build());
        responseObserver.onCompleted();
//        super.completeJob(request, responseObserver);
    }

    @Override
    public void throwError(GatewayOuterClass.ThrowErrorRequest request, StreamObserver<GatewayOuterClass.ThrowErrorResponse> responseObserver) {
        super.throwError(request, responseObserver);
    }

    @Override
    public void failJob(GatewayOuterClass.FailJobRequest request, StreamObserver<GatewayOuterClass.FailJobResponse> responseObserver) {
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
