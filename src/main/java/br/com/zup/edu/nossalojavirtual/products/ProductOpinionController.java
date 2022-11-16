package br.com.zup.edu.nossalojavirtual.products;

import br.com.zup.edu.nossalojavirtual.exception.UserNotValidException;
import br.com.zup.edu.nossalojavirtual.products.shared.validators.ObjectIsRegisteredValidator;
import br.com.zup.edu.nossalojavirtual.users.User;
import br.com.zup.edu.nossalojavirtual.users.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.net.URI;

import static org.springframework.http.ResponseEntity.created;

@RestController
@RequestMapping("/api/opinions")
class ProductOpinionController {

    private final ProductOpinionRepository productOpinionRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    private Logger logger = LoggerFactory.getLogger(ProductOpinionController.class);

    public ProductOpinionController(ProductOpinionRepository productOpinionRepository,
                                    ProductRepository productRepository, UserRepository userRepository) {
        this.productOpinionRepository = productOpinionRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @PostMapping
    ResponseEntity<?> create(@RequestBody @Valid NewOpinionRequest newOpinion,
                             @AuthenticationPrincipal(expression = "claims['email']") String username
    ) {

        User user = userRepository.findByEmail(username).orElseThrow(
                () -> new UserNotValidException("User not authenticated.")
        );

        var opinion = newOpinion.toProductOpinion(productRepository::findById, user);
        productOpinionRepository.save(opinion);

        logger.info("New opinion has been created! {}", opinion.toString());

        URI location = URI.create("/api/opinions/" + opinion.getId());
        return created(location).build();
    }

    @InitBinder(value = {"newOpinionRequest"})
    void initBinder(WebDataBinder binder) {
        binder.addValidators(new ObjectIsRegisteredValidator<>("productId",
                "product.id.dontExist",
                NewOpinionRequest.class,
                productRepository::existsById));
    }

}
