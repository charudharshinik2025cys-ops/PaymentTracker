package com.freelance.payment_tracker.service;

import org.springframework.stereotype.Service;

import com.freelance.payment_tracker.entity.Project;
import com.freelance.payment_tracker.repository.ProjectRepository;

import java.math.BigDecimal;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;

    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    public Project createProject(Project project) {

        // Initial escrow amount is the total project amount
        project.setReleasedAmount(BigDecimal.ZERO);
        project.setEscrowBalance(project.getTotalAmount());

        return projectRepository.save(project);
    }

    public Project getProject(Long projectId) {

        return projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));
    }
}