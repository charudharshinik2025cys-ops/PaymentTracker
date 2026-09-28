package com.freelance.payment_tracker.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.transaction.annotation.Transactional;
import io.swagger.v3.oas.annotations.Operation;

import com.freelance.payment_tracker.entity.Milestone;
import com.freelance.payment_tracker.service.MilestoneService;
import com.freelance.payment_tracker.repository.ProjectRepository;

@RestController
@RequestMapping("/api")
public class TrackerController {
    private final MilestoneService milestoneService;
    private final ProjectRepository projectRepository;

    public TrackerController(MilestoneService milestoneService, ProjectRepository projectRepository) {
        this.milestoneService = milestoneService;
        this.projectRepository = projectRepository;
    }

        @Operation(summary = "Get escrow and milestone overview", tags = { "Dashboard" })
        @GetMapping("/dashboard")
        @Transactional(readOnly = true)
    public Dashboard dashboard() {
        List<ProjectView> projects = projectRepository.findAll().stream()
                .map(project -> new ProjectView(project.getId(), project.getTitle(), project.getDescription(),
                        project.getClient().getName(), project.getFreelancer().getName(),
                        project.getTotalAmount(), project.getReleasedAmount(), project.getEscrowBalance(),
                        project.getStatus().name()))
                .toList();
        List<MilestoneView> milestones = milestoneService.getAll().stream()
                .map(milestone -> new MilestoneView(milestone.getId(), milestone.getTitle(), milestone.getDescription(),
                        milestone.getProject().getId(), milestone.getProject().getTitle(),
                        milestone.getProject().getClient().getName(), milestone.getProject().getFreelancer().getName(),
                        milestone.getAmount(), milestone.getDueDate(), milestone.getStatus().name()))
                .toList();
        BigDecimal funded = projects.stream().map(ProjectView::escrowBalance).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal released = projects.stream().map(ProjectView::releasedAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        long awaitingApproval = milestones.stream().filter(item -> item.status().equals("SUBMITTED")).count();
        return new Dashboard(projects, milestones, funded, released, awaitingApproval, LocalDateTime.now());
    }

        @Operation(summary = "Advance a milestone workflow", tags = { "Milestone Workflow" })
        @PostMapping("/milestones/{id}/advance")
        @Transactional
    public MilestoneView advance(@PathVariable Long id) {
        Milestone milestone = milestoneService.advance(id);
        return new MilestoneView(milestone.getId(), milestone.getTitle(), milestone.getDescription(),
                milestone.getProject().getId(), milestone.getProject().getTitle(),
                milestone.getProject().getClient().getName(), milestone.getProject().getFreelancer().getName(),
                milestone.getAmount(), milestone.getDueDate(), milestone.getStatus().name());
    }

    public record Dashboard(List<ProjectView> projects, List<MilestoneView> milestones, BigDecimal escrowBalance,
            BigDecimal releasedTotal, long awaitingApproval, LocalDateTime updatedAt) { }

    public record ProjectView(Long id, String title, String description, String client, String freelancer,
            BigDecimal totalAmount, BigDecimal releasedAmount, BigDecimal escrowBalance, String status) { }

    public record MilestoneView(Long id, String title, String description, Long projectId, String projectTitle,
            String client, String freelancer, BigDecimal amount, LocalDateTime dueDate, String status) { }
}