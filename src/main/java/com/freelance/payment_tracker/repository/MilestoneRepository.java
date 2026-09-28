package com.freelance.payment_tracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.freelance.payment_tracker.entity.Milestone;

public interface MilestoneRepository extends JpaRepository<Milestone, Long> {
}