const state = {
  auth: '',
  username: '',
  leads: [],
  selectedLeadId: null,
  dashboard: null,
  filters: { search: '', status: 'ALL', priority: 'ALL', source: 'ALL' }
};

const apiBase = `${window.LEAD_API_BASE_URL || 'https://leadproject-59dl.onrender.com'}/api/v1`;

const elements = {
  loginScreen: document.getElementById('loginScreen'),
  appScreen: document.getElementById('appScreen'),
  authForm: document.getElementById('authForm'),
  username: document.getElementById('username'),
  password: document.getElementById('password'),
  loginAlert: document.getElementById('loginAlert'),
  userLabel: document.getElementById('userLabel'),
  logoutBtn: document.getElementById('logoutBtn'),
  exportExcelBtn: document.getElementById('exportExcelBtn'),
  exportPdfBtn: document.getElementById('exportPdfBtn'),
  dashboardStats: document.getElementById('dashboardStats'),
  searchInput: document.getElementById('searchInput'),
  statusFilter: document.getElementById('statusFilter'),
  priorityFilter: document.getElementById('priorityFilter'),
  sourceFilter: document.getElementById('sourceFilter'),
  leadTableBody: document.getElementById('leadTableBody'),
  selectedLeadName: document.getElementById('selectedLeadName'),
  leadMeta: document.getElementById('leadMeta'),
  keyGrid: document.getElementById('keyGrid'),
  callList: document.getElementById('callList'),
  briefText: document.getElementById('briefText'),
  briefOutput: document.getElementById('briefOutput'),
  generateBriefBtn: document.getElementById('generateBriefBtn'),
  exportBriefPdfBtn: document.getElementById('exportBriefPdfBtn')
};

function setAuth(username, password) {
  state.username = username;
  state.auth = 'Basic ' + btoa(`${username}:${password}`);
}

function showAlert(message, isError = true) {
  elements.loginAlert.textContent = message;
  elements.loginAlert.classList.add('show');
  elements.loginAlert.style.background = isError ? '#fff3f3' : '#edf9f3';
  elements.loginAlert.style.color = isError ? '#a42a2a' : '#105c3d';
  elements.loginAlert.style.borderColor = isError ? 'rgba(164, 42, 42, 0.18)' : 'rgba(16, 92, 61, 0.18)';
}

function hideAlert() {
  elements.loginAlert.classList.remove('show');
}

function showApp() {
  elements.loginScreen.classList.add('hidden');
  elements.appScreen.classList.remove('hidden');
  elements.userLabel.textContent = `Signed in as ${state.username}`;
}

function showLogin() {
  elements.appScreen.classList.add('hidden');
  elements.loginScreen.classList.remove('hidden');
}

async function login() {
  const username = elements.username.value.trim();
  const password = elements.password.value.trim();

  if (!username || !password) {
    showAlert('Please enter both username and password.');
    return;
  }

  setAuth(username, password);

  try {
    const response = await fetch(`${apiBase}/auth/login`, {
      method: 'POST',
      headers: {
        Authorization: state.auth,
        'Content-Type': 'application/json'
      }
    });

    if (!response.ok) {
      throw new Error('Authentication failed');
    }

    hideAlert();
    showApp();
    await loadDashboard();
  } catch (error) {
    showAlert('Login failed. Use admin / admin123, sales / sales123 or compliance / compliance123.');
  }
}

function logout() {
  state.auth = '';
  state.username = '';
  elements.username.value = '';
  elements.password.value = '';
  showLogin();
}

async function fetchJson(url, options = {}) {
  const response = await fetch(url, {
    ...options,
    headers: {
      Authorization: state.auth,
      ...(options.headers || {})
    }
  });

  if (!response.ok) {
    const body = await response.text();
    throw new Error(body || 'Request failed');
  }

  return response.json();
}

