package app.services;

import app.daos.userOwned.FragmentDAO;
import app.daos.userOwned.TimeCapsuleDAO;
import app.dtos.chapter.ChapterRequestDTO;
import app.dtos.fragment.FragmentRequestDTO;
import app.dtos.timeCapsule.TimeCapsuleRequestDTO;
import app.entities.Chapter;
import app.entities.Fragment;
import app.entities.TimeCapsule;
import app.entities.User;
import app.mappers.FragmentMapper;
import app.mappers.TimeCapsuleMapper;

import java.util.List;

public class FragmentService {

    private final FragmentDAO fragmentDAO;
    private final UserService userService;
    private final FragmentMapper fragmentMapper;

    public FragmentService(FragmentDAO fragmentDAO, UserService userService, FragmentMapper fragmentMapper) {
        this.fragmentDAO = fragmentDAO;
        this.userService = userService;
        this.fragmentMapper = fragmentMapper;
    }

    public Fragment createFragment(FragmentRequestDTO dto, int userId) {
        User owner = userService.getById(userId);

        Fragment newFragment = fragmentMapper.toEntity(dto, owner);
        return fragmentDAO.create(newFragment);
    }

    public Fragment getById(Integer fragmentId, int userId) {
        Fragment fragment = fragmentDAO.getById(fragmentId);

        if (fragment == null) {
            throw new IllegalArgumentException("Fragment not found with ID: " + fragment);
        }

        if (!fragment.getUser().getUserId().equals(userId)) {
            throw new IllegalArgumentException("You do not have permission to access this fragment.");
        }
        return fragment;
    }

    public List<Fragment> getAllById(int userId) {
        return fragmentDAO.getAllByUserId(userId);
    }

    public Fragment update(Integer fragmentId, FragmentRequestDTO dto, int userId) {
        Fragment fragment = fragmentDAO.getById(fragmentId);

        if (fragment == null) {
            throw new IllegalArgumentException("Chapter not found with ID: " + fragmentId);
        }

        if (!fragment.getUser().getUserId().equals(userId)) {
            throw new IllegalArgumentException("You do not have permission to access this fragment.");
        }

        fragment.setTitle(dto.title());
        fragment.setSubtitle(dto.subtitle());
        fragment.setContent(dto.content());


        return fragmentDAO.update(fragment);
    }

    public void delete(Integer fragmentId, int userId) {
        Fragment fragment = getById(fragmentId, userId);
        fragmentDAO.delete(fragment.getFragmentId());
    }
}
