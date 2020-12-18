package com.speech.demo.controller;

import com.speech.demo.service.SpeechService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
public class SpeechController {

    @Autowired
    private SpeechService speechService;

    @PostMapping("/text-to-speech")
    public String textToSpeech(@RequestBody Map<String, String> map) {

        String speech = null;
        try {
            speech = speechService.textToSpeech(map.get("text"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return speech;
    }

    @PostMapping("/speech-to-text")
    public String speechToText(@RequestParam("audio_data") MultipartFile file) {
        System.out.println("File : "+file.getName());
        String text = speechService.speechToText(file);
        return text;
    }
}
