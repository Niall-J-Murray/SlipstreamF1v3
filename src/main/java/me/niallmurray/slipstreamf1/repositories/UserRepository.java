package me.niallmurray.slipstreamf1.repositories;

import me.niallmurray.slipstreamf1.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByUsername(String username);

  Optional<User> findByUsernameIgnoreCase(String username);

  Boolean existsByUsername(String username);

  Boolean existsByEmail(String email);

  User findByEmail(String email);
}
