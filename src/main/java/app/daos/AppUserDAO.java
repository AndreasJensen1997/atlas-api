package app.daos;


import app.entities.AppUser;
import app.exceptions.ApiException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceException;
import lombok.AllArgsConstructor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@AllArgsConstructor
public class AppUserDAO implements IDAO<AppUser, Integer> {
    EntityManagerFactory emf;


    @Override
    public AppUser create(AppUser appUser) {
        if (appUser == null) {
            throw new ApiException(400, "User is required");
        }
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            try {
                em.persist(appUser);
                em.getTransaction().commit();
            } catch (PersistenceException e) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                throw new ApiException(500, "Create user failed: " + e.getMessage());
            } catch (RuntimeException e) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                throw e;
            }
        }
        return appUser;
    }

    @Override
    public AppUser getById(Integer id) {
        if (id == null) {
            throw new ApiException(400, "Study id is required");
        }
        try (EntityManager em = emf.createEntityManager()) {
            try {
                AppUser appUser = em.find(AppUser.class, id);
                if (appUser != null) {
                    return appUser;
                }
                throw new ApiException(404, "User not found");
            } catch (PersistenceException e) {
                throw new ApiException(500, "Get user failed: " + e.getMessage());
            }
        }
    }

    @Override
    public Set<AppUser> getAll() {
        EntityManager em = emf.createEntityManager();
        try {
            List<AppUser> allAppUsers = em.createQuery("SELECT u FROM AppUser u", AppUser.class)
                    .getResultList();
            return new HashSet<>(allAppUsers);
        } finally {
            em.close();
        }
    }

    @Override
    public AppUser update(AppUser appUser) {
        if (appUser == null || appUser.getUserId() == null) {
            throw new ApiException(400, "User id is required");
        }
        AppUser updated;
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            try {
                AppUser existing = em.find(AppUser.class, appUser.getUserId());
                if (existing == null) {
                    throw new ApiException(404, "User not found");
                }
                updated = em.merge(appUser);
                em.getTransaction().commit();
            } catch (PersistenceException e) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                throw new ApiException(500, "Update user failed: " + e.getMessage());
            } catch (RuntimeException e) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                throw e;
            }
        }
        return updated;
    }

@Override
public boolean delete(Integer id) {
    if (id == null) {
        throw new ApiException(400, "User id is required");
    }
    try (EntityManager em = emf.createEntityManager()) {
        em.getTransaction().begin();
        try {
            AppUser userToRemove = em.find(AppUser.class, id);
            if (userToRemove != null) {
                em.remove(userToRemove);
            } else {
                throw new ApiException(404, "User not found");
            }
            em.getTransaction().commit();
        } catch (PersistenceException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new ApiException(500, "User study failed: " + e.getMessage());
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        }
    }
    return true;
}
}
