package com.reactivespring.moviesinfoservice.controller;

import com.reactivespring.moviesinfoservice.domain.MovieInfo;
import com.reactivespring.moviesinfoservice.service.MoviesInfoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class MoviesInfoController {

    private final MoviesInfoService service;

    @GetMapping("/movieinfos")
    public Flux<MovieInfo> getAll(
            @RequestParam(value = "year", required = false) Integer year,
            @RequestParam(value = "name", required = false) String name
    ) {
        if (year != null) {
            return service.getByYear(year);
        }
        if (name!=null) {
            return service.getByName(name);
        }
        return service.getAll();
    }

    @GetMapping("/movieinfos/{id}")
    public Mono<ResponseEntity<MovieInfo>> getById(@PathVariable String id) {
        return service.getById(id)
                .map(ResponseEntity.ok()::body)
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }

    @PostMapping("/movieinfos")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<MovieInfo> create(@RequestBody @Valid MovieInfo movieInfo) {
        return service.addMovieInfo(movieInfo);
    }

    @PutMapping("/movieinfos/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Mono<ResponseEntity<MovieInfo>> create(@RequestBody @Valid MovieInfo movieInfo, @PathVariable String id) {
        return service.updateMovieInfo(movieInfo, id)
                .map(ResponseEntity.ok()::body)
                .switchIfEmpty(
                        Mono.just(ResponseEntity.notFound().build())
                );
    }

    @DeleteMapping("/movieinfos/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> delete(@PathVariable String id) {
        return service.deleteMovieInfo(id);
    }
}
