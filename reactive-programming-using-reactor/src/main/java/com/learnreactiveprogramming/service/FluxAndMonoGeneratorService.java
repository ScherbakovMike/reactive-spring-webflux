package com.learnreactiveprogramming.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Random;

public class FluxAndMonoGeneratorService {

    public Flux<String> namesFlux() {
        return Flux.fromIterable(List.of("alex", "ben", "chloe")).log();
    }

    public Mono<String> nameMono() {
        return Mono.just("alex");
    }

    public Flux<String> namesFlux_map(int stringLength) {
        return namesFlux().map(String::toUpperCase)
                .filter(item -> item.length() > stringLength);
    }

    public Flux<String> namesFlux_flatMap(int stringLength) {
        return namesFlux().map(String::toUpperCase)
                .filter(item -> item.length() > stringLength)
                .flatMap(this::splitString);
    }

    public Flux<String> namesFlux_flatMap_async(int stringLength) {
        return namesFlux().map(String::toUpperCase)
                .filter(item -> item.length() > stringLength)
                .flatMap(this::splitString_withDelay)
                .log();
    }

    public Flux<String> splitString(String name) {
        var array = name.split("");
        return Flux.fromArray(array);
    }

    public Flux<String> splitString_withDelay(String name) {
        var array = name.split("");
        var delay = new Random().nextInt(1000);
        return Flux.fromArray(array).delayElements(Duration.ofMillis(delay));
    }

    public Flux<String> namesFlux_immutability() {
        return namesFlux().map(String::toUpperCase);
    }

    public static void main(String[] args) {
        var service = new FluxAndMonoGeneratorService();
        service.namesFlux()
                .subscribe(name -> System.out.printf("Name is: %s%n", name));
        service.nameMono()
                .subscribe(name -> System.out.printf("Mono name is: %s%n", name));
    }
}
