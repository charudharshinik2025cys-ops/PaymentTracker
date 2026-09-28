package com.freelance.payment_tracker.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "releases")
public class Release {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

        @Column(nullable = false, precision = 12, scale = 2)
        private BigDecimal amount;

        @Column(name = "released_at", nullable = false)
    private LocalDateTime releasedAt;

        @Column(nullable = false, length = 24)
    private String status;

    @OneToOne
        @JoinColumn(name = "milestone_id", nullable = false, unique = true,
            foreignKey = @ForeignKey(name = "fk_releases_milestone"))
    private Milestone milestone;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public LocalDateTime getReleasedAt() { return releasedAt; }
    public void setReleasedAt(LocalDateTime releasedAt) { this.releasedAt = releasedAt; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Milestone getMilestone() { return milestone; }
    public void setMilestone(Milestone milestone) { this.milestone = milestone; }
}