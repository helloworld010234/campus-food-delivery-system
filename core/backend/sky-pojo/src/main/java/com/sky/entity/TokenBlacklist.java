package com.sky.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenBlacklist implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String tokenHash;
    private String tokenType;
    private Long subjectId;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    private String reason;
}
