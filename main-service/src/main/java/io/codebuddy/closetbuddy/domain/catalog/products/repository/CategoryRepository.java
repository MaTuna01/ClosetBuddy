package io.codebuddy.closetbuddy.domain.catalog.products.repository;

import io.codebuddy.closetbuddy.domain.catalog.products.model.dto.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByCode(String code);
    // 루트카테고리만 조회
    List<Category> findByParentIsNull();
    // 특정 부모 카테고리의 자식 카테고리 조회
    List<Category> findByParent(Category parent);
}
