package app.controllers;

import app.controllers.generics.AbstractController;
import app.dtos.artifact.ArtifactRequestDTO;
import app.dtos.artifact.ArtifactResponseDTO;
import app.entities.Artifact;
import app.mappers.ArtifactMapper;
import app.services.ArtifactService;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.Context;

import java.util.List;

import static io.javalin.apibuilder.ApiBuilder.delete;
import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.post;
import static io.javalin.apibuilder.ApiBuilder.put;

public class ArtifactController extends AbstractController<ArtifactRequestDTO, ArtifactResponseDTO, Artifact, Integer> implements EndpointGroup {

    // ===== Dependencies =====

    private final ArtifactService artifactService;
    private final ArtifactMapper artifactMapper;

    // ===== Constructor =====

    public ArtifactController(ArtifactService artifactService, ArtifactMapper artifactMapper) {
        this.artifactService = artifactService;
        this.artifactMapper = artifactMapper;
    }

    // ===== Request Handling =====

    @Override
    protected ArtifactRequestDTO parseBody(Context ctx) {
        return ctx.bodyAsClass(ArtifactRequestDTO.class);
    }

    @Override
    protected Integer parseId(String idStr) {
        return Integer.parseInt(idStr);
    }

    // ===== Entity Operations =====

    @Override
    protected Artifact createEntity(ArtifactRequestDTO dto, Integer userId) {
        return artifactService.createArtifact(dto, userId);
    }

    @Override
    protected Artifact fetchEntityById(Integer artifactId, Integer userId) {
        return artifactService.getById(artifactId, userId);
    }

    @Override
    protected List<Artifact> fetchAllByUserId(Integer userId) {
        return artifactService.getAllById(userId);
    }

    @Override
    protected Artifact updateEntity(Integer entityId, ArtifactRequestDTO artifactRequestDTO, Integer userId) {
        return artifactService.update(entityId, artifactRequestDTO, userId);
    }

    @Override
    protected void deleteEntity(Integer entityId, Integer userId) {
        artifactService.delete(entityId, userId);
    }

    @Override
    protected Artifact getRandom(Integer userId) {
        return artifactService.getRandom(userId);
    }

    // ===== Response Mapping =====

    @Override
    protected ArtifactResponseDTO mapToResponse(Artifact entity) {
        return artifactMapper.toResponse(entity);
    }

    // ===== Endpoints =====

    @Override
    public void addEndpoints() {
        post("/api/artifacts", this::create);
        get("/api/artifacts/random", this::randomByUserId);
        get("/api/artifacts/{id}", this::getById);
        get("/api/artifacts", this::getAllById);
        put("/api/artifacts/{id}", this::updateById);
        delete("/api/artifacts/{id}", this::deleteById);
    }
}