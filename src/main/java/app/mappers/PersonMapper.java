package app.mappers;

import app.dtos.Person.PersonRequestDTO;
import app.dtos.Person.PersonResponseDTO;
import app.entities.Person;
import app.entities.User;

public class PersonMapper {
    public Person toEntity(PersonRequestDTO dto, User user) {
        return Person.builder()
                .name(dto.name())
                .content(dto.content())
                .relation(dto.relation())
                .visibility(dto.visibility())
                .user(user)
                .build();
    }

    public PersonResponseDTO toResponseDTO(Person person) {
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
