package by.egrius.app.integration.controller;

import by.egrius.app.dto.userDTO.UserCreateDto;
import by.egrius.app.service.UploadedFileService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UploadedFileService uploadedFileService;

    @Test
    void shouldCreateUserSuccessfully() throws Exception {
        UserCreateDto userCreateDto = new UserCreateDto("Alexey", "alexey@gmail.com", "12345");

        String jsonToCreate =
                """
                    {
                        "username":"Alexey",
                        "email":"alexey@gmail.com",
                        "rawPassword":"12345"
                    }
                """;

        mockMvc.perform(post("/user/create-user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonToCreate))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(userCreateDto.getUsername()))
                .andExpect(jsonPath("$.email").value(userCreateDto.getEmail()));
    }

}