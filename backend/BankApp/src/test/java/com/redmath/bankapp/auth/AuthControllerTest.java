package com.redmath.bankapp.auth;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redmath.bankapp.auth.entity.LocalCredential;
import com.redmath.bankapp.auth.repository.LocalCredentialRepository;
import com.redmath.bankapp.user.entity.AppUser;
import com.redmath.bankapp.user.entity.ApprovalStatus;
import com.redmath.bankapp.user.entity.Role;
import com.redmath.bankapp.user.repository.AppUserRepository;
import java.net.CookieManager;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "GOOGLE_CLIENT_ID=dummy",
        "GOOGLE_CLIENT_SECRET=dummy"
    }
)
@ActiveProfiles("test")
class AuthControllerTest {

  @LocalServerPort
  private int port;

  @org.springframework.beans.factory.annotation.Autowired
  private AppUserRepository appUserRepository;

  @org.springframework.beans.factory.annotation.Autowired
  private LocalCredentialRepository localCredentialRepository;

  @org.springframework.beans.factory.annotation.Autowired
  private com.redmath.bankapp.account.repository.BankAccountRepository bankAccountRepository;

  @org.springframework.beans.factory.annotation.Autowired
  private com.redmath.bankapp.account.repository.AccountBalanceRepository accountBalanceRepository;

  @org.springframework.beans.factory.annotation.Autowired
  private com.redmath.bankapp.transaction.repository.AccountTransactionRepository accountTransactionRepository;

  @org.springframework.beans.factory.annotation.Autowired
  private PasswordEncoder passwordEncoder;

  private HttpClient client;
  private ObjectMapper objectMapper;
  private String baseUrl;

  @BeforeEach
  void setUp() {
    client = HttpClient.newBuilder()
        .cookieHandler(new CookieManager())
        .build();
    objectMapper = new ObjectMapper();
    baseUrl = "http://localhost:" + port;
    accountTransactionRepository.deleteAllInBatch();
    accountBalanceRepository.deleteAllInBatch();
    bankAccountRepository.deleteAllInBatch();
    localCredentialRepository.deleteAllInBatch();
    appUserRepository.deleteAllInBatch();
  }

  private String json(Object value) throws Exception {
    return objectMapper.writeValueAsString(value);
  }

