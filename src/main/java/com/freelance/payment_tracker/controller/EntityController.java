package com.freelance.payment_tracker.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import io.swagger.v3.oas.annotations.Operation;

import com.freelance.payment_tracker.entity.Client;
import com.freelance.payment_tracker.entity.Freelancer;
import com.freelance.payment_tracker.entity.Milestone;
import com.freelance.payment_tracker.entity.MilestoneStatus;
import com.freelance.payment_tracker.entity.Project;
import com.freelance.payment_tracker.entity.ProjectStatus;
import com.freelance.payment_tracker.entity.Release;
import com.freelance.payment_tracker.repository.ClientRepository;
import com.freelance.payment_tracker.repository.FreelancerRepository;
import com.freelance.payment_tracker.repository.MilestoneRepository;
import com.freelance.payment_tracker.repository.ProjectRepository;
import com.freelance.payment_tracker.repository.ReleaseRepository;

@RestController
@RequestMapping("/api")
@Transactional
public class EntityController {
    private final ClientRepository clientRepository;
    private final FreelancerRepository freelancerRepository;
    private final ProjectRepository projectRepository;
    private final MilestoneRepository milestoneRepository;
    private final ReleaseRepository releaseRepository;

    public EntityController(ClientRepository clientRepository, FreelancerRepository freelancerRepository,
            ProjectRepository projectRepository, MilestoneRepository milestoneRepository,
            ReleaseRepository releaseRepository) {
        this.clientRepository = clientRepository;
        this.freelancerRepository = freelancerRepository;
        this.projectRepository = projectRepository;
        this.milestoneRepository = milestoneRepository;
        this.releaseRepository = releaseRepository;
    }

    @Operation(summary = "List clients", tags = { "Client Tracker" })
    @GetMapping("/clients")
    public List<ClientView> clients() {
        return clientRepository.findAll().stream().map(EntityController::clientView).toList();
    }

    @Operation(summary = "Get a client", tags = { "Client Tracker" })
    @GetMapping("/clients/{id}")
    public ClientView client(@PathVariable Long id) {
        return clientView(findClient(id));
    }

    @Operation(summary = "Create a client", tags = { "Client Tracker" })
    @PostMapping("/clients")
    public ClientView createClient(@RequestBody ClientData data) {
        Client client = new Client();
        client.setName(data.name());
        client.setEmail(data.email());
        return clientView(clientRepository.save(client));
    }

    @Operation(summary = "Update a client", tags = { "Client Tracker" })
    @PutMapping("/clients/{id}")
    public ClientView updateClient(@PathVariable Long id, @RequestBody ClientData data) {
        Client client = findClient(id);
        client.setName(data.name());
        client.setEmail(data.email());
        return clientView(clientRepository.save(client));
    }

    @Operation(summary = "Delete a client", tags = { "Client Tracker" })
    @DeleteMapping("/clients/{id}")
    public void deleteClient(@PathVariable Long id) {
        delete(clientRepository, findClient(id), "Client has projects and cannot be deleted");
    }

    @Operation(summary = "List freelancers", tags = { "Freelancer Tracker" })
    @GetMapping("/freelancers")
    public List<FreelancerView> freelancers() {
        return freelancerRepository.findAll().stream().map(EntityController::freelancerView).toList();
    }

    @Operation(summary = "Get a freelancer", tags = { "Freelancer Tracker" })
    @GetMapping("/freelancers/{id}")
    public FreelancerView freelancer(@PathVariable Long id) {
        return freelancerView(findFreelancer(id));
    }

    @Operation(summary = "Create a freelancer", tags = { "Freelancer Tracker" })
    @PostMapping("/freelancers")
    public FreelancerView createFreelancer(@RequestBody FreelancerData data) {
        Freelancer freelancer = new Freelancer();
        apply(freelancer, data);
        return freelancerView(freelancerRepository.save(freelancer));
    }

    @Operation(summary = "Update a freelancer", tags = { "Freelancer Tracker" })
    @PutMapping("/freelancers/{id}")
    public FreelancerView updateFreelancer(@PathVariable Long id, @RequestBody FreelancerData data) {
        Freelancer freelancer = findFreelancer(id);
        apply(freelancer, data);
        return freelancerView(freelancerRepository.save(freelancer));
    }

    @Operation(summary = "Delete a freelancer", tags = { "Freelancer Tracker" })
    @DeleteMapping("/freelancers/{id}")
    public void deleteFreelancer(@PathVariable Long id) {
        delete(freelancerRepository, findFreelancer(id), "Freelancer has projects and cannot be deleted");
    }

