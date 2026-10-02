package app.services;

import app.daos.userOwned.ArtifactDAO;
import app.daos.userOwned.ArtifactTypeDAO;
import app.entities.ArtifactType;
import app.entities.User;

public class ArtifactTypeService {


    private final ArtifactTypeDAO artifactTypeDAO;

    public ArtifactTypeService(ArtifactTypeDAO artifactTypeDAO) {
        this.artifactTypeDAO = artifactTypeDAO;
    }

    public ArtifactType resolveArtifactType(ArtifactType requestedType, User owner) {
        if (requestedType == null || requestedType.getName() == null) {
            throw new IllegalArgumentException("Artifact type name cannot be null.");
        }

        // Check if THIS user already has an artifact type with this name
        ArtifactType existingType = artifactTypeDAO.findByTitleAndUserId(requestedType.getName(), owner.getUserId());
        if (existingType != null) {
            return existingType;
        }

        // If it doesn't exist, create it and explicitly link it to the user!
        ArtifactType newType = new ArtifactType();
        newType.setName(requestedType.getName());
        newType.setUser(owner); // <-- THIS WAS MISSING

        return artifactTypeDAO.create(newType);
    }
}
