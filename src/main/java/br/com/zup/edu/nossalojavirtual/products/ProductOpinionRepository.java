package br.com.zup.edu.nossalojavirtual.products;

import org.springframework.data.repository.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ProductOpinionRepository extends Repository<ProductOpinion, Long> {

    ProductOpinion save(ProductOpinion productOpinion);

    Optional<ProductOpinion> findById(long idNewOpinionSaved);

    void deleteAll();

    List<ProductOpinion> findAll();
}
