package app.daos.userOwned;

import app.daos.generics.UserOwnedDAO;
import app.entities.EntityList;
import jakarta.persistence.EntityManagerFactory;

public class EntityListDAO extends UserOwnedDAO<EntityList, Integer> {


    public EntityListDAO(EntityManagerFactory emf) {
        super(emf, EntityList.class);
    }



}
