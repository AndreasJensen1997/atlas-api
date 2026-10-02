package app.controllers;

import app.controllers.generics.AbstractController;
import app.dtos.chapter.ChapterRequestDTO;
import app.dtos.chapter.ChapterResponseDTO;
import app.dtos.fragment.FragmentRequestDTO;
import app.dtos.fragment.FragmentResponseDTO;
import app.dtos.timeCapsule.TimeCapsuleRequestDTO;
import app.dtos.timeCapsule.TimeCapsuleResponseDTO;
import app.entities.Chapter;
import app.entities.Fragment;
import app.entities.TimeCapsule;
import app.exceptions.ApiException;
import app.mappers.FragmentMapper;
import app.mappers.TimeCapsuleMapper;
import app.services.FragmentService;
import app.services.TimeCapsuleService;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.Context;

import java.util.List;

import static io.javalin.apibuilder.ApiBuilder.*;

public class FragmentController extends AbstractController<FragmentRequestDTO, FragmentResponseDTO, Fragment, Integer> implements EndpointGroup {

    private final FragmentService fragmentService;
    private final FragmentMapper fragmentMapper;

    public FragmentController(FragmentService fragmentService, FragmentMapper fragmentMapper) {
        this.fragmentService = fragmentService;
        this.fragmentMapper = fragmentMapper;
    }

    @Override
    protected FragmentRequestDTO parseBody(Context ctx) {
        return ctx.bodyAsClass(FragmentRequestDTO.class);
    }

    @Override
    protected Fragment fetchEntityById(Integer fragmentId, Integer userId) {
        return fragmentService.getById(fragmentId, userId);
    }

    @Override
    protected List<Fragment> fetchAllByUserId(Integer userId) {
        return fragmentService.getAllById(userId);
    }

    @Override
    protected Fragment getRandom(Integer userId) {
        throw new ApiException(405, "Get random feature not supported for fragments");
    }

    @Override
    protected Integer parseId(String idStr) {
        return Integer.parseInt(idStr);
    }

    @Override
    protected Fragment createEntity(FragmentRequestDTO dto, Integer userId) {
        return fragmentService.createFragment(dto, userId);
    }

    @Override
    protected void deleteEntity(Integer entityId, Integer userId) {
        fragmentService.delete(entityId, userId);
    }

    @Override
    protected Fragment updateEntity(Integer entityId, FragmentRequestDTO fragmentRequestDTO, Integer userId) {
        return fragmentService.update(entityId, fragmentRequestDTO, userId);
    }

    // Maps entity to responseDTO
    @Override
    protected FragmentResponseDTO mapToResponse(Fragment entity) {
        return fragmentMapper.toResponse(entity);
    }

    @Override
    public void addEndpoints() {
        post("/api/fragments", this::create);
        get("/api/fragments/{id}", this::getById);
        get("/api/fragments", this::getAllById);
        delete("/api/fragments/{id}", this::deleteById);
        put("/api/fragments/{id}", this::updateById);

    }
}