package com.redmath.bankapp.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redmath.bankapp.auth.entity.LocalCredential;
import com.redmath.bankapp.auth.repository.LocalCredentialRepository;
import com.redmath.bankapp.auth.security.AuthCookieService;
import com.redmath.bankapp.user.entity.AppUser;
import com.redmath.bankapp.user.entity.ApprovalStatus;
import com.redmath.bankapp.user.entity.Role;
import com.redmath.bankapp.user.repository.AppUserRepository;
import jakarta.servlet.http.Cookie;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
class AuthControllerTest {

  @Autowired
  private MockMvc mockMvc;

  private final ObjectMapper objectMapper = new ObjectMapper();

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

  private JsonNode readBody(MvcResult result) throws Exception {
    return objectMapper.readTree(result.getResponse().getContentAsString());
  }

  private void createUserInDb(String email, String name, String address, String password) {
    AppUser appUser = new AppUser();
    appUser.setName(name);
    appUser.setEmail(email);
    appUser.setAddress(address);
    appUser.setRole(Role.ACCOUNT_HOLDER);
    appUser.setApprovalStatus(ApprovalStatus.PENDING);
    appUserRepository.save(appUser);

    LocalCredential credential = new LocalCredential();
    credential.setEmail(email);
    credential.setPasswordHash(passwordEncoder.encode(password));
    localCredentialRepository.save(credential);
  }

  private void createUserInDb(String email, String name, String address) {
    createUserInDb(email, name, address, "password123");
  }

  private Cookie establishSession(String email, String password) throws Exception {
    String formData = "username=" + URLEncoder.encode(email, StandardCharsets.UTF_8)
        + "&password=" + URLEncoder.encode(password, StandardCharsets.UTF_8);

    MvcResult loginResult = mockMvc.perform(
        post("/api/v1/auth/login")
            .contentType(APPLICATION_FORM_URLENCODED)
            .content(formData)
    ).andReturn();

    if (loginResult.getResponse().getStatus() != 200) {
      throw new IllegalStateException(
          "Login failed for test setup: " + loginResult.getResponse().getContentAsString()
      );
    }

    String header = loginResult.getResponse().getHeader("Set-Cookie");
    String token = extractAccessToken(header);
    return new Cookie(AuthCookieService.ACCESS_TOKEN_COOKIE, token);
  }

  private String extractAccessToken(String setCookieHeader) {
    String prefix = AuthCookieService.ACCESS_TOKEN_COOKIE + "=";
    int start = setCookieHeader.indexOf(prefix);
    int end = setCookieHeader.indexOf(';', start);
    return setCookieHeader.substring(start + prefix.length(), end);
  }

  private record SignupRequest(String name, String email, String address, String password) {
  }

  @Nested
  @DisplayName("POST /api/v1/auth/signup")
  class SignupTests {

    @Test
    @DisplayName("Should create user and return 201 CREATED with success message")
    void signup_ValidRequest_Returns201() throws Exception {
      String email = "john.signer." + System.currentTimeMillis() + "@example.com";

      MvcResult result = mockMvc.perform(
          post("/api/v1/auth/signup")
              .contentType(APPLICATION_JSON)
              .content(json(new SignupRequest(
                  "John Doe",
                  email,
                  "123 Main St, City",
                  "password123"
              )))
      ).andReturn();

      assertThat(result.getResponse().getStatus()).isEqualTo(201);

      JsonNode response = readBody(result);
      assertThat(response.get("success").asBoolean()).isTrue();
      assertThat(response.get("message").asText())
          .contains("pending administrator approval");

      assertThat(appUserRepository.findByEmail(email)).isPresent();
      assertThat(localCredentialRepository.findByEmail(email)).isPresent();
    }

    @Test
    @DisplayName("Should reject duplicate email")
    void signup_DuplicateEmail_ReturnsError() throws Exception {
      String email = "dup." + System.currentTimeMillis() + "@example.com";
      createUserInDb(email, "Existing User", "456 Oak Ave");

      MvcResult result = mockMvc.perform(
          post("/api/v1/auth/signup")
              .contentType(APPLICATION_JSON)
              .content(json(new SignupRequest(
                  "Another User",
                  email,
                  "789 Pine St",
                  "password123"
              )))
      ).andReturn();

      JsonNode response = readBody(result);
      assertThat(response.get("success").asBoolean()).isFalse();
      assertThat(response.get("message").asText()).contains("Email already exists");
    }

    @Test
    @DisplayName("Should reject signup with missing name")
    void signup_MissingName_Returns400() throws Exception {
      MvcResult result = mockMvc.perform(
          post("/api/v1/auth/signup")
              .contentType(APPLICATION_JSON)
              .content("{\"email\":\"noname@example.com\",\"address\":\"Addr\",\"password\":\"password123\"}")
      ).andReturn();

      assertThat(result.getResponse().getStatus()).isEqualTo(400);
    }

