package com.example.demo.controller;

import com.example.demo.model.IamResponse;
import com.example.demo.service.IamService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import software.amazon.awssdk.services.iam.model.IamException;

@RestController
@RequestMapping("/iam")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class IamController {
    private final IamService iamService;

    @Value("${aws.iam.user.accessKey.elapsedHours}")
    private long defaultElapsedHoursOfAccessKey;

    @GetMapping("/users")
    public IamResponse listIamKeys(@RequestParam(required = false) Long elapsedHoursOfAccessKey) {
        long finalElapsedHoursOfAccessKey = elapsedHoursOfAccessKey == null ? defaultElapsedHoursOfAccessKey : elapsedHoursOfAccessKey;

        return IamResponse.builder()
                .success(true)
                .elapsedHoursOfAccessKey(finalElapsedHoursOfAccessKey)
                .users(iamService.listIamUsers(finalElapsedHoursOfAccessKey))
                .build();
    }

    @ExceptionHandler({ IamException.class })
    public ResponseEntity<Object> handleIamException(Exception exception, WebRequest request) {
        return new ResponseEntity<>(
                "Access denied message here", new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
