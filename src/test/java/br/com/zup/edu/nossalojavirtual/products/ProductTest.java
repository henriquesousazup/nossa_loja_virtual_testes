package br.com.zup.edu.nossalojavirtual.products;

import br.com.zup.edu.nossalojavirtual.categories.Category;
import br.com.zup.edu.nossalojavirtual.users.Password;
import br.com.zup.edu.nossalojavirtual.users.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


class ProductTest {

    private Category category;
    private User user;
    private List<Photo> photos;
    private Set<Characteristic> characteristics;
    private PreProduct preProduct;

    @BeforeEach
    void setUp() {
        user = new User("henrique.desousa@zup.com.br", Password.encode("123456"));
        category = new Category("Eletrônicos");
        photos = Arrays.asList(new Photo("fototeste1.com.br"));

        Characteristic c1 = new Characteristic("Portátil", "Cabe no seu bolso");
        Characteristic c2 = new Characteristic("Resistente", "Dura anos");
        Characteristic c3 = new Characteristic("Azul", "Azul marinho");

        characteristics = Set.of(c1, c2, c3);

        preProduct = new PreProduct(user, category, "Tijorola", new BigDecimal("150.00"), 5, "Muito bom");
    }

    @Test
    @DisplayName("Should throw Illegal Argument Exception in case of less than 3 characteristics")
    void test1() {

        Characteristic c1 = new Characteristic("Portátil", "Cabe no seu bolso");
        Characteristic c2 = new Characteristic("Resistente", "Dura anos");

        Set<Characteristic> justTwoCharacteristics = Set.of(c1, c2);

        Executable executable = () -> new Product(preProduct, photos, justTwoCharacteristics);
        IllegalArgumentException illegalArgumentException = assertThrows(
                IllegalArgumentException.class,
                executable
        );

        assertEquals("product must have at least three characteristics", illegalArgumentException.getMessage());
    }

    @Test
    @DisplayName("Should throw Illegal Argument Exception in case of less than 1 photo")
    void test2() {

        List<Photo> emptyPhotoList = List.of();

        Executable executable = () -> new Product(preProduct, emptyPhotoList, characteristics);
        IllegalArgumentException illegalArgumentException = assertThrows(
                IllegalArgumentException.class,
                executable
        );

        assertEquals("product must have at least one photo", illegalArgumentException.getMessage());
    }

    @Test
    @DisplayName("Should throw Null Pointer Exception in case of preProduct is null")
    void test3() {

        PreProduct preProductIsNull = null;

        Executable executable = () -> new Product(preProductIsNull, photos, characteristics);
        NullPointerException nullPointerException = assertThrows(
                NullPointerException.class,
                executable
        );

        assertEquals("preProduct must not be null", nullPointerException.getMessage());
    }

    @Test
    @DisplayName("Should throw Illegal Argument Exception in case of stock is less than 0")
    void test4() {

        PreProduct preProductInvalidStock = new
                PreProduct(user, category, "Tijorola", new BigDecimal("150.00"), -1, "Muito bom");

        Executable executable = () -> new Product(preProductInvalidStock, photos, characteristics);
        IllegalArgumentException illegalArgumentException = assertThrows(
                IllegalArgumentException.class,
                executable
        );

        assertEquals("stock quantity must have 0 or more", illegalArgumentException.getMessage());
    }

    @Test
    @DisplayName("Should throw Illegal Argument Exception in case of stock is less than 0")
    void test5() {

        PreProduct preProductInvalidPrice = new
                PreProduct(user, category, "Tijorola", new BigDecimal("00.00"), 5, "Muito bom");

        Executable executable = () -> new Product(preProductInvalidPrice, photos, characteristics);
        IllegalArgumentException illegalArgumentException = assertThrows(
                IllegalArgumentException.class,
                executable
        );

        assertEquals("price must be greater than 0", illegalArgumentException.getMessage());
    }
}