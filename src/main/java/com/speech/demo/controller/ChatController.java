package com.speech.demo.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class ChatController {

    Map<String, String> intentData = new LinkedHashMap<>();

    ChatController(){
        intentData.put("Investments", "Ask me for Others or Company");
        intentData.put("Company", "Invested in abc company 10000$ and xyz company 40000$");
        intentData.put("Others", "Invested 20000$ ");
        intentData.put("Statements", "Ask me for Credit or Savings");
        intentData.put("Credit", "Automobile 100$, HotelBooking 200$");
        intentData.put("Savings", "Debited 100$, Credited 500$ ");
        intentData.put("Loans", "Ask me for Personal or House Loan");
        intentData.put("Personal", "OutStanding 2000$, EMI Per Month is 100$");
        intentData.put("House Loan", "OutStanding 20000$, EMI Per Month is 500$");
    }

    @PostMapping("/chat")
    public String chat(@RequestBody Map<String, String> map) {
        String defaultStr = "I am learning, I am happy to assist you on your Investments, Statements or Loans";
        String intentStr = map.get("intent").toLowerCase();

        String response = intentData.keySet()
                .stream()
                .filter(k -> intentStr.contains(k.toLowerCase()))
                .map(k -> intentData.get(k))
                .findFirst()
                .orElse(defaultStr);

        return response;
    }
}
