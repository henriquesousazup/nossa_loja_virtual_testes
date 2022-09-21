package br.com.zup.edu.nossalojavirtual.products;

import br.com.zup.edu.nossalojavirtual.categories.Category;
import br.com.zup.edu.nossalojavirtual.categories.CategoryRepository;
import br.com.zup.edu.nossalojavirtual.users.Password;
import br.com.zup.edu.nossalojavirtual.users.User;
import br.com.zup.edu.nossalojavirtual.users.UserRepository;
import br.com.zup.edu.nossalojavirtual.util.ExceptionUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.NestedServletException;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(printOnlyOnFailure = false)
class ProductControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper mapper;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    CategoryRepository categoryRepository;

    private final String apiUrl = "/api/products";

    private Category category;

    private User user;

    private List<String> photos;

    private List<NewCharacteristicRequest> newCharacteristicRequest;

    @BeforeEach
    void setUp() {

        this.clearDB();

        user = new User("henrique.desousa@zup.com.br", Password.encode("123456"));
        userRepository.save(user);

        category = new Category("Eletrônicos");
        categoryRepository.save(category);

        photos = Arrays.asList(
                "https://tm.ibxk.com.br/2014/11/03/03085750362011.jpg",
                "https://cf.shopee.com.br/file/be1b6889f9b5fdea9588a355d97427c9"
        );

        NewCharacteristicRequest c1 = new NewCharacteristicRequest("Portátil", "Cabe no seu bolso");
        NewCharacteristicRequest c2 = new NewCharacteristicRequest("Resistente", "Dura anos");
        NewCharacteristicRequest c3 = new NewCharacteristicRequest("Azul", "Azul marinho");

        newCharacteristicRequest = Arrays.asList(c1, c2, c3);
    }

    @AfterEach
    void tearDown() {
        this.clearDB();
    }

    @Test
    @DisplayName("Should create a new product")
    @Transactional
    void test1() throws Exception {

        NewProductRequest newProductRequest = new NewProductRequest(
                "Tijorola", new BigDecimal("150.00"),
                5, photos, newCharacteristicRequest,
                "Muito bom", category.getId());

        String payload = mapper.writeValueAsString(newProductRequest);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(apiUrl).with(jwt()
                        .jwt(jwt -> {
                            jwt.claim("email", user.getUsername());
                        })
                        .authorities(new SimpleGrantedAuthority("SCOPE_lojavirtual:write")))
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(request)
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.redirectedUrlPattern("/api/products/*"));

        assertEquals(1, productRepository.findAll().size());
    }

    @Test
    @DisplayName("Should not create a new product with invalid arguments")
    void test2() throws Exception {

        NewCharacteristicRequest c1 = new NewCharacteristicRequest("Portátil", "Cabe no seu bolso");
        NewCharacteristicRequest c2 = new NewCharacteristicRequest("Resistente", "Dura anos");

        List<NewCharacteristicRequest> justTwoCharacteristics = Arrays.asList(c1, c2);

        NewProductRequest newProductRequest = new NewProductRequest(
                "", new BigDecimal("00.00"),
                -1, List.of(), justTwoCharacteristics,
                "a".repeat(1001), category.getId());

        String payload = mapper.writeValueAsString(newProductRequest);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(apiUrl).with(jwt()
                        .jwt(jwt -> {
                            jwt.claim("email", user.getUsername());
                        })
                        .authorities(new SimpleGrantedAuthority("SCOPE_lojavirtual:write")))
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON);

        Exception resolvedException = mockMvc.perform(request)
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn()
                .getResolvedException();

        MethodArgumentNotValidException exception = (MethodArgumentNotValidException) resolvedException;

        assertNotNull(exception);
        List<String> errorMessages = exception.getBindingResult().getFieldErrors().stream().map(ExceptionUtil::getFieldAndDefaultErrorMessage).toList();

        MatcherAssert.assertThat(errorMessages, containsInAnyOrder(
                "stockQuantity must be greater than or equal to 0",
                "photos size must be between 1 and 2147483647",
                "price must be greater than or equal to 0.01",
                "name must not be blank",
                "characteristics size must be between 3 and 2147483647",
                "description length must be between 0 and 1000"
        ));
    }

    @Test
    @DisplayName("Should not create a product with invalid category id")
    void test3() throws Exception {

        NewProductRequest newProductRequest = new NewProductRequest(
                "Tijorola", new BigDecimal("150.00"),
                5, photos, newCharacteristicRequest,
                "Muito bom", Long.MAX_VALUE);

        String payload = mapper.writeValueAsString(newProductRequest);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(apiUrl).with(jwt()
                        .jwt(jwt -> {
                            jwt.claim("email", user.getUsername());
                        })
                        .authorities(new SimpleGrantedAuthority("SCOPE_lojavirtual:write")))
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON);

        Exception resolvedException = mockMvc.perform(request)
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn()
                .getResolvedException();

        MethodArgumentNotValidException exception = (MethodArgumentNotValidException) resolvedException;

        assertNotNull(exception);
        String errorMessage = exception.getBindingResult().getFieldErrors().get(0).getDefaultMessage();

        assertEquals("Category categoryId is not registered", errorMessage);
    }

    @Test
    @DisplayName("Should not create a product when category id is null")
    void test4() throws Exception {

        NewProductRequest newProductRequest = new NewProductRequest(
                "Tijorola", new BigDecimal("150.00"),
                5, photos, newCharacteristicRequest,
                "Muito bom", null);

        String payload = mapper.writeValueAsString(newProductRequest);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(apiUrl).with(jwt()
                        .jwt(jwt -> {
                            jwt.claim("email", user.getUsername());
                        })
                        .authorities(new SimpleGrantedAuthority("SCOPE_lojavirtual:write")))
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON);


        Exception resolvedException = mockMvc.perform(request)
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn()
                .getResolvedException();

        MethodArgumentNotValidException exception = (MethodArgumentNotValidException) resolvedException;

        assertNotNull(exception);
        List<String> errorMessages = exception.getBindingResult().getFieldErrors().stream().map(ExceptionUtil::getFieldAndDefaultErrorMessage).toList();

        MatcherAssert.assertThat(errorMessages, containsInAnyOrder(
                "categoryId must not be null"
        ));
    }

    @Test
    @DisplayName("Should not create a product without an user")
    void test5() throws Exception {

        NewProductRequest newProductRequest = new NewProductRequest(
                "Tijorola", new BigDecimal("150.00"),
                5, photos, newCharacteristicRequest,
                "Muito bom", category.getId());

        String payload = mapper.writeValueAsString(newProductRequest);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(apiUrl).with(jwt()
                        .authorities(new SimpleGrantedAuthority("SCOPE_lojavirtual:write")))
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON);

        Exception resolvedException = mockMvc.perform(request)
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andReturn()
                .getResolvedException();

        ResponseStatusException exception = (ResponseStatusException) resolvedException;
        assertEquals("User not authenticated.", exception.getReason());
    }

    @Test
    @DisplayName("Should not create a new product without token")
    void test6() throws Exception {

        NewProductRequest newProductRequest = new NewProductRequest(
                "Tijorola", new BigDecimal("150.00"),
                5, photos, newCharacteristicRequest,
                "Muito bom", category.getId());

        String payload = mapper.writeValueAsString(newProductRequest);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(apiUrl)
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(request)
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    @DisplayName("Should not create a new product without a valid scope")
    void test7() throws Exception {

        NewProductRequest newProductRequest = new NewProductRequest(
                "Tijorola", new BigDecimal("150.00"),
                5, photos, newCharacteristicRequest,
                "Muito bom", category.getId());

        String payload = mapper.writeValueAsString(newProductRequest);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(apiUrl)
                .with(jwt()
                        .jwt(jwt -> {
                            jwt.claim("email", user.getUsername());
                        })
                        .authorities(new SimpleGrantedAuthority("SCOPE_lojavirtual:read")))
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(request)
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    private void clearDB(){
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();
    }
}