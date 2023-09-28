package com.reactivespring.moviesinfoservice.controller;

import com.reactivespring.moviesinfoservice.domain.MovieInfo;
import com.reactivespring.moviesinfoservice.exceptionhandler.GlobalErrorHandler;
import com.reactivespring.moviesinfoservice.service.MoviesInfoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;


@WebFluxTest(controllers = {MoviesInfoController.class, GlobalErrorHandler.class})
@AutoConfigureWebTestClient
class MoviesInfoControllerUnitTest {

    private final String MOVIES_INFO_URL = "/v1/movieinfos";

    private final List<MovieInfo> moviesInfos = List.of(
            new MovieInfo(null,
                    "Batman Begins",
                    2005,
                    List.of("Christian Bale", "Michael Cane"),
                    LocalDate.parse("2005-06-15")),
            new MovieInfo(null,
                    "The Dark Knight",
                    2008,
                    List.of("Christian Bale", "HealthLedger"),
                    LocalDate.parse("2008-07-18")),
            new MovieInfo("abc",
                    "Dark Knight Rises",
                    2008,
                    List.of("Christian Bale", "HealthLedger"),
                    LocalDate.parse("2008-07-18")));

    @Autowired
    private WebTestClient client;

    @MockBean
    private MoviesInfoService service;

    @Test
    void getAllMoviesInfo() {

        when(service.getAll()).thenReturn(Flux.fromIterable(moviesInfos));
        client.get()
                .uri(MOVIES_INFO_URL)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBodyList(MovieInfo.class)
                .hasSize(3);
    }

    @Test
    void getMovieInfoById() {
        var expectedMovie = moviesInfos.get(2);
        var movieInfoId = "abc";
        when(service.getById(movieInfoId)).thenReturn(Mono.just(expectedMovie));
        client.get()
                .uri(MOVIES_INFO_URL + "/{id}", movieInfoId)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(MovieInfo.class)
                .consumeWith(result -> assertThat(result.getResponseBody()).isEqualTo(expectedMovie));
    }

    @Test
    void addMovieInfo() {
        var movieInfo = new MovieInfo(null,
                "Batman Begins1",
                2005,
                List.of("Christian Bale", "Michael Cane"),
                LocalDate.parse("2005-06-15"));

        when(service.addMovieInfo(isA(MovieInfo.class))).thenReturn(
                Mono.just(new MovieInfo("mockId",
                        "Batman Begins1",
                        2005,
                        List.of("Christian Bale", "Michael Cane"),
                        LocalDate.parse("2005-06-15")))
        );

        client.post()
                .uri(MOVIES_INFO_URL)
                .bodyValue(movieInfo)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(MovieInfo.class)
                .consumeWith(movieInfoEntityExchangeResult -> {
                    var savedMovieInfo = movieInfoEntityExchangeResult.getResponseBody();
                    assert savedMovieInfo != null;
                    assertEquals("mockId", savedMovieInfo.getMovieInfoId());
                });
    }


    @Test
    void addMovieInfo_wrongMovieInfo() {
        var movieInfo = new MovieInfo(null,
                "",
                null,
                List.of("", "Michael Cane"),
                LocalDate.parse("2005-06-15"));

        when(service.addMovieInfo(isA(MovieInfo.class))).thenReturn(
                Mono.just(new MovieInfo("mockId",
                        "Batman Begins1",
                        2005,
                        List.of("Christian Bale", "Michael Cane"),
                        LocalDate.parse("2005-06-15")))
        );

        client.post()
                .uri(MOVIES_INFO_URL)
                .bodyValue(movieInfo)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(String.class)
                .consumeWith(response -> {
                    var responseBody = response.getResponseBody();
                    var expectedErrorMessage = "movieInfo.cast must be present, movieInfo.name must be present, must not be null";
                    assertThat(responseBody).isEqualTo(expectedErrorMessage);
                });
    }


    @Test
    void updateMovieInfo() {
        var movieInfoId = "abc";
        var movieInfo = new MovieInfo(movieInfoId,
                "Batman Begins2",
                2005,
                List.of("Christian Bale", "Michael Cane"),
                LocalDate.parse("2005-06-15"));

        when(service.updateMovieInfo(isA(MovieInfo.class), isA(String.class))).thenReturn(
                Mono.just(new MovieInfo("mockId",
                        "Batman Begins2",
                        2005,
                        List.of("Christian Bale", "Michael Cane"),
                        LocalDate.parse("2005-06-15")))
        );

        client.put()
                .uri(MOVIES_INFO_URL + "/{id}", movieInfoId)
                .bodyValue(movieInfo)
                .exchange()
                .expectStatus().isOk()
                .expectBody(MovieInfo.class)
                .consumeWith(movieInfoEntityExchangeResult -> {
                    var updatedMovieInfo = movieInfoEntityExchangeResult.getResponseBody();
                    assert updatedMovieInfo != null;
                    assertEquals("mockId", updatedMovieInfo.getMovieInfoId());
                    assertEquals("Batman Begins2", updatedMovieInfo.getName());
                });
    }

    @Test
    void deleteMovieInfo() {
        var movieInfoId = "abc";

        when(service.deleteMovieInfo(isA(String.class)))
                .thenReturn(Mono.empty());
        when(service.getById(isA(String.class)))
                .thenReturn(Mono.empty());

        client.delete()
                .uri(MOVIES_INFO_URL + "/{id}", movieInfoId)
                .exchange()
                .expectStatus().isNoContent()
                .expectBody(MovieInfo.class);

        client.get()
                .uri(MOVIES_INFO_URL + "/{id}", movieInfoId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(MovieInfo.class)
                .consumeWith(entityExchangeResult -> {
                    var response = entityExchangeResult.getResponseBody();
                    assertNull(response);
                });
    }

}
