package app.mappers;

import app.dtos.Place.PlaceRequestDTO;
import app.dtos.Place.PlaceResponseDTO;
import app.entities.Place;
import app.entities.User;

public class PlaceMapper {

    public Place toEntity(PlaceRequestDTO dto, User user) {
        return Place.builder()
                .name(dto.name())
                .content(dto.content())
                .latitude(dto.latitude())
                .longitude(dto.longitude())
                .address(dto.address())
                .city(dto.city())
                .country(dto.country())
                .visibility(dto.visibility())
                .user(user)
                .build();
    }

    public PlaceResponseDTO toResponseDTO(Place place) {
        return new PlaceResponseDTO(
                place.getId(),
                place.getName(),
                place.getContent(),
                place.getLatitude(),
                place.getLongitude(),
                place.getAddress(),
                place.getCity(),
                place.getCountry(),
                place.getCreatedAt(),
                place.getUpdatedAt(),
                place.getVisibility());
    }
}