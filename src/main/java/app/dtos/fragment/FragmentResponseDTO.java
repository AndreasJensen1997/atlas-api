package app.dtos.fragment;

import java.time.LocalDate;

public record FragmentResponseDTO(
        Integer id,
        String title,
        String subtitle,
        String content,
        LocalDate createdAt,
        LocalDate updatedAt,
        Integer wordCount
) {
}
