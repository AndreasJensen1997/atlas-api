package app.services;

import app.daos.userOwned.ArtifactDAO;
import app.dtos.Artifact.ArtifactRequestDTO;
import app.entities.Artifact;
import app.entities.User;
import app.mappers.ArtifactMapper;

import java.util.List;

public class ArtifactService {

    private final ArtifactDAO artifactDAO;
    private final UserService userService;
    private final ArtifactMapper artifactMapper;

    public ArtifactService(ArtifactDAO artifactDAO, UserService userService, ArtifactMapper artifactMapper) {
        this.artifactDAO = artifactDAO;
        this.userService = userService;
        this.artifactMapper = artifactMapper;
    }

    public Artifact createArtifact(ArtifactRequestDTO dto, int userId) {
        User owner = userService.getById(userId);

        if (artifactDAO.findByTitleAndUserId(dto.title(), owner.getUserId()) != null) {
            throw new IllegalArgumentException("An artifact with this title already exists.");
        }

        Artifact newArtifact = artifactMapper.toEntity(dto, owner);

        return artifactDAO.create(newArtifact);
    }

    public Artifact getById(Integer artifactId, int userId) {
        Artifact artifact = artifactDAO.getById(artifactId);

        if (artifact == null) {
            throw new IllegalArgumentException("Artifact not found with ID: " + artifactId);
        }

        if (!artifact.getUser().getUserId().equals(userId)) {
            throw new IllegalArgumentException("You do not have permission to access this artifact.");
        }
        return artifact;
    }

    public List<Artifact> getAllById(int userId) {
        return artifactDAO.getAllByUserId(userId);
    }

    public void delete(Integer artifactId, int userId) {
        Artifact artifact = getById(artifactId, userId);

        artifactDAO.delete(artifact.getId());
    }

    public Artifact update(Integer artifactId, ArtifactRequestDTO dto, int userId) {
        Artifact artifact = artifactDAO.getById(artifactId);

        if (artifact == null) {
            throw new IllegalArgumentException("Artifact not found with ID: " + artifactId);
        }

        if (!artifact.getUser().getUserId().equals(userId)) {
            throw new IllegalArgumentException("You do not have permission to access this artifact.");
        }

        artifact.setTitle(dto.title());
        artifact.setSubtitle(dto.subtitle());
        artifact.setContent(dto.content());
        artifact.setVisibility(dto.visibility());

        // Update the artifact type if provided in the DTO
        if (dto.artifactType() != null) {
            artifact.setArtifactType(dto.artifactType());
        }

        return artifactDAO.update(artifact);
    }

    public Artifact getRandom(Integer userId) {
        Artifact randomArtifact = artifactDAO.getRandomByUserId(userId);

        if (randomArtifact == null) {
            throw new IllegalArgumentException("No artifacts were found.");
        }

        if (!randomArtifact.getUser().getUserId().equals(userId)) {
            throw new IllegalArgumentException("You do not have permission to access this artifact.");
        }

        return randomArtifact;
    }
}