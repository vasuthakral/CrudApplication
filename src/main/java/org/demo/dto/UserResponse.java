package org.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data // mutable
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {
    private Integer id;
    private String name;
    private Integer age;
    private List<PostResponse> posts;
    private ProfileResponse profile;
}

