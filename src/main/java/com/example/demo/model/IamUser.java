package com.example.demo.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class IamUser {
    private String id;
    private List<String> accessKeyIds;
}
