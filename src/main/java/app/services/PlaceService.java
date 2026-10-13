package app.services;

import app.daos.userOwned.PlaceDAO;
import app.dtos.place.PlaceRequestDTO;
import app.entities.Place;
import app.entities.User;
import app.exceptions.ApiException;
import app.mappers.PlaceMapper;

import java.util.List;

public class PlaceService {

    // ===== Dependencies =====

    private final PlaceDAO placeDAO;
    private final UserService userService;
    private final PlaceMapper placeMapper;

    // ===== Constructor =====

    public PlaceService(PlaceDAO placeDAO, UserService userService, PlaceMapper placeMapper) {
        this.placeDAO = placeDAO;
        this.userService = userService;
        this.placeMapper = placeMapper;
    }

    // ===== Create =====

    public Place createPlace(PlaceRequestDTO dto, int userId) {
        User owner = userService.getById(userId);
        Place newPlace = placeMapper.toEntity(dto, owner);

        return placeDAO.create(newPlace);
    }

    // ===== Read =====

    public Place getById(Integer placeId, int userId) {
        Place place = placeDAO.getById(placeId);

        if (!place.getUser().getId().equals(userId)) {
            throw new ApiException(404, "Place not found with ID: " + placeId);
        }
        return place;
    }

    public List<Place> getAllById(int userId) {
        return placeDAO.getAllByUserId(userId);
    }

    public Place getRandom(Integer userId) {
        Place randomPlace = placeDAO.getRandomByUserId(userId);

        if (randomPlace == null) {
            throw new ApiException(404, "No places were found");
        }

        return randomPlace;
    }

    // ===== Update =====

    public Place update(Integer placeId, PlaceRequestDTO dto, int userId) {
        Place place = getById(placeId,userId);

        place.setName(dto.name());
        place.setSubtitle(dto.subtitle());
        place.setContent(dto.content());
        place.setLatitude(dto.latitude());
        place.setLongitude(dto.longitude());
        place.setAddress(dto.address());
        place.setCity(dto.city());
        place.setCountry(dto.country());
        place.setVisibility(dto.visibility());

        return placeDAO.update(place);
    }
    // ===== Delete =====

    public void delete(Integer placeId, int userId) {
        Place place = getById(placeId, userId);
        placeDAO.delete(place.getId());
    }
}