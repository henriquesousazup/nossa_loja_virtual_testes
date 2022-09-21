package br.com.zup.edu.nossalojavirtual.purchase;

import br.com.zup.edu.nossalojavirtual.products.Product;

import java.time.LocalDateTime;

class PostPaymentProcessedPurchase {

    private final Purchase purchase;

    PostPaymentProcessedPurchase(Purchase purchase) {

        this.purchase = purchase;
    }

    public boolean isPaymentSuccessful() {
        return purchase.isPaymentSuccessful();
    }

    public Long getId() {
        return purchase.getId();
    }

    public String paymentUrl(String retryPaymentUrl) {
        return purchase.paymentUrl(retryPaymentUrl);
    }

    public String buyerEmail() {
        return purchase.buyerEmail();
    }

    public String sellerEmail() {
        return purchase.sellerEmail();
    }

    public Product getProduct() {
        return purchase.getProduct();
    }

    public int getQuantity() {
        return purchase.getQuantity();
    }

    public LocalDateTime paymentConfirmedTime() {

        return purchase.paymentConfirmedTime();
    }
}
