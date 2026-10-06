package app.mappers;

import app.dtos.fragment.FragmentRequestDTO;
import app.dtos.fragment.FragmentResponseDTO;
import app.entities.Fragment;
import app.entities.User;
import app.mappers.generics.IMapper;

public class FragmentMapper implements IMapper<FragmentRequestDTO, FragmentResponseDTO, Fragment,User> {
    @Override
    public Fragment toEntity(FragmentRequestDTO dto, User user) {
        if (dto == null) return null;
        return Fragment.builder()
                .title(dto.title())
                .subtitle(dto.subtitle())
                .content(dto.content())
                .user(user)
                .build();

    }

    @Override
    public FragmentResponseDTO toResponse(Fragment fragment) {
        if (fragment == null) return null;

        return new FragmentResponseDTO(
                fragment.getId(),
                fragment.getTitle(),
                fragment.getSubtitle(),
                fragment.getContent(),
                fragment.getCreatedAt(),
                fragment.getUpdatedAt(),
                fragment.getWordCount()
        );
    }
}