    @Test
    @DisplayName("Should reject signup with missing email")
    void signup_MissingEmail_Returns400() throws Exception {
      MvcResult result = mockMvc.perform(
          post("/api/v1/auth/signup")
              .contentType(APPLICATION_JSON)
              .content("{\"name\":\"No Email\",\"address\":\"Addr\",\"password\":\"password123\"}")
      ).andReturn();

      assertThat(result.getResponse().getStatus()).isEqualTo(400);
    }

    @Test
    @DisplayName("Should reject signup with missing password")
    void signup_MissingPassword_Returns400() throws Exception {
      String email = "nopass." + System.currentTimeMillis() + "@example.com";

      MvcResult result = mockMvc.perform(
          post("/api/v1/auth/signup")
              .contentType(APPLICATION_JSON)
              .content("{\"name\":\"No Pass\",\"email\":\"" + email + "\",\"address\":\"Addr\"}")
      ).andReturn();

      assertThat(result.getResponse().getStatus()).isEqualTo(400);
    }
  }

  @Nested
  @DisplayName("POST /api/v1/auth/login")
  class LoginTests {

    @Test
    @DisplayName("Should authenticate and set HttpOnly cookie on valid credentials")
    void login_ValidCredentials_Returns200AndSetsCookie() throws Exception {
      String email = "login." + System.currentTimeMillis() + "@example.com";
      createUserInDb(email, "Login User", "101 Auth St");

      String formData = "username=" + URLEncoder.encode(email, StandardCharsets.UTF_8)
          + "&password=password123";

      MvcResult result = mockMvc.perform(
          post("/api/v1/auth/login")
              .contentType(APPLICATION_FORM_URLENCODED)
              .content(formData)
      ).andReturn();

      assertThat(result.getResponse().getStatus()).isEqualTo(200);

      JsonNode body = readBody(result);
      assertThat(body.get("email").asText()).isEqualTo(email);
      assertThat(body.get("name").asText()).isEqualTo("Login User");
      assertThat(body.get("role").asText()).isEqualTo("ACCOUNT_HOLDER");
      assertThat(result.getResponse().getHeader("Set-Cookie"))
          .contains("bankapp_access_token");
    }

    @Test
    @DisplayName("Should reject invalid password")
    void login_InvalidPassword_Returns401() throws Exception {
      String email = "badpass." + System.currentTimeMillis() + "@example.com";
      createUserInDb(email, "Bad Pass User", "202 Block St");

      String formData = "username=" + URLEncoder.encode(email, StandardCharsets.UTF_8)
          + "&password=WrongPassword123";

      MvcResult result = mockMvc.perform(
          post("/api/v1/auth/login")
              .contentType(APPLICATION_FORM_URLENCODED)
              .content(formData)
      ).andReturn();

      assertThat(result.getResponse().getStatus()).isEqualTo(401);

      JsonNode body = readBody(result);
      assertThat(body.get("status").asInt()).isEqualTo(401);
      assertThat(body.get("error").asText()).isEqualTo("Unauthorized");
    }

    @Test
    @DisplayName("Should reject non-existent user")
    void login_NonExistentUser_Returns401() throws Exception {
      String formData = "username=nonexistent." + System.currentTimeMillis()
          + "@example.com&password=SomePass123";

      MvcResult result = mockMvc.perform(
          post("/api/v1/auth/login")
              .contentType(APPLICATION_FORM_URLENCODED)
              .content(formData)
      ).andReturn();

      assertThat(result.getResponse().getStatus()).isEqualTo(401);
    }
  }

  @Nested
  @DisplayName("POST /api/v1/auth/logout")
  class LogoutTests {

    @Test
    @DisplayName("Should clear auth cookie and return 204")
    void logout_Authenticated_Returns204() throws Exception {
      String email = "logout." + System.currentTimeMillis() + "@example.com";
      createUserInDb(email, "Logout User", "303 Exit St");
      Cookie cookie = establishSession(email, "password123");

      MvcResult result = mockMvc.perform(
          post("/api/v1/auth/logout").cookie(cookie)
                  .with(csrf())
      ).andReturn();

      assertThat(result.getResponse().getStatus()).isEqualTo(204);
      assertThat(result.getResponse().getHeader("Set-Cookie"))
          .contains("bankapp_access_token")
          .contains("Max-Age=0");
    }

    @Test
    @DisplayName("Should return 204 even when not authenticated")
    void logout_Unauthenticated_Returns204() throws Exception {
      MvcResult result = mockMvc.perform(post("/api/v1/auth/logout").with(csrf())).andReturn();

      assertThat(result.getResponse().getStatus()).isEqualTo(204);
    }
  }

  @Nested
  @DisplayName("Security filter and access control")
  class SecurityFilterTests {

