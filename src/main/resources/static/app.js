const currency = new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD', maximumFractionDigits: 0 });
const dateFormatter = new Intl.DateTimeFormat('en-US', { month: 'short', day: 'numeric' });
const dateLong = new Intl.DateTimeFormat('en-US', { weekday: 'long', month: 'long', day: 'numeric' });
const state = { dashboard: null, filter: 'ALL', expanded: false };
const actions = {
  FUNDED: ['Start work', 'Move to in progress'],
  IN_PROGRESS: ['Submit work', 'Mark work submitted'],
  SUBMITTED: ['Approve delivery', 'Approve submitted milestone'],
  APPROVED: ['Release payment', 'Release approved payment'],
  RELEASED: ['Payment released', 'Payment has been released']
};

document.querySelector('#today-date').textContent = dateLong.format(new Date());
document.querySelector('#refresh-button').addEventListener('click', loadDashboard);
document.querySelector('#all-milestones').addEventListener('click', () => {
  state.expanded = !state.expanded;
  document.querySelector('#all-milestones').innerHTML = state.expanded ? 'Show fewer <span>↑</span>' : 'View all <span>→</span>';
  renderMilestones();
});
document.querySelectorAll('.filter').forEach(button => button.addEventListener('click', () => {
  state.filter = button.dataset.filter;
  document.querySelectorAll('.filter').forEach(item => item.classList.toggle('active', item === button));
  renderMilestones();
}));

async function loadDashboard() {
  try {
    const response = await fetch('/api/dashboard');
    if (!response.ok) throw new Error('Could not load your workspace.');
    state.dashboard = await response.json();
    render();
  } catch (error) {
    document.querySelector('#milestone-list').innerHTML = '<div class="loading-state">We could not reach your workspace. Refresh to try again.</div>';
    showToast(error.message);
  }
}

function render() {
  const data = state.dashboard;
  const submitted = data.milestones.filter(item => item.status === 'SUBMITTED').length;
  document.querySelector('#escrow-total').textContent = currency.format(data.escrowBalance);
  document.querySelector('#released-total').textContent = currency.format(data.releasedTotal);
  document.querySelector('#review-total').textContent = submitted;
  document.querySelector('#project-total').textContent = data.projects.filter(item => item.status === 'ACTIVE').length;
  document.querySelector('#nav-count').textContent = submitted;
  document.querySelector('#filter-all').textContent = data.milestones.length;
  document.querySelector('#filter-review').textContent = submitted;
  document.querySelector('#project-number').textContent = String(data.projects.length).padStart(2, '0');
  document.querySelector('#updated-at').textContent = `Updated ${new Intl.DateTimeFormat('en-US', { hour: 'numeric', minute: '2-digit' }).format(new Date(data.updatedAt))}`;
  renderMilestones();
  renderProjects();
  renderActivity();
}

function renderMilestones() {
  const list = document.querySelector('#milestone-list');
  const empty = document.querySelector('#empty-state');
  if (!state.dashboard) return;
  const all = [...state.dashboard.milestones].sort((a, b) => priority(a.status) - priority(b.status) || new Date(a.dueDate) - new Date(b.dueDate));
  const filtered = state.filter === 'ALL' ? all : all.filter(item => item.status === state.filter);
  const visible = state.expanded || state.filter !== 'ALL' ? filtered : filtered.slice(0, 5);
  list.innerHTML = visible.map((milestone, index) => {
    const [label, description] = actions[milestone.status];
    const person = milestone.freelancer;
    const initials = person.split(' ').map(part => part[0]).slice(0, 2).join('');
    const due = new Date(milestone.dueDate);
    const dueText = due < new Date() && milestone.status !== 'RELEASED' ? 'Past due' : `Due ${dateFormatter.format(due)}`;
    const disabled = milestone.status === 'RELEASED';
    const releaseClass = milestone.status === 'APPROVED' ? ' release-action' : '';
    return `<article class="milestone-row" style="animation-delay:${index * 35}ms">
      <div class="milestone-main"><div class="milestone-title" title="${escapeHtml(milestone.title)}">${escapeHtml(milestone.title)}</div><div class="milestone-project">${escapeHtml(milestone.projectTitle)} · ${escapeHtml(milestone.client)}</div></div>
      <div class="person-line"><span class="mini-avatar ${index % 2 ? 'alt' : ''}">${escapeHtml(initials)}</span><span>${escapeHtml(person)}</span></div>
      <div class="milestone-amount">${currency.format(milestone.amount)}<span class="milestone-due">${dueText}</span></div>
      <button class="status-action${releaseClass}" data-id="${milestone.id}" ${disabled ? 'disabled' : ''} aria-label="${description}">${label}</button>
    </article>`;
  }).join('');
  empty.hidden = visible.length > 0;
  if (!visible.length) list.innerHTML = '';
  list.querySelectorAll('.status-action:not(:disabled)').forEach(button => button.addEventListener('click', () => advanceMilestone(button)));
  const viewAll = document.querySelector('#all-milestones');
  viewAll.hidden = state.filter !== 'ALL' || filtered.length <= 5;
}

