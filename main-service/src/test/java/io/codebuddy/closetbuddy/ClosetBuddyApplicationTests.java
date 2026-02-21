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

        // Rdis 컨테이너
        @Container
        @ServiceConnection
        static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:latest"))
                        .withExposedPorts(6379);

        // kafka 컨테이너
        @Container
        @ServiceConnection
        // test환경에 적합한 confluentinc이미지 사용하도록 명시
        static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:latest"));

        // els
        @Container
        @ServiceConnection
        static ElasticsearchContainer elasticsearch = new ElasticsearchContainer(
                        DockerImageName.parse("docker.elastic.co/elasticsearch/elasticsearch:7.17.10"));

        @Test
        void contextLoads() {
        }

}
