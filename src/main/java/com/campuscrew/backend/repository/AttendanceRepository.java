package com.campuscrew.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.campuscrew.backend.entity.Attendance;
import com.campuscrew.backend.entity.Events;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    // this finds everyone who attended a specific event
    List<Attendance> findByEvent(Events event);

    // this checks if a user already scanned the QR code so they can't check in twice
    boolean existsByEventIdAndUserId(Long eventId, Long userId);
}
