package br.com.zup.edu.nossalojavirtual.purchase;

import br.com.zup.edu.nossalojavirtual.categories.Category;
import br.com.zup.edu.nossalojavirtual.products.*;
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

class PurchaseTest {

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
    @DisplayName("Should throw Illegal Argument Exception in case of quantity is less than 1")
    void test1() {
        Executable executable = () -> new Purchase(user, product, 0, PaymentGateway.PAYPAL);

        IllegalArgumentException illegalArgumentException = assertThrows(
                IllegalArgumentException.class,
                executable
        );

        assertEquals("quantity must not be less than 0", illegalArgumentException.getMessage());
    }

    @Test
    @DisplayName("Should throw Illegal Argument Exception in case of product is null")
    void test2() {
        Executable executable = () -> new Purchase(user, null, 0, PaymentGateway.PAYPAL);

        IllegalArgumentException illegalArgumentException = assertThrows(
                IllegalArgumentException.class,
                executable
        );

        assertEquals("product must not be null", illegalArgumentException.getMessage());
    }

    @Test
    @DisplayName("Should throw Illegal Argument Exception in case of buyer is null")
    void test3() {
        Executable executable = () -> new Purchase(null, product, 0, PaymentGateway.PAYPAL);

        IllegalArgumentException illegalArgumentException = assertThrows(
                IllegalArgumentException.class,
                executable
        );

        assertEquals("user must not be null", illegalArgumentException.getMessage());
    }

    @Test
    @DisplayName("Should throw Illegal Argument Exception in case of payment gateway is null")
    void test4() {
        Executable executable = () -> new Purchase(user, product, 1, null);

        IllegalArgumentException illegalArgumentException = assertThrows(
                IllegalArgumentException.class,
                executable
        );

        assertEquals("paymentGateway must not be null", illegalArgumentException.getMessage());
    }

    @Test
    @DisplayName("Should throw Illegal State Exception in case of purchase is finished")
    void test5() {

        Purchase purchase = new Purchase(user, product, 1, PaymentGateway.PAYPAL);
        PaymentReturn paymentReturn = new PaymentReturn(purchase.getId(), "1", "1");

        PostPaymentProcessedPurchase process = purchase.process(paymentReturn);

        Executable executable = () -> purchase.process(paymentReturn);

        IllegalStateException illegalStateException = assertThrows(
                IllegalStateException.class,
                executable
        );

        assertEquals("A finished Purchase cannot be paid again", illegalStateException.getMessage());
    }

    @Test
    @DisplayName("Should throw Illegal State Exception in case of purchase is unfinished")
    void test6() {

        Purchase purchase = new Purchase(user, product, 1, PaymentGateway.PAYPAL);

        Executable executable = () -> purchase.paymentConfirmedTime();

        IllegalStateException illegalStateException = assertThrows(
                IllegalStateException.class,
                executable
        );

        assertEquals("An unfinished Purchase does not have a payment confirmation timestamp", illegalStateException.getMessage());
    }

    @Test
    @DisplayName("Should return payment confirmed time")
    void test7() {

        Purchase purchase = new Purchase(user, product, 1, PaymentGateway.PAYPAL);
        PaymentReturn paymentReturn = new PaymentReturn(purchase.getId(), "1", "1");

        PostPaymentProcessedPurchase process = purchase.process(paymentReturn);

        assertNotNull(purchase.paymentConfirmedTime());
    }

}