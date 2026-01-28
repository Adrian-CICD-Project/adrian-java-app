package com.example.wojtek_java_maven;

import com.example.wojtek_java_maven.controller.HelloController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@WebMvcTest(HelloController.class)
class HelloControllerTest {

    @Autowired
    private MockMvc mockMvc;

//    @Test
//    void shouldReturnHelloMessage() throws Exception {
//        mockMvc.perform(get("/hello"))
//                .andExpect(status().isOk())
//                .andExpect(content().string("Hello My firend!"));
//    }
//
//    @Test
//    void sayGoodbye_ReturnsGoodbyeMyFriend() throws Exception {
//        mockMvc.perform(get("/goodbye"))
//                .andExpect(status().isOk())
//                .andExpect(content().string("Goodbye my friend!"));
//    }
}