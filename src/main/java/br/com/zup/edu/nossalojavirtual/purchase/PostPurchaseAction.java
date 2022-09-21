package br.com.zup.edu.nossalojavirtual.purchase;

import org.springframework.web.util.UriComponentsBuilder;

interface PostPurchaseAction {

    void execute(PostPaymentProcessedPurchase payment, UriComponentsBuilder uriBuilder);
}
