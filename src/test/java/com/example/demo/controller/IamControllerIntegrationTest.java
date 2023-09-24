package com.example.demo.controller;

import com.example.demo.service.IamService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import software.amazon.awssdk.services.iam.model.IamException;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@ActiveProfiles("test")
@SpringBootTest
public class IamControllerIntegrationTest {
    @Autowired private MockMvc mockMvc;
    @MockBean private IamService iamService;

    @Test
    void testIamExceptionThrown() throws Exception {
        given(iamService.listIamUsers(anyLong())).willThrow(IamException.class);

        mockMvc.perform(
                        get("/iam/users")
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isInternalServerError())
                .andExpect(result -> assertInstanceOf(IamException.class, result.getResolvedException()));
    }
}
