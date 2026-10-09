package app.services;

import app.daos.generics.UserOwnedDAO;
import app.daos.userOwned.MentionDAO;
import app.dtos.Mention.MentionRequestDTO;
import app.dtos.Mention.MentionResponseDTO;
import app.entities.Mention;
import app.entities.User;
import app.enums.TargetType;
import app.exceptions.ApiException;
import app.mappers.MentionMapper;
import io.javalin.http.HttpStatus;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MentionService {

    // ===== Dependencies =====

    private final MentionDAO mentionDAO;
    private final MentionMapper mentionMapper;
    private final UserService userService;
    private final Map<TargetType, UserOwnedDAO<?, Integer>> pageDaos;


    // ===== Constructor =====

    public MentionService(MentionDAO mentionDAO, UserService userService, MentionMapper mentionMapper, Map<TargetType, UserOwnedDAO<?, Integer>> pageDaos) {
        this.mentionDAO = mentionDAO;
        this.userService = userService;
        this.mentionMapper = mentionMapper;
        this.pageDaos = pageDaos;
    }

    // ===== Helper methods =====
    private Mention getOwnedMention(Integer mentionId, int userId) {
        Mention mention = mentionDAO.getById(mentionId);
        if (!mention.getUser().getId().equals(userId)) {
            throw new ApiException(404, "Mention not found");
        }
        return mention;
    }

    private void requirePageOwnedByUser(TargetType type, Integer pageId, int userId) {
        UserOwnedDAO<?, Integer> dao = pageDaos.get(type);

        if (dao == null || !dao.existsByIdAndUserId(pageId, userId)) {
            throw new ApiException(404, type + " not found with ID: " + pageId);
        }
    }

    // ===== Create =====

    public MentionResponseDTO createMention(MentionRequestDTO dto, int userId) {
        requirePageOwnedByUser(dto.ownerType(), dto.ownerId(), userId);
        requirePageOwnedByUser(dto.targetType(), dto.targetId(), userId);
        User user = userService.getById(userId);
        Mention mention = mentionMapper.toEntity(dto, user);
        return mentionMapper.toResponse(mentionDAO.create(mention));
    }

    // ===== Read =====

    public MentionResponseDTO getById(Integer mentionId, int userId) {
        Mention mention = getOwnedMention(mentionId, userId);
        return mentionMapper.toResponse(mention);
    }

    public List<MentionResponseDTO> getOutgoingMentions(TargetType ownerType, Integer ownerId, int userId) {
        List<Mention> mentions = mentionDAO.getOutgoingMentions(ownerType, ownerId, userId);
        return mentions.stream()
                .map(mentionMapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<MentionResponseDTO> getIncomingMentions(TargetType targetType, Integer targetId, int userId) {
        List<Mention> mentions = mentionDAO.getIncomingMentions(targetType, targetId, userId);
        return mentions.stream()
                .map(mentionMapper::toResponse)
                .collect(Collectors.toList());
    }

    // ===== Delete =====

    public void deleteMention(Integer mentionId, int userId) {
        getOwnedMention(mentionId,userId);
        mentionDAO.delete(mentionId);
    }
}