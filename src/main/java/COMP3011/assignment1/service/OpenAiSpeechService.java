package COMP3011.assignment1.service;

import java.io.IOException;
import java.util.Map;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

@Service
public class OpenAiSpeechService implements SpeechService {

    private static final String API_URL =
            "https://api.stt.ai";

    private static final String MODEL_NAME =
            "large-v3-turbo";

    private final RestClient apiClient;

    public OpenAiSpeechService() {

        this.apiClient = RestClient.builder()
                .baseUrl(API_URL)
                .build();
    }

    @Override
    public String transcribe(MultipartFile audio)
            throws IOException {

        String key = System.getenv("STT_API_KEY");

        if (key == null || key.isBlank()) {
            throw new IllegalStateException(
                    "STT_API_KEY is not set."
            );
        }

        if (audio == null || audio.isEmpty()) {
            throw new IllegalArgumentException(
                    "No audio file was received."
            );
        }

        String fileName = audio.getOriginalFilename();

        if (fileName == null || fileName.isBlank()) {
            fileName = "recording.webm";
        }

        String uploadFileName = fileName;

        ByteArrayResource audioResource =
                new ByteArrayResource(audio.getBytes()) {

            @Override
            public String getFilename() {
                return uploadFileName;
            }
        };

        MultipartBodyBuilder body =
                new MultipartBodyBuilder();

        body.part("file", audioResource)
                .contentType(
                        audio.getContentType() != null
                                ? MediaType.parseMediaType(
                                        audio.getContentType()
                                )
                                : MediaType.APPLICATION_OCTET_STREAM
                );

        body.part("model", MODEL_NAME);

        Map<?, ?> response = apiClient
                .post()
                .uri("/v1/transcribe")
                .header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + key
                )
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(body.build())
                .retrieve()
                .body(Map.class);

        if (response == null) {
            throw new IllegalStateException(
                    "STT.ai returned no response."
            );
        }

        Object text = response.get("text");

        if (text == null) {
            throw new IllegalStateException(
                    "STT.ai returned no transcription."
            );
        }

        return text.toString();
    }
}