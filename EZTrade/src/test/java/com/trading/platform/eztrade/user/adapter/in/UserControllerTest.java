package com.trading.platform.eztrade.user.adapter.in;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trading.platform.eztrade.security.configuration.BeansConfig;
import com.trading.platform.eztrade.user.adapter.in.DTOs.UserDTO;
import com.trading.platform.eztrade.user.application.ports.in.GetUserUserCase;
import com.trading.platform.eztrade.user.application.ports.in.RegisterUserUserCase;
import com.trading.platform.eztrade.user.domain.User;
import com.trading.platform.eztrade.user.domain.exceptions.UserExistsException;
import com.trading.platform.eztrade.user.domain.exceptions.UserNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(UserControllerTest.TestConfig.class)
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private RegisterUserUserCase registerUserUserCase;

    @Autowired
    private GetUserUserCase getUserUserCase;

    @Test
    @DisplayName("POST /api/user/register devuelve 201 y el UserDTO creado")
    void registerUser_returns201WithBody() throws Exception {
        UserDTO request = new UserDTO("John", "Doe", "john.doe@test.com", "pwd123");
        User saved = new User("John", "Doe", "john.doe@test.com", "encoded");

        given(registerUserUserCase.registerUser(any(User.class))).willReturn(saved);

        mockMvc.perform(post("/api/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.firstname", is("John")))
                .andExpect(jsonPath("$.lastname", is("Doe")))
                .andExpect(jsonPath("$.email", is("john.doe@test.com")));
    }

    @Test
    @DisplayName("POST /api/user/register devuelve 409 cuando el usuario ya existe")
    void registerUser_returns409WhenUserExists() throws Exception {
        UserDTO request = new UserDTO("John", "Doe", "john.doe@test.com", "pwd123");

        doThrow(new UserExistsException("User already exists"))
                .when(registerUserUserCase).registerUser(any(User.class));

        mockMvc.perform(post("/api/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title", is("User already exists")))
                .andExpect(jsonPath("$.detail", is("User already exists")))
                .andExpect(jsonPath("$.status", is(409)));
    }

    @Test
    @WithMockUser(username = "john.doe@test.com", roles = "USER")
    @DisplayName("GET /api/user devuelve 200 y el UserDTO cuando existe")
    void getUser_returns200WhenExists() throws Exception {
        String email = "john.doe@test.com";
        User user = new User("John", "Doe", email, "pwd123");

        given(getUserUserCase.getUser(eq(email))).willReturn(user);

        mockMvc.perform(get("/api/user").param("email", email))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.firstname", is("John")))
                .andExpect(jsonPath("$.lastname", is("Doe")))
                .andExpect(jsonPath("$.email", is(email)));
    }

    @Test
    @WithMockUser(username = "missing@test.com", roles = "USER")
    @DisplayName("GET /api/user devuelve 404 cuando el usuario no existe")
    void getUser_returns404WhenNotFound() throws Exception {
        String email = "missing@test.com";

        given(getUserUserCase.getUser(eq(email))).willThrow(new UserNotFoundException("User not found"));

        mockMvc.perform(get("/api/user").param("email", email))
                .andExpect(status().isNotFound());
    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        RegisterUserUserCase registerUserUserCase() {
            return Mockito.mock(RegisterUserUserCase.class);
        }

        @Bean
        GetUserUserCase getUserUserCase() {
            return Mockito.mock(GetUserUserCase.class);
        }

        @Bean
        BeansConfig.SecurityPermissionEvaluator securityPermissionEvaluator() {
            return Mockito.mock(BeansConfig.SecurityPermissionEvaluator.class);
        }
    }
}

