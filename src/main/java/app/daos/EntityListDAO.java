package app.daos;

import app.entities.EntityList;
import jakarta.persistence.EntityManagerFactory;

public class EntityListDAO extends AbstractDAO <EntityList, Integer> {


    public EntityListDAO(EntityManagerFactory emf) {
        super(emf, EntityList.class);
    }

}
