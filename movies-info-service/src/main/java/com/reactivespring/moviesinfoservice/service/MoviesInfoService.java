package com.reactivespring.moviesinfoservice.service;

import com.reactivespring.moviesinfoservice.domain.MovieInfo;
import com.reactivespring.moviesinfoservice.repository.MovieInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class MoviesInfoService {

    private final MovieInfoRepository repository;

    public Mono<MovieInfo> addMovieInfo(MovieInfo movieInfo) {
        return repository.save(movieInfo);
    }

    public Flux<MovieInfo> getAll() {
        return repository.findAll();
    }

    public Mono<MovieInfo> getById(String id) {
        return repository.findById(id);
    }

    public Mono<MovieInfo> updateMovieInfo(MovieInfo movieInfo, String id) {
        return repository.findById(id)
                .flatMap(saved -> {
                    saved.setName(movieInfo.getName());
                    saved.setYear(movieInfo.getYear());
                    saved.setCast(movieInfo.getCast());
                    saved.setReleaseDate(movieInfo.getReleaseDate());
                    return repository.save(saved);
                });
    }

    public Mono<Void> deleteMovieInfo(String id) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(() -> new IllegalArgumentException("Not found")))
                .flatMap(saved -> repository.deleteById(id));
    }

    public Flux<MovieInfo> getByYear(Integer year) {
        return repository.findMovieInfoByYear(year);
    }

    public Flux<MovieInfo> getByName(String name) {
        return repository.findMovieInfoByName(name);
    }
}
