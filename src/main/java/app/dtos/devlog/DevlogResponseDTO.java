package app.dtos.devlog;

import java.time.LocalDate;

public record DevlogResponseDTO(
        Integer id,
        String title,
        String subtitle,
        String content,
        LocalDate createdAt
) {
}
