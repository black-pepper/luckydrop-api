package com.luckydrop.api.domain.invitationcode.repository;

import com.luckydrop.api.domain.invitationcode.entity.InvitationCode;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InvitationCodeRepository extends JpaRepository<InvitationCode, Long> {

    @Query("""
            SELECT ic
            FROM InvitationCode ic
            JOIN FETCH ic.content c
            WHERE c.id = :contentId
            ORDER BY ic.createdAt DESC
            """)
    List<InvitationCode> findAllByContentId(@Param("contentId") Long contentId);

    @Query("""
            SELECT ic
            FROM InvitationCode ic
            JOIN FETCH ic.content c
            LEFT JOIN FETCH c.user
            WHERE ic.id = :invitationCodeId
            """)
    Optional<InvitationCode> findByIdWithContentAndUser(@Param("invitationCodeId") Long invitationCodeId);

    boolean existsByContentIdAndCode(Long contentId, String code);

    @Query("""
            SELECT ic
            FROM InvitationCode ic
            JOIN FETCH ic.content c
            WHERE c.code = :contentCode
              AND ic.code = :invitationCode
            """)
    Optional<InvitationCode> findByContentCodeAndCodeWithContent(
            @Param("contentCode") String contentCode,
            @Param("invitationCode") String invitationCode
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT ic
            FROM InvitationCode ic
            JOIN FETCH ic.content c
            WHERE c.code = :contentCode
              AND ic.code = :invitationCode
            """)
    Optional<InvitationCode> findByContentCodeAndCodeWithLock(
            @Param("contentCode") String contentCode,
            @Param("invitationCode") String invitationCode
    );
}
