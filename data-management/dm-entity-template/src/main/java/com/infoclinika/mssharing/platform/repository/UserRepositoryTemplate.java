package com.infoclinika.mssharing.platform.repository;

import com.infoclinika.mssharing.platform.entity.UserTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

/**
 * @author : Alexander Serebriyan
 */
public interface UserRepositoryTemplate<T extends UserTemplate> extends JpaRepository<T, Long> {
    @Query("select u from #{#entityName} u left join u.changeEmailRequest where u.personData.email = :email")
    @Nullable
    T findByEmail(@Param("email") String email);

    @Query("select u.id from #{#entityName} u where u.personData.email = :email and u.passwordHash = :hash")
    Long findByCredentials(@Param("email") String email, @Param("hash") String passwordHash);

    @Query("select distinct u from #{#entityName} u join u.labMemberships ship where ship.lab.id=:labId")
    List<T> findAllUsersByLab(@Param("labId") Long labId);

    @Query("select u from #{#entityName} u where u.admin = true")
    List<T> findAdmins();

    @Query("select u.id from #{#entityName} u")
    Set<Long> findAllIds();


    @Query("select c.id from GroupTemplate g join g.collaborators c where g.id=:groupId")
    Set<Long> findUserIdsByGroup(@Param("groupId") Long groupId);

    @Query("select new com.infoclinika.mssharing.platform.repository.UserRepositoryTemplate$UserShortRecord(" +
            "u.id, u.personData.firstName, u.personData.lastName, u.personData.email" +
            ") from #{#entityName} u")
    <S extends UserShortRecord> List<S> findShortRecordsAll();

    /**
     * @author Herman Zamula
     */
    class UserShortRecord {

        public final long id;
        public final String firstName;
        public final String lastName;
        public final String email;
        public final String fullName;

        public UserShortRecord(long id, String firstName, String lastName, String email) {
            this.id = id;
            this.firstName = firstName;
            this.lastName = lastName;
            this.email = email;
            this.fullName = firstName + " " + lastName;
        }
    }

}