  private JsonNode post(String path, String body, String contentType) throws Exception {
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(baseUrl + path))
        .header("Content-Type", contentType)
        .POST(HttpRequest.BodyPublishers.ofString(body))
        .build();
    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
    return objectMapper.readTree(response.body());
  }

  private JsonNode post(String path, String body) throws Exception {
    return post(path, body, "application/json");
  }

  private HttpResponse<String> postRaw(String path, String body, String contentType)
      throws Exception {
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(baseUrl + path))
        .header("Content-Type", contentType)
        .POST(HttpRequest.BodyPublishers.ofString(body))
        .build();
    return client.send(request, HttpResponse.BodyHandlers.ofString());
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

  private void establishSession(String email, String password) throws Exception {
    String formData = "username=" + URLEncoder.encode(email, StandardCharsets.UTF_8) +
        "&password=" + URLEncoder.encode(password, StandardCharsets.UTF_8);
    HttpRequest loginRequest = HttpRequest.newBuilder()
        .uri(URI.create(baseUrl + "/api/v1/auth/login"))
        .header("Content-Type", "application/x-www-form-urlencoded")
        .POST(HttpRequest.BodyPublishers.ofString(formData))
        .build();
    HttpResponse<String> loginResponse = client.send(loginRequest,
        HttpResponse.BodyHandlers.ofString());
    if (loginResponse.statusCode() != 200) {
      throw new IllegalStateException("Login failed for test setup: " + loginResponse.body());
    }
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
      String requestBody = json(new SignupRequest(
          "John Doe",
          email,
          "123 Main St, City",
          "password123"
      ));

      JsonNode response = post("/api/v1/auth/signup", requestBody);

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

      String requestBody = json(new SignupRequest(
          "Another User",
          email,
          "789 Pine St",
          "password123"
      ));

      JsonNode response = post("/api/v1/auth/signup", requestBody);

      assertThat(response.get("success").asBoolean()).isFalse();
      assertThat(response.get("message").asText())
          .contains("Email already exists");
    }

    @Test
    @DisplayName("Should reject signup with missing name")
    void signup_MissingName_Returns400() throws Exception {
      String email = "noname." + System.currentTimeMillis() + "@example.com";
      String requestBody = "{\"email\":\"" + email + "\",\"address\":\"Addr\",\"password\":\"pass\"}";

      HttpResponse<String> response = postRaw("/api/v1/auth/signup", requestBody, "application/json");

      assertThat(response.statusCode()).isEqualTo(400);
    }

    @Test
    @DisplayName("Should reject signup with missing email")
    void signup_MissingEmail_Returns400() throws Exception {
      String requestBody = "{\"name\":\"No Email\",\"address\":\"Addr\",\"password\":\"pass\"}";

      HttpResponse<String> response = postRaw("/api/v1/auth/signup", requestBody, "application/json");

      assertThat(response.statusCode()).isEqualTo(400);
    }

    @Test
    @DisplayName("Should reject signup with missing password")
    void signup_MissingPassword_Returns400() throws Exception {
      String email = "nopass." + System.currentTimeMillis() + "@example.com";
      String requestBody = "{\"name\":\"No Pass\",\"email\":\"" + email + "\",\"address\":\"Addr\"}";

      HttpResponse<String> response = postRaw("/api/v1/auth/signup", requestBody, "application/json");

      assertThat(response.statusCode()).isEqualTo(400);
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

      String formData = "username=" + URLEncoder.encode(email, StandardCharsets.UTF_8) +
          "&password=password123";
      HttpResponse<String> response = postRaw(

          "/api/v1/auth/login",

          formData,

          "application/x-www-form-urlencoded"

      );

      assertThat(response.statusCode()).isEqualTo(200);

      JsonNode body = objectMapper.readTree(response.body());
      assertThat(body.get("email").asText()).isEqualTo(email);
      assertThat(body.get("name").asText()).isEqualTo("Login User");
      assertThat(body.get("role").asText()).isEqualTo("ACCOUNT_HOLDER");

      assertThat(response.headers().allValues("Set-Cookie"))
          .anyMatch(cookie -> cookie.contains("bankapp_access_token"));
    }

    @Test
    @DisplayName("Should reject invalid password")
    void login_InvalidPassword_Returns401() throws Exception {
      String email = "badpass." + System.currentTimeMillis() + "@example.com";
      createUserInDb(email, "Bad Pass User", "202 Block St");

      String formData = "username=" + URLEncoder.encode(email, StandardCharsets.UTF_8) +
          "&password=WrongPassword123";
      HttpResponse<String> response = postRaw(
          "/api/v1/auth/login",
          formData,
          "application/x-www-form-urlencoded;charset=UTF-8"
      );

      assertThat(response.statusCode()).isEqualTo(401);

      JsonNode body = objectMapper.readTree(response.body());
      assertThat(body.get("status").asInt()).isEqualTo(401);
      assertThat(body.get("error").asText()).isEqualTo("Unauthorized");
    }

    @Test
    @DisplayName("Should reject non-existent user")
    void login_NonExistentUser_Returns401() throws Exception {
      String formData = "username=nonexistent." + System.currentTimeMillis()
          + "@example.com&password=SomePass123";
      HttpResponse<String> response = postRaw(
          "/api/v1/auth/login",
          formData,
          "application/x-www-form-urlencoded;charset=UTF-8"
      );

      assertThat(response.statusCode()).isEqualTo(401);
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

      // Login first to establish a session
      String formData = "username=" + URLEncoder.encode(email, StandardCharsets.UTF_8) +
          "&password=password123";
      HttpResponse<String> loginResponse = postRaw(
          "/api/v1/auth/login",
          formData,
          "application/x-www-form-urlencoded;charset=UTF-8"
      );
      assertThat(loginResponse.statusCode()).isEqualTo(200);

      // Logout
      HttpRequest logoutRequest = HttpRequest.newBuilder()
          .uri(URI.create(baseUrl + "/api/v1/auth/logout"))
          .POST(HttpRequest.BodyPublishers.noBody())
          .build();
      HttpResponse<String> logoutResponse = client.send(logoutRequest,
          HttpResponse.BodyHandlers.ofString());

      assertThat(logoutResponse.statusCode()).isEqualTo(204);

      assertThat(logoutResponse.headers().allValues("Set-Cookie"))
          .anyMatch(
              cookie -> cookie.contains("bankapp_access_token") && cookie.contains("Max-Age=0"));
    }

    @Test
    @DisplayName("Should return 204 even when not authenticated")
    void logout_Unauthenticated_Returns204() throws Exception {
      HttpRequest logoutRequest = HttpRequest.newBuilder()
          .uri(URI.create(baseUrl + "/api/v1/auth/logout"))
          .POST(HttpRequest.BodyPublishers.noBody())
          .build();
      HttpResponse<String> logoutResponse = client.send(logoutRequest,
          HttpResponse.BodyHandlers.ofString());

      assertThat(logoutResponse.statusCode()).isEqualTo(204);
    }
  }

  @Nested
  @DisplayName("Security filter and access control")
  class SecurityFilterTests {

    @Test
    @DisplayName("Should return 401 when accessing protected endpoint without cookie")
    void protectedEndpoint_NoCookie_Returns401() throws Exception {
      HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create(baseUrl + "/api/v1/transaction/lookup?accountID=ACC123"))
          .GET()
          .build();

      HttpResponse<String> response = client.send(request,
          HttpResponse.BodyHandlers.ofString());

      assertThat(response.statusCode()).isEqualTo(401);
    }

    @Test
    @DisplayName("Should return 401 when JWT token is invalid")
    void protectedEndpoint_InvalidJwt_Returns401() throws Exception {
      HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create(baseUrl + "/api/v1/account/balance"))
          .header("Cookie", "bankapp_access_token=invalid-token-value")
          .GET()
          .build();

      HttpResponse<String> response = client.send(request,
          HttpResponse.BodyHandlers.ofString());

      assertThat(response.statusCode()).isEqualTo(401);
    }

    @Test
    @DisplayName("Should return 403 when pending user accesses non-profile endpoint")
    void pendingUser_AccessingNonMeEndpoint_Returns403() throws Exception {
      String email = "pending." + System.currentTimeMillis() + "@example.com";
      createUserInDb(email, "Pending User", "Pending Address");
      establishSession(email, "password123");

      HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create(baseUrl + "/api/v1/account/balance"))
          .GET()
          .build();

      HttpResponse<String> response = client.send(request,
          HttpResponse.BodyHandlers.ofString());

      assertThat(response.statusCode()).isEqualTo(403);
    }

    @Test
    @DisplayName("Should allow pending user to access own profile")
    void pendingUser_AccessingMeEndpoint_Returns200() throws Exception {
      String email = "pendingme." + System.currentTimeMillis() + "@example.com";
      createUserInDb(email, "Pending Me", "Pending Address");
      establishSession(email, "password123");

      HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create(baseUrl + "/api/v1/me"))
          .GET()
          .build();

      HttpResponse<String> response = client.send(request,
          HttpResponse.BodyHandlers.ofString());

      assertThat(response.statusCode()).isEqualTo(200);
    }

    @Test
    @DisplayName("Should allow pending user to access complete-profile sub-path")
    void pendingUser_AccessingMeCompleteProfile_Returns200() throws Exception {
      String email = "pendingsub." + System.currentTimeMillis() + "@example.com";
      createUserInDb(email, "Pending Sub", "Pending Address");
      establishSession(email, "password123");

      String requestBody = "{\"address\":\"New Address\"}";

      HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create(baseUrl + "/api/v1/me/complete-profile"))
          .header("Content-Type", "application/json")
          .POST(HttpRequest.BodyPublishers.ofString(requestBody))
          .build();

      HttpResponse<String> response = client.send(request,
          HttpResponse.BodyHandlers.ofString());

      assertThat(response.statusCode()).isEqualTo(200);
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

      // Create bank account and balance so /account/balance returns 200
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

      establishSession(email, "password123");

      HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create(baseUrl + "/api/v1/account/balance"))
          .GET()
          .build();

      HttpResponse<String> response = client.send(request,
          HttpResponse.BodyHandlers.ofString());

      assertThat(response.statusCode()).isEqualTo(200);
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
      // Intentionally skip creating LocalCredential

      String formData = "username=" + email + "&password=password123";
      HttpResponse<String> response = postRaw(
          "/api/v1/auth/login",
          formData,
          "application/x-www-form-urlencoded"
      );

      assertThat(response.statusCode()).isEqualTo(401);
    }
  }
}
