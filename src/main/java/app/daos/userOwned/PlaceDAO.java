package app.daos.userOwned;

import app.daos.generics.UserOwnedDAO;
import app.entities.Place;
import jakarta.persistence.EntityManagerFactory;

public class PlaceDAO extends UserOwnedDAO<Place, Integer> {

    // ===== Constructor =====

    public PlaceDAO(EntityManagerFactory emf) {
        super(emf, Place.class);
    }
}