async function loadDashboard() {
  try {
    const [dashboard, leads] = await Promise.all([
      fetchJson(`${apiBase}/dashboard`),
      fetchJson(`${apiBase}/leads`)
    ]);

    state.dashboard = dashboard;
    state.leads = leads || [];

    const metrics = [
      { label: 'Queued', value: dashboard.queued ?? state.leads.length },
      { label: 'Called Today', value: dashboard.calledToday ?? 0 },
      { label: 'Qualified', value: dashboard.qualified ?? 0 },
      { label: 'Hot Leads', value: dashboard.hotLeads ?? 0 }
    ];

    elements.dashboardStats.innerHTML = metrics.map(item => `
      <div class="metric-card">
        <div class="metric-label">${item.label}</div>
        <div class="metric-value">${item.value}</div>
      </div>
    `).join('');

    renderLeadTable();
    if (!state.selectedLeadId && state.leads.length) {
      selectLead(state.leads[0]);
    } else if (state.selectedLeadId) {
      const existing = state.leads.find(lead => Number(lead.id) === Number(state.selectedLeadId));
      if (existing) {
        selectLead(existing);
      } else if (state.leads.length) {
        selectLead(state.leads[0]);
      }
    }
  } catch (error) {
    showAlert(error.message || 'Unable to load dashboard data.');
  }
}

function getFilteredLeads() {
  const search = state.filters.search.trim().toLowerCase();

  return state.leads.filter(lead => {
    const matchesSearch = !search || [
      lead.name,
      lead.phone,
      lead.email,
      lead.source,
      lead.status,
      lead.summary,
      lead.preferredLocation,
      lead.propertyType
    ].join(' ').toLowerCase().includes(search);

    const matchesStatus = state.filters.status === 'ALL' || (lead.status || 'NEW') === state.filters.status;
    const matchesPriority = state.filters.priority === 'ALL' || (lead.scoreBand || 'COLD') === state.filters.priority;
    const matchesSource = state.filters.source === 'ALL' || (lead.source || '').toLowerCase() === state.filters.source.toLowerCase();

    return matchesSearch && matchesStatus && matchesPriority && matchesSource;
  });
}

function renderLeadTable() {
  const filteredLeads = getFilteredLeads();

  if (!filteredLeads.length) {
    elements.leadTableBody.innerHTML = `
      <tr>
        <td colspan="8" class="muted">No leads match the current filters.</td>
      </tr>
    `;
    return;
  }

  elements.leadTableBody.innerHTML = filteredLeads.map(lead => {
    const status = lead.status || 'NEW';
    const scoreBand = (lead.scoreBand || 'COLD').toLowerCase();
    const isSelected = Number(lead.id) === Number(state.selectedLeadId);

    return `
      <tr class="${isSelected ? 'selected' : ''}" data-id="${lead.id}">
        <td>${lead.name || 'Unknown'}</td>
        <td>${lead.phone || '—'}</td>
        <td>${lead.source || 'website_form'}</td>
        <td><span class="badge ${status.toLowerCase().replace(/\s+/g, '-')}">${status}</span></td>
        <td><span class="badge ${scoreBand}">${lead.scoreBand || 'COLD'}</span></td>
        <td>${lead.leadIntent || lead.propertyType || '—'}</td>
        <td>${lead.preferredLocation || '—'}</td>
        <td>${lead.summary || 'Awaiting qualification call'}</td>
      </tr>
    `;
  }).join('');

  elements.leadTableBody.querySelectorAll('tr[data-id]').forEach(row => {
    row.addEventListener('click', () => {
      const id = row.dataset.id;
      const lead = state.leads.find(item => Number(item.id) === Number(id));
      if (lead) selectLead(lead);
    });
  });
}

