package com.example.demo.service;

import com.example.demo.model.IamUser;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import software.amazon.awssdk.services.iam.IamClient;
import software.amazon.awssdk.services.iam.model.AccessKeyMetadata;
import software.amazon.awssdk.services.iam.model.ListAccessKeysRequest;
import software.amazon.awssdk.services.iam.model.ListAccessKeysResponse;
import software.amazon.awssdk.services.iam.model.ListUsersRequest;
import software.amazon.awssdk.services.iam.model.ListUsersResponse;
import software.amazon.awssdk.services.iam.model.User;

import java.time.Clock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class IamService {
    private final Clock clock;
    private final IamClient iamClient;

    public List<IamUser> listIamUsers(long elapsedHoursOfAccessKey) {
        List<IamUser> users = new ArrayList<>();

        listAllUsers().forEach(user -> {
            List<AccessKeyMetadata> keys = listOldAccessKeys(user.userName(), elapsedHoursOfAccessKey);

            if (!CollectionUtils.isEmpty(keys)) {
                users.add(IamUser.builder()
                        .id(user.userId())
                        .accessKeyIds(keys.stream().map(AccessKeyMetadata::accessKeyId).toList())
                        .build());
            }
        });
        return users;
    }

    private List<User> listAllUsers() {
        List<User> users = new ArrayList<>();
        boolean done = false;
        String newMarker = null;

        while (!done) {
            ListUsersRequest.Builder requestBuilder = ListUsersRequest.builder();

            if (newMarker != null) {
                requestBuilder.marker(newMarker);
            }
            ListUsersResponse response = iamClient.listUsers(requestBuilder.build());

            users.addAll(response.users());

            if (!response.isTruncated()) {
                done = true;
            } else {
                newMarker = response.marker();
            }
        }
        return users;
    }

    private List<AccessKeyMetadata> listOldAccessKeys(String userName, long elapsedHours) {
        List<AccessKeyMetadata> accessKeys = new ArrayList<>();
        boolean done = false;
        String newMarker = null;

        while (!done) {
            ListAccessKeysRequest.Builder requestBuilder = ListAccessKeysRequest.builder()
                    .userName(userName);

            if (newMarker != null) {
                requestBuilder.marker(newMarker);
            }
            ListAccessKeysResponse response = iamClient.listAccessKeys(requestBuilder.build());

            accessKeys.addAll(response.accessKeyMetadata().stream()
                    .filter(metadata -> Duration.between(metadata.createDate(), clock.instant()).toHours() > elapsedHours)
                    .toList());

            if (!response.isTruncated()) {
                done = true;
            } else {
                newMarker = response.marker();
            }
        }
        return accessKeys;
    }
}
