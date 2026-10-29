package app.controllers;

import app.dtos.artifactType.ArtifactTypeResponseDTO;
import static io.javalin.apibuilder.ApiBuilder.get;
import app.exceptions.ApiException;
import app.services.ArtifactTypeService;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

import java.util.List;

public class ArtifactTypeController implements EndpointGroup {


    private final ArtifactTypeService artifactTypeService;

    public ArtifactTypeController(ArtifactTypeService artifactTypeService) {
        this.artifactTypeService = artifactTypeService;
    }

    public void getAll(Context ctx) {
        Integer userId = ctx.attribute("userId");

        if (userId == null){
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }

        List<ArtifactTypeResponseDTO> types = artifactTypeService.getAllByUserId(userId).stream()
                .map(t -> new ArtifactTypeResponseDTO(t.getId(),t.getName()))
                .toList();
        ctx.json(types);

    }

    @Override
    public void addEndpoints() {
        get("artifact-types", this::getAll);
    }
}
