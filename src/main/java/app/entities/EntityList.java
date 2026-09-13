package app.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
@Builder
public class EntityList {


    @Id
    @GeneratedValue
    private Integer listId;
    private String title;
    private String subtitle;
    private LocalDate createdAt;
    private LocalDate updatedAt;
    private int itemAmount;

    // RELATIONS //

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @Setter
    private AppUser appUser;

    @OneToMany(mappedBy = "entityList", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Builder.Default
    @ToString.Exclude
    private List<EntityListItem> items = new ArrayList<>();


    // ===== JPA LIFECYCLE CALLBACKS =====
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDate.now();
        updatedAt = LocalDate.now();
        calculateEntityAmount();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDate.now();
        calculateEntityAmount();
    }

    public void calculateEntityAmount() {
        if (items == null || items.isEmpty()) {
            this.itemAmount = 0;
        } else {
            this.itemAmount = items.size();
        }
    }

    // ===== RELATIONSHIP HELPERS =====
    public void addItem(EntityListItem item) {
        items.add(item);
        item.setEntityList(this);
        calculateEntityAmount();
    }

    public void removeItem(EntityListItem item) {
        items.remove(item);
        item.setEntityList(null);
        calculateEntityAmount();
    }

    public void clearAllItems() {
        for (EntityListItem item : new ArrayList<>(items)) {
            removeItem(item);
        }
    }
    // ===== EQUALS & HASHCODE =====
    @Override
    public final boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null)
            return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer()
                .getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer()
                .getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass)
            return false;
        EntityList entityList = (EntityList) o;
        return getListId() != null && Objects.equals(getListId(), entityList.getListId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer()
                .getPersistentClass()
                .hashCode() : getClass().hashCode();

    }

}
