package app.daos.userOwned;

import app.daos.generics.GenericDAO;
import app.daos.generics.UserOwnedDAO;
import app.entities.ArtifactType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

public class ArtifactTypeDAO extends UserOwnedDAO<ArtifactType, Integer> {

    public ArtifactTypeDAO(EntityManagerFactory emf) {
        super(emf,ArtifactType.class);
    }

}