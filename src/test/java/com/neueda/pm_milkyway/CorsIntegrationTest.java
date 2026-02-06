// package com.neueda.pm_milkyway;

// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.test.web.servlet.MockMvc;

// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// @SpringBootTest
// @AutoConfigureMockMvc
// public class CorsIntegrationTest {

//     @Autowired
//     private MockMvc mockMvc;

//     @Test
//     public void testCorsHeaders() throws Exception {
//         mockMvc.perform(options("/api/test-cors")
//                 .header("Origin", "http://localhost:3000")
//                 .header("Access-Control-Request-Method", "GET"))
//                 .andExpect(status().isOk())
//                 .andExpect(header().exists("Access-Control-Allow-Origin"))
//                 .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"))
//                 .andExpect(header().exists("Access-Control-Allow-Methods"));
//     }
// }
