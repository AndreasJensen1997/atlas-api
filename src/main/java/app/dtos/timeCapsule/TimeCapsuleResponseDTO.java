package app.dtos.timeCapsule;

import java.time.LocalDate;

public record TimeCapsuleResponseDTO(
        Integer timeCapsuleId,
        String title,
        String subtitle,
        String content,
        LocalDate unlockDate,
        Boolean lockStatus
) {
}
