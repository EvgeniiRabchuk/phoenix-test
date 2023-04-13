package com.spotlight.platform.userprofile.api.core.command;

import com.spotlight.platform.userprofile.api.core.exceptions.EntityNotFoundException;
import com.spotlight.platform.userprofile.api.core.profile.persistence.UserProfileDao;
import com.spotlight.platform.userprofile.api.model.command.Command;
import com.spotlight.platform.userprofile.api.model.command.CommandResult;
import com.spotlight.platform.userprofile.api.model.command.primitives.CommandTypeEnum;
import com.spotlight.platform.userprofile.api.model.profile.UserProfile;
import com.spotlight.platform.userprofile.api.model.profile.primitives.UserId;
import com.spotlight.platform.userprofile.api.model.profile.primitives.UserProfileFixtures;
import com.spotlight.platform.userprofile.api.model.profile.primitives.UserProfilePropertyName;
import com.spotlight.platform.userprofile.api.model.profile.primitives.UserProfilePropertyValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.util.*;

import static com.spotlight.platform.userprofile.api.model.command.primitives.CommandResultEnum.DONE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Execution(ExecutionMode.SAME_THREAD)
public class CommandServiceTest {
    private final UserProfileDao userProfileDaoMock = mock(UserProfileDao.class);
    private final CommandService commandService = new CommandService(userProfileDaoMock);


    @Test
    void processBatchReplaceCommand_ProfileFound_returnDone() {
        UserProfile userProfile = UserProfileFixtures.USER_PROFILE_FOR_COMMANDS;
        var userPropKey1 = UserProfilePropertyName.valueOf("currentGold");
        var userPropKey2 = UserProfilePropertyName.valueOf("currentGems");

        userProfile.userProfileProperties().clear();
        userProfile.userProfileProperties().put(userPropKey1, UserProfilePropertyValue.valueOf(10));
        userProfile.userProfileProperties().put(userPropKey2, UserProfilePropertyValue.valueOf(20));
        when(userProfileDaoMock.get(any(UserId.class))).thenReturn(Optional.of(userProfile));

        Map<UserProfilePropertyName, UserProfilePropertyValue> propertiesMap = new HashMap<>();
        int i1 = 100;
        int i2 = 200;
        propertiesMap.put(userPropKey1, UserProfilePropertyValue.valueOf(i1));
        propertiesMap.put(userPropKey2, UserProfilePropertyValue.valueOf(i2));
        Command command1 = new Command(UserId.valueOf("any"), CommandTypeEnum.REPLACE, propertiesMap);
        Command command2 = new Command(UserId.valueOf("any"), CommandTypeEnum.INCREMENT, propertiesMap);

        assertEquals(List.of(new CommandResult(DONE), new CommandResult(DONE)), commandService.processBatchOfCommand(List.of(command1,command2)));
    }

    @Test
    void processSingleReplaceCommand_noProfileFound_returnException() {
        when(userProfileDaoMock.get(any(UserId.class))).thenReturn(Optional.empty());

        Command command = new Command(UserId.valueOf("any"), CommandTypeEnum.REPLACE, new HashMap<>());
        assertThrows(EntityNotFoundException.class, () ->
                commandService.processSingleCommand(command));
    }

    @Test
    void processSingleReplaceCommand_ProfileFound_returnDoneResult() {
        UserProfile userProfile = UserProfileFixtures.USER_PROFILE_FOR_COMMANDS;

        var userPropKey1 = UserProfilePropertyName.valueOf("currentGold");
        var userPropKey2 = UserProfilePropertyName.valueOf("currentGems");
        //initial 10 and 20
        userProfile.userProfileProperties().clear();
        userProfile.userProfileProperties().put(userPropKey1, UserProfilePropertyValue.valueOf(10));
        userProfile.userProfileProperties().put(userPropKey2, UserProfilePropertyValue.valueOf(20));
        when(userProfileDaoMock.get(any(UserId.class))).thenReturn(Optional.of(userProfile));

        Map<UserProfilePropertyName, UserProfilePropertyValue> propertiesMap = new HashMap<>();
        //after should be 100 and 200
        int i1 = 100;
        int i2 = 200;
        propertiesMap.put(userPropKey1, UserProfilePropertyValue.valueOf(i1));
        propertiesMap.put(userPropKey2, UserProfilePropertyValue.valueOf(i2));
        Command command = new Command(UserId.valueOf("any"), CommandTypeEnum.REPLACE, propertiesMap);

        assertEquals(new CommandResult(DONE), commandService.processSingleCommand(command));
        assertEquals(i1, userProfile.userProfileProperties().get(userPropKey1).getValue());
        assertEquals(i2, userProfile.userProfileProperties().get(userPropKey2).getValue());
    }

