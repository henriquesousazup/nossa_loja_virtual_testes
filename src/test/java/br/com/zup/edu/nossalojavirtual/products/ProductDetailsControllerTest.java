package br.com.zup.edu.nossalojavirtual.products;

import br.com.zup.edu.nossalojavirtual.categories.Category;
import br.com.zup.edu.nossalojavirtual.categories.CategoryRepository;
import br.com.zup.edu.nossalojavirtual.users.Password;
import br.com.zup.edu.nossalojavirtual.users.User;
import br.com.zup.edu.nossalojavirtual.users.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.Period;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc(printOnlyOnFailure = false)
@ActiveProfiles("test")
class ProductDetailsControllerTest {

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

    @Autowired
    ProductOpinionRepository opinionRepository;

    @Autowired
    QuestionRepository questionRepository;

    private PhotoUploader photoUploaderMock;
    private Product product;
    private ProductOpinion opinion;
    private String apiUrl = "/api/products";

    @BeforeEach
    void setUp() {

        this.clearDB();

        Photo p1 = new Photo("https://cf.shopee.com.br/file/be1b6889f9b5fdea9588a355d97427c9/uploadedLink1");
        Photo p2 = new Photo("https://cf.shopee.com.br/file/be1b6889f9b5fdea9588a355d97427c9/uploadedLink2");

        List<Photo> photos = Arrays.asList(p1, p2);

        photoUploaderMock = Mockito.mock(PhotoUploader.class);
        Mockito.when(photoUploaderMock.upload(Mockito.any(), Mockito.any())).thenReturn(photos);

        User user = new User("henrique.desousa@zup.com.br", Password.encode("123456"));
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

        opinion = new ProductOpinion(5, "Muito bonito", "Gostei muito", product, user);
        opinionRepository.save(opinion);

        Question question = new Question("Como funciona se colocar na tomada?", user, product);
        questionRepository.save(question);
    }

    @AfterEach
    void tearDown() {
        this.clearDB();
    }

    @Test
    @DisplayName("Should return product details")
    void test1() throws Exception {

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(apiUrl + "/" + product.getId())
                .contentType(MediaType.APPLICATION_JSON);

        String responsePayload = mockMvc.perform(request)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        ProductDetailsResponse productDetailsResponse = mapper.readValue(responsePayload, ProductDetailsResponse.class);

        assertNotNull(productDetailsResponse);

        assertThat(productDetailsResponse)
                .extracting("price", "stockQuantity", "description", "rating")
                .contains(
                        product.getPrice(),
                        product.getStockQuantity(),
                        product.getDescription(),
                        opinion.getRating()
                );

        assertNotNull(productDetailsResponse.getSellerOtherProducts());
        assertNotNull(productDetailsResponse.getSellersDetails());
        assertNotNull(productDetailsResponse.getId());
        assertNotNull(productDetailsResponse.getCategoryHierarchy());
        assertEquals(3, productDetailsResponse.getCharacteristics().size());
        assertEquals(2, productDetailsResponse.getPhotos().size());
        assertEquals(1, productDetailsResponse.getOpinions().size());
        assertEquals(1, productDetailsResponse.getQuestions().size());
    }

    @Test
    @DisplayName("Should not return product details if product doesn't exist")
    void test2() throws Exception {

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(apiUrl + "/" + UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(request)
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    private void clearDB() {
        questionRepository.deleteAll();
        opinionRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();
    }
}