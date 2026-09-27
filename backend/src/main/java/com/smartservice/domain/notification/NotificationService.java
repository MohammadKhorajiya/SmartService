package com.smartservice.domain.notification;

import com.smartservice.common.dto.PageResponse;
import com.smartservice.common.exception.ResourceNotFoundException;
import com.smartservice.common.exception.UnauthorizedAccessException;
import com.smartservice.common.util.SecurityUtils;
import com.smartservice.domain.notification.dto.NotificationDTO;
import com.smartservice.domain.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional
    public void sendNotification(User user, String title, String message, String type) {
        if (user == null) return;
        Notification n = Notification.builder()
                .user(user)
                .title(title)
                .message(message)
                .notificationType(type)
                .readStatus(false)
                .build();
        notificationRepository.save(n);
    }

    @Transactional(readOnly = true)
    public PageResponse<NotificationDTO> getUserNotifications(Pageable pageable) {
        Long userId = SecurityUtils.getCurrentUserId();
        Page<Notification> page = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return PageResponse.from(page.map(this::mapToDTO));
    }

    @Transactional(readOnly = true)
    public long getUnreadCount() {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) return 0;
        return notificationRepository.countByUserIdAndReadStatusFalse(userId);
    }

    @Transactional
    public void markAsRead(Long id) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        Notification n = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", id));

        if (n.getUser() == null || !n.getUser().getId().equals(currentUserId)) {
            throw new UnauthorizedAccessException("You are not authorized to modify another user's notifications");
        }

        n.setReadStatus(true);
        notificationRepository.save(n);
    }

    public NotificationDTO mapToDTO(Notification n) {
        return NotificationDTO.builder()
                .id(n.getId())
                .title(n.getTitle())
                .message(n.getMessage())
                .notificationType(n.getNotificationType())
                .readStatus(n.isReadStatus())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