    @Test
    void processSingleIncrementCommand_ProfileFound_returnException() {
        when(userProfileDaoMock.get(any(UserId.class))).thenReturn(Optional.empty());

        Command command = new Command(UserId.valueOf("any"), CommandTypeEnum.INCREMENT, new HashMap<>());
        assertThrows(EntityNotFoundException.class, () ->
                commandService.processSingleCommand(command));
    }

    @Test
    void processSingleIncrementCommand_ProfileFound_returnDoneResult() {
        UserProfile userProfile = UserProfileFixtures.USER_PROFILE_FOR_COMMANDS;
        var userPropKey1 = UserProfilePropertyName.valueOf("battleFought");
        var userPropKey2 = UserProfilePropertyName.valueOf("questsNotCompleted");
        //initial 10 and 20
        int i11 = 10;
        int i21 = 20;
        userProfile.userProfileProperties().clear();
        userProfile.userProfileProperties().put(userPropKey1, UserProfilePropertyValue.valueOf(i11));
        userProfile.userProfileProperties().put(userPropKey2, UserProfilePropertyValue.valueOf(i21));
        when(userProfileDaoMock.get(any(UserId.class))).thenReturn(Optional.of(userProfile));

        Map<UserProfilePropertyName, UserProfilePropertyValue> propertiesMap = new HashMap<>();
        //increment/decrement battleFought : 10 and questsNotCompleted : -1
        int i12 = 10;
        int i22 = -1;
        propertiesMap.put(userPropKey1, UserProfilePropertyValue.valueOf(i12));
        propertiesMap.put(userPropKey2, UserProfilePropertyValue.valueOf(i22));
        Command command = new Command(UserId.valueOf("any"), CommandTypeEnum.INCREMENT, propertiesMap);

        assertEquals(new CommandResult(DONE), commandService.processSingleCommand(command));
        assertEquals(UserProfilePropertyValue.valueOf(i11 + i12), userProfile.userProfileProperties().get(userPropKey1));
        assertEquals(UserProfilePropertyValue.valueOf(i21 + i22), userProfile.userProfileProperties().get(userPropKey2));
    }

    @Test
    void processSingleCollectCommand_ProfileFound_returnException() {
        when(userProfileDaoMock.get(any(UserId.class))).thenReturn(Optional.empty());

        Command command = new Command(UserId.valueOf("any"), CommandTypeEnum.COLLECT, new HashMap<>());
        assertThrows(EntityNotFoundException.class, () ->
                commandService.processSingleCommand(command));
    }

    @Test
    void processSingleCollectCommand_ProfileFound_returnDoneResult() {
        UserProfile userProfile = UserProfileFixtures.USER_PROFILE_FOR_COMMANDS;
        var userPropKey1 = UserProfilePropertyName.valueOf("inventory");
        var userPropKey2 = UserProfilePropertyName.valueOf("tools");
        //initial "sword1", "sword2" and "tool1"
        var list11 = List.of("sword1", "sword2");
        var list21 = List.of("tool1");

        userProfile.userProfileProperties().clear();
        userProfile.userProfileProperties().put(userPropKey1, UserProfilePropertyValue.valueOf(list11));
        userProfile.userProfileProperties().put(userPropKey2, UserProfilePropertyValue.valueOf(list21));
        when(userProfileDaoMock.get(any(UserId.class))).thenReturn(Optional.of(userProfile));

        Map<UserProfilePropertyName, UserProfilePropertyValue> propertiesMap = new HashMap<>();
        //collect shield1 and shield1
        var list12 = List.of("shield1");
        var list22 = List.of("tool2");
        propertiesMap.put(userPropKey1, UserProfilePropertyValue.valueOf(list12));
        propertiesMap.put(userPropKey2, UserProfilePropertyValue.valueOf(list22));
        Command command = new Command(UserId.valueOf("any"), CommandTypeEnum.COLLECT, propertiesMap);

        assertEquals(new CommandResult(DONE), commandService.processSingleCommand(command));

        var list13 = new ArrayList<>();
        list13.addAll(list11);
        list13.addAll(list12);
        var list23 = new ArrayList<>();
        list23.addAll(list21);
        list23.addAll(list22);
        List<String> result1 = (List<String>) userProfile.userProfileProperties().get(userPropKey1).getValue();
        List<String> result2 = (List<String>) userProfile.userProfileProperties().get(userPropKey2).getValue();

        assertTrue(result1.containsAll(list13));
        assertTrue(result2.containsAll(list23));
    }

}
