/*
 * Main entry point for the Speech-to-Text Spring Boot application.
 * Starts the application and web server.
 */
package COMP3011.assignment1;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SpeechToTextAssignment1Application {

    public static void main(String[] args) {
        SpringApplication.run(SpeechToTextAssignment1Application.class, args);
    }
}