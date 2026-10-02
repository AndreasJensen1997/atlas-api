package app.daos.userOwned;

import app.daos.generics.UserOwnedDAO;
import app.entities.GeminiPrompt;
import jakarta.persistence.EntityManagerFactory;

public class GeminiPromptDAO extends UserOwnedDAO<GeminiPrompt, Integer> {

    // ===== Constructor =====

    public GeminiPromptDAO(EntityManagerFactory emf) {
        super(emf, GeminiPrompt.class);
    }
}