package com.luckydrop.api.domain.user.repository;

import com.luckydrop.api.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByAuthId(UUID authId);
}
