package com.redmath.bankapp.admin.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.redmath.bankapp.account.entity.AccountStatus;
import com.redmath.bankapp.admin.dto.request.UpdateUserRequest;
import com.redmath.bankapp.admin.dto.response.AdminUserResponse;
import com.redmath.bankapp.admin.dto.response.UserApprovalResponse;
import com.redmath.bankapp.admin.dto.response.UserRejectionResponse;
import com.redmath.bankapp.admin.exception.AdminExceptionHandler;
import com.redmath.bankapp.admin.exception.ResourceNotFoundException;
import com.redmath.bankapp.admin.service.AdminApprovalService;
import com.redmath.bankapp.admin.service.AdminRejectionService;
import com.redmath.bankapp.admin.service.AdminUserService;
import com.redmath.bankapp.user.entity.ApprovalStatus;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

class AdminUserControllerTest {

    private static final Long USER_ID = 1L;
    private static final String EMAIL = "ali@example.com";
    private static final String ACCOUNT_NUMBER = "5839201746382915";

    private AdminApprovalService approvalService;
    private AdminUserService userService;
    private AdminRejectionService rejectionService;
    private LocalValidatorFactoryBean validator;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        approvalService = mock(AdminApprovalService.class);
        userService = mock(AdminUserService.class);
        rejectionService = mock(AdminRejectionService.class);

        AdminUserController controller = new AdminUserController(
                approvalService,
                userService,
                rejectionService
        );

        validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new AdminExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @AfterEach
    void closeValidator() {
        if (validator != null) {
            validator.close();
        }
    }

    @Test
    void shouldReturnPendingUsers() throws Exception {
        AdminUserResponse response = new AdminUserResponse(
                USER_ID,
                "Ali Khan",
                EMAIL,
                "Lahore",
                ApprovalStatus.PENDING
        );
        when(userService.getUsersByApprovalStatus(ApprovalStatus.PENDING))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/admin/users")
                        .param("approvalStatus", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(USER_ID))
                .andExpect(jsonPath("$[0].name").value("Ali Khan"))
                .andExpect(jsonPath("$[0].email").value(EMAIL))
                .andExpect(jsonPath("$[0].approvalStatus").value("PENDING"));

        verify(userService).getUsersByApprovalStatus(ApprovalStatus.PENDING);
    }

    @Test
    void shouldReturnUserById() throws Exception {
        AdminUserResponse response = new AdminUserResponse(
                USER_ID,
                "Ali Khan",
                EMAIL,
                "Lahore",
                ApprovalStatus.APPROVED
        );
        when(userService.getUserById(USER_ID)).thenReturn(response);

        mockMvc.perform(get("/api/v1/admin/users/{userId}", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(USER_ID))
                .andExpect(jsonPath("$.email").value(EMAIL));
    }

    @Test
    void shouldUpdateUser() throws Exception {
        UpdateUserRequest request = new UpdateUserRequest(
                "Ali Ahmed",
                "ali.ahmed@example.com",
                "Karachi"
        );
        AdminUserResponse response = new AdminUserResponse(
                USER_ID,
                request.name(),
                request.email(),
                request.address(),
                ApprovalStatus.APPROVED
        );
        when(userService.updateUser(eq(USER_ID), any(UpdateUserRequest.class)))
                .thenReturn(response);

        mockMvc.perform(patch("/api/v1/admin/users/{userId}", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Ali Ahmed",
                                  "email": "ali.ahmed@example.com",
                                  "address": "Karachi"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Ali Ahmed"))
                .andExpect(jsonPath("$.email").value(
                        "ali.ahmed@example.com"
                ))
                .andExpect(jsonPath("$.address").value("Karachi"));
    }

    @Test
    void shouldApproveUser() throws Exception {
        UserApprovalResponse response = new UserApprovalResponse(
                USER_ID,
                "Ali Khan",
                EMAIL,
                ApprovalStatus.APPROVED,
                ACCOUNT_NUMBER,
                AccountStatus.ACTIVE,
                BigDecimal.ZERO
        );
        when(approvalService.approveUser(USER_ID)).thenReturn(response);

        mockMvc.perform(post(
                        "/api/v1/admin/users/{userId}/approve",
                        USER_ID
                ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.approvalStatus").value("APPROVED"))
                .andExpect(jsonPath("$.accountNumber").value(ACCOUNT_NUMBER))
                .andExpect(jsonPath("$.accountStatus").value("ACTIVE"))
                .andExpect(jsonPath("$.balance").value(0));
    }

    @Test
    void shouldRejectUser() throws Exception {
        UserRejectionResponse response = new UserRejectionResponse(
                USER_ID,
                "Ali Khan",
                EMAIL,
                ApprovalStatus.REJECTED
        );
        when(rejectionService.rejectUser(USER_ID)).thenReturn(response);

        mockMvc.perform(post(
                        "/api/v1/admin/users/{userId}/reject",
                        USER_ID
                ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(USER_ID))
                .andExpect(jsonPath("$.approvalStatus").value("REJECTED"));
    }

    @Test
    void shouldReturnValidationErrorForInvalidEmail() throws Exception {
        mockMvc.perform(patch("/api/v1/admin/users/{userId}", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "invalid-email"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(
                        "ADMIN_VALIDATION_FAILED"
                ))
                .andExpect(jsonPath("$.errors.email").value(
                        "Email format is invalid"
                ));

        verifyNoInteractions(userService);
    }

    @Test
    void shouldReturnNotFoundError() throws Exception {
        when(userService.getUserById(USER_ID))
                .thenThrow(new ResourceNotFoundException(
                        "User not found with ID: " + USER_ID
                ));

        mockMvc.perform(get("/api/v1/admin/users/{userId}", USER_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value(
                        "ADMIN_RESOURCE_NOT_FOUND"
                ))
                .andExpect(jsonPath("$.detail").value(
                        "User not found with ID: " + USER_ID
                ));
    }
}
