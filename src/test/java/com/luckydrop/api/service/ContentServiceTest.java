package com.luckydrop.api.service;

import com.luckydrop.api.common.exception.DrawEventException;
import com.luckydrop.api.common.exception.ErrorCode;
import com.luckydrop.api.domain.content.dto.ParticipantContentDetailResponse;
import com.luckydrop.api.domain.content.entity.Content;
import com.luckydrop.api.domain.content.entity.ContentType;
import com.luckydrop.api.domain.content.repository.ContentRepository;
import com.luckydrop.api.domain.user.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContentServiceTest {

    @Mock
    private ContentRepository contentRepository;

    @InjectMocks
    private ContentService contentService;

    @Test
    void getDetailReturnsParticipantContentDetailWhenContentIsActive() {
        Content content = createContent("CONTENT-001", false);
        when(contentRepository.findByCodeWithUser("CONTENT-001")).thenReturn(Optional.of(content));

        ParticipantContentDetailResponse response = contentService.getDetail("CONTENT-001");

        assertThat(response.getCode()).isEqualTo("CONTENT-001");
        assertThat(response.getTitle()).isEqualTo("Lucky Drop");
        assertThat(response.getDescription()).isEqualTo("Event description");
    }

    @Test
    void getDetailThrowsWhenContentIsDeleted() {
        Content deletedContent = createContent("CONTENT-002", true);
        when(contentRepository.findByCodeWithUser("CONTENT-002")).thenReturn(Optional.of(deletedContent));

        assertThatThrownBy(() -> contentService.getDetail("CONTENT-002"))
                .isInstanceOf(DrawEventException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.CONTENT_NOT_FOUND);
    }

    private Content createContent(String code, boolean deleted) {
        Content content = new Content(code, ContentType.DRAW, new User(), "Lucky Drop", "Event description");
        ReflectionTestUtils.setField(content, "createdAt", OffsetDateTime.parse("2026-04-06T10:15:30+09:00"));
        if (deleted) {
            ReflectionTestUtils.setField(content, "deletedAt", OffsetDateTime.parse("2026-04-06T11:15:30+09:00"));
        }
        return content;
    }
}
