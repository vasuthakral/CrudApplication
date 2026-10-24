package org.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostResponse {
    private Integer id;
    private String title;
    private String content;
    private LocalDateTime createdAt;
    private List<TagResponse> tags;
}

