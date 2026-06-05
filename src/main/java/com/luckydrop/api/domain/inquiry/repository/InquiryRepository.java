package com.luckydrop.api.domain.inquiry.repository;

import com.luckydrop.api.domain.inquiry.entity.Inquiry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InquiryRepository extends JpaRepository<Inquiry, Long> {
    List<Inquiry> findAllByUserIdOrderByCreatedAtDesc(Long userId);
}
