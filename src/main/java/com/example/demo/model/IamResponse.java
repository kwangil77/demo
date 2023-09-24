package com.example.demo.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class IamResponse {
    private boolean success;
    private long elapsedHoursOfAccessKey;
    private List<IamUser> users;
}
