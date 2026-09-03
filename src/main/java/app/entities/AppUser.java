package app.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;

import java.awt.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
@Builder
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer userId;
    private String name;
    private String email;
    private String password;


    // RELATIONS

    // 1:M
    @OneToMany(mappedBy = "user")
    @Setter
    private Set<Chapter> chapters = new HashSet<>();

    @OneToMany(mappedBy = "user")
    @Setter
    private Set<Artifact> artifacts = new HashSet<>();

    @OneToMany(mappedBy = "user")
    @Setter
    private Set<Person> people = new HashSet<>();

    @OneToMany(mappedBy = "user")
    @Setter
    private Set<Place> places = new HashSet<>();

    @OneToMany(mappedBy = "user")
    @Setter
    private Set<List> lists = new HashSet<>();

    @OneToMany(mappedBy = "user")
    @Setter
    private Set<TimeCapsule> timeCapsules = new HashSet<>();

    @OneToMany(mappedBy = "user")
    @Setter
    private Set<Fragment> fragments = new HashSet<>();





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
        AppUser appUser = (AppUser) o;
        return getUserId() != null && Objects.equals(getUserId(), appUser.getUserId());
    }



    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer()
                .getPersistentClass()
                .hashCode() : getClass().hashCode();


    }

}
