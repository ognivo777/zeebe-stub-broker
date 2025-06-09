package org.obiz;

import com.alibaba.fastjson2.JSON;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.obiz.entity.JobRequest;

@Path("/payload")
public class AddWorkService {

    @Inject
    JobsQueue queue;

    @Path("{worker}")
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<String> putPayload(@PathParam("worker") String worker, @QueryParam("repeatCount") @DefaultValue("1") int repeatCount, String payload) throws InterruptedException {
        JobRequest jobRequest = new JobRequest(worker, payload, repeatCount);
        return Uni.createFrom().item(JSON.toJSONString(queue.enqueue(jobRequest)));
    }
}
