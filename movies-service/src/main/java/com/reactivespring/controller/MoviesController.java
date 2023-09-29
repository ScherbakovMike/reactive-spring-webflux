package com.reactivespring.controller;

import com.reactivespring.client.MovieInfoRestClient;
import com.reactivespring.client.ReviewsRestClient;
import com.reactivespring.domain.Movie;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/movies")
public class MoviesController {

    private final MovieInfoRestClient movieInfoRestClient;
    private final ReviewsRestClient reviewsRestClient;

    @GetMapping("/{id}")
    public Mono<Movie> retrieveMovieById(@PathVariable("id") String movieId) {

        return movieInfoRestClient.retrieveMovieInfo(movieId)
                .flatMap(movieInfo -> reviewsRestClient.retrieveReviews(movieId)
                        .collectList()
                        .map(revs -> new Movie(movieInfo, revs)));
    }
}
