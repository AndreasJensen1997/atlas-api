package app.mappers;

import app.dtos.place.PlaceRequestDTO;
import app.dtos.place.PlaceResponseDTO;
import app.entities.Place;
import app.entities.User;
import app.mappers.generics.IMapper;

public class PlaceMapper implements IMapper<PlaceRequestDTO, PlaceResponseDTO, Place, User> {

    @Override
    public Place toEntity(PlaceRequestDTO dto, User user) {
        if (dto == null) return null;

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

    @Override
    public PlaceResponseDTO toResponse(Place place) {
        if (place == null) return null;

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