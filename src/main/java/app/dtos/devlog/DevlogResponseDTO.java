package app.dtos.devlog;

import java.time.LocalDate;

public record DevlogResponseDTO(
        Integer devlogId,
        String title,
        String subtitle,
        String content,
        LocalDate createdAt
) {
}
