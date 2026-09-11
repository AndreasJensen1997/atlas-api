package app.daos;

import app.entities.Place;
import jakarta.persistence.EntityManagerFactory;

public class PlaceDAO extends UserOwnedDAO<Place, Integer> {

    public PlaceDAO(EntityManagerFactory emf) {
        super(emf, Place.class);
    }
}
