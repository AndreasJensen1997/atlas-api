package app.services;

import app.daos.userOwned.FragmentDAO;
import app.dtos.fragment.FragmentRequestDTO;
import app.entities.Fragment;
import app.entities.User;
import app.exceptions.ApiException;
import app.mappers.FragmentMapper;
import io.javalin.http.HttpStatus;


import java.util.List;

public class FragmentService {

    // ===== Dependencies =====

    private final FragmentDAO fragmentDAO;
    private final UserService userService;
    private final FragmentMapper fragmentMapper;

    // ===== Constructor =====

    public FragmentService(FragmentDAO fragmentDAO, UserService userService, FragmentMapper fragmentMapper) {
        this.fragmentDAO = fragmentDAO;
        this.userService = userService;
        this.fragmentMapper = fragmentMapper;
    }

    // ===== Create =====

    public Fragment createFragment(FragmentRequestDTO dto, int userId) {
        User owner = userService.getById(userId);

        Fragment newFragment = fragmentMapper.toEntity(dto, owner);
        return fragmentDAO.create(newFragment);
    }

    // ===== Read =====

    public Fragment getById(Integer fragmentId, int userId) {
        Fragment fragment = fragmentDAO.getById(fragmentId);

        if (!fragment.getUser().getId().equals(userId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Fragment not found with ID: " + fragmentId);
        }
        return fragment;
    }

    public List<Fragment> getAllById(int userId) {
        return fragmentDAO.getAllByUserId(userId);
    }

    // ===== Update =====

    public Fragment update(Integer fragmentId, FragmentRequestDTO dto, int userId) {
        Fragment fragment = getById(fragmentId,userId);

        fragment.setTitle(dto.title());
        fragment.setSubtitle(dto.subtitle());
        fragment.setContent(dto.content());


        return fragmentDAO.update(fragment);
    }

    // ===== Delete =====

    public void delete(Integer fragmentId, int userId) {
        Fragment fragment = getById(fragmentId, userId);
        fragmentDAO.delete(fragment.getId());
    }
}
