package es.academia.campushub.support;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Assertions;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mongodb.MongoDBContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public abstract class ApiIntegrationSupport {
  @Container
  static final PostgreSQLContainer POSTGRE_SQL_CONTAINER = new PostgreSQLContainer("postgres:17");

  @Container static final MongoDBContainer MONGO_DB_CONTAINER = new MongoDBContainer("mongo:8");

  @Container
  static final GenericContainer<?> REDIS_CONTAINER =
      new GenericContainer<>("redis:8-alpine").withExposedPorts(6379);

  @DynamicPropertySource
  static void props(DynamicPropertyRegistry r) {
    r.add("spring.datasource.url", POSTGRE_SQL_CONTAINER::getJdbcUrl);
    r.add("spring.datasource.username", POSTGRE_SQL_CONTAINER::getUsername);
    r.add("spring.datasource.password", POSTGRE_SQL_CONTAINER::getPassword);
    r.add("spring.mongodb.uri", MONGO_DB_CONTAINER::getReplicaSetUrl);
    r.add("spring.data.redis.host", REDIS_CONTAINER::getHost);
    r.add("spring.data.redis.port", () -> REDIS_CONTAINER.getMappedPort(6379));
  }

  @LocalServerPort int port;
  private final HttpClient client = HttpClient.newHttpClient();

  protected HttpResponse<String> call(String method, String endpoint, String json)
      throws IOException, InterruptedException {
    var b =
        HttpRequest.newBuilder(URI.create("http://localhost:" + port + endpoint))
            .header("Content-Type", "application/json");
    var req =
        b.method(
                method,
                json == null
                    ? HttpRequest.BodyPublishers.noBody()
                    : HttpRequest.BodyPublishers.ofString(json))
            .build();
    return client.send(req, HttpResponse.BodyHandlers.ofString());
  }

  protected HttpResponse<String> post(String path, String json) throws Exception {
    return call("POST", path, json);
  }

  protected HttpResponse<String> get(String path) throws Exception {
    return call("GET", path, null);
  }

  protected HttpResponse<String> put(String path, String json) throws Exception {
    return call("PUT", path, json);
  }

  protected void status(int expected, HttpResponse<String> actual) {
    Assertions.assertEquals(expected, actual.statusCode(), actual.body());
  }

  protected long id(HttpResponse<String> r) {
    var m = Pattern.compile("\"id\"\\s*:\\s*(\\d+)").matcher(r.body());
    Assertions.assertTrue(m.find(), "Falta id en respuesta: " + r.body());
    return Long.parseLong(m.group(1));
  }
}
