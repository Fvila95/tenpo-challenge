package com.tenpo.challenge.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tenpo.challenge.client.ExternalPercentageClient;
import com.tenpo.challenge.dto.PercentageCalculationDTO;
import com.tenpo.challenge.dto.RequestNumbersDTO;
import com.tenpo.challenge.model.PercentageCalculation;
import com.tenpo.challenge.repository.PercentageCalculationRepository;
import com.tenpo.challenge.utils.PageableResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PercentageCalculatorServiceTest {

    @Mock
    private ReactiveRedisTemplate<String, Double> redisTemplate;

    @Mock
    private ReactiveValueOperations<String, Double> valueOperations;

    @Mock
    private PercentageCalculationRepository percentageCalculationRepository;

    @Mock
    private ExternalPercentageClient externalPercentageClient;

    @InjectMocks
    private PercentageCalculatorService service;

    @BeforeEach
    void setUp() throws NoSuchFieldException, IllegalAccessException {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        lenient().when(valueOperations.set(any(String.class), any(Double.class), any())).thenReturn(Mono.empty());

        Field field = service.getClass().getDeclaredField("cacheDuration");
        field.setAccessible(true);
        field.set(service, 30);
    }

    @Test
    void testCalculatePercentage() {
        lenient().when(valueOperations.get(any())).thenReturn(Mono.empty());
        lenient().when(externalPercentageClient.getPercentage()).thenReturn(Mono.just(10.0));
        lenient().when(percentageCalculationRepository.save(any())).thenReturn(new PercentageCalculation(10.0, 1.0, 2.0, 3.3, Instant.now()));

        Mono<Double> response = service.calculatePercentage(new RequestNumbersDTO(1.0, 2.0));

        StepVerifier.create(response)
                .expectNext(3.3)
                .verifyComplete();
    }

    @Test
    void testFindAllCalculations() {
        String expectedJson = "{\"content\":[{\"id\":1,\"percentage\":10,\"firstNumber\":5,\"secondNumber\":5,\"result\":11,\"timestamp\":\"2023-07-26T19:10:36.443076Z\"},{\"id\":2,\"percentage\":10,\"firstNumber\":5,\"secondNumber\":5,\"result\":11,\"timestamp\":\"2023-07-26T19:19:24.294533Z\"},{\"id\":52,\"percentage\":10,\"firstNumber\":5,\"secondNumber\":5,\"result\":11,\"timestamp\":\"2023-07-26T19:28:26.154274Z\"},{\"id\":53,\"percentage\":10,\"firstNumber\":5,\"secondNumber\":5,\"result\":11,\"timestamp\":\"2023-07-26T19:28:27.867419Z\"},{\"id\":54,\"percentage\":10,\"firstNumber\":5,\"secondNumber\":5,\"result\":11,\"timestamp\":\"2023-07-26T19:28:28.699331Z\"},{\"id\":55,\"percentage\":10,\"firstNumber\":5,\"secondNumber\":5,\"result\":11,\"timestamp\":\"2023-07-26T19:28:29.201931Z\"},{\"id\":56,\"percentage\":10,\"firstNumber\":5,\"secondNumber\":5,\"result\":11,\"timestamp\":\"2023-07-26T19:28:31.143289Z\"},{\"id\":57,\"percentage\":10,\"firstNumber\":5,\"secondNumber\":5,\"result\":11,\"timestamp\":\"2023-07-26T19:28:31.738722Z\"},{\"id\":102,\"percentage\":10,\"firstNumber\":5,\"secondNumber\":5,\"result\":11,\"timestamp\":\"2023-07-26T19:40:27.415280Z\"},{\"id\":103,\"percentage\":10,\"firstNumber\":5,\"secondNumber\":5,\"result\":11,\"timestamp\":\"2023-07-26T19:40:28.161514Z\"}],\"pageable\":{\"sort\":{\"empty\":true,\"sorted\":false,\"unsorted\":true},\"offset\":0,\"pageNumber\":0,\"pageSize\":10,\"paged\":true,\"unpaged\":false},\"last\":false,\"totalPages\":3,\"totalElements\":26,\"first\":true,\"size\":10,\"number\":0,\"sort\":{\"empty\":true,\"sorted\":false,\"unsorted\":true},\"numberOfElements\":10,\"empty\":false}";
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.registerModule(new JavaTimeModule());
        PageableResponse<PercentageCalculation> page = null;
        try {
            page = mapper.readValue(expectedJson, mapper.getTypeFactory().constructParametricType(PageableResponse.class, PercentageCalculation.class));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        when(percentageCalculationRepository.findAll(any(PageRequest.class)))
                .thenReturn( page );

        Mono<Page<PercentageCalculationDTO>> response = service.findAllCalculations(0, 10);

        StepVerifier.create(response)
                .assertNext(percentageCalculationPage -> {
                    List<PercentageCalculationDTO> content = percentageCalculationPage.getContent();
                    assertEquals(10, content.size());

                    PercentageCalculationDTO firstCalculation = content.get(0);
                    assertEquals(10, firstCalculation.getPercentage());
                    assertEquals(5, firstCalculation.getFirstNumber());
                    assertEquals(5, firstCalculation.getSecondNumber());
                    assertEquals(11, firstCalculation.getResult());
                    assertEquals(Instant.parse("2023-07-26T19:10:36.443076Z"), firstCalculation.getTimestamp());

                })
                .verifyComplete();
    }
}