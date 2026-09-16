/*
 * Defines the speech transcription service.
 * Allows controllers to use transcription without depending on a specific implementation.
 */
package COMP3011.assignment1.service;

import java.io.IOException;
import org.springframework.web.multipart.MultipartFile;

public interface SpeechService {

    String transcribe(MultipartFile audio) throws IOException;
}