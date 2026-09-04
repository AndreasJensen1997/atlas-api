package app.daos;

import app.entities.AppUser;
import app.entities.Artifact;
import jakarta.persistence.EntityManagerFactory;

public class ArtifactDAO extends AbstractDAO<Artifact, Integer> {


    public ArtifactDAO(EntityManagerFactory emf) {
        super(emf, Artifact.class);
    }





}