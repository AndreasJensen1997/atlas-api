package app.controllers.generics;

import app.dtos.chapter.ChapterRequestDTO;
import app.entities.Chapter;
import app.exceptions.ApiException;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

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

    @SuppressWarnings("unchecked")
    protected ID parseId(Context ctx) {
        Integer id = ctx.pathParamAsClass("id", Integer.class)
                .check(i -> i > 0, "ID must be a positive integer")
                .get();
        return (ID) id;
    }

    protected abstract Res mapToResponse(T entity);

    protected abstract Req parseBody(Context ctx);

    protected ID getUserIdOrThrow(Context ctx) {
        ID userId = ctx.attribute("userId");
        if (userId == null) {
            throw new ApiException(401, "Unauthorized");
        }
        return userId;
    }

    public void create(Context ctx) {
        Req requestDTO = parseBody(ctx);
        ID userId = getUserIdOrThrow(ctx);

        T savedEntity = createEntity(requestDTO, userId);
        Res responseDTO = mapToResponse(savedEntity);

        ctx.status(HttpStatus.CREATED).json(responseDTO);
    }

    public void getById(Context ctx) {
        ID userId = getUserIdOrThrow(ctx);

        ID entityId = parseId(ctx);
        T entity = fetchEntityById(entityId, userId);

        Res responseDTO = mapToResponse(entity);
        ctx.status(200).json(responseDTO);
    }

    public void getAllById(Context ctx) {
        ID userId = getUserIdOrThrow(ctx);

        List<T> allEntities = fetchAllByUserId(userId);
        List<Res> allResponseDTOs = allEntities.stream()
                .map(this::mapToResponse)
                .toList();

        ctx.status(200).json(allResponseDTOs);
    }

    public void updateById(Context ctx) {
        ID userId = getUserIdOrThrow(ctx);

        ID entityId = parseId(ctx);
        Req requestDTO = parseBody(ctx);

        T updatedEntity = updateEntity(entityId, requestDTO, userId);
        Res responseDTO = mapToResponse(updatedEntity);

        ctx.status(200).json(responseDTO);
    }

    public void deleteById(Context ctx) {
        ID userId = getUserIdOrThrow(ctx);

        ID entityId = parseId(ctx);
        deleteEntity(entityId, userId);

        ctx.status(200).json(Map.of("message", "Deleted successfully"));
    }

    public void randomByUserId(Context ctx) {
        ID userId = getUserIdOrThrow(ctx);

        T randomEntity = getRandom(userId);
        Res responseDTO = mapToResponse(randomEntity);

        ctx.status(200).json(responseDTO);
    }
}