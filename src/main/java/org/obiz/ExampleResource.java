package org.obiz;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.obiz.entity.JobRequest;

@Path("/payload")
public class ExampleResource {

    @Inject
    JobsQueue queue;

//    @Inject
//    @Channel("jobs-out")
//    Emitter<JobRequest> emitter;
//
//    @Inject
//    @Any
//    InMemoryConnector connector;

    @Path("{worker}")
    @PUT
    @Produces(MediaType.TEXT_PLAIN)
    public String putPayload(@PathParam("worker") String worker, @QueryParam("repeatCount") @DefaultValue("1") int repeatCount, String payload) throws InterruptedException {

        JobRequest jobRequest = new JobRequest(worker, payload, repeatCount);
//        connector.source(worker).send(jobRequest);
//        emitter.send(jobRequest);
        if(queue.enqueue(jobRequest))
            return "%s: %d : %s".formatted(worker, repeatCount, payload);
        else
            return "No space for store: %s: %d : %s".formatted(worker, repeatCount, payload);
    }
}
