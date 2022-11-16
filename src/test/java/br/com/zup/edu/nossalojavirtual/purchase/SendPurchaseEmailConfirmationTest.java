package br.com.zup.edu.nossalojavirtual.purchase;

import br.com.zup.edu.nossalojavirtual.categories.Category;
import br.com.zup.edu.nossalojavirtual.products.*;
import br.com.zup.edu.nossalojavirtual.products.shared.email.EmailService;
import br.com.zup.edu.nossalojavirtual.users.Password;
import br.com.zup.edu.nossalojavirtual.users.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SendPurchaseEmailConfirmationTest {

    private SendPurchaseEmailConfirmation sendPurchaseEmailConfirmation;
    private EmailService emailServiceMock;

    private PhotoUploader photoUploaderMock;
    private User user;
    private Product product;
    private Purchase purchase;
    private PostPaymentProcessedPurchase postPaymentProcessedPurchase;


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

        purchase = new Purchase(user, product, 1, PaymentGateway.PAYPAL);
        PaymentReturn paymentReturn = new PaymentReturn(purchase.getId(), "1", "1");

        postPaymentProcessedPurchase = purchase.process(paymentReturn);

        emailServiceMock = mock(EmailService.class);
        sendPurchaseEmailConfirmation = new SendPurchaseEmailConfirmation(emailServiceMock);
    }

    @Test
    @DisplayName("Verify if email was sent")
    void test1() {
        sendPurchaseEmailConfirmation.execute(postPaymentProcessedPurchase, UriComponentsBuilder.newInstance());
        verify(emailServiceMock, times(1)).send(any());
    }

    @Test
    @DisplayName("Verify if email was not sent in case of payment is not successful")
    void test2() {

        Purchase notSuccessfulPurchase = new Purchase(user, product, 1, PaymentGateway.PAYPAL);
        PaymentReturn paymentReturn = new PaymentReturn(notSuccessfulPurchase.getId(), "1", "2");

        PostPaymentProcessedPurchase postPaymentProcessedPurchase = notSuccessfulPurchase.process(paymentReturn);

        sendPurchaseEmailConfirmation.execute(postPaymentProcessedPurchase, UriComponentsBuilder.newInstance());
        verify(emailServiceMock, times(0)).send(any());
    }
}