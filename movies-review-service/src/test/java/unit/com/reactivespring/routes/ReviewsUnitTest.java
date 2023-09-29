package com.reactivespring.routes;

import com.reactivespring.domain.Review;
import com.reactivespring.exceptionhandler.GlobalErrorHandler;
import com.reactivespring.handler.ReviewHandler;
import com.reactivespring.repository.ReviewReactiveRepository;
import com.reactivespring.router.ReviewRouter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;

@WebFluxTest
@ContextConfiguration(classes = {ReviewRouter.class, ReviewHandler.class, GlobalErrorHandler.class})
@AutoConfigureWebTestClient
class ReviewsUnitTest {

    private final String MOVIES_REVIEW_URL = "/v1/reviews";
    private final List<Review> reviews = List.of(
            new Review(null, 1L, "Awesome Movie", 9.0),
            new Review(null, 1L, "Awesome Movie1", 9.0),
            new Review(null, 2L, "Excellent Movie", 8.0)
    );

    @MockBean
    private ReviewReactiveRepository repository;

    @Autowired
    private WebTestClient client;

    @Test
    void addReview() {
        var review = new Review(null, 1L, "Awesome Movie", 9.0);

        when(repository.save(isA(Review.class)))
                .thenReturn(Mono.just(new Review("abc", 1L, "Awesome Movie", 9.0)));
        client
                .post()
                .uri(MOVIES_REVIEW_URL)
                .bodyValue(review)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Review.class)
                .consumeWith(response -> {
                            var savedReview = response.getResponseBody();
                            assert savedReview != null;
                            assert savedReview.getReviewId() != null;
                        }
                );
    }

    @Test
    void addReview_validation_error() {
        var review = new Review(null, null, "Awesome Movie", -9.0);

        when(repository.save(isA(Review.class)))
                .thenReturn(Mono.just(new Review("abc", 1L, "Awesome Movie", 9.0)));
        client
                .post()
                .uri(MOVIES_REVIEW_URL)
                .bodyValue(review)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(String.class)
                .isEqualTo("rating.movieInfoId : must not be null,rating.negative : please pass a non-negative value");
    }

    @Test
    void getAllReviews() {
        when(repository.findAll()).thenReturn(Flux.fromIterable(reviews));
        client.get()
                .uri(MOVIES_REVIEW_URL)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBodyList(Review.class)
                .hasSize(3);
    }

    @Test
    void updateReview() {
        var review = new Review(null, 1L, "Awesome Movie", 9.0);
        var reviewWithId = new Review("abc", 1L, "Awesome Movie", 9.0);
        when(repository.save(isA(Review.class))).thenReturn(Mono.just(reviewWithId));
        when(repository.findById(isA(String.class))).thenReturn(Mono.just(reviewWithId));

        var addResponse = client
                .post()
                .uri(MOVIES_REVIEW_URL)
                .bodyValue(review)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Review.class)
                .returnResult();
        var movieReviewId = addResponse.getResponseBody().getReviewId();
        client
                .put()
                .uri(MOVIES_REVIEW_URL + "/{id}", movieReviewId)
                .bodyValue(review)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Review.class)
                .consumeWith(response -> {
                            var updatedReview = response.getResponseBody();
                            assert updatedReview != null;
                            assertThat(updatedReview.getRating()).isEqualTo(review.getRating());
                        }
                );
    }

    @Test
    void deleteReview() {
        var review = new Review(null, 1L, "Awesome Movie", 9.0);
        var reviewWithId = new Review("abc", 1L, "Awesome Movie", 9.0);
        when(repository.save(isA(Review.class))).thenReturn(Mono.just(reviewWithId));
        when(repository.findById(isA(String.class))).thenReturn(Mono.just(reviewWithId));
        when(repository.delete(isA(Review.class))).thenReturn(Mono.empty());
        var addResponse = client
                .post()
                .uri(MOVIES_REVIEW_URL)
                .bodyValue(review)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Review.class)
                .returnResult();
        var movieReviewId = addResponse.getResponseBody().getReviewId();
        client.delete()
                .uri(MOVIES_REVIEW_URL + "/{id}", movieReviewId)
                .exchange()
                .expectStatus().isNoContent()
                .expectBody(Review.class);
    }

    @Test
    void getReviewByMovieInfoId() {
        var review = new Review(null, 1L, "Awesome Movie", 9.0);
        var reviewWithId = new Review("abc", 1L, "Awesome Movie", 9.0);
        when(repository.save(isA(Review.class))).thenReturn(Mono.just(reviewWithId));
        when(repository.findReviewsByMovieInfoId(isA(Long.class))).thenReturn(Flux.fromIterable(reviews));

        var addResponse = client
                .post()
                .uri(MOVIES_REVIEW_URL)
                .bodyValue(review)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Review.class)
                .returnResult();
        var movieMovieInfoId = addResponse.getResponseBody().getMovieInfoId();
        client
                .get()
                .uri(MOVIES_REVIEW_URL+"?movieInfoId={movieInfoId}", movieMovieInfoId)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Review.class)
                .consumeWith(response -> {
                            var savedReviews = response.getResponseBody();
                            assertThat(savedReviews).hasSize(3);
                        }
                );
    }
}
