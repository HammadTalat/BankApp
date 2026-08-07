package com.redmath.bankapp.user;

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
import java.net.CookiePolicy;
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
class UserControllerTest {

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
        .cookieHandler(new CookieManager(null, CookiePolicy.ACCEPT_ALL))
        .version(HttpClient.Version.HTTP_1_1)
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

  private JsonNode post(String path, String body, String contentType, boolean authenticated)
      throws Exception {
    String email = null;
    String password = null;
    if (authenticated) {
      email = "user." + System.currentTimeMillis() + "@example.com";
      password = "SecurePass1!";
      createUserInDb(email, "Profile User", "Original Address");
      establishSession(email, password);
    }

    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(baseUrl + path))
        .header("Content-Type", contentType)
        .POST(HttpRequest.BodyPublishers.ofString(body))
        .build();
    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
    return objectMapper.readTree(response.body());
  }

  private JsonNode get(String path, boolean authenticated) throws Exception {
    if (authenticated) {
      String email = "getuser." + System.currentTimeMillis() + "@example.com";
      createUserInDb(email, "Get User", "Get Address St");
      establishSession(email, "SecurePass1!");
    }

    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(baseUrl + path))
        .GET()
        .build();
    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
    return objectMapper.readTree(response.body());
  }

  private HttpResponse<String> getRaw(String path, boolean authenticated) throws Exception {
    if (authenticated) {
      String email = "rawget." + System.currentTimeMillis() + "@example.com";
      createUserInDb(email, "Get User", "Get Address St");
      establishSession(email, "SecurePass1!");
    }

    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(baseUrl + path))
        .GET()
        .build();
    return client.send(request, HttpResponse.BodyHandlers.ofString());
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
    System.err.println("ESTABLISH SESSION STATUS: " + loginResponse.statusCode() + " for " + email);
    System.err.println(
        "ESTABLISH SESSION SET-COOKIE: " + loginResponse.headers().allValues("Set-Cookie"));
    if (loginResponse.statusCode() != 200) {
      throw new IllegalStateException("Login failed for test setup: " + loginResponse.body());
    }
  }

  private void createUserInDb(String email, String name, String address) {
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
  }

  private record CompleteProfileRequest(String address) {

  }

  @Nested
  @DisplayName("GET /api/v1/me")
  class GetProfileTests {

    @Test
    @DisplayName("Should return user profile when authenticated")
    void getCurrentUserProfile_Authenticated_Returns200() throws Exception {
      HttpResponse<String> response = getRaw("/api/v1/me", true);

      assertThat(response.statusCode()).isEqualTo(200);

      JsonNode body = objectMapper.readTree(response.body());
      assertThat(body.get("name").asText()).isEqualTo("Get User");
      assertThat(body.get("email").asText()).contains("@example.com");
      assertThat(body.get("address").asText()).isEqualTo("Get Address St");
      assertThat(body.get("role").asText()).isEqualTo("ACCOUNT_HOLDER");
      assertThat(body.get("approvalStatus").asText()).isEqualTo("PENDING");
    }

    @Test
    @DisplayName("Should return 401 when not authenticated")
    void getCurrentUserProfile_Unauthenticated_Returns401() throws Exception {
      HttpResponse<String> response = getRaw("/api/v1/me", false);

      assertThat(response.statusCode()).isEqualTo(401);
    }
  }

  @Nested
  @DisplayName("POST /api/v1/me/complete-profile")
  class CompleteProfileTests {

    @Test
    @DisplayName("Should update address and return updated profile when authenticated")
    void completeProfile_ValidAddress_ReturnsUpdatedProfile() throws Exception {
      String requestBody = json(new CompleteProfileRequest("456 New Address Blvd"));

      JsonNode response = post("/api/v1/me/complete-profile", requestBody, "application/json",
          true);

      assertThat(response.get("address").asText()).isEqualTo("456 New Address Blvd");
      assertThat(response.get("name").asText()).isEqualTo("Profile User");
      assertThat(response.get("role").asText()).isEqualTo("ACCOUNT_HOLDER");
    }

    @Test
    @DisplayName("Should return 400 when address is blank")
    void completeProfile_BlankAddress_Returns400() throws Exception {
      String email = "blankaddr." + System.currentTimeMillis() + "@example.com";
      createUserInDb(email, "Blank User", "Old Address");
      establishSession(email, "SecurePass1!");

      String requestBody = json(new CompleteProfileRequest("   "));

      HttpResponse<String> response = client.send(
          HttpRequest.newBuilder()
              .uri(URI.create(baseUrl + "/api/v1/me/complete-profile"))
              .header("Content-Type", "application/json")
              .POST(HttpRequest.BodyPublishers.ofString(requestBody))
              .build(),
          HttpResponse.BodyHandlers.ofString()
      );

      assertThat(response.statusCode()).isEqualTo(400);
    }

    @Test
    @DisplayName("Should return 400 when address is null")
    void completeProfile_NullAddress_Returns400() throws Exception {
      String email = "nulladdr." + System.currentTimeMillis() + "@example.com";
      createUserInDb(email, "Null User", "Old Address");
      establishSession(email, "SecurePass1!");

      String requestBody = "{\"address\":null}";

      HttpResponse<String> response = client.send(
          HttpRequest.newBuilder()
              .uri(URI.create(baseUrl + "/api/v1/me/complete-profile"))
              .header("Content-Type", "application/json")
              .POST(HttpRequest.BodyPublishers.ofString(requestBody))
              .build(),
          HttpResponse.BodyHandlers.ofString()
      );

      assertThat(response.statusCode()).isEqualTo(400);
    }

    @Test
    @DisplayName("Should return 401 when not authenticated")
    void completeProfile_Unauthenticated_Returns401() throws Exception {
      String requestBody = json(new CompleteProfileRequest("Some Address"));

      HttpResponse<String> response = client.send(
          HttpRequest.newBuilder()
              .uri(URI.create(baseUrl + "/api/v1/me/complete-profile"))
              .header("Content-Type", "application/json")
              .POST(HttpRequest.BodyPublishers.ofString(requestBody))
              .build(),
          HttpResponse.BodyHandlers.ofString()
      );

      assertThat(response.statusCode()).isEqualTo(401);
    }
  }
}
