package com.reactivespring.controller;


import com.github.tomakehurst.wiremock.client.WireMock;
import com.reactivespring.domain.Movie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;


import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@AutoConfigureWireMock(port = 0) // spin up a httpserver in port 8084
@TestPropertySource(
        properties = {
                "rest-client.movies-info-url=http://localhost:${wiremock.server.port}/v1/movieinfos",
                "rest-client.reviews-url=http://localhost:${wiremock.server.port}/v1/reviews"
        }
)
class MoviesControllerIntgTest {

    @Autowired
    WebTestClient client;

    @Value("${wiremock.server.port}")
    private String wiremockServerPort;

    @Test
    void retrieveMovieId() {

        var movieId = "abc";
        stubFor(get(urlEqualTo("/v1/movieinfos/" + movieId))
                .willReturn(
                        aResponse()
                                .withHeader("Content-Type", "application/json")
                                .withBodyFile("movieinfo.json")
                )
        );

        stubFor(get(urlPathEqualTo("/v1/reviews"))
                .willReturn(
                        aResponse()
                                .withHeader("Content-Type", "application/json")
                                .withBodyFile("reviews.json")
                )
        );

        client.get()
                .uri("/v1/movies/{id}", movieId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Movie.class)
                .consumeWith(response -> {
                    var movie = response.getResponseBody();
                    assertThat(movie.getReviewList()).hasSize(2);
                });
    }

    @Test
    void retrieveMovieId_not_found() {

        var movieId = "abc";
        stubFor(get(urlEqualTo("/v1/movieinfos/" + movieId))
                .willReturn(
                        aResponse()
                                .withStatus(404)
                )
        );

        stubFor(get(urlPathEqualTo("/v1/reviews"))
                .willReturn(
                        aResponse()
                                .withHeader("Content-Type", "application/json")
                                .withBodyFile("reviews.json")
                )
        );

        client.get()
                .uri("/v1/movies/{id}", movieId)
                .exchange()
                .expectStatus().is4xxClientError()
                .expectBody(String.class)
                .isEqualTo("There is no MovieInfo available for the passed Id : abc");
    }

    @Test
    void retrieveMovieId_not_found_reviews() {

        var movieId = "abc";
        stubFor(get(urlEqualTo("/v1/movieinfos/" + movieId))
                .willReturn(
                        aResponse()
                                .withHeader("Content-Type", "application/json")
                                .withBodyFile("movieinfo.json")
                )
        );

        stubFor(get(urlPathEqualTo("/v1/reviews"))
                .willReturn(
                        aResponse()
                                .withStatus(404)
                )
        );

        client.get()
                .uri("/v1/movies/{id}", movieId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Movie.class)
                .consumeWith(response -> {
                    var movie = response.getResponseBody();
                    assertThat(movie.getMovieInfo().getName()).isEqualTo("Batman Begins");
                    assertThat(movie.getReviewList()).isEmpty();
                });
    }

    @Test
    void retrieveMovieId_500() {

        var movieId = "abc";
        stubFor(get(urlEqualTo("/v1/movieinfos/" + movieId))
                .willReturn(
                        aResponse()
                                .withStatus(500)
                                .withBody("MovieInfo Service Unavailable")
                )
        );

        client.get()
                .uri("/v1/movies/{id}", movieId)
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody(String.class)
                .isEqualTo("Server exception caught : MovieInfo Service Unavailable");
    }

    @Test
    void retrieveMovieId_5xx_with_retries() {

        var movieId = "abc";
        stubFor(get(urlEqualTo("/v1/movieinfos/" + movieId))
                .willReturn(
                        aResponse()
                                .withStatus(500)
                                .withBody("MovieInfo Service Unavailable")
                )
        );

        client.get()
                .uri("/v1/movies/{id}", movieId)
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody(String.class)
                .isEqualTo("Server exception caught : MovieInfo Service Unavailable");
        WireMock.verify(4, getRequestedFor(urlEqualTo("/v1/movieinfos/" + movieId)));
    }
}
