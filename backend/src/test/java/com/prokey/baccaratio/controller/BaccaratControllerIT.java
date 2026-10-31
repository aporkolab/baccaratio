package com.prokey.baccaratio.controller;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BaccaratControllerIT {

    private static final String PASSWORD = "password123";

    @Autowired
    private MockMvc mockMvc;

    private static String uniqueUsername() {
        return "it_" + UUID.randomUUID().toString().substring(0, 8);
    }

    private static String credentials(String username, String password) {
        return "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}";
    }

    private void register(String username) throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credentials(username, PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    private String login(String username) throws Exception {
        String body = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credentials(username, PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn().getResponse().getContentAsString();
        return "Bearer " + JsonPath.read(body, "$.token");
    }

    private String registerAndLogin() throws Exception {
        String username = uniqueUsername();
        register(username);
        return login(username);
    }

    @Test
    void fullGameFlow_RegisterLoginBetPlay_ShouldSucceed() throws Exception {
        String token = registerAndLogin();

        // Every request is independent (no session cookie), exactly like the Angular client
        mockMvc.perform(post("/baccarat/bet/player/100").header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Bet placed on PLAYER with amount 100"))
                .andExpect(jsonPath("$.chips").value(1000));

        mockMvc.perform(get("/baccarat/play").header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(anyOf(is("Player"), is("Banker"), is("Tie"))))
                .andExpect(jsonPath("$.playerCards.length()").value(anyOf(is(2), is(3))))
                .andExpect(jsonPath("$.bankerCards.length()").value(anyOf(is(2), is(3))))
                .andExpect(jsonPath("$.playerCards[0].suit").isString())
                .andExpect(jsonPath("$.playerCards[0].value").isString())
                .andExpect(jsonPath("$.playerCards[0].points").isNumber())
                .andExpect(jsonPath("$.playerChips").value(anyOf(is(900), is(1000), is(1100))));

        mockMvc.perform(get("/baccarat/state").header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Current game state."))
                .andExpect(jsonPath("$.playerCards.length()").value(anyOf(is(2), is(3))));

        mockMvc.perform(post("/baccarat/reset").header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("Game reset"))
                .andExpect(jsonPath("$.playerChips").value(1000))
                .andExpect(jsonPath("$.playerCards.length()").value(0));
    }

    @Test
    void playWithoutBet_ReturnsConflict() throws Exception {
        String token = registerAndLogin();

        mockMvc.perform(get("/baccarat/play").header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("A bet must be placed before starting the round."))
                .andExpect(jsonPath("$.path").value("/baccarat/play"));
    }

    @Test
    void invalidBets_ReturnBadRequest() throws Exception {
        String token = registerAndLogin();

        mockMvc.perform(post("/baccarat/bet/dragon/10").header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid bet type."));
        mockMvc.perform(post("/baccarat/bet/player/0").header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/baccarat/bet/player/5000").header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Insufficient chips for this bet."));
        mockMvc.perform(post("/baccarat/bet/player/abc").header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isBadRequest());
    }

    @Test
    void gameEndpoints_RequireAuthentication() throws Exception {
        mockMvc.perform(get("/baccarat/state"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/baccarat/state").header(HttpHeaders.AUTHORIZATION, "Bearer not-a-jwt"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_WithWrongPassword_ReturnsGenericUnauthorized() throws Exception {
        String username = uniqueUsername();
        register(username);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credentials(username, "wrong-password")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid username or password."));
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credentials("nobody_" + username, PASSWORD)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid username or password."));
    }

    @Test
    void register_ValidatesInputAndRejectsDuplicates() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credentials("", "")))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credentials(uniqueUsername(), "123")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("password size must be between 6 and 72"));
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{not json"))
                .andExpect(status().isBadRequest());

        String username = uniqueUsername();
        register(username);
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credentials(username, PASSWORD)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Username is already taken!"));
    }

    @Test
    void corsPreflight_AllowsConfiguredOriginsOnly() throws Exception {
        mockMvc.perform(options("/baccarat/play")
                        .header(HttpHeaders.ORIGIN, "http://localhost:4200")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "authorization"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:4200"));

        mockMvc.perform(options("/baccarat/play")
                        .header(HttpHeaders.ORIGIN, "https://evil.example")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                .andExpect(status().isForbidden());
    }

    @Test
    void h2ConsoleIsNotExposedByDefault() throws Exception {
        mockMvc.perform(get("/h2-console"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unknownEndpoint_ReturnsNotFoundInsteadOfServerError() throws Exception {
        String token = registerAndLogin();

        mockMvc.perform(get("/baccarat/does-not-exist").header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isNotFound());
    }

    @Test
    void healthEndpointIsPublic() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }
}
