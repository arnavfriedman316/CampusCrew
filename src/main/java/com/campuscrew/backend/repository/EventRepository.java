package com.campuscrew.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.campuscrew.backend.entity.Events;

@Repository
public interface EventRepository extends JpaRepository<Events, Long> {

    // this will sort the events according to the date and time, in first come first served model
    List<Events> findAllByOrderByDateTimeAsc();

    // This will allow user to find the events, like by searching them
    List<Events> findByTitleContainingIgnoreCaseOrLocationContainingIgnoreCase(String title, String location);

}
