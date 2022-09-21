package br.com.zup.edu.nossalojavirtual.products;

import br.com.zup.edu.nossalojavirtual.categories.Category;
import br.com.zup.edu.nossalojavirtual.categories.CategoryRepository;
import br.com.zup.edu.nossalojavirtual.shared.email.EmailRepository;
import br.com.zup.edu.nossalojavirtual.users.Password;
import br.com.zup.edu.nossalojavirtual.users.User;
import br.com.zup.edu.nossalojavirtual.users.UserRepository;
import br.com.zup.edu.nossalojavirtual.util.ExceptionUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(printOnlyOnFailure = false)
class QuestionControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper mapper;

    @Autowired
    QuestionRepository questionRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    EmailRepository emailRepository;

    private PhotoUploader photoUploaderMock;
    private User user;
    private Product product;
    private final String baseUrl = "/api/products/";


    @BeforeEach
    void setUp() {

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
        emailRepository.deleteAll();
        questionRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Should ask a new question")
    void test1() throws Exception {

        NewQuestionRequest newQuestionRequest = new NewQuestionRequest("Como funciona tal coisa?");

        String payload = mapper.writeValueAsString(newQuestionRequest);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(baseUrl + product.getId() + "/questions")
                .with(jwt()
                        .jwt(jwt ->
                                jwt.claim("email", user.getUsername())
                        )
                        .authorities(new SimpleGrantedAuthority("SCOPE_lojavirtual:write")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload);

        String responsePayload = mockMvc.perform(request)
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.redirectedUrlPattern("/api/products/**/questions/**"))
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);


        TypeFactory typeFactory = mapper.getTypeFactory();
        List<QuestionResponse> questionResponse = mapper.readValue(responsePayload, typeFactory.constructCollectionType(List.class, QuestionResponse.class));

        assertEquals(newQuestionRequest.getTitle(), questionResponse.get(0).getTitle());
        assertEquals(user.getUsername(), questionResponse.get(0).getUser());
        assertTrue(questionResponse.get(0).getCreatedAt().isBefore(LocalDateTime.now()));

        assertEquals(1, questionRepository.findAll().size());
    }

    @Test
    @DisplayName("Should not ask a new question in case of product doesn't exist")
    void test2() throws Exception {

        NewQuestionRequest newQuestionRequest = new NewQuestionRequest("Como funciona tal coisa?");

        String payload = mapper.writeValueAsString(newQuestionRequest);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(baseUrl + UUID.randomUUID() + "/questions")
                .with(jwt()
                        .jwt(jwt ->
                                jwt.claim("email", user.getUsername())
                        )
                        .authorities(new SimpleGrantedAuthority("SCOPE_lojavirtual:write")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload);

        mockMvc.perform(request)
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    @DisplayName("Should not ask a new question in case of invalid arguments")
    void test3() throws Exception {

        NewQuestionRequest newQuestionRequest = new NewQuestionRequest("");

        String payload = mapper.writeValueAsString(newQuestionRequest);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(baseUrl + product.getId() + "/questions")
                .with(jwt()
                        .jwt(jwt ->
                                jwt.claim("email", user.getUsername())
                        )
                        .authorities(new SimpleGrantedAuthority("SCOPE_lojavirtual:write")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload);

        Exception resolvedException = mockMvc.perform(request)
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn()
                .getResolvedException();

        MethodArgumentNotValidException exception = (MethodArgumentNotValidException) resolvedException;

        assertNotNull(exception);
        List<String> errorMessages = exception.getBindingResult().getFieldErrors().stream().map(ExceptionUtil::getFieldAndDefaultErrorMessage).toList();

        MatcherAssert.assertThat(errorMessages, containsInAnyOrder(
                "title must not be blank"
        ));
    }

    @Test
    @DisplayName("Should not ask a new question without token")
    void test4() throws Exception {

        NewQuestionRequest newQuestionRequest = new NewQuestionRequest("Como funciona tal coisa?");

        String payload = mapper.writeValueAsString(newQuestionRequest);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(baseUrl + product.getId() + "/questions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload);

        mockMvc.perform(request)
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    @DisplayName("Should not ask a new question without a valid scope")
    void test5() throws Exception {

        NewQuestionRequest newQuestionRequest = new NewQuestionRequest("Como funciona tal coisa?");

        String payload = mapper.writeValueAsString(newQuestionRequest);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(baseUrl + product.getId() + "/questions")
                .with(jwt()
                        .jwt(jwt ->
                                jwt.claim("email", user.getUsername())
                        )
                        .authorities(new SimpleGrantedAuthority("SCOPE_lojavirtual:read")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload);

        mockMvc.perform(request)
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @DisplayName("Should not ask a new question without a user")
    void test6() throws Exception {

        NewQuestionRequest newQuestionRequest = new NewQuestionRequest("Como funciona tal coisa?");

        String payload = mapper.writeValueAsString(newQuestionRequest);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(baseUrl + product.getId() + "/questions")
                .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_lojavirtual:write")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload);

        mockMvc.perform(request)
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

}