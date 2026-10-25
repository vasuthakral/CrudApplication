package org.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProfileRequest {
    @NotBlank
    @Size(min = 5, max = 100, message = "Bio must be between 5 and 100 characters")
    private String bio;

    @NotBlank
    private String avatarUrl;
}
