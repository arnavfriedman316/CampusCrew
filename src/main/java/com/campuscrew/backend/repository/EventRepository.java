package com.campuscrew.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.campuscrew.backend.entity.Events;

@Repository
public interface EventRepository extends JpaRepository<Events, Long> {

    // 1. Fetches all events sorted by date/time (Upcoming first)
    List<Events> findAllByOrderByDateTimeAsc();

    // 2. NEW: Search functionality
    // Finds events where the Title OR Location contains the search word (ignoring upper/lowercase)
    List<Events> findByTitleContainingIgnoreCaseOrLocationContainingIgnoreCase(String title, String location);

}
