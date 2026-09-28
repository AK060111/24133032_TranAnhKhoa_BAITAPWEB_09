package vn.iotstar.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.iotstar.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmailIgnoreCase(String email);

    @Query("""
            SELECT u
            FROM User u
            JOIN FETCH u.role
            WHERE LOWER(u.email) = LOWER(:email)
            """)
    Optional<User> findByEmailWithRole(@Param("email") String email);
}
