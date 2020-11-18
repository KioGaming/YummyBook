package pp.ua.library.yummybook.spring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pp.ua.library.yummybook.domain.UserRole;

public interface UserRoleRepository extends JpaRepository<UserRole, Long> {
}
