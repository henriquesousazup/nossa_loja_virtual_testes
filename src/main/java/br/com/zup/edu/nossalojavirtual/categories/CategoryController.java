package br.com.zup.edu.nossalojavirtual.categories;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;

@RestController
@RequestMapping("/api/categories")
class CategoryController {

    private final CategoryRepository categoryRepository;

    private Logger logger = LoggerFactory.getLogger(CategoryController.class);

    CategoryController(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @PostMapping
    ResponseEntity<?> createCategory(@RequestBody @Valid NewCategoryRequest newCategory) {
        Category category = newCategory.toCategory(categoryRepository::findCategoryById);

        categoryRepository.save(category);

        logger.info("New category has been created! {}", category);

        URI location = URI.create("/api/categories/" + category.getId());
        return ResponseEntity.created(location).build();
     }

    @InitBinder(value = { "newCategoryRequest" })
    void initBinder(WebDataBinder binder) {

        binder.addValidators(new CategoryUniqueNameValidator(categoryRepository),
                             new SuperCategoryExistsValidator(categoryRepository));
    }
}
