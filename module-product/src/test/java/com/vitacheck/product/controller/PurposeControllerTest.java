//package com.vitacheck.product.controller;
//
//import com.vitacheck.product.TestProductApplication;
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.jdbc.Sql;
//import org.springframework.test.web.servlet.MockMvc;
//
//import java.nio.charset.StandardCharsets;
//
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@SpringBootTest(classes = TestProductApplication.class)
//@AutoConfigureMockMvc
//@Sql(scripts = "/sql/insert-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
//@Sql(scripts = "/sql/clear-test-data.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
//class PurposeControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Test
//    void getAllPurposes() throws Exception {
//        String response = mockMvc.perform(get("/api/v1/purposes"))
//                .andExpect(status().isOk())
//                .andReturn()
//                .getResponse()
//                .getContentAsString(StandardCharsets.UTF_8);
//
//        System.out.println("getAllPurposes 응답: " + response);
//    }
//
//    @Test
//    void getPurposesWithIngredienSupplement() throws Exception {
//        String response = mockMvc.perform(
//                        get("/api/v1/purposes/filter")
//                                .param("goals", "1")
//                                .param("goals", "2"))
//                .andExpect(status().isOk())
//                .andReturn()
//                .getResponse()
////                .getContentAsString();
//                .getContentAsString(StandardCharsets.UTF_8);
//
//        System.out.println("getPurposesWithIngredienSupplement 응답: " + response);
//    }
//}
