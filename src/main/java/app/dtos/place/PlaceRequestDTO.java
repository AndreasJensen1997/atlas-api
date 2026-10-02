package app.dtos.place;

import app.enums.Visibility;

public record PlaceRequestDTO(
        String name,
        String content,
        Double latitude,
        Double longitude,
        String address,
        String city,
        String country,
        Visibility visibility
) {
}
