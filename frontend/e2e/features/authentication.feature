Feature: Authentication flows
  System tests for signup, login, logout, route guards, and profile access.

  Scenario: Signup, approval, login, and dashboard
    Given I am on the signup page
    When I register a new account
    And an administrator approves my account
    And I log in with the new account credentials
    Then I should see the account dashboard

  Scenario: Login with invalid credentials shows an error
    Given I am on the login page
    When I log in with email "wrong.user@redmath.test" and password "NotTheRightPassword1!"
    Then I should see a login error

  Scenario: Login, view profile, and logout
    Given an approved account holder exists
    And I am logged in as the test account holder
    When I open my profile menu
    Then I should see my profile details
    When I log out from the profile menu
    Then I should be redirected to the login page

  Scenario: Access protected page while logged out redirects to login
    When I try to visit "/account" while logged out
    Then I should be redirected to the login page

  Scenario: Access chat while not logged in redirects to login
    When I try to visit the chat page while logged out
    Then I should be redirected to the login page

  Scenario: Pending user cannot access chat
    Given a pending account holder exists
    And I am logged in as a pending account holder
    When I try to visit the chat page as a pending user
    Then I should remain on the application status page
