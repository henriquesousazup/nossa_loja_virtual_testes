package br.com.zup.edu.nossalojavirtual.users;

import br.com.zup.edu.nossalojavirtual.util.ExceptionUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import javax.transaction.Transactional;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc(printOnlyOnFailure = false)
@ActiveProfiles("test")
class UserControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper mapper;

    @Autowired
    UserRepository userRepository;

    private final String apiUrl = "/api/users";

    @BeforeEach
    void setUp() {
        this.clearDB();
    }

    @AfterEach
    void tearDown() {
        this.clearDB();
    }

    @Test
    @DisplayName("Should create a new user")
    void test1() throws Exception {

        NewUserRequest newUserRequest = new NewUserRequest("henrique.desousa@zup.com.br", "123456");

        String payload = mapper.writeValueAsString(newUserRequest);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(apiUrl)
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(request)
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.redirectedUrlPattern("/api/users/*"));

        assertEquals(1, userRepository.findAll().size());
    }

    @Test
    @DisplayName("Should not create a new user with empty email and password")
    void test2() throws Exception {

        NewUserRequest newUserRequest = new NewUserRequest("", "");

        String payload = mapper.writeValueAsString(newUserRequest);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(apiUrl)
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON);

        Exception resolvedException = mockMvc.perform(request)
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn()
                .getResolvedException();

        MethodArgumentNotValidException exception = (MethodArgumentNotValidException) resolvedException;

        assertNotNull(exception);
        List<String> errorMessages = exception.getBindingResult().getFieldErrors().stream().map(ExceptionUtil::getFieldAndDefaultErrorMessage).toList();

        assertThat(errorMessages, containsInAnyOrder(
                "login must not be empty",
                "password size must be between 6 and 2147483647"
        ));
    }

    @Test
    @DisplayName("Should not create a new user with invalid email and password")
    void test3() throws Exception {

        NewUserRequest newUserRequest = new NewUserRequest("invalidemail.com", "12345");

        String payload = mapper.writeValueAsString(newUserRequest);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(apiUrl)
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON);

        Exception resolvedException = mockMvc.perform(request)
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn()
                .getResolvedException();

        MethodArgumentNotValidException exception = (MethodArgumentNotValidException) resolvedException;

        assertNotNull(exception);
        List<String> errorMessages = exception.getBindingResult().getFieldErrors().stream().map(ExceptionUtil::getFieldAndDefaultErrorMessage).toList();

        assertThat(errorMessages, containsInAnyOrder(
                "login must be a well-formed email address",
                "password size must be between 6 and 2147483647"
        ));
    }

    @Test
    @DisplayName("Should not create a new user with a login already registered")
    void test4() throws Exception {

        User user = new User("henrique.desousa@zup.com.br", Password.encode("123456"));
        userRepository.save(user);

        NewUserRequest newUserRequest = new NewUserRequest("henrique.desousa@zup.com.br", "1234567");

        String payload = mapper.writeValueAsString(newUserRequest);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(apiUrl)
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON);

        Exception resolvedException = mockMvc.perform(request)
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn()
                .getResolvedException();

        MethodArgumentNotValidException exception = (MethodArgumentNotValidException) resolvedException;

        assertNotNull(exception);
        String errorMessage = exception.getFieldErrors().get(0).getDefaultMessage();

        assertEquals("login is already registered", errorMessage);
    }

    private void clearDB() {
        userRepository.deleteAll();
    }
}