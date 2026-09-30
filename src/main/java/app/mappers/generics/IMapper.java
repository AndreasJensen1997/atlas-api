package app.mappers.generics;

public interface IMapper <Req, Res, T, U> {

    T toEntity(Req req, U user);

    Res toResponse(T entity);


}
