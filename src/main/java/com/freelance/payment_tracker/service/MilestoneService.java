package com.freelance.payment_tracker.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.freelance.payment_tracker.entity.Milestone;
import com.freelance.payment_tracker.entity.MilestoneStatus;
import com.freelance.payment_tracker.entity.Project;
import com.freelance.payment_tracker.entity.Release;
import com.freelance.payment_tracker.repository.MilestoneRepository;

@Service
public class MilestoneService {
	private final MilestoneRepository milestoneRepository;

	public MilestoneService(MilestoneRepository milestoneRepository) {
		this.milestoneRepository = milestoneRepository;
	}

	@Transactional(readOnly = true)
	public List<Milestone> getAll() {
		return milestoneRepository.findAll();
	}

	@Transactional
	public Milestone advance(Long id) {
		Milestone milestone = milestoneRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Milestone not found"));
		Project project = milestone.getProject();

		switch (milestone.getStatus()) {
			case FUNDED -> milestone.setStatus(MilestoneStatus.IN_PROGRESS);
			case IN_PROGRESS -> {
				milestone.setStatus(MilestoneStatus.SUBMITTED);
				milestone.setDeliveredAt(LocalDateTime.now());
			}
			case SUBMITTED -> {
				milestone.setStatus(MilestoneStatus.APPROVED);
				milestone.setApprovedAt(LocalDateTime.now());
			}
			case APPROVED -> {
				if (project.getEscrowBalance().compareTo(milestone.getAmount()) < 0) {
					throw new ResponseStatusException(HttpStatus.CONFLICT, "Project escrow is insufficient");
				}
				milestone.setStatus(MilestoneStatus.RELEASED);
				project.setEscrowBalance(project.getEscrowBalance().subtract(milestone.getAmount()));
				project.setReleasedAmount(project.getReleasedAmount().add(milestone.getAmount()));

				Release release = new Release();
				release.setAmount(milestone.getAmount());
				release.setReleasedAt(LocalDateTime.now());
				release.setStatus("RELEASED");
				release.setMilestone(milestone);
				milestone.setRelease(release);
			}
			case RELEASED -> throw new ResponseStatusException(HttpStatus.CONFLICT, "Milestone payment is already released");
		}

		return milestoneRepository.save(milestone);
	}
}
