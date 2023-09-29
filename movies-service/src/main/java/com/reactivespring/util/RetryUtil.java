package com.reactivespring.util;

import com.reactivespring.exception.MoviesInfoClientException;
import lombok.experimental.UtilityClass;
import reactor.core.Exceptions;
import reactor.util.retry.Retry;

import java.time.Duration;

@UtilityClass
public class RetryUtil {

    public static Retry retrySpec() {
        return Retry.fixedDelay(3, Duration.ofSeconds(1))
                .filter(MoviesInfoClientException.class::isInstance)
                .onRetryExhaustedThrow(((retryBackoffSpec, retrySignal) -> Exceptions.propagate(retrySignal.failure())));
    }
}
