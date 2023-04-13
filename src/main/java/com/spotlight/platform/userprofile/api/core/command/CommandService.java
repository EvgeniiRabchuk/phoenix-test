package com.spotlight.platform.userprofile.api.core.command;

import com.spotlight.platform.userprofile.api.core.profile.persistence.UserProfileDao;
import com.spotlight.platform.userprofile.api.model.command.Command;
import com.spotlight.platform.userprofile.api.model.command.CommandResult;
import com.spotlight.platform.userprofile.api.model.profile.UserProfile;
import com.spotlight.platform.userprofile.api.model.profile.primitives.UserProfilePropertyName;
import com.spotlight.platform.userprofile.api.model.profile.primitives.UserProfilePropertyValue;


import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.spotlight.platform.userprofile.api.model.command.primitives.CommandResultEnum.DONE;
import static com.spotlight.platform.userprofile.api.model.command.primitives.CommandResultEnum.FAIL;

public class CommandService {

    private final UserProfileDao userProfileDao;

    @Inject
    public CommandService(UserProfileDao userProfileDao) {
        this.userProfileDao = userProfileDao;
    }

    public CommandResult processSingleCommand(Command command) {
        return switch (command.type()) {
            case REPLACE -> replaceCommandProcess(command);
            case INCREMENT -> incrementCommandProcess(command);
            case COLLECT -> collectCommandProcess(command);
        };
    }

    public List<CommandResult> processBatchOfCommand(List<Command> commands) {
        return commands.stream()
                .map(c -> processSingleCommand(c))
                .collect(Collectors.toList());
    }

    private CommandResult replaceCommandProcess(Command command) {
        Optional<UserProfile> userProfileOptional = userProfileDao.get(command.userId());
        if (userProfileOptional.isPresent()) {
            UserProfile userProfile = userProfileOptional.get();
            var currentMapOfProp = userProfile.userProfileProperties();
            var newMapOfProp = command.properties();
            for (Map.Entry<UserProfilePropertyName, UserProfilePropertyValue> entry : newMapOfProp.entrySet()) {
                currentMapOfProp.put(entry.getKey(),entry.getValue());
            }
            return new CommandResult(DONE);
        } else {
            return new CommandResult(FAIL);
        }
    }

    private CommandResult incrementCommandProcess(Command command) {
        Optional<UserProfile> userProfileOptional = userProfileDao.get(command.userId());
        if (userProfileOptional.isPresent()) {
            UserProfile userProfile = userProfileOptional.get();
            var currentMapOfProp = userProfile.userProfileProperties();
            var newMapOfProp = command.properties();
            for (Map.Entry<UserProfilePropertyName, UserProfilePropertyValue> entry : newMapOfProp.entrySet()) {
                var value = currentMapOfProp.get(entry.getKey());
                try {
                    Integer newValue = (Integer) value.getValue() + (Integer) entry.getValue().getValue();
                    currentMapOfProp.put(entry.getKey(), UserProfilePropertyValue.valueOf(newValue));
                } catch (Exception ex) {
                    //Depends on business logic probably need to throw Exception here
                    return new CommandResult(FAIL);
                }
            }
            return new CommandResult(DONE);
        } else {
            return new CommandResult(FAIL);
        }
    }

    private CommandResult collectCommandProcess(Command command) {
        Optional<UserProfile> userProfileOptional = userProfileDao.get(command.userId());
        if (userProfileOptional.isPresent()) {
            UserProfile userProfile = userProfileOptional.get();
            var currentMapOfProp = userProfile.userProfileProperties();
            var newMapOfProp = command.properties();
            for (Map.Entry<UserProfilePropertyName, UserProfilePropertyValue> entry : newMapOfProp.entrySet()) {
                try {
                    var value = currentMapOfProp.get(entry.getKey());
                    @SuppressWarnings("unchecked")
                    List<String> listOld = (List<String>) value.getValue();
                    @SuppressWarnings("unchecked")
                    List<String> listNew = (List<String>) entry.getValue().getValue();
                    List<String> resultList = new ArrayList<>();
                    resultList.addAll(listOld);
                    resultList.addAll(listNew);
                    currentMapOfProp.put(entry.getKey(), UserProfilePropertyValue.valueOf(resultList));
                } catch (Exception exception) {
                    //Depends on business logic probably need to throw Exception here
                    return new CommandResult(FAIL);
                }
            }
            return new CommandResult(DONE);
        } else {
            return new CommandResult(FAIL);
        }
    }




}
