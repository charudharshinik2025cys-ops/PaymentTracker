package com.freelance.payment_tracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.freelance.payment_tracker.entity.Project;

public interface ProjectRepository extends JpaRepository<Project, Long> {
}