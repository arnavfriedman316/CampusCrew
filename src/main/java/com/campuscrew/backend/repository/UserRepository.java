package com.campuscrew.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.campuscrew.backend.entity.AppUser;

@Repository
public interface UserRepository extends JpaRepository<AppUser, Long> {

    // This adds the functionality to find users by email automatically
    AppUser findByEmail(String email);
}
