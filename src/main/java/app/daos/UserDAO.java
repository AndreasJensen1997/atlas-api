package app.daos;


import app.entities.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import lombok.AllArgsConstructor;

import java.util.Set;

@AllArgsConstructor
public class UserDAO implements IDAO<User, Integer> {
    EntityManagerFactory emf;


    @Override
    public User create(User user) {
        EntityManager em = emf.createEntityManager();

        em.getTransaction().begin();
        em.persist(user);
        em.getTransaction().commit();
        em.close();
        return user;
    }

    @Override
    public User getById(Integer id) {
        EntityManager em = emf.createEntityManager();

        em.getTransaction().begin();
        User user = em.find(User.class, id);
        em.getTransaction().commit();
        em.close();
        return user;
    }

    @Override
    public Set<User> getAll() {
        EntityManager em = emf.createEntityManager();

        em.getTransaction().begin();
        Set<User> allUsers = (Set<User>) em.createQuery("SELECT s FROM User s ", User.class)
                .getResultList();
        em.getTransaction().commit();
        em.close();

        return allUsers;
    }

    @Override
    public User update(User user) {
        EntityManager em = emf.createEntityManager();

        em.getTransaction().begin();
        em.merge(user);
        em.getTransaction().commit();
        em.close();

        return user;
    }

    @Override
    public void delete(User user) {
        EntityManager em = emf.createEntityManager();

        em.getTransaction().begin();
        em.remove(user);
        em.getTransaction().commit();
        em.close();
    }
}