    @Operation(summary = "List projects", tags = { "Project Tracker" })
    @GetMapping("/projects")
    public List<ProjectView> projects() {
        return projectRepository.findAll().stream().map(EntityController::projectView).toList();
    }

    @Operation(summary = "Get a project", tags = { "Project Tracker" })
    @GetMapping("/projects/{id}")
    public ProjectView project(@PathVariable Long id) {
        return projectView(findProject(id));
    }

    @Operation(summary = "Create a project", tags = { "Project Tracker" })
    @PostMapping("/projects")
    public ProjectView createProject(@RequestBody ProjectData data) {
        Project project = new Project();
        project.setReleasedAmount(BigDecimal.ZERO);
        project.setEscrowBalance(data.totalAmount());
        apply(project, data);
        return projectView(projectRepository.save(project));
    }

    @Operation(summary = "Update a project", tags = { "Project Tracker" })
    @PutMapping("/projects/{id}")
    public ProjectView updateProject(@PathVariable Long id, @RequestBody ProjectData data) {
        Project project = findProject(id);
        BigDecimal delta = data.totalAmount().subtract(project.getTotalAmount());
        if (project.getReleasedAmount().compareTo(data.totalAmount()) > 0
                || project.getEscrowBalance().add(delta).signum() < 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "The new total cannot be less than released payments or remaining escrow");
        }
        project.setEscrowBalance(project.getEscrowBalance().add(delta));
        apply(project, data);
        return projectView(projectRepository.save(project));
    }

    @Operation(summary = "Delete a project", tags = { "Project Tracker" })
    @DeleteMapping("/projects/{id}")
    public void deleteProject(@PathVariable Long id) {
        projectRepository.delete(findProject(id));
    }

    @Operation(summary = "List milestones", tags = { "Milestone Tracker" })
    @GetMapping("/milestones")
    public List<MilestoneView> milestones() {
        return milestoneRepository.findAll().stream().map(EntityController::milestoneView).toList();
    }

    @Operation(summary = "Get a milestone", tags = { "Milestone Tracker" })
    @GetMapping("/milestones/{id}")
    public MilestoneView milestone(@PathVariable Long id) {
        return milestoneView(findMilestone(id));
    }

    @Operation(summary = "Create a milestone", tags = { "Milestone Tracker" })
    @PostMapping("/milestones")
    public MilestoneView createMilestone(@RequestBody MilestoneData data) {
        Milestone milestone = new Milestone();
        apply(milestone, data);
        return milestoneView(milestoneRepository.save(milestone));
    }

    @Operation(summary = "Update a milestone", tags = { "Milestone Tracker" })
    @PutMapping("/milestones/{id}")
    public MilestoneView updateMilestone(@PathVariable Long id, @RequestBody MilestoneData data) {
        Milestone milestone = findMilestone(id);
        apply(milestone, data);
        return milestoneView(milestoneRepository.save(milestone));
    }

    @Operation(summary = "Delete a milestone", tags = { "Milestone Tracker" })
    @DeleteMapping("/milestones/{id}")
    public void deleteMilestone(@PathVariable Long id) {
        milestoneRepository.delete(findMilestone(id));
    }

    @Operation(summary = "List releases", tags = { "Release Tracker" })
    @GetMapping("/releases")
    public List<ReleaseView> releases() {
        return releaseRepository.findAll().stream().map(EntityController::releaseView).toList();
    }

    @Operation(summary = "Get a release", tags = { "Release Tracker" })
    @GetMapping("/releases/{id}")
    public ReleaseView release(@PathVariable Long id) {
        return releaseView(findRelease(id));
    }

    @Operation(summary = "Create a release", tags = { "Release Tracker" })
    @PostMapping("/releases")
    public ReleaseView createRelease(@RequestBody ReleaseData data) {
        Release release = new Release();
        apply(release, data);
        return releaseView(releaseRepository.save(release));
    }

    @Operation(summary = "Update a release", tags = { "Release Tracker" })
    @PutMapping("/releases/{id}")
    public ReleaseView updateRelease(@PathVariable Long id, @RequestBody ReleaseData data) {
        Release release = findRelease(id);
        apply(release, data);
        return releaseView(releaseRepository.save(release));
    }

    @Operation(summary = "Delete a release", tags = { "Release Tracker" })
    @DeleteMapping("/releases/{id}")
    public void deleteRelease(@PathVariable Long id) {
        releaseRepository.delete(findRelease(id));
    }

    private void apply(Freelancer freelancer, FreelancerData data) {
        freelancer.setName(data.name());
        freelancer.setEmail(data.email());
        freelancer.setSkill(data.skill());
    }

    private void apply(Project project, ProjectData data) {
        project.setTitle(data.title());
        project.setDescription(data.description());
        project.setTotalAmount(data.totalAmount());
        project.setClient(findClient(data.clientId()));
        project.setFreelancer(findFreelancer(data.freelancerId()));
        project.setStatus(data.status() == null ? ProjectStatus.ACTIVE : data.status());
    }

    private void apply(Milestone milestone, MilestoneData data) {
        milestone.setTitle(data.title());
        milestone.setDescription(data.description());
        milestone.setAmount(data.amount());
        milestone.setDueDate(data.dueDate());
        milestone.setProject(findProject(data.projectId()));
        milestone.setStatus(data.status() == null ? MilestoneStatus.FUNDED : data.status());
    }

    private void apply(Release release, ReleaseData data) {
        release.setAmount(data.amount());
        release.setReleasedAt(data.releasedAt() == null ? LocalDateTime.now() : data.releasedAt());
        release.setStatus(data.status() == null ? "RELEASED" : data.status());
        release.setMilestone(findMilestone(data.milestoneId()));
    }

    private Client findClient(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Client not found: " + id));
    }

    private Freelancer findFreelancer(Long id) {
        return freelancerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Freelancer not found: " + id));
    }

    private Project findProject(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found: " + id));
    }

    private Milestone findMilestone(Long id) {
        return milestoneRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Milestone not found: " + id));
    }

    private Release findRelease(Long id) {
        return releaseRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Release not found: " + id));
    }

    private <T, ID> void delete(org.springframework.data.repository.CrudRepository<T, ID> repository, T entity,
            String conflictMessage) {
        try {
            repository.delete(entity);
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, conflictMessage);
        }
    }

    private static ClientView clientView(Client client) {
        return new ClientView(client.getId(), client.getName(), client.getEmail());
    }

    private static FreelancerView freelancerView(Freelancer freelancer) {
        return new FreelancerView(freelancer.getId(), freelancer.getName(), freelancer.getEmail(), freelancer.getSkill());
    }

    private static ProjectView projectView(Project project) {
        return new ProjectView(project.getId(), project.getTitle(), project.getDescription(),
                project.getTotalAmount(), project.getReleasedAmount(), project.getEscrowBalance(),
                project.getStatus(), project.getClient().getId(), project.getFreelancer().getId());
    }

    private static MilestoneView milestoneView(Milestone milestone) {
        return new MilestoneView(milestone.getId(), milestone.getTitle(), milestone.getDescription(),
                milestone.getAmount(), milestone.getDueDate(), milestone.getDeliveredAt(), milestone.getApprovedAt(),
                milestone.getStatus(), milestone.getProject().getId());
    }

    private static ReleaseView releaseView(Release release) {
        return new ReleaseView(release.getId(), release.getAmount(), release.getReleasedAt(), release.getStatus(),
                release.getMilestone().getId());
    }

    public record ClientData(String name, String email) { }
    public record ClientView(Long id, String name, String email) { }
    public record FreelancerData(String name, String email, String skill) { }
    public record FreelancerView(Long id, String name, String email, String skill) { }
    public record ProjectData(String title, String description, BigDecimal totalAmount, Long clientId,
            Long freelancerId, ProjectStatus status) { }
    public record ProjectView(Long id, String title, String description, BigDecimal totalAmount,
            BigDecimal releasedAmount, BigDecimal escrowBalance, ProjectStatus status, Long clientId,
            Long freelancerId) { }
    public record MilestoneData(String title, String description, BigDecimal amount, LocalDateTime dueDate,
            Long projectId, MilestoneStatus status) { }
    public record MilestoneView(Long id, String title, String description, BigDecimal amount, LocalDateTime dueDate,
            LocalDateTime deliveredAt, LocalDateTime approvedAt, MilestoneStatus status, Long projectId) { }
    public record ReleaseData(BigDecimal amount, LocalDateTime releasedAt, String status, Long milestoneId) { }
    public record ReleaseView(Long id, BigDecimal amount, LocalDateTime releasedAt, String status, Long milestoneId) { }
}