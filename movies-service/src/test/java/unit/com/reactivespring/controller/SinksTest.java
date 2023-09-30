package com.reactivespring.controller;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Sinks;

class SinksTest {

    @Test
    void sink() {
        Sinks.Many<Integer> replaySink = Sinks.many().replay().all();

        replaySink.emitNext(1, Sinks.EmitFailureHandler.FAIL_FAST);
        replaySink.emitNext(2, Sinks.EmitFailureHandler.FAIL_FAST);

        var integerFlux = replaySink.asFlux();
        integerFlux.subscribe(i->
                System.out.println("Subscriber 1 : " + i));

        replaySink.tryEmitNext(3);
    }

    @Test
    void sink_multicast() {
        Sinks.Many<Integer> multicast = Sinks.many().multicast().onBackpressureBuffer();

        multicast.emitNext(1, Sinks.EmitFailureHandler.FAIL_FAST);
        multicast.emitNext(2, Sinks.EmitFailureHandler.FAIL_FAST);

        var integerFlux = multicast.asFlux();
        integerFlux.subscribe(i->
                System.out.println("Subscriber 1 : " + i));

        var integerFlux2 = multicast.asFlux();
        integerFlux2.subscribe(i->
                System.out.println("Subscriber 2 : " + i));

        multicast.tryEmitNext(3);
    }

    @Test
    void sink_unicast() {
        Sinks.Many<Integer> multicast = Sinks.many().unicast().onBackpressureBuffer();

        multicast.emitNext(1, Sinks.EmitFailureHandler.FAIL_FAST);
        multicast.emitNext(2, Sinks.EmitFailureHandler.FAIL_FAST);

        var integerFlux = multicast.asFlux();
        integerFlux.subscribe(i->
                System.out.println("Subscriber 1 : " + i));

//        var integerFlux2 = multicast.asFlux();
//        integerFlux2.subscribe(i->
//                System.out.println("Subscriber 2 : " + i));

        multicast.tryEmitNext(3);
    }

}

