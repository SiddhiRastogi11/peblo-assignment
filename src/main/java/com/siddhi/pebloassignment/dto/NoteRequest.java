package com.siddhi.pebloassignment.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.Set;

@Getter
@Setter
public class NoteRequest {
    private String title;
    private String content;
    private Set<String> tags; // Strings like ["work", "idea"]
}