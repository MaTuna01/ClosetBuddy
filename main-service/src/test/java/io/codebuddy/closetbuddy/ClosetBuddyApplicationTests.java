package io.codebuddy.closetbuddy;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class ClosetBuddyApplicationTests {

        // mysql 컨테이너
        @Container
        @ServiceConnection
        static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0");

        // Redis 컨테이너
        @Container
        @ServiceConnection
        static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:latest"))
                .withExposedPorts(16379);

        // kafka 컨테이너
        @Container
        @ServiceConnection
        static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.4"));

        // els
        @Container
        @ServiceConnection
        // 형태소 분석이미지 그대로 테스트에 활용
        static ElasticsearchContainer elasticsearch = new ElasticsearchContainer(
                DockerImageName.parse("ghcr.io/eddie1031/es:latest")
                        .asCompatibleSubstituteFor("docker.elastic.co/elasticsearch/elasticsearch"))

                // docker compose 파일을 참고하여 개발 환경과 똑같은 환경 구성으로 띄움
                // 단일 노드 모드 설정
                .withEnv("discovery.type", "single-node")
                // 보안 기능 비활성화
                .withEnv("xpack.security.enabled", "false")
                // ssl 인증 비활성화
                .withEnv("xpack.security.http.ssl.enabled", "false")
                // jvm 메모리 설정
                .withEnv("ES_JAVA_OPTS", "-Xms512m -Xmx512m");

        @Test
        void contextLoads() {
        }

}
