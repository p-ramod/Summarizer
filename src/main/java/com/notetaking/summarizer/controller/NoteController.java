package com.notetaking.summarizer.controller;

import com.notetaking.summarizer.dto.NoteRequest;
import com.notetaking.summarizer.dto.NoteResponse;
import com.notetaking.summarizer.entity.NoteEntity;
import com.notetaking.summarizer.entity.UserEntity;
import com.notetaking.summarizer.repository.NoteRepository;
import com.notetaking.summarizer.repository.UserRepository;
import com.notetaking.summarizer.service.ClaudeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
public class NoteController {

    private final NoteRepository noteRepository;
    private final UserRepository userRepository;
    private final ClaudeService claudeService;

    @PostMapping
    public ResponseEntity<NoteResponse> createNote(@RequestBody NoteRequest request) {
        UserEntity user = getCurrentUser();

        // Generate summary using Claude (optional - returns null if not configured)
        String summary = claudeService.summarize(request.getTitle(), request.getContent());

        NoteEntity note = NoteEntity.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .summary(summary)
                .user(user)
                .build();

        NoteEntity savedNote = noteRepository.save(note);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(savedNote));
    }

    @GetMapping
    public ResponseEntity<List<NoteResponse>> getAllNotes() {
        UserEntity user = getCurrentUser();
        List<NoteEntity> notes = noteRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        List<NoteResponse> response = notes.stream().map(this::toResponse).toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<NoteResponse> getNoteById(@PathVariable Long id) {
        UserEntity user = getCurrentUser();
        return noteRepository.findByIdAndUserId(id, user.getId())
                .map(note -> ResponseEntity.ok(toResponse(note)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<NoteResponse> updateNote(@PathVariable Long id, @RequestBody NoteRequest request) {
        UserEntity user = getCurrentUser();
        return noteRepository.findByIdAndUserId(id, user.getId())
                .map(note -> {
                    note.setTitle(request.getTitle());
                    note.setContent(request.getContent());
                    // Optionally regenerate summary on update
                    String summary = claudeService.summarize(request.getTitle(), request.getContent());
                    if (summary != null) {
                        note.setSummary(summary);
                    }
                    NoteEntity updatedNote = noteRepository.save(note);
                    return ResponseEntity.ok(toResponse(updatedNote));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNote(@PathVariable Long id) {
        UserEntity user = getCurrentUser();
        return noteRepository.findByIdAndUserId(id, user.getId())
                .map(note -> {
                    noteRepository.delete(note);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    private UserEntity getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private NoteResponse toResponse(NoteEntity note) {
        return NoteResponse.builder()
                .id(note.getId())
                .title(note.getTitle())
                .content(note.getContent())
                .summary(note.getSummary())
                .createdAt(note.getCreatedAt())
                .updatedAt(note.getUpdatedAt())
                .build();
    }
}