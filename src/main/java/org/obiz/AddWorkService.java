package org.obiz;

import com.alibaba.fastjson2.JSON;
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
    JobsQueue queue;

    @Path("{worker}")
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<String> putPayload(
            @PathParam("worker") String worker,
            @QueryParam("async") @DefaultValue("true") boolean isAsync, String payload) {
        JobRequest jobRequest = new JobRequest(worker, payload);
        EnqueueResult enqueueResult = queue.enqueue(jobRequest);
        if (!isAsync && enqueueResult.isSuccess()) {
            return Uni.createFrom().<JobResult>emitter(uniEmitter -> {
                queue.addResponseEmitterForJob(jobRequest, uniEmitter);
            }).map(JSON::toJSONString);
        }
        return Uni.createFrom().item(JSON.toJSONString(enqueueResult));
    }
}
