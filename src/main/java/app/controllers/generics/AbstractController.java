package app.controllers.generics;

import app.entities.Chapter;
import io.javalin.http.Context;

import java.util.Map;

public abstract class AbstractController<Req, Res, T, ID> {


    protected abstract T createEntity(Req dto, ID userId);

    protected abstract T fetchEntityById(ID entityId, ID userId);

    protected abstract ID parseId(String idStr);

    protected abstract Res mapToResponse(T entity);

    protected abstract Req parseBody(Context ctx);

    public void create(Context ctx) {
        try {
            Req requestDTO = parseBody(ctx);

            ID userId = ctx.attribute("currentUserId");
            if (userId == null) {
                ctx.status(401).json(Map.of("error", "Unauthorized"));
                return;
            }

            T savedEntity = createEntity(requestDTO, userId);
            Res responseDTO = mapToResponse(savedEntity);

            ctx.status(201).json(responseDTO);

        } catch (IllegalArgumentException e) {
            ctx.status(400).json(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            ctx.status(500).json(Map.of("error", e.getMessage()));
        }
    }


    public void getById(Context ctx) {
        try {
            ID userId = ctx.attribute("currentUserId");
            if (userId == null) {
                ctx.status(401).json(Map.of("error", "Unauthorized"));
                return;
            }

            String idParam = ctx.pathParam("id");
            ID entityId = parseId(idParam);

            T entity = fetchEntityById(entityId, userId);

            Res responseDTO = mapToResponse(entity);
            ctx.status(200).json(responseDTO);

        } catch (IllegalArgumentException e) {
            ctx.status(404).json(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            ctx.status(500).json(Map.of("error", e.getMessage()));
        }


    }
}