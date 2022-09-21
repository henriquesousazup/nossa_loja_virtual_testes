package br.com.zup.edu.nossalojavirtual.purchase;

import br.com.zup.edu.nossalojavirtual.categories.Category;
import br.com.zup.edu.nossalojavirtual.categories.CategoryRepository;
import br.com.zup.edu.nossalojavirtual.products.*;
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
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(printOnlyOnFailure = false)
class PurchaseControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper mapper;

    @Autowired
    PurchaseRepository purchaseRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    CategoryRepository categoryRepository;

    private PhotoUploader photoUploaderMock;
    private Product product;
    private String apiUrl = "/api/purchase";
    private User user;

    @AfterEach
    void tearDown() {
        this.clearDB();
    }

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

    @Test
    @DisplayName("Should return pagseguro payment url")
    void test1() throws Exception {
        NewPurchaseRequest newPurchaseRequest = new NewPurchaseRequest(product.getId(), 1, PaymentGateway.PAGSEGURO);

        String payload = mapper.writeValueAsString(newPurchaseRequest);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(apiUrl)
                .with(jwt()
                        .jwt(jwt ->
                                jwt.claim("email", user.getUsername())
                        )
                        .authorities(new SimpleGrantedAuthority("SCOPE_lojavirtual:write")))
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON);

        String responsePayload = mockMvc.perform(request)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        Map map = mapper.readValue(responsePayload, Map.class);

        assertNotNull(map.get("paymentUrl"));
        assertEquals(1, purchaseRepository.findAll().size());
    }

    @Test
    @DisplayName("Should return paypal payment url")
    void test2() throws Exception {
        NewPurchaseRequest newPurchaseRequest = new NewPurchaseRequest(product.getId(), 1, PaymentGateway.PAYPAL);

        String payload = mapper.writeValueAsString(newPurchaseRequest);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(apiUrl)
                .with(jwt()
                        .jwt(jwt ->
                                jwt.claim("email", user.getUsername())
                        )
                        .authorities(new SimpleGrantedAuthority("SCOPE_lojavirtual:write")))
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON);

        String responsePayload = mockMvc.perform(request)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        Map map = mapper.readValue(responsePayload, Map.class);

        assertNotNull(map.get("paymentUrl"));
        assertEquals(1, purchaseRepository.findAll().size());
    }

    @Test
    @DisplayName("Should not return payment url in case of invalid arguments")
    void test3() throws Exception {
        NewPurchaseRequest newPurchaseRequest = new NewPurchaseRequest(product.getId(), 0, null);

        String payload = mapper.writeValueAsString(newPurchaseRequest);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(apiUrl)
                .with(jwt()
                        .jwt(jwt ->
                                jwt.claim("email", user.getUsername())
                        )
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
                "paymentGateway must not be null",
                "quantity must be greater than or equal to 1"
        ));
    }

    @Test
    @DisplayName("Should not return payment url in case of product doesn't exist")
    void test4() throws Exception {
        NewPurchaseRequest newPurchaseRequest = new NewPurchaseRequest(UUID.randomUUID(), 1, PaymentGateway.PAGSEGURO);

        String payload = mapper.writeValueAsString(newPurchaseRequest);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(apiUrl)
                .with(jwt()
                        .jwt(jwt ->
                                jwt.claim("email", user.getUsername())
                        )
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
                "productId Category productId is not registered"
        ));
    }

    @Test
    @DisplayName("Should not return payment url in case of product out of stock")
    void test5() throws Exception {
        NewPurchaseRequest newPurchaseRequest = new NewPurchaseRequest(product.getId(), 6, PaymentGateway.PAYPAL);

        String payload = mapper.writeValueAsString(newPurchaseRequest);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(apiUrl)
                .with(jwt()
                        .jwt(jwt ->
                                jwt.claim("email", user.getUsername())
                        )
                        .authorities(new SimpleGrantedAuthority("SCOPE_lojavirtual:write")))
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON);

        Exception resolvedException = mockMvc.perform(request)
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn()
                .getResolvedException();

        BindException bindException = (BindException) resolvedException;
        String error = bindException.getAllErrors().get(0).getDefaultMessage();

        assertEquals("This product is out of stock", error);
    }

    @Test
    @DisplayName("Should not return payment url without token")
    void test6() throws Exception {
        NewPurchaseRequest newPurchaseRequest = new NewPurchaseRequest(product.getId(), 1, PaymentGateway.PAGSEGURO);

        String payload = mapper.writeValueAsString(newPurchaseRequest);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(apiUrl)
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(request)
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    @DisplayName("Should not return payment url without user")
    void test7() throws Exception {
        NewPurchaseRequest newPurchaseRequest = new NewPurchaseRequest(product.getId(), 1, PaymentGateway.PAGSEGURO);

        String payload = mapper.writeValueAsString(newPurchaseRequest);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(apiUrl)
                .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_lojavirtual:write")))
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(request)
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @DisplayName("Should not return payment url with invalid scope")
    void test8() throws Exception {
        NewPurchaseRequest newPurchaseRequest = new NewPurchaseRequest(product.getId(), 1, PaymentGateway.PAGSEGURO);

        String payload = mapper.writeValueAsString(newPurchaseRequest);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(apiUrl)
                .with(jwt()
                        .jwt(jwt ->
                                jwt.claim("email", user.getUsername())
                        )
                        .authorities(new SimpleGrantedAuthority("SCOPE_lojavirtual:read")))
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(request)
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    private void clearDB() {
        purchaseRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();
    }

}