package com.freelance.payment_tracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.freelance.payment_tracker.entity.Client;

public interface ClientRepository extends JpaRepository<Client, Long> {
}