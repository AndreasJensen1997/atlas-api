package app.dtos.devlog;



import java.time.LocalDate;

public record DevlogRequestDTO(
        String title,
        String subtitle,
        String content,
        LocalDate createdAt

) {
}
