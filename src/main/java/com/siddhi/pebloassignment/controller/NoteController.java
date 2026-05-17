package com.siddhi.pebloassignment.controller;

import com.siddhi.pebloassignment.dto.NoteRequest;
import com.siddhi.pebloassignment.model.Note;
import com.siddhi.pebloassignment.service.AIService;
import com.siddhi.pebloassignment.service.NoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
public class NoteController {

    @Autowired
    private NoteService noteService;


    @Autowired
    private AIService aiService;

    @PostMapping("/api/notes/{id}/ai-features")
    public ResponseEntity<com.siddhi.pebloassignment.dto.AiSummaryResponse> getAiInsights(@PathVariable String id, Principal principal) {
        return ResponseEntity.ok(aiService.generateNoteSummary(id, principal.getName()));
    }

    // Secure Workspace Actions (Requires Token Validation)
    @GetMapping("/api/notes")
    public ResponseEntity<List<Note>> getAllNotes(Principal principal) {
        return ResponseEntity.ok(noteService.getActiveNotesForUser(principal.getName()));
    }

    @PostMapping("/api/notes")
    public ResponseEntity<Note> createNote(Principal principal) {
        return ResponseEntity.ok(noteService.createNote(principal.getName()));
    }

    @PatchMapping("/api/notes/{id}")
    public ResponseEntity<Note> updateNote(@PathVariable String id, @RequestBody NoteRequest request, Principal principal) {
        return ResponseEntity.ok(noteService.updateNote(id, request, principal.getName()));
    }

    @PutMapping("/api/notes/{id}/archive")
    public ResponseEntity<?> archiveNote(@PathVariable String id, Principal principal) {
        noteService.archiveNote(id, principal.getName());
        return ResponseEntity.ok().body(Map.of("message", "Note archived successfully"));
    }

    @PostMapping("/api/notes/{id}/share")
    public ResponseEntity<?> shareNote(@PathVariable String id, Principal principal) {
        String token = noteService.generatePublicShareLink(id, principal.getName());
        return ResponseEntity.ok().body(Map.of("shareToken", token));
    }

    @GetMapping("/api/notes/search")
    public ResponseEntity<List<Note>> searchNotes(@RequestParam String query, Principal principal) {
        return ResponseEntity.ok(noteService.searchUserNotes(principal.getName(), query));
    }

    // Unauthenticated Public Visibility Link (Does NOT run against security filter requirements)
    @GetMapping("/api/public/shared/{shareToken}")
    public ResponseEntity<Note> getSharedNote(@PathVariable String shareToken) {
        return noteService.getPublicNote(shareToken)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}