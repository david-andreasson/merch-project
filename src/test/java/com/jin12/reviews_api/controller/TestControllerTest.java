package com.jin12.reviews_api.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TestControllerTest {
    private MockMvc mockMvc;
    @InjectMocks
    private TestController controller;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void testGetProductInfo_success() throws Exception {
        mockMvc.perform(get("/test")
                .param("productId", "T12345"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productName").value("Volvo"));
    }

    @Test
    void testGetProductInfo_notFound() throws Exception {
        mockMvc.perform(get("/test")
                .param("productId", "NOTFOUND"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("No product found")));
    }
}
