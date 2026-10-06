package app.dtos.place;

import app.enums.Visibility;

import java.time.LocalDate;

public record PlaceResponseDTO(
        Integer id,
        String name,
        String content,
        Double latitude,
        Double longitude,
        String address,
        String city,
        String country,
        LocalDate createdAt,
        LocalDate updatedAt,
        Visibility visibility

) {
}
