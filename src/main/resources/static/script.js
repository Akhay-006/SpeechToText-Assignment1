const startButton = document.getElementById("startButton");
const stopButton = document.getElementById("stopButton");
const statusText = document.getElementById("status");
const transcriptionText = document.getElementById("transcription");

let mediaRecorder;
let audioChunks = [];
let currentStream;

startButton.addEventListener("click", async () => {
    try {
        currentStream = await navigator.mediaDevices.getUserMedia({ audio: true });

        const mimeType = MediaRecorder.isTypeSupported("audio/webm")
            ? "audio/webm"
            : "";

        mediaRecorder = mimeType
            ? new MediaRecorder(currentStream, { mimeType })
            : new MediaRecorder(currentStream);

        audioChunks = [];

        mediaRecorder.ondataavailable = event => {
            if (event.data.size > 0) {
                audioChunks.push(event.data);
            }
        };

        mediaRecorder.onstop = uploadRecording;
        mediaRecorder.start();

        statusText.textContent = "Recording in progress...";
        transcriptionText.textContent = "";

        startButton.disabled = true;
        stopButton.disabled = false;

    } catch (error) {
        console.error("Microphone error:", error);
        statusText.textContent = "Unable to access the microphone.";
    }
});

stopButton.addEventListener("click", () => {
    if (mediaRecorder?.state === "recording") {
        mediaRecorder.stop();

        stopButton.disabled = true;
        statusText.textContent = "Processing...";
    }
});

async function uploadRecording() {
    try {
        const audioFile = new Blob(audioChunks, {
            type: mediaRecorder.mimeType || "audio/webm"
        });

        const formData = new FormData();
        formData.append("file", audioFile, "recording.webm");

        statusText.textContent = "Transcribing...";

        const response = await fetch("/api/v1/transcriptions", {
            method: "POST",
            body: formData
        });

        const result = await response.json();

        if (!response.ok) {
            throw new Error(result.message || "Transcription failed, try again.");
        }

        transcriptionText.textContent = result.text;
        statusText.textContent = "Finished";

    } catch (error) {
        console.error("Transcription error:", error);

        statusText.textContent = "Transcription failed.";
        transcriptionText.textContent =
            "API is unable to transcribe the audio recording.";

    } finally {
        currentStream?.getTracks().forEach(track => track.stop());

        audioChunks = [];
        startButton.disabled = false;
        stopButton.disabled = true;
    }
}