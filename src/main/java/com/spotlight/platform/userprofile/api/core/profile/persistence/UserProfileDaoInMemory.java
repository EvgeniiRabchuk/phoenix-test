package com.spotlight.platform.userprofile.api.core.profile.persistence;

import com.spotlight.platform.userprofile.api.model.profile.UserProfile;
import com.spotlight.platform.userprofile.api.model.profile.primitives.UserId;
import com.spotlight.platform.userprofile.api.model.profile.primitives.UserProfilePropertyName;
import com.spotlight.platform.userprofile.api.model.profile.primitives.UserProfilePropertyValue;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class UserProfileDaoInMemory implements UserProfileDao {
    private final Map<UserId, UserProfile> storage = new ConcurrentHashMap<>();

    {
        UserId usedId = UserId.valueOf("1");
        Map<UserProfilePropertyName, UserProfilePropertyValue> map = new HashMap<>();
        map.put(UserProfilePropertyName.valueOf("currentGold"), UserProfilePropertyValue.valueOf(100));
        map.put(UserProfilePropertyName.valueOf("currentGems"), UserProfilePropertyValue.valueOf(200));
        map.put(UserProfilePropertyName.valueOf("battleFought"), UserProfilePropertyValue.valueOf(300));
        map.put(UserProfilePropertyName.valueOf("questsNotCompleted"), UserProfilePropertyValue.valueOf(400));
        UserProfile userProfile = new UserProfile(usedId, Instant.now(), map);
        storage.put(UserId.valueOf("1"), userProfile);


        UserId usedId3 = UserId.valueOf("3");
        Map<UserProfilePropertyName, UserProfilePropertyValue> map3 = new HashMap<>();
        map3.put(UserProfilePropertyName.valueOf("inventory"), UserProfilePropertyValue.valueOf(List.of("sword77")));
        map3.put(UserProfilePropertyName.valueOf("tools"), UserProfilePropertyValue.valueOf(List.of("tool77")));
        UserProfile userProfile3 = new UserProfile(usedId3, Instant.now(), map3);
        storage.put(UserId.valueOf("3"), userProfile3);
    }

    @Override
    public Optional<UserProfile> get(UserId userId) {
        return Optional.ofNullable(storage.get(userId));
    }

    @Override
    public void put(UserProfile userProfile) {
        storage.put(userProfile.userId(), userProfile);
    }
}
