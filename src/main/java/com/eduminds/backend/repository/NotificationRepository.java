package com.eduminds.backend.repository;
import com.eduminds.backend.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Notification> findByUserIdAndEstLueFalseOrderByCreatedAtDesc(Long userId);
    long countByUserIdAndEstLueFalse(Long userId);
}
