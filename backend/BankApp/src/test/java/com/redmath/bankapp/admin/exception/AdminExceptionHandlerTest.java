package com.redmath.bankapp.admin.exception;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.redmath.bankapp.admin.controller.AdminAccountController;
import com.redmath.bankapp.admin.controller.AdminUserController;
import com.redmath.bankapp.admin.dto.request.UpdateUserRequest;
import com.redmath.bankapp.admin.dto.response.AdminUserResponse;
import com.redmath.bankapp.admin.service.AdminAccountService;
import com.redmath.bankapp.admin.service.AdminApprovalService;
import com.redmath.bankapp.admin.service.AdminRejectionService;
import com.redmath.bankapp.admin.service.AdminUserService;
import com.redmath.bankapp.user.entity.ApprovalStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class AdminExceptionHandlerTest {

    private AdminApprovalService approvalService;
    private AdminUserService userService;
    private AdminRejectionService rejectionService;
    private AdminAccountService accountService;
    private LocalValidatorFactoryBean validator;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        approvalService = mock(AdminApprovalService.class);
        userService = mock(AdminUserService.class);
        rejectionService = mock(AdminRejectionService.class);
        accountService = mock(AdminAccountService.class);

        AdminUserController userController = new AdminUserController(
                approvalService,
                userService,
                rejectionService
        );
        AdminAccountController accountController =
                new AdminAccountController(accountService);

        validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(
                        userController,
                        accountController
                )
                .setControllerAdvice(new AdminExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @AfterEach
    void closeValidator() {
        validator.close();
    }

    @Test
    void returnsNotFoundWhenAdminResourceDoesNotExist() throws Exception {
        when(userService.getUserById(99L))
                .thenThrow(new ResourceNotFoundException(
                        "User not found with ID: 99"
                ));

        mockMvc.perform(get("/api/v1/admin/users/99"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_PROBLEM_JSON
                ))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("Resource not found"))
                .andExpect(jsonPath("$.code").value(
                        "ADMIN_RESOURCE_NOT_FOUND"
                ))
                .andExpect(jsonPath("$.detail").value(
                        "User not found with ID: 99"
                ))
                .andExpect(jsonPath("$.instance").value(
                        "/api/v1/admin/users/99"
                ))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void returnsBadRequestForInvalidUpdateRequest() throws Exception {
        when(userService.updateUser(
                eq(7L),
                any(UpdateUserRequest.class)
        )).thenThrow(new InvalidUpdateRequestException(
                "At least one field must be provided"
        ));

        mockMvc.perform(patch("/api/v1/admin/users/7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("ADMIN_INVALID_UPDATE"))
                .andExpect(jsonPath("$.detail").value(
                        "At least one field must be provided"
                ));
    }

    @Test
    void returnsConflictForDuplicateEmail() throws Exception {
        when(userService.updateUser(
                eq(7L),
                any(UpdateUserRequest.class)
        )).thenThrow(new DuplicateEmailException(
                "Email is already registered"
        ));

        mockMvc.perform(patch("/api/v1/admin/users/7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"existing@example.com"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.code").value("ADMIN_DUPLICATE_EMAIL"))
                .andExpect(jsonPath("$.detail").value(
                        "Email is already registered"
                ));
    }

    @Test
    void returnsConflictForInvalidUserState() throws Exception {
        when(approvalService.approveUser(7L))
                .thenThrow(new InvalidUserStateException(
                        "Only pending users can be approved"
                ));

        mockMvc.perform(post("/api/v1/admin/users/7/approve"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(
                        "ADMIN_INVALID_USER_STATE"
                ))
                .andExpect(jsonPath("$.detail").value(
                        "Only pending users can be approved"
                ));
    }

    @Test
    void returnsConflictForInvalidAccountState() throws Exception {
        when(accountService.closeAccount("1234567890123456"))
                .thenThrow(new InvalidAccountStateException(
                        "Account balance must be zero before closing"
                ));

        mockMvc.perform(post(
                        "/api/v1/admin/accounts/1234567890123456/close"
                ))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(
                        "ADMIN_INVALID_ACCOUNT_STATE"
                ))
                .andExpect(jsonPath("$.detail").value(
                        "Account balance must be zero before closing"
                ));
    }

    @Test
    void returnsFieldErrorsForInvalidRequestBody() throws Exception {
        mockMvc.perform(patch("/api/v1/admin/users/7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"not-an-email"}
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
    void returnsAllowedValuesForInvalidEnumParameter() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users")
                        .param("approvalStatus", "UNKNOWN"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(
                        "ADMIN_INVALID_PARAMETER"
                ))
                .andExpect(jsonPath("$.detail").value(
                        containsString("Allowed values: PENDING, APPROVED, REJECTED")
                ));

        verifyNoInteractions(userService);
    }

    @Test
    void returnsBadRequestWhenRequiredParameterIsMissing() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(
                        "ADMIN_MISSING_PARAMETER"
                ))
                .andExpect(jsonPath("$.detail").value(
                        "Required parameter 'approvalStatus' is missing"
                ));
    }

    @Test
    void returnsBadRequestForMalformedJson() throws Exception {
        mockMvc.perform(patch("/api/v1/admin/users/7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(
                        "ADMIN_MALFORMED_REQUEST"
                ))
                .andExpect(jsonPath("$.detail").value(
                        "Request body is missing, malformed, or contains an invalid value"
                ));
    }

    @Test
    void returnsBadRequestForZeroPageSize() throws Exception {
        mockMvc.perform(get("/api/v1/admin/accounts")
                        .param("size", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(
                        "ADMIN_INVALID_PARAMETER"
                ))
                .andExpect(jsonPath("$.detail").value(
                        containsString("Page size")
                ));

        verifyNoInteractions(accountService);
    }

    @Test
    void hidesInternalDetailsForUnexpectedFailures() throws Exception {
        when(userService.getUserById(7L))
                .thenThrow(new RuntimeException(
                        "database password leaked by driver"
                ));

        mockMvc.perform(get("/api/v1/admin/users/7"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("ADMIN_INTERNAL_ERROR"))
                .andExpect(jsonPath("$.detail").value(
                        "The server could not complete the admin request"
                ))
                .andExpect(content().string(not(containsString(
                        "database password leaked by driver"
                ))));
    }

    @Test
    void leavesSuccessfulAdminResponsesUnchanged() throws Exception {
        when(userService.getUserById(7L)).thenReturn(
                new AdminUserResponse(
                        7L,
                        "Ayesha Khan",
                        "ayesha.khan@example.com",
                        "Lahore",
                        ApprovalStatus.APPROVED
                )
        );

        mockMvc.perform(get("/api/v1/admin/users/7"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.email").value(
                        "ayesha.khan@example.com"
                ));
    }
}
