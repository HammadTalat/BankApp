package com.redmath.bankapp.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redmath.bankapp.auth.entity.LocalCredential;
import com.redmath.bankapp.auth.repository.LocalCredentialRepository;
import com.redmath.bankapp.auth.security.ApiSecurityService;
import com.redmath.bankapp.auth.security.AuthCookieService;
import com.redmath.bankapp.user.entity.AppUser;
import com.redmath.bankapp.user.entity.ApprovalStatus;
import com.redmath.bankapp.user.entity.Role;
import com.redmath.bankapp.user.repository.AppUserRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest(
    properties = {
        "GOOGLE_CLIENT_ID=dummy",
        "GOOGLE_CLIENT_SECRET=dummy"
    }
)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserControllerTest {

  @Autowired
  private MockMvc mockMvc;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Autowired
  private ApiSecurityService apiSecurityService;

  @Autowired
  private AppUserRepository appUserRepository;

  @Autowired
  private LocalCredentialRepository localCredentialRepository;

  @Autowired
  private com.redmath.bankapp.account.repository.BankAccountRepository bankAccountRepository;

  @Autowired
  private com.redmath.bankapp.account.repository.AccountBalanceRepository accountBalanceRepository;

  @Autowired
  private com.redmath.bankapp.transaction.repository.AccountTransactionRepository accountTransactionRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @BeforeEach
  void setUp() {
    accountTransactionRepository.deleteAllInBatch();
    accountBalanceRepository.deleteAllInBatch();
    bankAccountRepository.deleteAllInBatch();
    localCredentialRepository.deleteAllInBatch();
    appUserRepository.deleteAllInBatch();
  }

  private String json(Object value) throws Exception {
    return objectMapper.writeValueAsString(value);
  }

  private Cookie authenticatedCookie(String email, String name, String address) {
    AppUser user = createUserInDb(email, name, address);
    String token = apiSecurityService.generateToken(user);
    return new Cookie(AuthCookieService.ACCESS_TOKEN_COOKIE, token);
  }

  private AppUser createUserInDb(String email, String name, String address) {
    AppUser appUser = new AppUser();
    appUser.setName(name);
    appUser.setEmail(email);
    appUser.setAddress(address);
    appUser.setRole(Role.ACCOUNT_HOLDER);
    appUser.setApprovalStatus(ApprovalStatus.PENDING);
    appUserRepository.save(appUser);

    LocalCredential credential = new LocalCredential();
    credential.setEmail(email);
    credential.setPasswordHash(passwordEncoder.encode("SecurePass1!"));
    localCredentialRepository.save(credential);

    return appUser;
  }

  private record CompleteProfileRequest(String address) {
  }

  @Nested
  @DisplayName("GET /api/v1/me")
  class GetProfileTests {

    @Test
    @DisplayName("Should return user profile when authenticated")
    void getCurrentUserProfile_Authenticated_Returns200() throws Exception {
      Cookie cookie = authenticatedCookie(
          "getuser." + System.currentTimeMillis() + "@example.com",
          "Get User",
          "Get Address St"
      );

      MvcResult result = mockMvc.perform(
          get("/api/v1/me").cookie(cookie)
      ).andReturn();

      assertThat(result.getResponse().getStatus()).isEqualTo(200);

      JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
      assertThat(body.get("name").asText()).isEqualTo("Get User");
      assertThat(body.get("email").asText()).contains("@example.com");
      assertThat(body.get("address").asText()).isEqualTo("Get Address St");
      assertThat(body.get("role").asText()).isEqualTo("ACCOUNT_HOLDER");
      assertThat(body.get("approvalStatus").asText()).isEqualTo("PENDING");
    }

    @Test
    @DisplayName("Should return 401 when not authenticated")
    void getCurrentUserProfile_Unauthenticated_Returns401() throws Exception {
      MvcResult result = mockMvc.perform(get("/api/v1/me")).andReturn();

      assertThat(result.getResponse().getStatus()).isEqualTo(401);
    }
  }

  @Nested
  @DisplayName("POST /api/v1/me/complete-profile")
  class CompleteProfileTests {

    @Test
    @DisplayName("Should update address and return updated profile when authenticated")
    void completeProfile_ValidAddress_ReturnsUpdatedProfile() throws Exception {
      Cookie cookie = authenticatedCookie(
          "user." + System.currentTimeMillis() + "@example.com",
          "Profile User",
          "Original Address"
      );

      MvcResult result = mockMvc.perform(
          post("/api/v1/me/complete-profile")
              .cookie(cookie)
              .contentType(APPLICATION_JSON)
              .content(json(new CompleteProfileRequest("456 New Address Blvd")))
      ).andReturn();

      assertThat(result.getResponse().getStatus()).isEqualTo(200);

      JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
      assertThat(body.get("address").asText()).isEqualTo("456 New Address Blvd");
      assertThat(body.get("name").asText()).isEqualTo("Profile User");
      assertThat(body.get("role").asText()).isEqualTo("ACCOUNT_HOLDER");
    }

    @Test
    @DisplayName("Should return 400 when address is blank")
    void completeProfile_BlankAddress_Returns400() throws Exception {
      Cookie cookie = authenticatedCookie(
          "blankaddr." + System.currentTimeMillis() + "@example.com",
          "Blank User",
          "Old Address"
      );

      MvcResult result = mockMvc.perform(
          post("/api/v1/me/complete-profile")
              .cookie(cookie)
              .contentType(APPLICATION_JSON)
              .content(json(new CompleteProfileRequest("   ")))
      ).andReturn();

      assertThat(result.getResponse().getStatus()).isEqualTo(400);
    }

    @Test
    @DisplayName("Should return 400 when address is null")
    void completeProfile_NullAddress_Returns400() throws Exception {
      Cookie cookie = authenticatedCookie(
          "nulladdr." + System.currentTimeMillis() + "@example.com",
          "Null User",
          "Old Address"
      );

      MvcResult result = mockMvc.perform(
          post("/api/v1/me/complete-profile")
              .cookie(cookie)
              .contentType(APPLICATION_JSON)
              .content("{\"address\":null}")
      ).andReturn();

      assertThat(result.getResponse().getStatus()).isEqualTo(400);
    }

    @Test
    @DisplayName("Should return 401 when not authenticated")
    void completeProfile_Unauthenticated_Returns401() throws Exception {
      MvcResult result = mockMvc.perform(
          post("/api/v1/me/complete-profile")
              .contentType(APPLICATION_JSON)
              .content(json(new CompleteProfileRequest("Some Address")))
      ).andReturn();

      assertThat(result.getResponse().getStatus()).isEqualTo(401);
    }
  }
}
