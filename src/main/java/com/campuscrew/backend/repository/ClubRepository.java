package com.campuscrew.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.campuscrew.backend.entity.Club;

@Repository
public interface ClubRepository extends JpaRepository<Club, Long> {

    Club findByName(String name);
}
