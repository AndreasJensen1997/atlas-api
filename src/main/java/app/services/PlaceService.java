package app.services;

import app.daos.userOwned.PlaceDAO;
import app.dtos.Place.PlaceRequestDTO;
import app.entities.Place;
import app.entities.User;
import app.mappers.PlaceMapper;

import java.util.List;

public class PlaceService {

    private final PlaceDAO placeDAO;
    private final UserService userService;
    private final PlaceMapper placeMapper;

    public PlaceService(PlaceDAO placeDAO, UserService userService, PlaceMapper placeMapper) {
        this.placeDAO = placeDAO;
        this.userService = userService;
        this.placeMapper = placeMapper;
    }

    public Place createPlace(PlaceRequestDTO dto, int userId) {
        User owner = userService.getById(userId);

        // UserOwnedDAO's findByTitleAndUserId handles 'name' automatically for Place entities
        if (placeDAO.findByTitleAndUserId(dto.name(), owner.getUserId()) != null) {
            throw new IllegalArgumentException("A place with this name already exists.");
        }

        Place newPlace = placeMapper.toEntity(dto, owner);
        return placeDAO.create(newPlace);
    }

    public Place getById(Integer placeId, int userId) {
        Place place = placeDAO.getById(placeId);

        if (place == null) {
            throw new IllegalArgumentException("Place not found with ID: " + placeId);
        }

        if (!place.getUser().getUserId().equals(userId)) {
            throw new IllegalArgumentException("You do not have permission to access this place.");
        }
        return place;
    }

    public List<Place> getAllById(int userId) {
        return placeDAO.getAllByUserId(userId);
    }

    public void delete(Integer placeId, int userId) {
        Place place = getById(placeId, userId);
        placeDAO.delete(place.getId());
    }

    public Place update(Integer placeId, PlaceRequestDTO dto, int userId) {
        Place place = placeDAO.getById(placeId);

        if (place == null) {
            throw new IllegalArgumentException("Place not found with ID: " + placeId);
        }

        if (!place.getUser().getUserId().equals(userId)) {
            throw new IllegalArgumentException("You do not have permission to access this place.");
        }

        place.setName(dto.name());
        place.setContent(dto.content());
        place.setLatitude(dto.latitude());
        place.setLongitude(dto.longitude());
        place.setAddress(dto.address());
        place.setCity(dto.city());
        place.setCountry(dto.country());
        place.setVisibility(dto.visibility());

        return placeDAO.update(place);
    }

    public Place getRandom(Integer userId) {
        Place randomPlace = placeDAO.getRandomByUserId(userId);

        if (randomPlace == null) {
            throw new IllegalArgumentException("No places were found");
        }

        if (!randomPlace.getUser().getUserId().equals(userId)) {
            throw new IllegalArgumentException("You do not have permission to access this place.");
        }

        return randomPlace;
    }
}