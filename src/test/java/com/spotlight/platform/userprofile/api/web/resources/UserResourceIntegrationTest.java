package com.spotlight.platform.userprofile.api.web.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

import com.spotlight.platform.userprofile.api.core.profile.persistence.UserProfileDao;
import com.spotlight.platform.userprofile.api.model.command.Command;
import com.spotlight.platform.userprofile.api.model.command.CommandResult;
import com.spotlight.platform.userprofile.api.model.command.primitives.CommandResultEnum;
import com.spotlight.platform.userprofile.api.model.command.primitives.CommandTypeEnum;
import com.spotlight.platform.userprofile.api.model.profile.UserProfile;
import com.spotlight.platform.userprofile.api.model.profile.primitives.UserId;
import com.spotlight.platform.userprofile.api.model.profile.primitives.UserProfileFixtures;
import com.spotlight.platform.userprofile.api.web.UserProfileApiApplication;

import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import ru.vyarus.dropwizard.guice.test.ClientSupport;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.TestDropwizardAppExtension;

import javax.ws.rs.client.Entity;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

@Execution(ExecutionMode.SAME_THREAD)
class UserResourceIntegrationTest {
    @RegisterExtension
    static TestDropwizardAppExtension APP = TestDropwizardAppExtension.forApp(UserProfileApiApplication.class)
            .randomPorts()
            .hooks(builder -> builder.modulesOverride(new AbstractModule() {
                @Provides
                @Singleton
                public UserProfileDao getUserProfileDao() {
                    return mock(UserProfileDao.class);
                }
            }))
            .randomPorts()
            .create();

    @BeforeEach
    void beforeEach(UserProfileDao userProfileDao) {
        reset(userProfileDao);
    }

    @Nested
    @DisplayName("getUserProfile")
    class GetUserProfile {
        private static final String USER_ID_PATH_PARAM = "userId";
        private static final String URL = "/users/{%s}/profile".formatted(USER_ID_PATH_PARAM);

        @Test
        void existingUser_correctObjectIsReturned(ClientSupport client, UserProfileDao userProfileDao) {
            when(userProfileDao.get(any(UserId.class))).thenReturn(Optional.of(UserProfileFixtures.USER_PROFILE));

            var response = client.targetRest().path(URL).resolveTemplate(USER_ID_PATH_PARAM, UserProfileFixtures.USER_ID).request().get();

            assertThat(response.getStatus()).isEqualTo(HttpStatus.OK_200);
            assertThatJson(response.readEntity(UserProfile.class)).isEqualTo(UserProfileFixtures.SERIALIZED_USER_PROFILE);
        }

        @Test
        void nonExistingUser_returns404(ClientSupport client, UserProfileDao userProfileDao) {
            when(userProfileDao.get(any(UserId.class))).thenReturn(Optional.empty());

            var response = client.targetRest().path(URL).resolveTemplate(USER_ID_PATH_PARAM, UserProfileFixtures.USER_ID).request().get();

            assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND_404);
        }

        @Test
        void validationFailed_returns400(ClientSupport client) {
            var response = client.targetRest()
                    .path(URL)
                    .resolveTemplate(USER_ID_PATH_PARAM, UserProfileFixtures.INVALID_USER_ID)
                    .request()
                    .get();

            assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST_400);
        }

        @Test
        void unhandledExceptionOccured_returns500(ClientSupport client, UserProfileDao userProfileDao) {
            when(userProfileDao.get(any(UserId.class))).thenThrow(new RuntimeException("Some unhandled exception"));

            var response = client.targetRest().path(URL).resolveTemplate(USER_ID_PATH_PARAM, UserProfileFixtures.USER_ID).request().get();

            assertThat(response.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR_500);
        }
    }

    @Nested
    @DisplayName("postCommand")
    class PostCommand {
        private static final String URL_SINGLE_RESPONSE = "/users/command";
        private static final String URL_BATCH_RESPONSE = "/users/command-batch";

        @Test
        void existingUserSingle_correctResponse(ClientSupport client, UserProfileDao userProfileDao) {
            when(userProfileDao.get(any(UserId.class)))
                    .thenReturn(Optional.of(UserProfileFixtures.USER_PROFILE_FOR_COMMANDS));

            var response = client.targetRest()
                    .path(URL_SINGLE_RESPONSE).resolveTemplate(UserProfileFixtures.USER_PROFILE_FOR_COMMANDS.userId().toString(), UserProfileFixtures.USER_PROFILE_FOR_COMMANDS)
                    .request()
                    .post(Entity.json(UserProfileFixtures.SERIALIZED_COMMAND));
            assertThat(response.getStatus()).isEqualTo(HttpStatus.OK_200);
            CommandResult commandResult = response.readEntity(CommandResult.class);
            assertEquals(CommandResultEnum.DONE, commandResult.result());
        }

        @Test
        void existingUserBatch_correctResponse(ClientSupport client, UserProfileDao userProfileDao) {
            when(userProfileDao.get(any(UserId.class)))
                    .thenReturn(Optional.of(UserProfileFixtures.USER_PROFILE_FOR_COMMANDS));

            var response = client.targetRest()
                    .path(URL_BATCH_RESPONSE).resolveTemplate(UserProfileFixtures.USER_PROFILE_FOR_COMMANDS.userId().toString(), UserProfileFixtures.USER_PROFILE_FOR_COMMANDS)
                    .request()
                    .post(Entity.json(UserProfileFixtures.SERIALIZED_COMMAND_BATCH));
            assertThat(response.getStatus()).isEqualTo(HttpStatus.OK_200);
        }

        @Test
        void badRequest_returns404(ClientSupport client) {
            var response = client.targetRest()
                    .path(URL_SINGLE_RESPONSE).resolveTemplate(UserProfileFixtures.USER_PROFILE_FOR_COMMANDS.userId().toString(), UserProfileFixtures.USER_PROFILE_FOR_COMMANDS)
                    .request()
                    .post(Entity.json(UserProfileFixtures.SERIALIZED_COMMAND));
            assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND_404);
        }
    }

}