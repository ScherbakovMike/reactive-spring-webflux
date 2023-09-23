package com.learnreactiveprogramming.service;

import reactor.core.publisher.Flux;

import java.util.List;

public class FluxAndMonoGeneratorService {

    public Flux<String> namesFlux() {
        return Flux.fromIterable(List.of("alex", "ben", "chloe"));
    }

    public static void main(String[] args) {
        var service = new FluxAndMonoGeneratorService();
        service.namesFlux()
                .subscribe(name -> System.out.printf("Name is: %s%n", name));
    }
}
