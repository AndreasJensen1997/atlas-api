package app.mappers;

import app.dtos.person.PersonRequestDTO;
import app.dtos.person.PersonResponseDTO;
import app.entities.Person;
import app.entities.User;
import app.mappers.generics.IMapper;

public class PersonMapper implements IMapper<PersonRequestDTO, PersonResponseDTO, Person, User> {

    @Override
    public Person toEntity(PersonRequestDTO dto, User user) {
        if (dto == null) return null;

        return Person.builder()
                .name(dto.name())
                .content(dto.content())
                .relation(dto.relation())
                .visibility(dto.visibility())
                .user(user)
                .build();
    }

    @Override
    public PersonResponseDTO toResponse(Person person) {
        if (person == null) return null;

        return new PersonResponseDTO(
                person.getId(),
                person.getName(),
                person.getContent(),
                person.getRelation(),
                person.getVisibility(),
                person.getCreatedAt(),
                person.getUpdatedAt());
    }
}
