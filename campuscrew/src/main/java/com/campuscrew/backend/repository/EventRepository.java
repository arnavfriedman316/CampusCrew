package com.campuscrew.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.campuscrew.backend.entity.Events;

public interface EventRepository extends JpaRepository<Events, Long> {

    // This gives us save(), delete(), and findAll() for free!
    //this is for sorting students related to when they register, the one who registers latest shows first and so on and so forth
    List<Events> findAllByOrderByDateTimeAsc();
}
