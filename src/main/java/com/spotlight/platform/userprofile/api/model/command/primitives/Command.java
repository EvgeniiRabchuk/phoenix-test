package com.spotlight.platform.userprofile.api.model.command.primitives;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.spotlight.platform.userprofile.api.model.profile.primitives.UserId;
import com.spotlight.platform.userprofile.api.model.profile.primitives.UserProfilePropertyName;
import com.spotlight.platform.userprofile.api.model.profile.primitives.UserProfilePropertyValue;

import java.util.Map;

public record Command(@JsonProperty UserId userId,
                      @JsonProperty CommandTypeEnum type,
                      @JsonProperty Map<UserProfilePropertyName, UserProfilePropertyValue> userProfileProperties) {
}
