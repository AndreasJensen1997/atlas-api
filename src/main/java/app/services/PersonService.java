package app.services;

import app.daos.userOwned.PersonDAO;
import app.dtos.Person.PersonRequestDTO;
import app.entities.Person;
import app.entities.User;
import app.mappers.PersonMapper;

import java.util.List;

public class PersonService {

    private final PersonDAO personDAO;
    private final UserService userService;
    private final PersonMapper personMapper;

    public PersonService(PersonDAO personDAO, UserService userService, PersonMapper personMapper) {
        this.personDAO = personDAO;
        this.userService = userService;
        this.personMapper = personMapper;
    }

    public Person createPerson(PersonRequestDTO dto, int userId) {
        User owner = userService.getById(userId);

        // UserOwnedDAO's findByTitleAndUserId automatically handles 'name' for Person entities
        if (personDAO.findByTitleAndUserId(dto.name(), owner.getUserId()) != null) {
            throw new IllegalArgumentException("A person with this name already exists.");
        }

        Person newPerson = personMapper.toEntity(dto, owner);
        return personDAO.create(newPerson);
    }

    public Person getById(Integer personId, int userId) {
        Person person = personDAO.getById(personId);

        if (person == null) {
            throw new IllegalArgumentException("Person not found with ID: " + personId);
        }

        if (!person.getUser().getUserId().equals(userId)) {
            throw new IllegalArgumentException("You do not have permission to access this person.");
        }
        return person;
    }

    public List<Person> getAllById(int userId) {
        return personDAO.getAllByUserId(userId);
    }

    public void delete(Integer personId, int userId) {
        Person person = getById(personId, userId);
        personDAO.delete(person.getId());
    }

    public Person update(Integer personId, PersonRequestDTO dto, int userId) {
        Person person = personDAO.getById(personId);

        if (person == null) {
            throw new IllegalArgumentException("Person not found with ID: " + personId);
        }

        if (!person.getUser().getUserId().equals(userId)) {
            throw new IllegalArgumentException("You do not have permission to access this person.");
        }

        person.setName(dto.name());
        person.setContent(dto.content());
        person.setRelation(dto.relation());
        person.setVisibility(dto.visibility());

        return personDAO.update(person);
    }

    public Person getRandom(Integer userId) {
        Person randomPerson = personDAO.getRandomByUserId(userId);

        if (randomPerson == null) {
            throw new IllegalArgumentException("No people were found");
        }

        if (!randomPerson.getUser().getUserId().equals(userId)) {
            throw new IllegalArgumentException("You do not have permission to access this person.");
        }

        return randomPerson;
    }
}