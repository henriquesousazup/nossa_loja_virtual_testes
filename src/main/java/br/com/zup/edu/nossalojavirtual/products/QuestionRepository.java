package br.com.zup.edu.nossalojavirtual.products;

import org.springframework.data.repository.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface QuestionRepository extends Repository<Question, Long> {

    Question save(Question question);

    List<Question> findByProduct(Product product);

    Optional<Question> findById(Long id);

    List<Question> findAll();

    void deleteAll();
}
