package com.spotlight.platform.userprofile.api.model.command;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.spotlight.platform.userprofile.api.model.command.primitives.CommandResultEnum;


public record CommandResult(@JsonProperty CommandResultEnum result) {}
