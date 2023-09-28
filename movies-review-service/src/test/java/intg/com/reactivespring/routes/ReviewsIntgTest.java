package com.reactivespring.routes;

import com.reactivespring.domain.Review;
import com.reactivespring.repository.ReviewReactiveRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
class ReviewsIntgTest {

    private final String MOVIES_REVIEW_URL = "/v1/reviews";

    @Autowired
    WebTestClient client;

    @Autowired
    ReviewReactiveRepository repository;

    @BeforeEach
    void setUp() {
        var reviewList = List.of(
                new Review(null, 1L, "Awesome Movie", 9.0),
                new Review(null, 1L, "Awesome Movie1", 9.0),
                new Review(null, 2L, "Excellent Movie", 8.0)
        );
        repository.saveAll(reviewList)
                .blockLast();
    }

    @AfterEach
    void tearDown() {
        repository.deleteAll().block();
    }

    @Test
    void addReview() {
        var review = new Review(null, 1L, "Awesome Movie", 9.0);

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
    void getAllReviews() {
        client
                .get()
                .uri(MOVIES_REVIEW_URL)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Review.class)
                .consumeWith(response -> {
                            var savedReviews = response.getResponseBody();
                            assertThat(savedReviews).hasSize(3);
                        }
                );
    }

    @Test
    void getReviewById() {
        var review = new Review(null, 1L, "Awesome Movie", 9.0);

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

    @Test
    void updateReview() {
        var review = new Review(null, 1L, "Awesome Movie", 9.0);

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
}
