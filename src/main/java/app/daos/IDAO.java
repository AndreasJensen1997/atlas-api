package app.daos;
import java.util.Set;


public interface IDAO<T, I> {


    T create(T t);

    T getById(I i);

    Set<T> getAll();

    T update ( T t);

    void delete (T t);



}
