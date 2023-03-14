package com.github.chepby.logbookbugdemo;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.spring.LogbookClientHttpRequestInterceptor;

@ExtendWith(MockitoExtension.class)
class LogbookClientHttpRequestInterceptorTest {

  private final WireMockServer server = new WireMockServer(options().dynamicPort());

  private RestTemplate restTemplate;

  @BeforeEach
  void setup() {
    server.start();
    LogbookClientHttpRequestInterceptor interceptor =
        new LogbookClientHttpRequestInterceptor(Logbook.builder().build());
    restTemplate = new RestTemplate();
    restTemplate.getInterceptors().add(interceptor);
  }

  @AfterEach
  void tearDown() {
    server.stop();
  }

  @Test
  void post200() {
    server.stubFor(
        post("/test/post/withcontent")
            .willReturn(aResponse().withStatus(200).withBody("response")));
    var url = server.baseUrl() + "/test/post/withcontent";

    assertThatCode(() -> restTemplate.postForObject(url, "request", String.class))
        .doesNotThrowAnyException();
  }

  @Test
  void post400() {
    server.stubFor(
        post("/test/post/withcontent")
            .willReturn(aResponse().withStatus(400).withBody("response")));
    var url = server.baseUrl() + "/test/post/withcontent";

    assertThatThrownBy(() -> restTemplate.postForObject(url, "request", String.class))
        .isInstanceOf(HttpClientErrorException.class);
  }

  @Test
  void post500() {
    server.stubFor(
        post("/test/post/withcontent")
            .willReturn(aResponse().withStatus(500).withBody("response")));
    var url = server.baseUrl() + "/test/post/withcontent";

    assertThatThrownBy(() -> restTemplate.postForObject(url, "request", String.class))
        .isInstanceOf(HttpServerErrorException.class);
  }

  @Test
  void put400() {
    server.stubFor(
        put("/test/put/withcontent").willReturn(aResponse().withStatus(400).withBody("response")));
    var url = server.baseUrl() + "/test/put/withcontent";

    assertThatThrownBy(() -> restTemplate.put(url, "request", String.class))
        .isInstanceOf(HttpServerErrorException.class);
  }
}
