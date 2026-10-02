package app.daos.userOwned;

import app.daos.generics.UserOwnedDAO;
import app.entities.Fragment;
import jakarta.persistence.EntityManagerFactory;

public class FragmentDAO extends UserOwnedDAO<Fragment, Integer> {

    // ===== Constructor =====

    public FragmentDAO(EntityManagerFactory emf) {
        super(emf, Fragment.class);
    }
}