function renderProjects() {
  const projects = state.dashboard.projects;
  document.querySelector('#project-list').innerHTML = projects.map(project => {
    const progress = project.totalAmount ? Math.min(100, Math.round(project.releasedAmount / project.totalAmount * 100)) : 0;
    return `<article class="project-item"><div class="project-top"><div><div class="project-title">${escapeHtml(project.title)}</div><div class="project-client">${escapeHtml(project.client)} <span>·</span> ${escapeHtml(project.freelancer)}</div></div><div class="project-total">${currency.format(project.totalAmount)}</div></div><div class="progress-track"><div class="progress-bar" style="width:${progress}%"></div></div><div class="project-foot"><span>${currency.format(project.releasedAmount)} released</span><span>${currency.format(project.escrowBalance)} held</span></div></article>`;
  }).join('');
}

function renderActivity() {
  const recent = [...state.dashboard.milestones].sort((a, b) => priority(a.status) - priority(b.status)).slice(0, 4);
  document.querySelector('#activity-list').innerHTML = recent.map(item => {
    const message = item.status === 'SUBMITTED' ? 'submitted work for' : item.status === 'APPROVED' ? 'is approved for' : item.status === 'RELEASED' ? 'payment released for' : item.status === 'IN_PROGRESS' ? 'is working on' : 'is funded for';
    const dotClass = item.status === 'RELEASED' ? 'green' : item.status === 'IN_PROGRESS' ? 'blue' : '';
    return `<div class="activity-item"><span class="activity-dot ${dotClass}"></span><div class="activity-copy"><strong>${escapeHtml(item.freelancer)}</strong> ${message} <strong>${escapeHtml(item.title)}</strong><span class="activity-time">${escapeHtml(item.projectTitle)} · ${item.status.toLowerCase().replace('_', ' ')}</span></div></div>`;
  }).join('');
}

async function advanceMilestone(button) {
  button.disabled = true;
  const label = button.textContent;
  button.textContent = 'Saving…';
  try {
    const response = await fetch(`/api/milestones/${button.dataset.id}/advance`, { method: 'POST' });
    const result = await response.json();
    if (!response.ok) throw new Error(result.detail || result.message || 'Milestone update failed.');
    await loadDashboard();
    showToast(`${result.title}: ${result.status === 'RELEASED' ? 'payment released' : result.status.toLowerCase().replace('_', ' ')}`);
  } catch (error) {
    button.disabled = false;
    button.textContent = label;
    showToast(error.message);
  }
}

function priority(status) { return ({ SUBMITTED: 0, APPROVED: 1, IN_PROGRESS: 2, FUNDED: 3, RELEASED: 4 })[status] ?? 5; }
function escapeHtml(value) { return String(value ?? '').replace(/[&<>"']/g, char => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' })[char]); }
let toastTimer;
function showToast(message) {
  const toast = document.querySelector('#toast');
  toast.textContent = message;
  toast.classList.add('visible');
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => toast.classList.remove('visible'), 2800);
}

loadDashboard();