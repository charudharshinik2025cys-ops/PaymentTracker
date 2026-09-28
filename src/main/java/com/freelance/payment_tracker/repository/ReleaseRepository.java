package com.freelance.payment_tracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.freelance.payment_tracker.entity.Release;

public interface ReleaseRepository extends JpaRepository<Release, Long> {
}