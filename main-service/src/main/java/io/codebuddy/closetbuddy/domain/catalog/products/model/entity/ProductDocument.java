package io.codebuddy.closetbuddy.domain.catalog.products.model.entity;


import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.elasticsearch.annotations.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@ToString
// 인덱스 이름 지정
@Document(indexName = "products")
// 커스텀 설정 파일
@Setting(settingPath = "/elasticsearch/products-settings.json")
public class ProductDocument {

    @Id
    private String id;

    @MultiField(
            // 일반적인 키워드 검색
            mainField = @Field(type = FieldType.Text, analyzer = "nori_analyzer", searchAnalyzer = "nori_synonym_analyzer"),
            otherFields = {
                    // 부분일치
                    @InnerField(suffix = "ngram", type = FieldType.Text, analyzer = "edge_ngram_analyzer", searchAnalyzer = "nori_synonym_analyzer"),
            }
    )
    private String productName;

    @Field(type = FieldType.Long)
    private Long productPrice;

    @Field(type = FieldType.Integer)
    private int productStock;

    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "nori_analyzer"),
            otherFields = {
                    @InnerField(suffix = "keyword", type = FieldType.Keyword)
            }
    )
    private String storeName;

    // 상위 카테고리
    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "nori_synonym_analyzer"),
            otherFields = {
                    @InnerField(suffix = "keyword", type = FieldType.Keyword)
            }
    )
    private String topCategory;

    // 하위 카테고리
    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "nori_synonym_analyzer"),
            otherFields = {
                    @InnerField(suffix = "keyword", type = FieldType.Keyword)
            }
    )
    private String subCategory;

    // 이미지를 검색할 필요 없으므로 색인 제외 (저장 공간 절약)
    @Field(type = FieldType.Keyword, index = false)
    private String imageUrl;



}