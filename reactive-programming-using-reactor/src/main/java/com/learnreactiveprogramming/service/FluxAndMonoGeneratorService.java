package com.learnreactiveprogramming.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

public class FluxAndMonoGeneratorService {

    public Flux<String> namesFlux() {
        return Flux.fromIterable(List.of("alex", "ben", "chloe")).log();
    }

    public Mono<String> nameMono() {
        return Mono.just("alex");
    }

    public Mono<String> namesMono(int stringLength) {
        return Mono.just("alex")
                .map(String::toUpperCase)
                .filter(s -> s.length() > stringLength);
    }

    public Mono<List<String>> namesMono_flatMap(int stringLength) {
        return Mono.just("alex")
                .filter(s -> s.length() > stringLength)
                .map(String::toUpperCase)
                .flatMap(this::splitStringMono);
    }

    public Flux<String> namesMono_flatMapMany(int stringLength) {
        return Mono.just("alex")
                .filter(s -> s.length() > stringLength)
                .map(String::toUpperCase)
                .flatMapMany(this::splitString);
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

    public Flux<String> namesFlux_transform(int stringLength) {
        Function<Flux<String>, Flux<String>> filterMap =
                name -> name.map(String::toUpperCase)
                        .filter(item -> item.length() > stringLength);
        return namesFlux()
                .transform(filterMap)
                .concatMap(this::splitString)
                .defaultIfEmpty("default")
                .log();
    }

    public Flux<String> namesFlux_transform_switchIfEmpty(int stringLength) {
        Function<Flux<String>, Flux<String>> filterMap =
                name -> name.map(String::toUpperCase)
                        .filter(item -> item.length() > stringLength)
                        .concatMap(this::splitString);
        var defaultFlux = Flux.just("default")
                .transform(filterMap);
        return namesFlux()
                .transform(filterMap)
                .switchIfEmpty(defaultFlux)
                .log();
    }

    public Flux<String> explore_concat() {
        var abcFlux = Flux.just("A", "B", "C");
        var defFlux = Flux.just("D", "E", "F");
        return Flux.concat(abcFlux, defFlux);
    }

    public Flux<String> explore_concatWith() {
        var abcFlux = Flux.just("A", "B", "C");
        var defFlux = Flux.just("D", "E", "F");
        return abcFlux.concatWith(defFlux);
    }

    public Flux<String> explore_concatWithMono() {
        var aMono = Flux.just("A");
        var defFlux = Flux.just("D", "E", "F");
        return aMono.concatWith(defFlux);
    }

    public Flux<String> explore_mergeWith() {
        var abcFlux = Flux.just("A", "B", "C").delayElements(Duration.ofMillis(100));
        var defFlux = Flux.just("D", "E", "F").delayElements(Duration.ofMillis(125));
        return abcFlux.mergeWith(defFlux);
    }

    public Flux<String> explore_mergeWithSequential() {
        var abcFlux = Flux.just("A", "B", "C").delayElements(Duration.ofMillis(100));
        var defFlux = Flux.just("D", "E", "F").delayElements(Duration.ofMillis(125));
        return Flux.mergeSequential(abcFlux, defFlux);
    }

    public Flux<String> explore_zip() {
        var abcFlux = Flux.just("A", "B", "C").delayElements(Duration.ofMillis(100));
        var defFlux = Flux.just("D", "E", "F").delayElements(Duration.ofMillis(125));
        return Flux.zip(abcFlux, defFlux, (first, second) -> first + second);
    }

    public Flux<String> explore_zip_1() {
        var abcFlux = Flux.just("A", "B", "C");
        var defFlux = Flux.just("D", "E", "F");
        var _123Flux = Flux.just(1, 2, 3);
        var _456Flux = Flux.just(4, 5, 6);
        return Flux.zip(abcFlux, defFlux, _123Flux, _456Flux)
                .map(t4 -> t4.getT1() + t4.getT2() + t4.getT3() + t4.getT4());
    }

    public Flux<String> explore_zip_3() {
        var abcFlux = Flux.just("A", "B", "C").delayElements(Duration.ofMillis(100));
        var defFlux = Flux.just("D", "E", "F").delayElements(Duration.ofMillis(125));
        return abcFlux.zipWith(defFlux, (first, second) -> first + second);
    }

    public Mono<String> explore_zip_mono() {
        var aMono = Mono.just("A");
        var bMono = Mono.just("B");
        return aMono.zipWith(bMono).map(t2 -> t2.getT1() + t2.getT2());
    }

    public Flux<String> namesFlux_flatMap_async(int stringLength) {
        return namesFlux().map(String::toUpperCase)
                .filter(item -> item.length() > stringLength)
                .flatMap(this::splitString_withDelay)
                .log();
    }

    public Flux<String> namesFlux_concatMap_async(int stringLength) {
        return namesFlux().map(String::toUpperCase)
                .filter(item -> item.length() > stringLength)
                .concatMap(this::splitString_withDelay)
                .log();
    }

    public Flux<String> splitString(String name) {
        var array = name.split("");
        //var delay = new Random().nextInt(1000);
        var delay = 1000;
        return Flux.fromArray(array).delayElements(Duration.ofMillis(delay));
    }

    public Mono<List<String>> splitStringMono(String name) {
        var array = name.split("");
        //var delay = new Random().nextInt(1000);
        var delay = 1000;
        return Mono.just(List.of(array));
    }

    public Flux<String> splitString_withDelay(String name) {
        var array = name.split("");
        return Flux.fromArray(array);
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
