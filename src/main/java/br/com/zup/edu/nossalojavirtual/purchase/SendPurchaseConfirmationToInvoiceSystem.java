package br.com.zup.edu.nossalojavirtual.purchase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@Profile("prod")
class SendPurchaseConfirmationToInvoiceSystem implements PostPurchaseAction {

    private final InvoiceClient invoiceClient;
    private Logger logger = LoggerFactory.getLogger(SendPurchaseConfirmationToInvoiceSystem.class);

    SendPurchaseConfirmationToInvoiceSystem(InvoiceClient invoiceClient) {
        this.invoiceClient = invoiceClient;
    }

    /**
     * do the action if purchase is confirmed
     *
     * @param postPaymentPurchase a success post payment purchase
     * @param uriBuilder build uri component
     */
    @Override
    public void execute(PostPaymentProcessedPurchase postPaymentPurchase, UriComponentsBuilder uriBuilder) {
        if (!postPaymentPurchase.isPaymentSuccessful()) {
            return;
        }

        InvoiceClient.InvoiceRequest request = new InvoiceClient.InvoiceRequest(postPaymentPurchase.getId(), postPaymentPurchase.buyerEmail());
        invoiceClient.requestInvoice(request);

        logger.info("Purchase confirmation has been sent to invoice system! {}", request);
    }
}
