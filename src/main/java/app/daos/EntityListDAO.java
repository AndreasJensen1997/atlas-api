package app.daos;

import app.entities.EntityList;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;

import java.util.List;

public class EntityListDAO extends UserOwnedDAO <EntityList, Integer> {


    public EntityListDAO(EntityManagerFactory emf) {
        super(emf, EntityList.class);
    }



}
