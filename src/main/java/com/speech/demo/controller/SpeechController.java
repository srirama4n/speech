package com.speech.demo.controller;

import com.speech.demo.service.SpeechService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

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
    public String speechToText(@RequestBody Map<String, String> map) {
        String text = speechService.speechToText(map.get("speech"));
        return text;
    }
}