    @Test
    @DisplayName("Should return 401 when accessing protected endpoint without cookie")
    void protectedEndpoint_NoCookie_Returns401() throws Exception {
      MvcResult result = mockMvc.perform(
          get("/api/v1/transaction/lookup").param("accountID", "ACC123")
      ).andReturn();

      assertThat(result.getResponse().getStatus()).isEqualTo(401);
    }

    @Test
    @DisplayName("Should return 401 when JWT token is invalid")
    void protectedEndpoint_InvalidJwt_Returns401() throws Exception {
      MvcResult result = mockMvc.perform(
          get("/api/v1/account/balance")
              .cookie(new Cookie(AuthCookieService.ACCESS_TOKEN_COOKIE, "invalid-token-value"))
      ).andReturn();

      assertThat(result.getResponse().getStatus()).isEqualTo(401);
    }

    @Test
    @DisplayName("Should return 403 when pending user accesses non-profile endpoint")
    void pendingUser_AccessingNonMeEndpoint_Returns403() throws Exception {
      String email = "pending." + System.currentTimeMillis() + "@example.com";
      createUserInDb(email, "Pending User", "Pending Address");
      Cookie cookie = establishSession(email, "password123");

      MvcResult result = mockMvc.perform(
          get("/api/v1/account/balance").cookie(cookie)
      ).andReturn();

      assertThat(result.getResponse().getStatus()).isEqualTo(403);
    }

    @Test
    @DisplayName("Should allow pending user to access own profile")
    void pendingUser_AccessingMeEndpoint_Returns200() throws Exception {
      String email = "pendingme." + System.currentTimeMillis() + "@example.com";
      createUserInDb(email, "Pending Me", "Pending Address");
      Cookie cookie = establishSession(email, "password123");

      MvcResult result = mockMvc.perform(
          get("/api/v1/me").cookie(cookie)
      ).andReturn();

      assertThat(result.getResponse().getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("Should allow pending user to access complete-profile sub-path")
    void pendingUser_AccessingMeCompleteProfile_Returns200() throws Exception {
      String email = "pendingsub." + System.currentTimeMillis() + "@example.com";
      createUserInDb(email, "Pending Sub", "Pending Address");
      Cookie cookie = establishSession(email, "password123");

      MvcResult result = mockMvc.perform(
          post("/api/v1/me/complete-profile")
                  .with(csrf())
              .cookie(cookie)
              .contentType(APPLICATION_JSON)
              .content("{\"address\":\"New Address\"}")
      ).andReturn();

      assertThat(result.getResponse().getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("Should allow approved user to access non-profile protected endpoint")
    void approvedUser_AccessingNonMeEndpoint_Returns200() throws Exception {
      String email = "approvednonme." + System.currentTimeMillis() + "@example.com";
      AppUser appUser = new AppUser();
      appUser.setName("Approved NonMe");
      appUser.setEmail(email);
      appUser.setAddress("Approved Address");
      appUser.setRole(Role.ACCOUNT_HOLDER);
      appUser.setApprovalStatus(ApprovalStatus.APPROVED);
      appUserRepository.save(appUser);

      LocalCredential credential = new LocalCredential();
      credential.setEmail(email);
      credential.setPasswordHash(passwordEncoder.encode("password123"));
      localCredentialRepository.save(credential);

      com.redmath.bankapp.account.entity.BankAccount bankAccount =
          new com.redmath.bankapp.account.entity.BankAccount(
              "ACC" + System.currentTimeMillis(),
              appUser
          );
      bankAccountRepository.save(bankAccount);

      com.redmath.bankapp.account.entity.AccountBalance balance =
          new com.redmath.bankapp.account.entity.AccountBalance(
              bankAccount,
              new java.math.BigDecimal("1000.00"),
              com.redmath.bankapp.account.entity.BalanceIndicator.CREDIT
          );
      accountBalanceRepository.save(balance);

      Cookie cookie = establishSession(email, "password123");

      MvcResult result = mockMvc.perform(
          get("/api/v1/account/balance").cookie(cookie)
      ).andReturn();

      assertThat(result.getResponse().getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("Should return 401 when user exists but has no local credentials")
    void login_MissingLocalCredential_Returns401() throws Exception {
      String email = "nocred." + System.currentTimeMillis() + "@example.com";
      AppUser appUser = new AppUser();
      appUser.setName("No Cred User");
      appUser.setEmail(email);
      appUser.setAddress("No Cred Address");
      appUser.setRole(Role.ACCOUNT_HOLDER);
      appUser.setApprovalStatus(ApprovalStatus.PENDING);
      appUserRepository.save(appUser);

      String formData = "username=" + email + "&password=password123";

      MvcResult result = mockMvc.perform(
          post("/api/v1/auth/login")
              .contentType(APPLICATION_FORM_URLENCODED)
              .content(formData)
      ).andReturn();

      assertThat(result.getResponse().getStatus()).isEqualTo(401);
    }
  }
}
