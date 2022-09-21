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
import org.mockito.Mockito;
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
import org.springframework.web.util.NestedServletException;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.*;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

@SpringBootTest
@AutoConfigureMockMvc(printOnlyOnFailure = false)
@ActiveProfiles("test")
class ProductOpinionControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper mapper;

    @Autowired
    ProductOpinionRepository opinionRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    UserRepository userRepository;

    private final String apiUrl = "/api/opinions";
    private User user;
    private Product product;
    private PhotoUploader photoUploaderMock;

    @BeforeEach
    void setUp() {

        this.clearDB();

        Photo p1 = new Photo("https://cf.shopee.com.br/file/be1b6889f9b5fdea9588a355d97427c9/uploadedLink1");
        Photo p2 = new Photo("https://cf.shopee.com.br/file/be1b6889f9b5fdea9588a355d97427c9/uploadedLink2");

        List<Photo> photos = Arrays.asList(p1, p2);

        photoUploaderMock = Mockito.mock(PhotoUploader.class);
        Mockito.when(photoUploaderMock.upload(Mockito.any(), Mockito.any())).thenReturn(photos);

        user = new User("henrique.desousa@zup.com.br", Password.encode("123456"));
        userRepository.save(user);

        Category category = new Category("Eletrônicos");
        categoryRepository.save(category);

        List<String> newPhotos = Arrays.asList(
                "https://tm.ibxk.com.br/2014/11/03/03085750362011.jpg",
                "https://cf.shopee.com.br/file/be1b6889f9b5fdea9588a355d97427c9"
        );

        Characteristic c1 = new Characteristic("Portátil", "Cabe no seu bolso");
        Characteristic c2 = new Characteristic("Resistente", "Dura anos");
        Characteristic c3 = new Characteristic("Azul", "Azul marinho");
        Set<Characteristic> productCharacteristics = Set.of(c1, c2, c3);

        PreProduct preProduct = new PreProduct(user, category, "Tijorola", new BigDecimal("150.00"), 5, "Muito bom");
        List<Photo> uploadedPhotos = photoUploaderMock.upload(newPhotos, preProduct);

        product = new Product(preProduct, uploadedPhotos, productCharacteristics);
        productRepository.save(product);
    }

    @AfterEach
    void tearDown() {
        this.clearDB();
    }

    @Test
    @DisplayName("Should create a new opinion to a product")
    void test1() throws Exception {

        NewOpinionRequest newOpinionRequest =
                new NewOpinionRequest(5, "Recomendo!", "Comprei e gostei bastante", product.getId());

        String payload = mapper.writeValueAsString(newOpinionRequest);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(apiUrl)
                .with(jwt()
                        .jwt(jwt -> {
                            jwt.claim("email", user.getUsername());
                        })
                        .authorities(new SimpleGrantedAuthority("SCOPE_lojavirtual:write")))
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(request)
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.redirectedUrlPattern("/api/opinions/*"));

        assertEquals(1, opinionRepository.findAll().size());
    }

    @Test
    @DisplayName("Should not create a new opinion to a product in case of invalid arguments")
    void test2() throws Exception {

        NewOpinionRequest newOpinionRequest =
                new NewOpinionRequest(0, " ", "a".repeat(501), product.getId());

        String payload = mapper.writeValueAsString(newOpinionRequest);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(apiUrl)
                .with(jwt()
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
                "title must not be blank",
                "rating must be between 1 and 5",
                "description length must be between 0 and 500"
        ));
    }

    @Test
    @DisplayName("Should not create a new opinion to a product in case of invalid rating range")
    void test3() throws Exception {

        NewOpinionRequest newOpinionRequest =
                new NewOpinionRequest(6, "Recomendo!", "Comprei e gostei bastante", product.getId());

        String payload = mapper.writeValueAsString(newOpinionRequest);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(apiUrl)
                .with(jwt()
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
                "rating must be between 1 and 5"
        ));
    }

    @Test
    @DisplayName("Should not create a new opinion to a product in case of invalid product id")
    void test4() throws Exception {

        NewOpinionRequest newOpinionRequest =
                new NewOpinionRequest(5, "Recomendo!", "Comprei e gostei bastante", UUID.randomUUID());

        String payload = mapper.writeValueAsString(newOpinionRequest);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(apiUrl)
                .with(jwt()
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

        assertEquals("Category productId is not registered", errorMessage);
    }

    @Test
    @DisplayName("Should not create a new opinion to a product in case of null product id")
    void test5() throws Exception {

        NewOpinionRequest newOpinionRequest =
                new NewOpinionRequest(5, "Recomendo!", "Comprei e gostei bastante", null);

        String payload = mapper.writeValueAsString(newOpinionRequest);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(apiUrl)
                .with(jwt()
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
                "productId must not be null"
        ));
    }

    @Test
    @DisplayName("Should not create a new opinion to a product without token")
    void test6() throws Exception {

        NewOpinionRequest newOpinionRequest =
                new NewOpinionRequest(5, "Recomendo!", "Comprei e gostei bastante", product.getId());

        String payload = mapper.writeValueAsString(newOpinionRequest);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(apiUrl)
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(request)
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    @DisplayName("Should not create a new opinion to a product without an user")
    void test7() throws Exception {

        NewOpinionRequest newOpinionRequest =
                new NewOpinionRequest(5, "Recomendo!", "Comprei e gostei bastante", product.getId());

        String payload = mapper.writeValueAsString(newOpinionRequest);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(apiUrl)
                .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_lojavirtual:write")))
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(request)
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @DisplayName("Should not create a new opinion to a product with a invalid scope")
    void test8() throws Exception {

        NewOpinionRequest newOpinionRequest =
                new NewOpinionRequest(5, "Recomendo!", "Comprei e gostei bastante", product.getId());

        String payload = mapper.writeValueAsString(newOpinionRequest);

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


    private void clearDB() {
        opinionRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();
    }
}