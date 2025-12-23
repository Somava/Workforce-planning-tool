import React, { useState } from 'react';
import { Users, Target, BarChart3, CheckCircle2, UserPlus, Search, Info } from 'lucide-react';

const ResourcePlannerMatch = () => {
  // Mock data representing the "Approved Request" from the previous BPMN step
  const [selectedRequest] = useState({
    id: 'REQ-442',
    project: 'Cloud Migration Phase 2',
    requiredSkills: ['AWS', 'Terraform', 'Docker', 'Security'],
    workload: 40,
    priority: 'High'
  });

  // Mock data for the Employee Pool to match against
  const [candidates] = useState([
    { id: 'E001', name: 'Sarah Chen', skills: ['AWS', 'Docker', 'Kubernetes'], availability: 40, matchScore: 75 },
    { id: 'E002', name: 'Marcus Vane', skills: ['AWS', 'Terraform', 'Docker', 'Security'], availability: 20, matchScore: 100 },
    { id: 'E003', name: 'Elena Rodriguez', skills: ['Terraform', 'Python', 'Security'], availability: 40, matchScore: 50 },
  ]);

  return (
    <div style={styles.container}>
      <header style={styles.header}>
        <div style={styles.headerTitle}>
          <Target size={28} color="#4f46e5" />
          <h1>Planner: Matchmaking Engine</h1>
        </div>
        <div style={styles.requestSummary}>
          <strong>Current Focus:</strong> {selectedRequest.project} 
          <span style={styles.priorityBadge}>{selectedRequest.priority} Priority</span>
        </div>
      </header>

      <div style={styles.grid}>
        {/* Left: Requirements Analysis */}
        <div style={styles.card}>
          <h3 style={styles.cardTitle}><BarChart3 size={18} /> Requirements Gap</h3>
          <p style={styles.subtext}>Skills required for this project:</p>
          <div style={styles.tagGroup}>
            {selectedRequest.requiredSkills.map(skill => (
              <span key={skill} style={styles.reqTag}>{skill}</span>
            ))}
          </div>
          <div style={styles.infoBox}>
            <Info size={16} />
            <span>Target Workload: <strong>{selectedRequest.workload} hrs/week</strong></span>
          </div>
        </div>

        {/* Right: Candidate Matching */}
        <div style={{ ...styles.card, flex: 2 }}>
          <h3 style={styles.cardTitle}><Users size={18} /> Best Fit Candidates</h3>
          <div style={styles.tableWrapper}>
            <table style={styles.table}>
              <thead>
                <tr>
                  <th>Employee</th>
                  <th>Skill Match</th>
                  <th>Availability</th>
                  <th>Fit Score</th>
                  <th>Action</th>
                </tr>
              </thead>
              <tbody>
                {candidates.map(emp => (
                  <tr key={emp.id} style={styles.row}>
                    <td>
                      <div style={styles.empName}>{emp.name}</div>
                      <div style={styles.empId}>{emp.id}</div>
                    </td>
                    <td>
                      <div style={styles.tagGroup}>
                        {emp.skills.map(s => (
                          <span key={s} style={{
                            ...styles.miniTag,
                            backgroundColor: selectedRequest.requiredSkills.includes(s) ? '#dcfce7' : '#f1f5f9',
                            color: selectedRequest.requiredSkills.includes(s) ? '#166534' : '#64748b'
                          }}>{s}</span>
                        ))}
                      </div>
                    </td>
                    <td>
                      <div style={styles.progressBg}>
                        <div style={{ ...styles.progressFill, width: `${(emp.availability / 40) * 100}%` }}></div>
                      </div>
                      <span style={styles.subtext}>{emp.availability}h free</span>
                    </td>
                    <td>
                      <span style={{ 
                        ...styles.score, 
                        color: emp.matchScore > 80 ? '#10b981' : emp.matchScore > 60 ? '#f59e0b' : '#ef4444' 
                      }}>
                        {emp.matchScore}%
                      </span>
                    </td>
                    <td>
                      <button style={styles.assignBtn}>
                        <UserPlus size={14} /> Assign
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  );
};

const styles = {
  container: { padding: '20px' },
  header: { marginBottom: '30px', borderBottom: '1px solid #e2e8f0', paddingBottom: '20px' },
  headerTitle: { display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '10px' },
  requestSummary: { fontSize: '14px', color: '#64748b', display: 'flex', alignItems: 'center', gap: '10px' },
  priorityBadge: { backgroundColor: '#fef2f2', color: '#991b1b', padding: '2px 8px', borderRadius: '4px', fontSize: '11px', fontWeight: 'bold' },
  grid: { display: 'flex', gap: '20px' },
  card: { background: 'white', padding: '25px', borderRadius: '12px', boxShadow: '0 4px 6px -1px rgba(0,0,0,0.1)', flex: 1 },
  cardTitle: { display: 'flex', alignItems: 'center', gap: '8px', fontSize: '16px', marginBottom: '15px', color: '#1e293b' },
  tagGroup: { display: 'flex', gap: '6px', flexWrap: 'wrap', marginBottom: '15px' },
  reqTag: { padding: '4px 10px', background: '#eef2ff', color: '#4338ca', borderRadius: '6px', fontSize: '12px', fontWeight: '600' },
  miniTag: { padding: '2px 6px', borderRadius: '4px', fontSize: '10px', fontWeight: '500' },
  infoBox: { display: 'flex', alignItems: 'center', gap: '8px', padding: '12px', background: '#f8fafc', borderRadius: '8px', fontSize: '13px', color: '#475569' },
  subtext: { fontSize: '12px', color: '#94a3b8', marginBottom: '8px' },
  tableWrapper: { marginTop: '10px' },
  table: { width: '100%', borderCollapse: 'collapse' },
  row: { borderBottom: '1px solid #f1f5f9' },
  empName: { fontSize: '14px', fontWeight: '600', color: '#1e293b' },
  empId: { fontSize: '11px', color: '#94a3b8' },
  progressBg: { width: '100%', height: '6px', background: '#e2e8f0', borderRadius: '10px', overflow: 'hidden', marginBottom: '4px' },
  progressFill: { height: '100%', background: '#10b981' },
  score: { fontWeight: 'bold', fontSize: '15px' },
  assignBtn: { display: 'flex', alignItems: 'center', gap: '6px', padding: '8px 12px', background: '#1e293b', color: 'white', border: 'none', borderRadius: '6px', cursor: 'pointer', fontSize: '12px' }
};

export default ResourcePlannerMatch;