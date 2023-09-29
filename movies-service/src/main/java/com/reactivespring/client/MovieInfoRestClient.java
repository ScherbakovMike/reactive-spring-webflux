package com.reactivespring.client;

import com.reactivespring.domain.MovieInfo;
import com.reactivespring.exception.MoviesInfoClientException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@Slf4j
@RequiredArgsConstructor
public class MovieInfoRestClient {

    @Value("${rest-client.movies-info-url}")
    private String moviesInfoUrl;

    private final WebClient client;

    public Mono<MovieInfo> retrieveMovieInfo(String movieId) {
        return client.get()
                .uri(moviesInfoUrl+"/{id}", movieId)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> {
                    if (response.statusCode().equals(HttpStatus.NOT_FOUND)) {
                        return Mono.error(new MoviesInfoClientException(
                                "There is no MovieInfo available for the passed Id : " + movieId, response.statusCode().value()
                        ));
                    }
                    return response.bodyToMono(String.class)
                            .flatMap(responseMessage -> Mono.error(new MoviesInfoClientException(
                                    responseMessage, response.statusCode().value()
                            )));
                })
                .onStatus(HttpStatusCode::is5xxServerError, response -> {
                    log.info("Status code is : {}", response.statusCode().value());
                    return response.bodyToMono(String.class)
                            .flatMap(responseMessage -> Mono.error(new MoviesInfoClientException(
                                    "Server exception caught : {}", response.statusCode().value()
                            )));
                })
                .bodyToMono(MovieInfo.class)
                .log();
    }
}
