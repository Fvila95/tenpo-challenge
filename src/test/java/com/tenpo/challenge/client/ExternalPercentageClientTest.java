package com.tenpo.challenge.client;

import static org.mockito.Mockito.*;

import com.tenpo.challenge.exception.ExternalPercentageException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class ExternalPercentageClientTest {

    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    private ExternalPercentageClient externalPercentageClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        WebClient webClient = mock(WebClient.class);
        when(webClientBuilder.baseUrl(anyString())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(webClient);

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/percentage")).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);

        externalPercentageClient = new ExternalPercentageClient(webClientBuilder);
    }

    @Test
    void testGetPercentage_Success() {
        Double expectedPercentage = 10.0;
        when(responseSpec.bodyToMono(Double.class)).thenReturn(Mono.just(expectedPercentage));

        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);

        Mono<Double> result = externalPercentageClient.getPercentage();

        StepVerifier.create(result)
                .expectNext(expectedPercentage)
                .verifyComplete();
    }

    @Test
    void testGetPercentage_4xxError() {
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Double.class)).thenReturn(Mono.error(new ExternalPercentageException("4xx error occurred while making the request")));

        Mono<Double> result = externalPercentageClient.getPercentage();

        StepVerifier.create(result).expectErrorMatches(throwable ->
                throwable instanceof ExternalPercentageException &&
                        throwable.getMessage().equals("4xx error occurred while making the request")
        ).verify();
    }

    @Test
    void testGetPercentage_5xxError() {
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Double.class)).thenReturn(Mono.error(new ExternalPercentageException("5xx error occurred while making the request")));

        Mono<Double> result = externalPercentageClient.getPercentage();

        StepVerifier.create(result).expectErrorMatches(throwable ->
                throwable instanceof ExternalPercentageException &&
                        throwable.getMessage().equals("5xx error occurred while making the request")
        ).verify();
    }
}
