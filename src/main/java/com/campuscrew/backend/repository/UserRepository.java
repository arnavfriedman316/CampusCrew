package com.campuscrew.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.campuscrew.backend.entity.AppUser;

@Repository
public interface UserRepository extends JpaRepository<AppUser, Long> {

    //this allows for automatic finding of user by their email
    AppUser findByEmail(String email);
}
