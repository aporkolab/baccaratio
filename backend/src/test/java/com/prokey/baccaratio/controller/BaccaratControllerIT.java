package com.prokey.baccaratio.controller;

import tools.jackson.databind.ObjectMapper;
import com.prokey.baccaratio.controller.dto.AuthRequest;
import com.prokey.baccaratio.controller.dto.AuthResponse;
import com.prokey.baccaratio.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BaccaratControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void fullGameFlow_RegisterLoginBetPlay_ShouldSucceed() throws Exception {
        // Use a unique username for each test run to avoid conflicts in the database
        String username = "integrationtestuser_" + System.currentTimeMillis();

        // 1. Register a new user
        User registration = new User(username, "password123");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registration)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(username));

        // 2. Log in to get a JWT token
        AuthRequest authRequest = new AuthRequest();
        authRequest.setUsername(username);
        authRequest.setPassword("password123");
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn();

        String responseString = loginResult.getResponse().getContentAsString();
        AuthResponse authResponse = objectMapper.readValue(responseString, AuthResponse.class);
        String token = authResponse.getToken();

        // 3. Place a bet using the token
        mockMvc.perform(post("/baccarat/bet/PLAYER/100")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Bet placed on PLAYER with amount 100"))
                .andExpect(jsonPath("$.chips").value(1000)); // Initial chips

        // 4. Play a round using the token
        mockMvc.perform(get("/baccarat/play")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").exists())
                .andExpect(jsonPath("$.playerCards").isArray())
                .andExpect(jsonPath("$.bankerCards").isArray())
                .andExpect(jsonPath("$.chips").isNumber());
    }
}
