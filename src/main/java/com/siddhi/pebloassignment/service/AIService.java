package com.siddhi.pebloassignment.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.siddhi.pebloassignment.dto.AiSummaryResponse;
import com.siddhi.pebloassignment.model.Note;
import com.siddhi.pebloassignment.repository.NoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
public class AIService {

    @Autowired
    private NoteRepository noteRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final String geminiApiKey = "AIzaSyB7zbVw6KpTSa9qlVbTUt3B5kf4W6whzDU";
    private final String geminiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + geminiApiKey;

    public AiSummaryResponse generateNoteSummary(String noteId, String email) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new RuntimeException("Note context not found"));

        if (!note.getUser().getEmail().equals(email)) {
            throw new RuntimeException("Unauthorized workspace access");
        }

        String systemInstruction = "You are an expert AI productivity assistant. Analyze the note text provided. " +
                "You MUST respond ONLY with a raw JSON object matching this structure exactly, with no markdown formatting, no code blocks, and no extra prose: " +
                "{ \"summary\": \"string\", \"actionItems\": [\"string\"], \"suggestedTitle\": \"string\" }";

        String userContent = "Note Title: " + note.getTitle() + "\nNote Content:\n" + note.getContent();

        Map<String, Object> requestBody = Map.of(
                "contents", new Object[]{
                        Map.of("parts", new Object[]{
                                Map.of("text", systemInstruction + "\n\nAnalyze this note content:\n" + userContent)
                        })
                }
        );

        try {
            RestClient restClient = RestClient.create();

            String rawJsonResponse = restClient.post()
                    .uri(geminiUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            String aiResponseText = objectMapper.readTree(rawJsonResponse)
                    .path("candidates").get(0)
                    .path("content").path("parts").get(0)
                    .path("text").asText().trim();

            if (aiResponseText.startsWith("```")) {
                aiResponseText = aiResponseText.replaceAll("```json|```", "").trim();
            }

            return objectMapper.readValue(aiResponseText, AiSummaryResponse.class);

        } catch (JsonProcessingException e) {
            System.err.println("JSON Parsing failure: " + e.getMessage());
            throw new RuntimeException("Failed to process AI JSON response structure.");
        } catch (Exception e) {
            System.err.println("Gemini API Engine failure: " + e.getMessage());
            throw new RuntimeException("Failed to generate AI insights. Check API keys and payload structures.");
        }
    }
}