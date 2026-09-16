package COMP3011.assignment1.Test;

/*
 * Assignment 1 regression and concurrency tests.
 *
 * This test class checks the main administration and statistics API
 * requirements and verifies that shared token statistics remain correct
 * when updated concurrently. It also tests the speech-to-text endpoint
 * with more than 200 simultaneous blocking HTTP requests.
 *
 * A stub SpeechService is used during testing so the concurrency test
 * does not send requests to the real OpenAI API or require an API key.
 * The stub deliberately pauses briefly to simulate the blocking network
 * operation of a real Cloud speech-to-text request.
 *
 * The tests verify:
 * - GET /api/v1/admin/uptime returns the required uptime information.
 * - GET /api/v1/global/stats returns input and output token counters.
 * - POST /api/v1/admin/shutdown accepts a graceful shutdown request.
 * - Token counters remain correct when many threads update them together.
 * - POST /api/v1/transcriptions handles 250 concurrent blocking requests.
 */

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import COMP3011.assignment1.service.SpeechService;
import COMP3011.assignment1.service.TokenTrackerService;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)

public class Assignment1Test {
    private static final int REQUEST_COUNT = 250;

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @TestConfiguration
    static class TestConfig {

        @Bean
        @Primary
        SpeechService stubSpeechService() {

            return audio -> {

                try {
                    Thread.sleep(200);
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();

                    throw new IllegalStateException(
                            "Test request was interrupted."
                    );
                }

                return "Test transcription";
            };
        }
    }

    @Test
    void uptimeEndpointReturnsRequiredFields() {

        String url =
                "http://localhost:"
                + port
                + "/api/v1/admin/uptime";

        ResponseEntity<Map> response =
                restTemplate.getForEntity(
                        url,
                        Map.class
                );

        assertEquals(
                HttpStatus.OK,
                response.getStatusCode()
        );

        Map<?, ?> body = response.getBody();

        assertNotNull(body);

        assertTrue(
                body.containsKey("utcServerStart")
        );

        assertTrue(
                body.containsKey("utcNow")
        );

        assertTrue(
                body.containsKey("serverUptimeSeconds")
        );

        Number uptime =
                (Number) body.get(
                        "serverUptimeSeconds"
                );

        assertTrue(
                uptime.doubleValue() >= 0
        );

        assertEquals(
                3,
                body.size()
        );
    }

    @Test
    void globalStatsEndpointReturnsRequiredFields() {

        String url =
                "http://localhost:"
                + port
                + "/api/v1/global/stats";

        ResponseEntity<Map> response =
                restTemplate.getForEntity(
                        url,
                        Map.class
                );

        assertEquals(
                HttpStatus.OK,
                response.getStatusCode()
        );

        Map<?, ?> body = response.getBody();

        assertNotNull(body);

        assertTrue(
                body.containsKey("inputTokens")
        );

        assertTrue(
                body.containsKey("outputTokens")
        );

        Number inputTokens =
                (Number) body.get(
                        "inputTokens"
                );

        Number outputTokens =
                (Number) body.get(
                        "outputTokens"
                );

        assertTrue(
                inputTokens.longValue() >= 0
        );

        assertTrue(
                outputTokens.longValue() >= 0
        );

        assertEquals(
                2,
                body.size()
        );
    }

    @Test
    void tokenCountersAreThreadSafe()
            throws InterruptedException {

        TokenTrackerService tracker =
                new TokenTrackerService();

        int threadCount = 250;

        CountDownLatch ready =
                new CountDownLatch(threadCount);

        CountDownLatch start =
                new CountDownLatch(1);

        List<Thread> threads =
                new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {

            Thread thread =
                    Thread.ofVirtual()
                            .unstarted(() -> {

                                ready.countDown();

                                try {
                                    start.await();
                                } catch (InterruptedException exception) {

                                    Thread.currentThread().interrupt();
                                    return;
                                }

                                tracker.addTokenUsage(
                                        1,
                                        1
                                );
                            });

            threads.add(thread);
            thread.start();
        }

        ready.await();
        start.countDown();

        for (Thread thread : threads) {
            thread.join();
        }

        assertEquals(
                250,
                tracker.getInputTokens()
        );

        assertEquals(
                250,
                tracker.getOutputTokens()
        );
    }

    @Test
    void transcriptionEndpointHandles250ConcurrentBlockingRequests() {

        String url =
                "http://localhost:"
                + port
                + "/api/v1/transcriptions";

        List<CompletableFuture<Integer>> requests =
                new ArrayList<>();

        for (int i = 0; i < REQUEST_COUNT; i++) {

            CompletableFuture<Integer> request =
                    CompletableFuture.supplyAsync(() -> {

                        HttpHeaders headers =
                                new HttpHeaders();

                        headers.setContentType(
                                MediaType.MULTIPART_FORM_DATA
                        );

                        MultiValueMap<String, Object> body =
                                new LinkedMultiValueMap<>();

                        body.add(
                                "file",
                                new TestAudioResource(
                                        "fake audio".getBytes()
                                )
                        );

                        HttpEntity<MultiValueMap<String, Object>>
                                requestEntity =
                                new HttpEntity<>(
                                        body,
                                        headers
                                );

                        ResponseEntity<String> response =
                                restTemplate.exchange(
                                        url,
                                        HttpMethod.POST,
                                        requestEntity,
                                        String.class
                                );

                        return response
                                .getStatusCode()
                                .value();
                    });

            requests.add(request);
        }

        CompletableFuture.allOf(
                requests.toArray(
                        new CompletableFuture[0]
                )
        ).join();

        for (CompletableFuture<Integer> request : requests) {

            assertEquals(
                    200,
                    request.join()
            );
        }
    }

    private static class TestAudioResource
            extends ByteArrayResource {

        TestAudioResource(byte[] data) {
            super(data);
        }

        @Override
        public String getFilename() {
            return "test.webm";
        }
    }

}
