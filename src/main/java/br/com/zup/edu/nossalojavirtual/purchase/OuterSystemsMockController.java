package br.com.zup.edu.nossalojavirtual.purchase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static org.springframework.http.ResponseEntity.ok;

@RestController
class OuterSystemsMockController {

    private Logger logger = LoggerFactory.getLogger(OuterSystemsMockController.class);

    @PostMapping("/invoice/register")
    ResponseEntity<?> invoice(@RequestBody Map<String, Object> request) {
        System.out.println(request);

        logger.info("New invoice has been registered! {}", request.toString());

        return ok().build();
    }

    @PostMapping("/sellerRanking/newPurchase")
    ResponseEntity<?> newPurchase(@RequestBody Map<String, Object> request) {
        System.out.println(request);

        logger.info("New purchase has been registered! {}", request.toString());

        return ok().build();
    }
}
