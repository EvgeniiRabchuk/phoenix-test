package com.spotlight.platform.userprofile.api.web.resources;

import com.spotlight.platform.userprofile.api.core.command.CommandService;
import com.spotlight.platform.userprofile.api.core.profile.UserProfileService;
import com.spotlight.platform.userprofile.api.model.command.Command;
import com.spotlight.platform.userprofile.api.model.command.CommandResult;
import com.spotlight.platform.userprofile.api.model.profile.UserProfile;
import com.spotlight.platform.userprofile.api.model.profile.primitives.UserId;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/users")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class UserResource {

    private final UserProfileService userProfileService;
    private final CommandService commandService;

    @Inject
    public UserResource(UserProfileService userProfileService, CommandService commandService) {
        this.userProfileService = userProfileService;
        this.commandService = commandService;
    }

    @Path("/{userId}/profile")
    @GET
    public UserProfile getUserProfile(@Valid @PathParam("userId") UserId userId) {
        return userProfileService.get(userId);
    }

    @Path("/command")
    @POST
    public CommandResult postCommandToUpdateProfile(@Valid Command command) {
        return commandService.processSingleCommand(command);
    }

    //Maybe not necessary endpoint
    @Path("/command-batch")
    @POST
    public List<CommandResult> postCommandsBatchToUpdateProfile(@Valid List<Command> commands) {
        return commandService.processBatchOfCommand(commands);
    }
}
