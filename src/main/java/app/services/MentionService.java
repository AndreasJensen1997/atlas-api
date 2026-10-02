package app.services;

import app.daos.userOwned.MentionDAO;
import app.dtos.Mention.MentionRequestDTO;
import app.dtos.Mention.MentionResponseDTO;
import app.entities.Mention;
import app.enums.TargetType;
import app.mappers.MentionMapper;

import java.util.List;
import java.util.stream.Collectors;

public class MentionService {

    // ===== Dependencies =====

    private final MentionDAO mentionDAO;
    private final MentionMapper mentionMapper;

    // ===== Constructor =====

    public MentionService(MentionDAO mentionDAO, MentionMapper mentionMapper) {
        this.mentionDAO = mentionDAO;
        this.mentionMapper = mentionMapper;
    }

    // ===== Create =====

    public MentionResponseDTO createMention(MentionRequestDTO dto) {
        Mention mention = mentionMapper.toEntity(dto);
        Mention created = mentionDAO.create(mention);
        return mentionMapper.toResponse(created);
    }

    // ===== Read =====

    public MentionResponseDTO getById(Integer mentionId) {
        Mention mention = mentionDAO.getById(mentionId);
        if (mention == null) {
            throw new IllegalArgumentException("Mention not found with ID: " + mentionId);
        }
        return mentionMapper.toResponse(mention);
    }

    public List<MentionResponseDTO> getOutgoingMentions(TargetType ownerType, Integer ownerId) {
        List<Mention> mentions = mentionDAO.getOutgoingMentions(ownerType, ownerId);
        return mentions.stream()
                .map(mentionMapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<MentionResponseDTO> getIncomingMentions(TargetType targetType, Integer targetId) {
        List<Mention> mentions = mentionDAO.getIncomingMentions(targetType, targetId);
        return mentions.stream()
                .map(mentionMapper::toResponse)
                .collect(Collectors.toList());
    }

    // ===== Delete =====

    public void deleteMention(Integer mentionId) {
        Mention mention = mentionDAO.getById(mentionId);
        if (mention == null) {
            throw new IllegalArgumentException("Mention not found with ID: " + mentionId);
        }
        mentionDAO.delete(mentionId);
    }
}