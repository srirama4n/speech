package com.speech.demo.service;

import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.speech.v1.*;
import com.google.cloud.texttospeech.v1.*;
import com.google.protobuf.ByteString;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

@Service
public class SpeechService {

    public String textToSpeech(String text) throws IOException {
        ByteString audioContents = null;
        try (TextToSpeechClient textToSpeechClient = TextToSpeechClient.create()) {
            // Set the text input to be synthesized.
            SynthesisInput input = SynthesisInput.newBuilder().setText(text).build();

            // Build the voice request, select the language code ("en-US") and the ssml voice gender
            // ("neutral")
            VoiceSelectionParams voice =
                    VoiceSelectionParams.newBuilder()
                            .setLanguageCode("en-US")
                            .setSsmlGender(SsmlVoiceGender.NEUTRAL)
                            .build();

            // Select the type of audio file you want returned
            AudioConfig audioConfig =
                    AudioConfig.newBuilder().setAudioEncoding(AudioEncoding.LINEAR16).build();

            // Perform the text-to-speech request on the text input with the selected voice parameters and
            // audio file type
            SynthesizeSpeechResponse response =
                    textToSpeechClient.synthesizeSpeech(input, voice, audioConfig);

            // Get the audio contents from the response
            audioContents = response.getAudioContent();

            // Write the response to the output file.
            try (OutputStream out = new FileOutputStream("output.wav")) {
                out.write(audioContents.toByteArray());
                System.out.println("Audio content written to file \"output.wav\"");
            }
        }

//        System.out.println(audioContents.toByteArray());
        String encoded = Base64.getEncoder().encodeToString(audioContents.toByteArray());
        System.out.println(encoded);
        return encoded;
    }

    public String speechToText(String speech) {
        String text = "";
        String localFilePath = "C:\\Users\\dell\\Documents\\Apps\\demo\\output.wav";
        try (SpeechClient speechClient = SpeechClient.create()) {

            // The language of the supplied audio
            String languageCode = "en-US";

//            String decoded = new String(Base64.getDecoder().decode(encoded.getBytes()));
//            System.out.println(decoded);

            // Sample rate in Hertz of the audio data sent
            int sampleRateHertz = 24000;

//            String decoded = new String(Base64.getDecoder().decode(encoded.getBytes()));
//            System.out.println(decoded);

            // Encoding of audio data sent. This sample sets this explicitly.
            // This field is optional for FLAC and WAV audio formats.
            RecognitionConfig.AudioEncoding encoding = RecognitionConfig.AudioEncoding.LINEAR16;
            RecognitionConfig config =
                    RecognitionConfig.newBuilder()
                            .setLanguageCode(languageCode)
                            .setSampleRateHertz(sampleRateHertz)
                            .setEncoding(encoding)
                            .build();
            Path path = Paths.get(localFilePath);
            byte[] data = Files.readAllBytes(path);
            ByteString content = ByteString.copyFrom(data);
            RecognitionAudio audio = RecognitionAudio.newBuilder().setContent(content).build();
            LongRunningRecognizeRequest request =
                    LongRunningRecognizeRequest.newBuilder().setConfig(config).setAudio(audio).build();
            OperationFuture<LongRunningRecognizeResponse, LongRunningRecognizeMetadata> future =
                    speechClient.longRunningRecognizeAsync(request);

            System.out.println("Waiting for operation to complete...");
            LongRunningRecognizeResponse response = future.get();
            for (SpeechRecognitionResult result : response.getResultsList()) {
                // First alternative is the most probable result
                SpeechRecognitionAlternative alternative = result.getAlternativesList().get(0);
                System.out.printf("Transcript: %s\n", alternative.getTranscript());
                text = String.join( " ",text , alternative.getTranscript());
            }
        } catch (Exception exception) {
            System.err.println("Failed to create the client due to: " + exception);
        }
        return text;
    }

}
