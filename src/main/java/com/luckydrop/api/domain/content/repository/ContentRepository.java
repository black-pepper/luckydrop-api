package com.luckydrop.api.domain.content.repository;

import com.luckydrop.api.domain.content.entity.Content;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ContentRepository extends JpaRepository<Content, Long> {

    boolean existsByCode(String code);

    @Query("SELECT c FROM Content c LEFT JOIN FETCH c.user WHERE c.code = :code")
    Optional<Content> findByCodeWithUser(@Param("code") String code);

    @Query("""
            SELECT c
            FROM Content c
            LEFT JOIN FETCH c.user
            WHERE c.user.id = :userId
            ORDER BY c.createdAt DESC
            """)
    List<Content> findAllActiveByUserIdWithUser(@Param("userId") Long userId);
}
