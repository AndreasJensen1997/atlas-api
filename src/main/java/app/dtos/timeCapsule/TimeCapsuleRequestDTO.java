package app.dtos.timeCapsule;

import java.time.LocalDate;

public record TimeCapsuleRequestDTO(
        String title,
        String subtitle,
        String content,
        LocalDate unlockDate
) {
}
