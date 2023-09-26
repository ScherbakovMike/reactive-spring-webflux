package com.reactivespring.moviesinfoservice.repository;

import com.reactivespring.moviesinfoservice.domain.MovieInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataMongoTest
@ActiveProfiles("test")
class MovieInfoRepositoryIntegrationTest {

    @Autowired
    MovieInfoRepository repository;

    @BeforeEach
    void setUp() {
        var moviesInfos = List.of(
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
                        LocalDate.parse("2008-07-18"))
        );
        repository.saveAll(moviesInfos)
                .blockLast();
    }

    @AfterEach
    void tearDown() {
        repository.deleteAll().block();
    }

    @Test
    void findAll() {
        var moviesInfoFlux = repository.findAll();
        StepVerifier.create(moviesInfoFlux)
                .expectNextCount(3)
                .verifyComplete();
    }

    @Test
    void findById() {
        var moviesInfoMono = repository.findById("abc");
        StepVerifier.create(moviesInfoMono)
                .assertNext(movieInfo -> assertEquals("Dark Knight Rises", movieInfo.getName()))
                .verifyComplete();
    }

    @Test
    void saveMovieInfo() {
        var movieInfo = new MovieInfo(null,
                "Batman Begins1",
                2005,
                List.of("Christian Bale", "Michael Cane"),
                LocalDate.parse("2005-06-15"));

        var moviesInfoMono = repository.save(movieInfo);
        StepVerifier.create(moviesInfoMono)
                .assertNext(movieInfo1 -> {
                    assertNotNull(movieInfo1.getMovieInfoId());
                    assertEquals("Batman Begins1", movieInfo1.getName());
                })
                .verifyComplete();
    }

    @Test
    void updateMovieInfo() {
        var movieInfo = repository.findById("abc").block();
        movieInfo.setYear(2021);

        var moviesInfoMono = repository.save(movieInfo);
        StepVerifier.create(moviesInfoMono)
                .assertNext(movieInfo1 -> assertEquals(2021, movieInfo1.getYear()))
                .verifyComplete();
    }

    @Test
    void deleteMovieInfo() {
        repository.deleteById("abc").block();
        StepVerifier.create(repository.findById("abc"))
                .expectNextCount(0)
                .verifyComplete();
    }
}