async function selectLead(lead) {
  state.selectedLeadId = Number(lead.id);
  renderLeadTable();

  elements.selectedLeadName.textContent = lead.name || 'Unknown lead';
  elements.leadMeta.textContent = `${lead.phone || '—'} • ${lead.source || 'website_form'} • ${lead.status || 'NEW'}`;

  const keyItems = [
    ['Intent', lead.leadIntent || 'Not captured'],
    ['Property', lead.propertyType || 'Not captured'],
    ['Budget', lead.budgetRange || 'Not captured'],
    ['Location', lead.preferredLocation || 'Not captured'],
    ['Timeline', lead.timeline || 'Not captured'],
    ['Decision maker', lead.decisionMaker || 'Not captured'],
    ['Callback', lead.preferredCallbackTime || 'Not captured'],
    ['Status', lead.status || 'NEW']
  ];

  elements.keyGrid.innerHTML = keyItems.map(([label, value]) => `
    <div class="key-item">
      <small>${label}</small>
      <strong>${value}</strong>
    </div>
  `).join('');

  try {
    const history = await fetchJson(`${apiBase}/calls/lead/${lead.id}`);
    renderCallHistory(history || []);
  } catch (error) {
    elements.callList.innerHTML = '<div class="muted">No call history available.</div>';
  }

  const leadBrief = lead.summary || 'Lead details are still being collected. The sales brief will appear here once the call is completed.';
  elements.briefText.value = leadBrief;
  elements.briefOutput.textContent = leadBrief;
}

function renderCallHistory(history) {
  if (!history.length) {
    elements.callList.innerHTML = '<div class="muted">No call history exists for this lead yet.</div>';
    return;
  }

  elements.callList.innerHTML = history.map(call => `
    <div class="call-item">
      <div class="call-item-header">
        <strong>Call #${escapeHtml(call.id)}</strong>
        <span class="badge ${String(call.status || 'PLACED').toLowerCase().replace(/\s+/g, '-')}\">${escapeHtml(call.status || 'PLACED')}</span>
      </div>
      <div class="muted">${escapeHtml(call.phone || '—')} • ${call.createdAt ? new Date(call.createdAt).toLocaleString() : '—'}</div>
      <div class="call-summary">${escapeHtml((call.summary || 'No summary captured yet.').substring(0, 220))}</div>
      ${renderConversation(call.conversation || call.transcript)}
    </div>
  `).join('');
}

const questionLabels = {
  1: 'Buying, renting, selling, or investing',
  2: 'Area and property type',
  3: 'Budget range',
  4: 'Purchase timeline',
  5: 'Decision maker',
  6: 'Preferred callback time'
};

function renderConversation(conversation) {
  if (!conversation) {
    return '<div class="answers-empty">Answers will appear here as the call progresses.</div>';
  }

  const answers = conversation.split(/\r?\n/).map(entry => {
    const match = entry.match(/^Q(\d+):\s*(.*)$/s);
    return match ? { number: match[1], answer: match[2] } : { number: '', answer: entry };
  });

  return `
    <div class="call-answers">
      <div class="answers-heading">Captured answers</div>
      ${answers.map(item => `
        <div class="answer-row">
          <span class="answer-question">${escapeHtml(questionLabels[item.number] || `Question ${item.number}`)}</span>
          <span class="answer-value">${escapeHtml(item.answer)}</span>
        </div>
      `).join('')}
    </div>
  `;
}

function escapeHtml(value) {
  return String(value)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;');
}

async function generateBrief() {
  const lead = state.leads.find(item => Number(item.id) === Number(state.selectedLeadId));
  if (!lead) {
    showAlert('Select a lead before generating a sales brief.');
    return;
  }

  try {
    const payload = {
      transcript: elements.briefText.value || lead.summary || '',
      summary: lead.summary || elements.briefText.value || ''
    };

    const result = await fetchJson(`${apiBase}/leads/${lead.id}/sales-brief`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });

    const text = result?.brief || result?.salesBrief || JSON.stringify(result, null, 2);
    elements.briefOutput.textContent = text;
    elements.briefText.value = text;
    showAlert('Sales brief generated successfully.', false);
  } catch (error) {
    showAlert(error.message || 'Unable to generate sales brief.');
  }
}

