package br.com.zup.edu.nossalojavirtual.purchase;

import br.com.zup.edu.nossalojavirtual.categories.Category;
import br.com.zup.edu.nossalojavirtual.categories.CategoryRepository;
import br.com.zup.edu.nossalojavirtual.products.*;
import br.com.zup.edu.nossalojavirtual.users.Password;
import br.com.zup.edu.nossalojavirtual.users.User;
import br.com.zup.edu.nossalojavirtual.users.UserRepository;
import br.com.zup.edu.nossalojavirtual.util.ExceptionUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(printOnlyOnFailure = false)
class PaymentGatewayReturnControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper mapper;

    @Autowired
    UserRepository userRepository;

    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    PurchaseRepository purchaseRepository;

    private PhotoUploader photoUploaderMock;
    private Product product;
    private Purchase purchase;
    private String apiUrl = "/api/purchases/confirm-payment";
    private User user;

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

        NewPurchaseRequest newPurchase = new NewPurchaseRequest(product.getId(), 2, PaymentGateway.PAYPAL);
        purchase = product.reserveQuantityFor(newPurchase, user).get();
        purchaseRepository.save(purchase);
    }

    @AfterEach
    void tearDown() {
        purchaseRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Should confirm paypal success payment")
    void test1() throws Exception {

        PaymentReturn paymentReturn = new PaymentReturn(purchase.getId(), "1", "1");

        String payload = mapper.writeValueAsString(paymentReturn);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(apiUrl)
                .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_lojavirtual:write")))
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(request)
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @DisplayName("Should confirm pagseguro success payment")
    void test2() throws Exception {

        NewPurchaseRequest newPurchase = new NewPurchaseRequest(product.getId(), 2, PaymentGateway.PAGSEGURO);
        Purchase pagseguroPurchase = product.reserveQuantityFor(newPurchase, user).get();
        purchaseRepository.save(pagseguroPurchase);

        PaymentReturn paymentReturn = new PaymentReturn(pagseguroPurchase.getId(), "1", "SUCESSO");

        String payload = mapper.writeValueAsString(paymentReturn);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(apiUrl)
                .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_lojavirtual:write")))
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(request)
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @DisplayName("Should confirm paypal error payment")
    void test3() throws Exception {

        PaymentReturn paymentReturn = new PaymentReturn(purchase.getId(), "1", "2");

        String payload = mapper.writeValueAsString(paymentReturn);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(apiUrl)
                .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_lojavirtual:write")))
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(request)
                .andExpect(MockMvcResultMatchers.status().isOk());

    }

    @Test
    @DisplayName("Should confirm pagseguro error payment")
    void test4() throws Exception {

        NewPurchaseRequest newPurchase = new NewPurchaseRequest(product.getId(), 2, PaymentGateway.PAGSEGURO);
        Purchase pagseguroPurchase = product.reserveQuantityFor(newPurchase, user).get();
        purchaseRepository.save(pagseguroPurchase);

        PaymentReturn paymentReturn = new PaymentReturn(pagseguroPurchase.getId(), "1", "ERROR");

        String payload = mapper.writeValueAsString(paymentReturn);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(apiUrl)
                .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_lojavirtual:write")))
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(request)
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @DisplayName("Should not confirm payment with invalid arguments")
    void test5() throws Exception {

        PaymentReturn paymentReturn = new PaymentReturn(purchase.getId(), "", "");

        String payload = mapper.writeValueAsString(paymentReturn);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(apiUrl)
                .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_lojavirtual:write")))
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
                "paymentId must not be blank",
                "status must not be blank"
        ));
    }

    @Test
    @DisplayName("Should not confirm payment without token")
    void test6() throws Exception {

        PaymentReturn paymentReturn = new PaymentReturn(purchase.getId(), "1", "1");

        String payload = mapper.writeValueAsString(paymentReturn);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(apiUrl)
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(request)
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    @DisplayName("Should not confirm payment with invalid scope")
    void test7() throws Exception {

        NewPurchaseRequest newPurchase = new NewPurchaseRequest(product.getId(), 2, PaymentGateway.PAGSEGURO);
        Purchase pagseguroPurchase = product.reserveQuantityFor(newPurchase, user).get();
        purchaseRepository.save(pagseguroPurchase);

        PaymentReturn paymentReturn = new PaymentReturn(pagseguroPurchase.getId(), "1", "SUCCESS");

        String payload = mapper.writeValueAsString(paymentReturn);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(apiUrl)
                .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_lojavirtual:read")))
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(request)
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

}