package io.codebuddy.closetbuddy.domain.catalog.products.repository;

import io.codebuddy.closetbuddy.domain.catalog.products.model.entity.ProductDocument;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.SearchPage;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ProductElasticRepository extends ElasticsearchRepository<ProductDocument,String> {

    // query : ?0 : 첫번째 파라미터
    // fields : 쿼리 결과에 가중치
    // best_fields : 필드(name,ngram) 중 가장 점수가 높은 필드의 점수를 document의 최종 점수로 삼음
    // fuzziness:  오타 허용 범위
    @Query("""
    {
        "multi_match": {
            "query": "?0",
            "fields": [
                "productName^3",
                "productName.ngram^3",
                "storeName^2",
                "category"
            ],
            "type": "best_fields",
            "fuzziness": "AUTO"
        }
    }
    """)
    SearchPage<ProductDocument> searchByKeyword(String keyword, Pageable pageable);

    // bool_prefix : 띄어쓰기가 포함된 자동완성
    @Query("""
    {
        "multi_match": {
            "query": "#{#prefix}",
            "type": "bool_prefix",
            "fields": [
                "productName",
                "productName.ngram"
            ]
        }
    }
    """)
    SearchPage<ProductDocument> autoComplete(String prefix, Pageable pageable);

}
