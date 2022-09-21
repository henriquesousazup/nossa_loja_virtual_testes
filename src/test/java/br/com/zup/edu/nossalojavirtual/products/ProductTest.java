package br.com.zup.edu.nossalojavirtual.products;

import br.com.zup.edu.nossalojavirtual.categories.Category;
import br.com.zup.edu.nossalojavirtual.users.Password;
import br.com.zup.edu.nossalojavirtual.users.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;


class ProductTest {

    @Test
    @DisplayName("Should throw an exception in case of less than 3 characteristics")
    void test1() {
        User user = new User("henrique.desousa@zup.com.br", Password.encode("123456"));
        Category category = new Category("Eletrônicos");

        List<Photo> photos = Arrays.asList(new Photo("fototeste1.com.br"));

        Characteristic c1 = new Characteristic("Portátil", "Cabe no seu bolso");
        Characteristic c2 = new Characteristic("Resistente", "Dura anos");

        Set<Characteristic> justTwoCharacteristics = Set.of(c1, c2);

        PreProduct preProduct = new PreProduct(user, category, "Tijorola", new BigDecimal("150.00"), 5, "Muito bom");

        Executable executable = () -> new Product(preProduct, photos, justTwoCharacteristics);
        assertThrows(
                IllegalArgumentException.class,
                executable
        );
    }

    @Test
    @DisplayName("Should throw an exception in case of less than 1 photo")
    void test2() {
        User user = new User("henrique.desousa@zup.com.br", Password.encode("123456"));
        Category category = new Category("Eletrônicos");

        List<Photo> emptyPhotoList = List.of();

        Characteristic c1 = new Characteristic("Portátil", "Cabe no seu bolso");
        Characteristic c2 = new Characteristic("Resistente", "Dura anos");
        Characteristic c3 = new Characteristic("Azul", "Azul marinho");

        Set<Characteristic> characteristics = Set.of(c1, c2, c3);

        PreProduct preProduct = new PreProduct(user, category, "Tijorola", new BigDecimal("150.00"), 5, "Muito bom");

        Executable executable = () -> new Product(preProduct, emptyPhotoList, characteristics);
        assertThrows(
                IllegalArgumentException.class,
                executable
        );
    }

//    @Test
//    @DisplayName("Should throw an exception in case of preProduct is null")
//    void test3() {
//        User user = new User("henrique.desousa@zup.com.br", Password.encode("123456"));
//        Category category = new Category("Eletrônicos");
//
//        List<Photo> photos = Arrays.asList(new Photo("fototeste1.com.br"));
//
//        Characteristic c1 = new Characteristic("Portátil", "Cabe no seu bolso");
//        Characteristic c2 = new Characteristic("Resistente", "Dura anos");
//        Characteristic c3 = new Characteristic("Azul", "Azul marinho");
//
//        Set<Characteristic> characteristics = Set.of(c1, c2, c3);
//
//        PreProduct preProduct = null;
//
//        Executable executable = () -> new Product(preProduct, photos, characteristics);
//        assertThrows(
//                IllegalArgumentException.class,
//                executable
//        );
//    }
}