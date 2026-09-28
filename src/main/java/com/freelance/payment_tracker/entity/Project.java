package com.freelance.payment_tracker.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "projects")
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 180)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal releasedAmount = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal escrowBalance = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private ProjectStatus status;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false, foreignKey = @ForeignKey(name = "fk_projects_client"))
    private Client client;

    @ManyToOne
    @JoinColumn(name = "freelancer_id", nullable = false, foreignKey = @ForeignKey(name = "fk_projects_freelancer"))
    private Freelancer freelancer;

    @OneToMany(
        mappedBy = "project",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private List<Milestone> milestones = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public BigDecimal getReleasedAmount() { return releasedAmount; }
    public void setReleasedAmount(BigDecimal releasedAmount) { this.releasedAmount = releasedAmount; }
    public BigDecimal getEscrowBalance() { return escrowBalance; }
    public void setEscrowBalance(BigDecimal escrowBalance) { this.escrowBalance = escrowBalance; }
    public ProjectStatus getStatus() { return status; }
    public void setStatus(ProjectStatus status) { this.status = status; }
    public Client getClient() { return client; }
    public void setClient(Client client) { this.client = client; }
    public Freelancer getFreelancer() { return freelancer; }
    public void setFreelancer(Freelancer freelancer) { this.freelancer = freelancer; }
    public List<Milestone> getMilestones() { return milestones; }
    public void setMilestones(List<Milestone> milestones) { this.milestones = milestones; }
}