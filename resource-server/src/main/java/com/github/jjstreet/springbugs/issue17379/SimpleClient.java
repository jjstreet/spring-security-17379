package com.github.jjstreet.springbugs.issue17379;

import io.netty.channel.ConnectTimeoutException;
import io.netty.handler.timeout.TimeoutException;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.util.retry.Retry;

import java.time.Duration;

public class SimpleClient {

    private final WebClient webClient;
    private final Retry retry;

    /**
     * Retryable responses are those that are 401, 403, 5xx, and timeouts.
     */
    static boolean defaultIsRetryable(Throwable thrown) {
        var tested = thrown;
        if (thrown.getCause() != null && (
                thrown instanceof WebClientResponseException || thrown instanceof WebClientRequestException)) {
            tested = thrown.getCause();
        }
        return tested instanceof WebClientResponseException.InternalServerError
                || tested instanceof WebClientResponseException.Unauthorized
                || tested instanceof WebClientResponseException.ServiceUnavailable
                || tested instanceof TimeoutException
                || tested instanceof ConnectTimeoutException;
    }

    public SimpleClient(
            WebClient webClient) {
        this.webClient = webClient;
        this.retry = Retry
                .fixedDelay(3, Duration.ofMillis(500))
                .filter(SimpleClient::defaultIsRetryable);
    }

    public String getValue() {
        return webClient
                .get()
                .uri("/protected/value")
                .retrieve()
                .bodyToMono(String.class)
                .retryWhen(retry)
                .block();
    }
}
