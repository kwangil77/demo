package com.example.demo.controller;

import com.example.demo.model.IamUser;
import com.example.demo.service.IamService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@WebMvcTest(IamController.class)
class IamControllerTest {
    @Autowired private MockMvc mockMvc;
    @MockBean private IamService iamService;

    @Test
    void testListIamKeys() throws Exception {
        final String expectId = "AIDACKCEVSQ6C2EXAMPLE";
        final String expectAccessKeyId = "AKIAIOSFODNN7EXAMPLE";
        final long elapsedHoursOfAccessKey = 1L;

        List<IamUser> users = List.of(
                IamUser.builder()
                        .id(expectId)
                        .accessKeyIds(List.of(
                                expectAccessKeyId
                        ))
                        .build()
        );
        given(iamService.listIamUsers(anyLong())).willReturn(users);

        ResultMatcher[] matchers = {
                status().isOk(),
                jsonPath("success").value(true),
                jsonPath("users").isNotEmpty(),
                jsonPath("users.[0].id").value(expectId),
                jsonPath("users.[0].accessKeyIds").isNotEmpty(),
                jsonPath("users.[0].accessKeyIds.[0]").value(expectAccessKeyId)
        };
        mockMvc.perform(
                        get("/iam/users")
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpectAll(matchers);

        verify(iamService).listIamUsers(elapsedHoursOfAccessKey);

        mockMvc.perform(
                        get("/iam/users")
                                .param("elapsedHoursOfAccessKey", String.valueOf(elapsedHoursOfAccessKey))
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpectAll(matchers);
    }

    @Test
    void testMethodArgumentTypeMismatchExceptionThrown() throws Exception {
        final String elapsedHoursOfAccessKey = "A";

        mockMvc.perform(
                        get("/iam/users")
                                .param("elapsedHoursOfAccessKey", elapsedHoursOfAccessKey)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertInstanceOf(
                        MethodArgumentTypeMismatchException.class, result.getResolvedException()));
    }
}
