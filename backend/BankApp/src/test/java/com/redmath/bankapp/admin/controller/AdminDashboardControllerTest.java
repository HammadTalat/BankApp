package com.redmath.bankapp.admin.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.redmath.bankapp.admin.dto.response.AdminDashboardResponse;
import com.redmath.bankapp.admin.exception.AdminExceptionHandler;
import com.redmath.bankapp.admin.service.AdminDashboardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class AdminDashboardControllerTest {

    private AdminDashboardService dashboardService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        dashboardService = mock(AdminDashboardService.class);
        AdminDashboardController controller =
                new AdminDashboardController(dashboardService);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new AdminExceptionHandler())
                .build();
    }

    @Test
    void shouldReturnDashboardCounts() throws Exception {
        AdminDashboardResponse response = new AdminDashboardResponse(
                3,
                8,
                2,
                10,
                7,
                3
        );
        when(dashboardService.getDashboardSummary()).thenReturn(response);

        mockMvc.perform(get("/api/v1/admin/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pendingUsers").value(3))
                .andExpect(jsonPath("$.approvedUsers").value(8))
                .andExpect(jsonPath("$.rejectedUsers").value(2))
                .andExpect(jsonPath("$.totalAccounts").value(10))
                .andExpect(jsonPath("$.activeAccounts").value(7))
                .andExpect(jsonPath("$.closedAccounts").value(3));

        verify(dashboardService).getDashboardSummary();
    }
}
