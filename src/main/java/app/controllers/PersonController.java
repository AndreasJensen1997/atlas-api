package app.controllers;

import app.controllers.generics.AbstractController;
import app.dtos.Person.PersonRequestDTO;
import app.dtos.Person.PersonResponseDTO;
import app.entities.Person;
import app.mappers.PersonMapper;
import app.services.PersonService;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.Context;

import java.util.List;

import static io.javalin.apibuilder.ApiBuilder.*;

public class PersonController extends AbstractController<PersonRequestDTO, PersonResponseDTO, Person, Integer> implements EndpointGroup {

    private final PersonService personService;
    private final PersonMapper personMapper;

    public PersonController(PersonService personService, PersonMapper personMapper) {
        this.personService = personService;
        this.personMapper = personMapper;
    }

    @Override
    protected PersonRequestDTO parseBody(Context ctx) {
        return ctx.bodyAsClass(PersonRequestDTO.class);
    }

    @Override
    protected Person fetchEntityById(Integer personId, Integer userId) {
        return personService.getById(personId, userId);
    }

    @Override
    protected List<Person> fetchAllByUserId(Integer userId) {
        return personService.getAllById(userId);
    }

    @Override
    protected Person getRandom(Integer userId) {
        return personService.getRandom(userId);
    }

    @Override
    protected Integer parseId(String idStr) {
        return Integer.parseInt(idStr);
    }

    @Override
    protected Person createEntity(PersonRequestDTO dto, Integer userId) {
        return personService.createPerson(dto, userId);
    }

    @Override
    protected void deleteEntity(Integer entityId, Integer userId) {
        personService.delete(entityId, userId);
    }

    @Override
    protected Person updateEntity(Integer entityId, PersonRequestDTO personRequestDTO, Integer userId) {
        return personService.update(entityId, personRequestDTO, userId);
    }

    @Override
    protected PersonResponseDTO mapToResponse(Person entity) {
        return personMapper.toResponseDTO(entity);
    }

    @Override
    public void addEndpoints() {
        post("/api/people", this::create);
        get("/api/people/random", this::randomByUserId);
        get("/api/people/{id}", this::getById);
        get("/api/people", this::getAllById);
        delete("/api/people/{id}", this::deleteById);
        put("/api/people/{id}", this::updateById);
    }
}