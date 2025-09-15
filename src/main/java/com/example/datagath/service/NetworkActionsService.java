package com.example.datagath.service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;

import jakarta.mail.MessagingException;

public class NetworkActionsService {

    public final EmailServiceImpl emailService;

    public NetworkActionsService(EmailServiceImpl emailService) {
        this.emailService = emailService;
    }

    public static String llm_response(String model, String apikey, String prompt)
            throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        ObjectMapper mapper = new ObjectMapper();
        String AI_RESPONSE = new String();

        switch (model) {
            case "OPENAI":
                // 1. Get model list
                String endpoint = "https://api.openai.com/v1/models";
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(endpoint))
                        .header("Authorization", "Bearer " + apikey)
                        .GET()
                        .build();

                HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
                JsonNode root = mapper.readTree(response.body());

                JsonNode dataArray = root.get("data");
                String modelName = dataArray.get(0).get("id").asText();

                // 2. Build chat request
                String jsonBody = """
                        {
                          "model": "%s",
                          "messages": [
                            {"role": "user", "content": "%s"}
                          ]
                        }
                        """.formatted(modelName, prompt);

                endpoint = "https://api.openai.com/v1/chat/completions";
                request = HttpRequest.newBuilder()
                        .uri(URI.create(endpoint))
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + apikey)
                        .POST(BodyPublishers.ofString(jsonBody))
                        .build();

                response = client.send(request, BodyHandlers.ofString());
                root = mapper.readTree(response.body());

                if (root.has("choices") && root.get("choices").size() > 0) {
                    JsonNode firstChoice = root.get("choices").get(0);
                    AI_RESPONSE = firstChoice.get("message").get("content").asText();
                } else {
                    AI_RESPONSE = "No response from assistant";
                }
                break;
            case "ANTHROPIC":
                // 1. Get available models (Anthropic doesn't have a models endpoint, so we'll
                // use a default model)
                String anthropicModel = "claude-3-sonnet-20240229"; // Default model, could be made configurable

                // 2. Build chat request for Anthropic API
                String anthropicJsonBody = """
                        {
                          "model": "%s",
                          "max_tokens": 1000,
                          "messages": [
                            {"role": "user", "content": "%s"}
                          ]
                        }
                        """.formatted(anthropicModel, prompt);

                String anthropicEndpoint = "https://api.anthropic.com/v1/messages";
                HttpRequest anthropicRequest = HttpRequest.newBuilder()
                        .uri(URI.create(anthropicEndpoint))
                        .header("Content-Type", "application/json")
                        .header("x-api-key", apikey)
                        .header("anthropic-version", "2023-06-01")
                        .POST(BodyPublishers.ofString(anthropicJsonBody))
                        .build();

                HttpResponse<String> anthropicResponse = client.send(anthropicRequest, BodyHandlers.ofString());
                JsonNode anthropicRoot = mapper.readTree(anthropicResponse.body());

                if (anthropicRoot.has("content") && anthropicRoot.get("content").size() > 0) {
                    JsonNode firstContent = anthropicRoot.get("content").get(0);
                    if (firstContent.has("text")) {
                        AI_RESPONSE = firstContent.get("text").asText();
                    } else {
                        AI_RESPONSE = "No text content in response";
                    }
                } else {
                    AI_RESPONSE = "No response from assistant";
                }
                break;
            case "GOOGLE":
                // 1. Set a default model (like gemini-pro), as Google's API doesn't require a
                // separate models list call.
                String googleModel = "gemini-pro";

                // 2. Build the chat request for the Google Gemini API.
                String googleJsonBody = """
                        {
                         "contents": [
                          {
                           "parts": [
                            {
                             "text": "%s"
                            }
                           ]
                          }
                         ]
                        }
                        """.formatted(prompt);

                String googleEndpoint = "https://generativelanguage.googleapis.com/v1beta/models/" + googleModel
                        + ":generateContent?key=" + apikey;
                HttpRequest googleRequest = HttpRequest.newBuilder()
                        .uri(URI.create(googleEndpoint))
                        .header("Content-Type", "application/json")
                        .POST(BodyPublishers.ofString(googleJsonBody))
                        .build();

                HttpResponse<String> googleResponse = client.send(googleRequest, BodyHandlers.ofString());
                JsonNode googleRoot = mapper.readTree(googleResponse.body());

                if (googleRoot.has("candidates") && googleRoot.get("candidates").size() > 0) {
                    JsonNode firstCandidate = googleRoot.get("candidates").get(0);
                    if (firstCandidate.has("content")
                            && firstCandidate.get("content").get("parts").size() > 0) {
                        AI_RESPONSE = firstCandidate.get("content").get("parts").get(0).get("text").asText();
                    } else {
                        AI_RESPONSE = "No text content in response";
                    }
                } else {
                    AI_RESPONSE = "No response from assistant";
                }
                break;
            case "META":
                // 1. Get available models (Meta Llama API doesn't have a public models
                // endpoint, so we'll use a default model)
                String llamaModel = "llama-3.1-70b-instruct"; // Default model, could be made configurable

                // 2. Build chat request for Meta Llama API
                String llamaJsonBody = """
                        {
                          "model": "%s",
                          "messages": [
                            {"role": "user", "content": "%s"}
                          ],
                          "max_tokens": 1000,
                          "temperature": 0.7
                        }
                        """.formatted(llamaModel, prompt);

                String llamaEndpoint = "https://api.llama.com/v1/chat/completions";
                HttpRequest llamaRequest = HttpRequest.newBuilder()
                        .uri(URI.create(llamaEndpoint))
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + apikey)
                        .POST(BodyPublishers.ofString(llamaJsonBody))
                        .build();

                HttpResponse<String> llamaResponse = client.send(llamaRequest, BodyHandlers.ofString());
                JsonNode llamaRoot = mapper.readTree(llamaResponse.body());

                if (llamaRoot.has("choices") && llamaRoot.get("choices").size() > 0) {
                    JsonNode firstChoice = llamaRoot.get("choices").get(0);
                    if (firstChoice.has("message") && firstChoice.get("message").has("content")) {
                        AI_RESPONSE = firstChoice.get("message").get("content").asText();
                    } else {
                        AI_RESPONSE = "No message content in response";
                    }
                } else {
                    AI_RESPONSE = "No response from assistant";
                }
                break;

        }
        return AI_RESPONSE;
    }

    public static void sendResponse(String response, String sendAddress) {

    }

    public void sendResponse(byte[] document, String sendAddress)
            throws FileNotFoundException, DocumentException, MessagingException {

        emailService.sendMessageWithAttachment("iljazaicevs@outlook.com", "test", "this is an email test", document);

    }

}
