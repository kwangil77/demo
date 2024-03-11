package com.example.demo.service;

import com.example.demo.model.IamUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.util.CollectionUtils;
import software.amazon.awssdk.services.iam.IamClient;
import software.amazon.awssdk.services.iam.model.AccessKeyMetadata;
import software.amazon.awssdk.services.iam.model.IamException;
import software.amazon.awssdk.services.iam.model.ListAccessKeysRequest;
import software.amazon.awssdk.services.iam.model.ListAccessKeysResponse;
import software.amazon.awssdk.services.iam.model.ListUsersRequest;
import software.amazon.awssdk.services.iam.model.ListUsersResponse;
import software.amazon.awssdk.services.iam.model.User;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import static java.time.temporal.ChronoUnit.HOURS;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.mock;

class IamServiceTest {
    private Clock clock;
    private IamClient iamClient;
    private IamService iamService;

    @BeforeEach
    void setUp() {
        iamClient = mock(IamClient.class);
        final Instant now = Instant.now();
        clock = Clock.fixed(now, ZoneId.systemDefault());
        iamService = new IamService(clock, iamClient);
    }

    @Test
    void testListIamUsers() {
        final String expectId = "AIDACKCEVSQ6C2EXAMPLE";
        final String expectAccessKeyId = "AKIAIOSFODNN7EXAMPLE";
        final long elapsedHoursOfAccessKey = 1L;

        ListUsersResponse usersResponse = ListUsersResponse.builder()
                .isTruncated(false)
                .users(
                        User.builder()
                            .userId(expectId)
                            .userName("demo")
                            .build()
                )
                .build();
        ListAccessKeysResponse accessKeysResponse = ListAccessKeysResponse.builder()
                .isTruncated(false)
                .accessKeyMetadata(
                        AccessKeyMetadata.builder()
                            .accessKeyId("AKIA111111111EXAMPLE")
                            .createDate(clock.instant().minus(elapsedHoursOfAccessKey, HOURS))
                            .build(),
                        AccessKeyMetadata.builder()
                            .accessKeyId(expectAccessKeyId)
                            .createDate(clock.instant().minus(elapsedHoursOfAccessKey + 1L, HOURS))
                            .build()
                )
                .build();

        given(iamClient.listUsers(any(ListUsersRequest.class))).willReturn(usersResponse);
        given(iamClient.listAccessKeys(any(ListAccessKeysRequest.class))).willReturn(accessKeysResponse);

        List<IamUser> iamUsers = iamService.listIamUsers(elapsedHoursOfAccessKey);

        assertFalse(CollectionUtils.isEmpty(iamUsers));
        assertEquals(1, iamUsers.size());
        assertEquals(iamUsers.get(0).getId(), expectId);

        assertFalse(CollectionUtils.isEmpty(iamUsers.get(0).getAccessKeyIds()));
        assertEquals(1, iamUsers.get(0).getAccessKeyIds().size());
        assertEquals(iamUsers.get(0).getAccessKeyIds().get(0), expectAccessKeyId);

        iamUsers = iamService.listIamUsers(elapsedHoursOfAccessKey + 1L);

        assertTrue(CollectionUtils.isEmpty(iamUsers));
    }

    @Test
    void testIamExceptionThrown() {
        given(iamClient.listUsers(any(ListUsersRequest.class))).willThrow(IamException.class);

        assertThatThrownBy(() -> iamService.listIamUsers(1L)).isInstanceOf(IamException.class);
    }
}
