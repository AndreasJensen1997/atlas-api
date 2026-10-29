package app.services;

import app.daos.userOwned.ArtifactTypeDAO;
import app.entities.ArtifactType;
import app.entities.User;
import java.util.Comparator;
import java.util.List;

public class ArtifactTypeService {

    // ===== Dependencies =====

    private final ArtifactTypeDAO artifactTypeDAO;

    // ===== Constructor =====

    public ArtifactTypeService(ArtifactTypeDAO artifactTypeDAO) {
        this.artifactTypeDAO = artifactTypeDAO;
    }

    // ===== Artifact Type Resolution =====

    public ArtifactType resolveArtifactType(ArtifactType requestedType, User owner) {
        if (requestedType == null || requestedType.getName() == null || requestedType.getName().isBlank()) {
            throw new IllegalArgumentException("Artifact type name cannot be blank.");
        }
        String name = requestedType.getName().trim();

        ArtifactType existing = artifactTypeDAO.findByNameIgnoreCase(name, owner.getId());
        if (existing != null) {
            return existing;
        }

        ArtifactType newType = new ArtifactType();
        newType.setName(name);
        newType.setUser(owner);
        return artifactTypeDAO.create(newType);
    }

    public List<ArtifactType> getAllByUserId(int userId) {
        return artifactTypeDAO.getAllByUserId(userId).stream()
                .sorted(Comparator.comparing(ArtifactType::getName, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }
}
