package app.daos;

import app.entities.AppUser;
import jakarta.persistence.EntityManagerFactory;

public class ArtifactDAO extends AbstractDAO<AppUser, Integer> {


    public ArtifactDAO(EntityManagerFactory emf) {
        super(emf, AppUser.class);
    }





}