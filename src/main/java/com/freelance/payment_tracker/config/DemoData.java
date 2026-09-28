package com.freelance.payment_tracker.config;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.freelance.payment_tracker.entity.Client;
import com.freelance.payment_tracker.entity.Freelancer;
import com.freelance.payment_tracker.entity.Milestone;
import com.freelance.payment_tracker.entity.MilestoneStatus;
import com.freelance.payment_tracker.entity.Project;
import com.freelance.payment_tracker.entity.ProjectStatus;
import com.freelance.payment_tracker.entity.Release;
import com.freelance.payment_tracker.repository.ClientRepository;
import com.freelance.payment_tracker.repository.FreelancerRepository;
import com.freelance.payment_tracker.repository.ProjectRepository;

@Configuration
public class DemoData {
    @Bean
    CommandLineRunner seedData(ClientRepository clients, FreelancerRepository freelancers, ProjectRepository projects) {
        return args -> {
            if (projects.count() > 0) return;

            Client northstar = client("Northstar Studio", "hello@northstar.studio");
            Client cedar = client("Cedar & Co.", "team@cedar.co");
            clients.saveAll(List.of(northstar, cedar));

            Freelancer mina = freelancer("Mina Park", "mina@studio.dev", "Product designer");
            Freelancer theo = freelancer("Theo Alvarez", "theo@build.dev", "Full-stack developer");
            freelancers.saveAll(List.of(mina, theo));

            Project atlas = project("Atlas brand system", "A complete identity system for a new digital product.", northstar, mina, 8400, 2100, 6300);
            atlas.setMilestones(List.of(
                    milestone(atlas, "Discovery & direction", 2100, -6, MilestoneStatus.RELEASED),
                    milestone(atlas, "Visual identity", 2100, 2, MilestoneStatus.SUBMITTED),
                    milestone(atlas, "Design system", 2100, 8, MilestoneStatus.IN_PROGRESS),
                    milestone(atlas, "Launch files", 2100, 16, MilestoneStatus.FUNDED)));
            Release previous = new Release();
            previous.setAmount(new BigDecimal("2100.00"));
            previous.setReleasedAt(LocalDateTime.now().minusDays(5));
            previous.setStatus("RELEASED");
            previous.setMilestone(atlas.getMilestones().get(0));
            atlas.getMilestones().get(0).setRelease(previous);

            Project cedarProject = project("Cedar storefront", "A clean, fast storefront for the seasonal collection.", cedar, theo, 4400, 0, 4400);
            cedarProject.setMilestones(List.of(
                    milestone(cedarProject, "Storefront wireframes", 2200, -2, MilestoneStatus.APPROVED),
                    milestone(cedarProject, "Checkout integration", 2200, 11, MilestoneStatus.FUNDED)));
            projects.saveAll(List.of(atlas, cedarProject));
        };
    }

    private Client client(String name, String email) {
        Client client = new Client();
        client.setName(name);
        client.setEmail(email);
        return client;
    }

    private Freelancer freelancer(String name, String email, String skill) {
        Freelancer freelancer = new Freelancer();
        freelancer.setName(name);
        freelancer.setEmail(email);
        freelancer.setSkill(skill);
        return freelancer;
    }

    private Project project(String title, String description, Client client, Freelancer freelancer,
            long total, long released, long escrow) {
        Project project = new Project();
        project.setTitle(title);
        project.setDescription(description);
        project.setClient(client);
        project.setFreelancer(freelancer);
        project.setTotalAmount(BigDecimal.valueOf(total));
        project.setReleasedAmount(BigDecimal.valueOf(released));
        project.setEscrowBalance(BigDecimal.valueOf(escrow));
        project.setStatus(ProjectStatus.ACTIVE);
        return project;
    }

    private Milestone milestone(Project project, String title, long amount, int dueDays, MilestoneStatus status) {
        Milestone milestone = new Milestone();
        milestone.setProject(project);
        milestone.setTitle(title);
        milestone.setDescription(title + " delivery for " + project.getTitle() + ".");
        milestone.setAmount(BigDecimal.valueOf(amount));
        milestone.setDueDate(LocalDateTime.now().plusDays(dueDays));
        milestone.setStatus(status);
        if (status == MilestoneStatus.SUBMITTED || status == MilestoneStatus.APPROVED || status == MilestoneStatus.RELEASED) {
            milestone.setDeliveredAt(LocalDateTime.now().minusDays(1));
        }
        if (status == MilestoneStatus.APPROVED || status == MilestoneStatus.RELEASED) {
            milestone.setApprovedAt(LocalDateTime.now().minusHours(5));
        }
        return milestone;
    }
}