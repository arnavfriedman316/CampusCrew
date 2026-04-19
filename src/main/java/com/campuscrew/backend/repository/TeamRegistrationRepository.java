package com.campuscrew.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.campuscrew.backend.entity.TeamRegistration;
import com.campuscrew.backend.entity.Events;
import com.campuscrew.backend.entity.AppUser;

@Repository
public interface TeamRegistrationRepository extends JpaRepository<TeamRegistration, Long> {
    TeamRegistration findByEventAndLeader(Events event, AppUser leader);
    void deleteByEventAndLeader(Events event, AppUser leader);
}
