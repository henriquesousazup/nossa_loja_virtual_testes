package br.com.zup.edu.nossalojavirtual.users;

import br.com.zup.edu.nossalojavirtual.products.shared.validators.UniqueFieldValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;

import static org.springframework.http.ResponseEntity.created;

@RestController
@RequestMapping("/api/users")
class UserController {

    private final UserRepository userRepository;

    private Logger logger = LoggerFactory.getLogger(UserController.class);

    UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping
    ResponseEntity<?> createUser(@RequestBody @Valid NewUserRequest newUser) {
        Password password = Password.encode(newUser.getPassword());
        var user = new User(newUser.getLogin(), password);

        userRepository.save(user);

        logger.info("New user has been created! {}", user.toString());

        URI location = URI.create("/api/users/" + user.getId());
        return created(location).build();
    }

    @InitBinder(value = { "newUserRequest" })
    void initBinder(WebDataBinder binder) {

        binder.addValidators(
                             new UniqueFieldValidator<NewUserRequest, String>("login",
                                     "user.login.alreadyRegistered",
                                     NewUserRequest.class,
                                     userRepository::existsByEmail));
    }

}
