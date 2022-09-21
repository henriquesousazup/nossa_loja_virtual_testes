package br.com.zup.edu.nossalojavirtual.products;

import br.com.zup.edu.nossalojavirtual.categories.Category;
import br.com.zup.edu.nossalojavirtual.users.Password;
import br.com.zup.edu.nossalojavirtual.users.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ProductOpinionTest {

    private User user;
    private Product product;
    private PhotoUploader photoUploaderMock;

    @BeforeEach
    void setUp() {
        user = new User("henrique.desousa@zup.com.br", Password.encode("12345678"));
        Category category = new Category("Eletrônicos");

        Photo p1 = new Photo("https://cf.shopee.com.br/file/be1b6889f9b5fdea9588a355d97427c9/uploadedLink1");
        Photo p2 = new Photo("https://cf.shopee.com.br/file/be1b6889f9b5fdea9588a355d97427c9/uploadedLink2");

        List<Photo> photos = Arrays.asList(p1, p2);

        photoUploaderMock = Mockito.mock(PhotoUploader.class);
        Mockito.when(photoUploaderMock.upload(Mockito.any(), Mockito.any())).thenReturn(photos);

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

        photoUploaderMock = Mockito.mock(PhotoUploader.class);
        Mockito.when(photoUploaderMock.upload(Mockito.any(), Mockito.any())).thenReturn(photos);
    }

    @Test
    @DisplayName("Should throw Illegal Argument Exception in case of rating is less than 1")
    void test1() {

        Executable executable = () -> new ProductOpinion(0, "Muito bonito", "Gostei muito", product, user);
        IllegalArgumentException illegalArgumentException = assertThrows(
                IllegalArgumentException.class,
                executable
        );

        assertEquals("rating must be between 1 and 5", illegalArgumentException.getMessage());
    }

    @Test
    @DisplayName("Should throw Illegal Argument Exception in case of rating is more than 5")
    void test2() {

        Executable executable = () -> new ProductOpinion(6, "Muito bonito", "Gostei muito", product, user);
        IllegalArgumentException illegalArgumentException = assertThrows(
                IllegalArgumentException.class,
                executable
        );

        assertEquals("rating must be between 1 and 5", illegalArgumentException.getMessage());
    }

    @Test
    @DisplayName("Should throw Illegal Argument Exception in case of title is blank")
    void test3() {

        Executable executable = () -> new ProductOpinion(5, "", "Gostei muito", product, user);
        IllegalArgumentException illegalArgumentException = assertThrows(
                IllegalArgumentException.class,
                executable
        );

        assertEquals("title must not be blank", illegalArgumentException.getMessage());
    }

    @Test
    @DisplayName("Should throw Illegal Argument Exception in case of invalid description size")
    void test4() {

        Executable executable = () -> new ProductOpinion(5, "Testando", "a".repeat(501), product, user);
        IllegalArgumentException illegalArgumentException = assertThrows(
                IllegalArgumentException.class,
                executable
        );

        assertEquals("description must have at most 500 characters", illegalArgumentException.getMessage());
    }

    @Test
    @DisplayName("Should throw Null Pointer Exception in case of product is null")
    void test5() {

        Executable executable = () -> new ProductOpinion(5, "Testando", "Muito bom", null, user);
        NullPointerException nullPointerException = assertThrows(
                NullPointerException.class,
                executable
        );

        assertEquals("product must not be null", nullPointerException.getMessage());
    }

    @Test
    @DisplayName("Should throw Null Pointer Exception in case of user is null")
    void test6() {

        Executable executable = () -> new ProductOpinion(5, "Testando", "Muito bom", product, null);
        NullPointerException nullPointerException = assertThrows(
                NullPointerException.class,
                executable
        );

        assertEquals("user must not be null", nullPointerException.getMessage());
    }
}