function exportLeadTableToExcel() {
  const rows = state.leads.map(lead => ({
    ID: lead.id,
    Name: lead.name,
    Phone: lead.phone,
    Email: lead.email || '',
    Source: lead.source || '',
    Status: lead.status || 'NEW',
    Score: lead.scoreBand || 'COLD',
    Intent: lead.leadIntent || '',
    Property: lead.propertyType || '',
    Budget: lead.budgetRange || '',
    Location: lead.preferredLocation || '',
    Timeline: lead.timeline || '',
    Summary: lead.summary || ''
  }));

  const worksheet = XLSX.utils.json_to_sheet(rows);
  const workbook = XLSX.utils.book_new();
  XLSX.utils.book_append_sheet(workbook, worksheet, 'Leads');
  XLSX.writeFile(workbook, 'lead-report.xlsx');
}

function exportBriefPdf() {
  const { jsPDF } = window.jspdf;
  const doc = new jsPDF();
  const lead = state.leads.find(item => Number(item.id) === Number(state.selectedLeadId));
  const text = elements.briefOutput.textContent || 'No sales brief available.';

  doc.setFontSize(18);
  doc.text('Sales Brief', 14, 18);
  doc.setFontSize(11);
  doc.text(`Lead: ${lead ? lead.name : 'N/A'}`, 14, 30);
  doc.text(`Phone: ${lead ? lead.phone : 'N/A'}`, 14, 38);
  doc.text(`Status: ${lead ? (lead.status || 'NEW') : 'N/A'}`, 14, 46);

  const lines = doc.splitTextToSize(text, 180);
  doc.text(lines, 14, 58);
  doc.save('sales-brief.pdf');
}

function updateFilters() {
  state.filters.search = elements.searchInput.value;
  state.filters.status = elements.statusFilter.value;
  state.filters.priority = elements.priorityFilter.value;
  state.filters.source = elements.sourceFilter.value;
  renderLeadTable();
}

function bindEvents() {
  elements.authForm.addEventListener('submit', event => {
    event.preventDefault();
    login();
  });

  elements.logoutBtn.addEventListener('click', logout);
  elements.exportExcelBtn.addEventListener('click', exportLeadTableToExcel);
  elements.exportPdfBtn.addEventListener('click', () => {
    const lead = state.leads.find(item => Number(item.id) === Number(state.selectedLeadId));
    if (!lead) {
      showAlert('Select a lead before exporting PDF.');
      return;
    }

    const { jsPDF } = window.jspdf;
    const doc = new jsPDF();
    doc.setFontSize(18);
    doc.text('AI Lead Agent Lead Summary', 14, 18);
    doc.setFontSize(11);
    doc.text(`Name: ${lead.name || 'N/A'}`, 14, 32);
    doc.text(`Phone: ${lead.phone || 'N/A'}`, 14, 40);
    doc.text(`Source: ${lead.source || 'N/A'}`, 14, 48);
    doc.text(`Status: ${lead.status || 'NEW'}`, 14, 56);
    doc.text(`Score: ${lead.scoreBand || 'COLD'}`, 14, 64);
    doc.text(`Summary: ${lead.summary || '—'}`, 14, 72, { maxWidth: 180 });
    doc.save(`${(lead.name || 'lead').replace(/\s+/g, '-').toLowerCase()}.pdf`);
  });

  elements.searchInput.addEventListener('input', updateFilters);
  elements.statusFilter.addEventListener('change', updateFilters);
  elements.priorityFilter.addEventListener('change', updateFilters);
  elements.sourceFilter.addEventListener('change', updateFilters);
  elements.generateBriefBtn.addEventListener('click', generateBrief);
  elements.exportBriefPdfBtn.addEventListener('click', exportBriefPdf);
}

bindEvents();
showLogin();
