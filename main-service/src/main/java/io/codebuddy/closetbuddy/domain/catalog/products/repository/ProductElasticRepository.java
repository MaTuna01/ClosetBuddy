package io.codebuddy.closetbuddy.domain.catalog.products.repository;

import io.codebuddy.closetbuddy.domain.catalog.products.model.entity.ProductDocument;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.core.SearchPage;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ProductElasticRepository extends ElasticsearchRepository<ProductDocument,String> {

    // query : ?0 : 첫번째 파라미터
    // fields : 쿼리 결과에 가중치
    // best_fields : 필드(name,ngram) 중 가장 점수가 높은 필드의 점수를 document의 최종 점수로 삼음
    // fuzziness:  오타 허용 범위
    @Query("""
    {
        "bool": {
            "must": [
                {
                    "multi_match": {
                        "query": "?0",
                            "fields": [
                                "subCategory^5",     // 하위 카테고리 일치 시 가장 상위 노출
                                "topCategory^3",
                                "productName^3",
                                "productName.ngram^2",
                                "storeName^2"],
                            "type": "best_fields",
                            "fuzziness": "AUTO"
                    }
                }
            ]
        }
    }
    """)
    SearchPage<ProductDocument> searchByKeyword(String keyword, Pageable pageable);

    // 하위 카테고리 내에서 검색 (카테고리 탭을 누른 후 검색했을 때)
    @Query("""
    {
        "bool": {
            "filter": [
                {
                    "term": {
                        "subCategory.keyword": "?0" // ?0 : 하위 카테고리명
                    }
                }
            ],
            "must": [
                {
                    "multi_match": {
                        "query": "?1",              // ?1 : 검색 키워드
                        "fields": [
                            "productName^3",
                            "productName.ngram^2",
                            "storeName"
                        ],
                        "type": "best_fields",
                        "fuzziness": "AUTO"
                    }
                }
            ]
        }
    }
    """)
    SearchPage<ProductDocument> searchByCategoryAndKeyword(String category, String keyword, Pageable pageable);

    // 키워드 없이 카테고리만 선택
    @Query("""
    {
        "bool": {
            "filter": [
                {
                    "term": {
                        "subCategory.keyword": "?0"
                    }
                }
            ]
        }
    }
    """)
    SearchPage<ProductDocument> searchByCategory(String category, Pageable pageable);

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
