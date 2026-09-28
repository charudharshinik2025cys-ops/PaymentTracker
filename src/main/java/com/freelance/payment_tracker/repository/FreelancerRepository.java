package com.freelance.payment_tracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.freelance.payment_tracker.entity.Freelancer;

public interface FreelancerRepository extends JpaRepository<Freelancer, Long> {
}