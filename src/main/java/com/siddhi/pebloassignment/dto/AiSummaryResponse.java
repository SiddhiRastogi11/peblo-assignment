package com.siddhi.pebloassignment.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class AiSummaryResponse {
    private String summary;
    private List<String> actionItems;
    private String suggestedTitle;
}