package com.siddhi.pebloassignment.service;

import com.siddhi.pebloassignment.dto.NoteRequest;
import com.siddhi.pebloassignment.model.Note;
import com.siddhi.pebloassignment.model.Tag;
import com.siddhi.pebloassignment.model.User;
import com.siddhi.pebloassignment.repository.NoteRepository;
import com.siddhi.pebloassignment.repository.TagRepository;
import com.siddhi.pebloassignment.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class NoteService {

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TagRepository tagRepository;

    public List<Note> getActiveNotesForUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User context not found"));
        return noteRepository.findByUserIdAndArchivedFalseOrderByUpdatedAtDesc(user.getId());
    }

    @Transactional
    public Note createNote(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User context not found"));

        Note note = Note.builder()
                .title("Untitled Note")
                .content("")
                .archived(false)
                .user(user)
                .tags(new HashSet<>())
                .build();

        return noteRepository.save(note);
    }

    @Transactional
    public Note updateNote(String noteId, NoteRequest request, String email) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new RuntimeException("Note not found"));

        // Guard rails: Ensure users cannot modify other users' private notes
        if (!note.getUser().getEmail().equals(email)) {
            throw new RuntimeException("Unauthorized workspace access");
        }

        note.setTitle(request.getTitle());
        note.setContent(request.getContent());

        // Process tag allocations cleanly map names to unique database items
        if (request.getTags() != null) {
            HashSet<Tag> matchedTags = new HashSet<>();
            for (String tagName : request.getTags()) {
                String cleanName = tagName.trim().toLowerCase();
                if (!cleanName.isEmpty()) {
                    Tag tag = tagRepository.findByName(cleanName)
                            .orElseGet(() -> tagRepository.save(Tag.builder().name(cleanName).build()));
                    matchedTags.add(tag);
                }
            }
            note.setTags(matchedTags);
        }

        return noteRepository.save(note);
    }

    @Transactional
    public Note archiveNote(String noteId, String email) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new RuntimeException("Note not found"));

        if (!note.getUser().getEmail().equals(email)) {
            throw new RuntimeException("Unauthorized workspace access");
        }

        note.setArchived(true);
        return noteRepository.save(note);
    }

    @Transactional
    public String generatePublicShareLink(String noteId, String email) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new RuntimeException("Note not found"));

        if (!note.getUser().getEmail().equals(email)) {
            throw new RuntimeException("Unauthorized workspace access");
        }

        if (note.getShareToken() == null) {
            note.setShareToken(UUID.randomUUID().toString());
            noteRepository.save(note);
        }
        return note.getShareToken();
    }

    public Optional<Note> getPublicNote(String shareToken) {
        return noteRepository.findByShareTokenAndArchivedFalse(shareToken);
    }

    public List<Note> searchUserNotes(String email, String keyword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User context not found"));
        return noteRepository.searchNotes(user.getId(), keyword);
    }
}