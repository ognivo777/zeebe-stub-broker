package org.obiz;

//import com.alibaba.fastjson2.JSON;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.obiz.entity.EnqueueResult;
import org.obiz.entity.JobRequest;
import org.obiz.entity.JobResult;

@Path("/payload")
public class AddWorkService {

    @Inject
    JobsDispatcher jobsDispatcher;

    @Path("{worker}")
    @PUT
    @Produces(MediaType.TEXT_HTML)
    public Uni<String> putPayload(
            @PathParam("worker") String worker,
            @QueryParam("async") @DefaultValue("true") boolean isAsync, String payload) {
        JobRequest jobRequest = new JobRequest(worker, payload);
        EnqueueResult enqueueResult = jobsDispatcher.enqueue(jobRequest);
        if (!isAsync && enqueueResult.isSuccess()) {
            //TODO expand EnqueueResult with optional worker result and always return EnqueueResult
            return Uni.createFrom().<JobResult>emitter(uniEmitter -> {
                jobsDispatcher.addResponseEmitterForJob(jobRequest, uniEmitter);
//            }).map(JSON::toJSONString);
            }).map(JobResult::toString);
        }
//        return Uni.createFrom().item(JSON.toJSONString(enqueueResult));
        return Uni.createFrom().item(enqueueResult).map(EnqueueResult::toString);
    }
}
