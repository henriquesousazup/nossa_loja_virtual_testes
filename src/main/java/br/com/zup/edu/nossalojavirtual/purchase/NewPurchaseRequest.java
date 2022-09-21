package br.com.zup.edu.nossalojavirtual.purchase;

import br.com.zup.edu.nossalojavirtual.users.User;
import br.com.zup.edu.nossalojavirtual.products.Product;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.UUID;

public class NewPurchaseRequest {

    @NotNull
    private UUID productId;

    @Min(1)
    private int quantity;

    @NotNull
    private PaymentGateway paymentGateway;

    @Deprecated
    public NewPurchaseRequest() {
    }

    public NewPurchaseRequest(UUID productId, int quantity, PaymentGateway paymentGateway) {
        this.productId = productId;
        this.quantity = quantity;
        this.paymentGateway = paymentGateway;
    }

    public UUID getProductId() {
        return productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public PaymentGateway getPaymentGateway() {
        return paymentGateway;
    }

    public Purchase toPurchase(User buyer, Product product) {

        return new Purchase(buyer, product, quantity, paymentGateway);
    }

}
