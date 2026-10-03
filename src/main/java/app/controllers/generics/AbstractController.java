package app.controllers.generics;

import app.dtos.chapter.ChapterRequestDTO;
import app.entities.Chapter;
import io.javalin.http.Context;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class AbstractController<Req, Res, T, ID> {


    protected abstract T createEntity(Req dto, ID userId);

    protected abstract void deleteEntity(ID entityId, ID userId);

    protected abstract T updateEntity(ID entityId, Req dto, ID userId);

    protected abstract T fetchEntityById(ID entityId, ID userId);

    protected abstract List<T> fetchAllByUserId(ID userId);

    protected abstract T getRandom(ID userId);

    protected abstract ID parseId(String idStr);

    protected abstract Res mapToResponse(T entity);

    protected abstract Req parseBody(Context ctx);

    public void create(Context ctx) {
        try {
            Req requestDTO = parseBody(ctx);

            ID userId = ctx.attribute("userId");
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
            ID userId = ctx.attribute("userId");
            if (userId == null) {
                ctx.status(401).json(Map.of("error", "Unauthorized"));
                return;
            }

            // Parses id from string to int from url
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

    public void getAllById(Context ctx) {
        try {
            ID userId = ctx.attribute("userId");
            if (userId == null) {
                ctx.status(401).json(Map.of("error", "Unauthorized"));
                return;
            }

            List<T> allEntities = fetchAllByUserId(userId);
            List<Res> allRespondDTOs = new ArrayList<>();

            for (T entity : allEntities) {
                Res responseDTO = mapToResponse(entity);
                allRespondDTOs.add(responseDTO);
            }
            ctx.status(200).json(allRespondDTOs);


        } catch (IllegalArgumentException e) {
            ctx.status(404).json(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            ctx.status(500).json(Map.of("error", e.getMessage()));
        }
    }

    public void deleteById(Context ctx) {
        try {
            ID userId = ctx.attribute("userId");
            if (userId == null) {
                ctx.status(401).json(Map.of("error", "Unauthorized"));
                return;
            }

            String idParam = ctx.pathParam("id");
            ID entityId = parseId(idParam);
            deleteEntity(entityId, userId);

            ctx.status(200).json(Map.of("message", "Deleted successfully"));

        } catch (IllegalArgumentException e) {
            ctx.status(404).json(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            ctx.status(500).json(Map.of("error", e.getMessage()));
        }
    }

    public void updateById(Context ctx) {
        try {
            ID userId = ctx.attribute("userId");
            if (userId == null) {
                ctx.status(401).json(Map.of("error", "Unauthorized"));
                return;
            }

            String idParam = ctx.pathParam("id");
            ID entityId = parseId(idParam);

            // 1. Parse the incoming JSON body into the DTO
            Req requestDTO = parseBody(ctx);

            // 2. Pass the entityId, the DTO, and the userId down to the implementation
            T updatedEntity = updateEntity(entityId, requestDTO, userId);

            ctx.status(200).json(Map.of("message", "Updated successfully"));

        } catch (IllegalArgumentException e) {
            ctx.status(404).json(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            ctx.status(500).json(Map.of("error", e.getMessage()));
        }
    }

    public void randomByUserId(Context ctx) {
        try {
            ID userId = ctx.attribute("userId");
            if (userId == null) {
                ctx.status(401).json(Map.of("error", "Unauthorized"));
                return;
            }
            T randomEntity = getRandom(userId);
            Res responseDTO = mapToResponse(randomEntity);
            ctx.status(200).json(responseDTO);

        } catch (IllegalArgumentException e) {
            ctx.status(404).json(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            ctx.status(500).json(Map.of("error", e.getMessage()));
        }


    }


}