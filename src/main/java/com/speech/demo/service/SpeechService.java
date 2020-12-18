package com.speech.demo.service;

import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.speech.v1.*;
import com.google.cloud.texttospeech.v1.*;
import com.google.protobuf.ByteString;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Base64;

@Service
public class SpeechService {

    public String textToSpeech(String text) throws IOException {
        ByteString audioContents = null;
        try (TextToSpeechClient textToSpeechClient = TextToSpeechClient.create()) {
            // Set the text input to be synthesized.
            SynthesisInput input = SynthesisInput.newBuilder().setText(text).build();

            VoiceSelectionParams voice =
                    VoiceSelectionParams.newBuilder()
                            .setLanguageCode("en-US")
                            .setSsmlGender(SsmlVoiceGender.NEUTRAL)
                            .build();
            AudioConfig audioConfig =
                    AudioConfig.newBuilder().setAudioEncoding(AudioEncoding.LINEAR16).build();
            SynthesizeSpeechResponse response =
                    textToSpeechClient.synthesizeSpeech(input, voice, audioConfig);
            audioContents = response.getAudioContent();
            try (OutputStream out = new FileOutputStream("output.wav")) {
                out.write(audioContents.toByteArray());
//                System.out.println("Audio content written to file \"output.wav\"");
            }
        }
        String encoded = Base64.getEncoder().encodeToString(audioContents.toByteArray());
//        System.out.println(encoded);
        return encoded;
    }

    public String speechToText(MultipartFile multipartFile) {
        String text = "";
        try (SpeechClient speechClient = SpeechClient.create()) {
            String languageCode = "en-US";
            int sampleRateHertz = 48000;

            RecognitionConfig.AudioEncoding encoding = RecognitionConfig.AudioEncoding.LINEAR16;
            RecognitionConfig config =
                    RecognitionConfig.newBuilder()
                            .setLanguageCode(languageCode)
                            .setSampleRateHertz(sampleRateHertz)
                            .setEncoding(encoding)
                            .build();
            ByteString content = ByteString.copyFrom(multipartFile.getBytes());
            RecognitionAudio audio = RecognitionAudio.newBuilder().setContent(content).build();
            LongRunningRecognizeRequest request =
                    LongRunningRecognizeRequest.newBuilder().setConfig(config).setAudio(audio).build();
            OperationFuture<LongRunningRecognizeResponse, LongRunningRecognizeMetadata> future =
                    speechClient.longRunningRecognizeAsync(request);

//            System.out.println("Waiting for operation to complete...");
            LongRunningRecognizeResponse response = future.get();
            for (SpeechRecognitionResult result : response.getResultsList()) {
                // First alternative is the most probable result
                SpeechRecognitionAlternative alternative = result.getAlternativesList().get(0);
                text = String.join(" ", text, alternative.getTranscript());
//                System.out.printf("Transcript: %s\n", text);
            }
        } catch (Exception exception) {
            System.err.println("Failed to create the client due to: " + exception);
        }
        return text;
    }

}
