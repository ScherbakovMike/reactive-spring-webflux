package com.reactivespring.client;

import com.reactivespring.domain.Review;
import com.reactivespring.exception.ReviewsClientException;
import com.reactivespring.util.RetryUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class ReviewsRestClient {

    @Value("${rest-client.reviews-url}")
    private String reviewsUrl;

    private final WebClient client;

    public Flux<Review> retrieveReviews(String movieId) {
        var url = UriComponentsBuilder.fromHttpUrl(reviewsUrl)
                .queryParam("movieInfoId", movieId)
                .buildAndExpand()
                .toUriString();
        return client.get()
                .uri(url)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> {
                    if (response.statusCode().equals(HttpStatus.NOT_FOUND)) {
                        return Mono.empty();
                    }
                    return response.bodyToMono(String.class)
                            .flatMap(responseMessage -> Mono.error(new ReviewsClientException(
                                    "Server Exception in ReviewService" + responseMessage
                            )));
                })
                .bodyToFlux(Review.class)
                .retryWhen(RetryUtil.retrySpec())
                .log();
    }
}
