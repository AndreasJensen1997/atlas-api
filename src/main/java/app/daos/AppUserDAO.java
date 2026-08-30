package app.daos;


import app.entities.AppUser;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import lombok.AllArgsConstructor;

import java.util.Set;

@AllArgsConstructor
public class AppUserDAO implements IDAO<AppUser, Integer> {
    EntityManagerFactory emf;


    @Override
    public AppUser create(AppUser appUser) {
        EntityManager em = emf.createEntityManager();

        em.getTransaction().begin();
        em.persist(appUser);
        em.getTransaction().commit();
        em.close();
        return appUser;
    }

    @Override
    public AppUser getById(Integer id) {
        EntityManager em = emf.createEntityManager();

        em.getTransaction().begin();
        AppUser appUser = em.find(AppUser.class, id);
        em.getTransaction().commit();
        em.close();
        return appUser;
    }

    @Override
    public Set<AppUser> getAll() {
        EntityManager em = emf.createEntityManager();

        em.getTransaction().begin();
        Set<AppUser> allAppUsers = (Set<AppUser>) em.createQuery("SELECT u FROM AppUser u ", AppUser.class)
                .getResultList();
        em.getTransaction().commit();
        em.close();

        return allAppUsers;
    }

    @Override
    public AppUser update(AppUser appUser) {
        EntityManager em = emf.createEntityManager();

        em.getTransaction().begin();
        em.merge(appUser);
        em.getTransaction().commit();
        em.close();

        return appUser;
    }

    @Override
    public void delete(AppUser appUser) {
        EntityManager em = emf.createEntityManager();

        em.getTransaction().begin();
        em.remove(appUser);
        em.getTransaction().commit();
        em.close();
    }
}
