package br.com.zup.edu.nossalojavirtual.purchase;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Map;

@SpringBootTest
@AutoConfigureMockMvc(printOnlyOnFailure = false)
@ActiveProfiles("test")
class OuterSystemsMockControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper mapper;


    @Test
    @DisplayName("Should return status OK on invoice")
    void test1() throws Exception {

        Map requestMap = Map.of("key", "value");
        String payload = mapper.writeValueAsString(requestMap);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/invoice/register")
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(request)
                .andExpect(MockMvcResultMatchers.status().isOk());

    }

    @Test
    @DisplayName("Should return status OK on newPurchase")
    void test2() throws Exception {

        Map requestMap = Map.of("key", "value");
        String payload = mapper.writeValueAsString(requestMap);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/sellerRanking/newPurchase")
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(request)
                .andExpect(MockMvcResultMatchers.status().isOk());

    }

    @Test
    @DisplayName("Should return status BAD REQUEST on invoice in case of invalid arguments")
    void test3() throws Exception {

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/invoice/register")
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(request)
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

    }

    @Test
    @DisplayName("Should return status BAD REQUEST on newPuchase in case of invalid arguments")
    void test4() throws Exception {

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/sellerRanking/newPurchase")
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(request)
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

    }


}