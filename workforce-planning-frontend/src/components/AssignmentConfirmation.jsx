import React, { useState } from 'react';
import { ShieldCheck, User, Briefcase, Calendar, CheckCircle, XCircle, AlertCircle } from 'lucide-react';

const AssignmentConfirmation = () => {
  // Mock data representing the "Final Match" from the Planner step
  const [pendingConfirmation, setPendingConfirmation] = useState({
    id: 'ASGN-772',
    project: 'Cloud Migration',
    employee: 'Alice Johnson',
    role: 'Lead Cloud Architect',
    startDate: '2025-03-01',
    workload: '40 hrs/week',
    matchScore: '95%'
  });

  const [status, setStatus] = useState('pending'); // pending, confirmed, declined

  const handleFinalAction = (action) => {
    setStatus(action === 'approve' ? 'confirmed' : 'declined');
  };

  if (status !== 'pending') {
    return (
      <div style={styles.resultContainer}>
        {status === 'confirmed' ? (
          <div style={styles.successCard}>
            <CheckCircle size={60} color="#10b981" />
            <h2>Assignment Finalized!</h2>
            <p><strong>{pendingConfirmation.employee}</strong> has been officially assigned to <strong>{pendingConfirmation.project}</strong>.</p>
            <button onClick={() => window.location.href='/dashboard'} style={styles.backBtn}>Return to Dashboard</button>
          </div>
        ) : (
          <div style={styles.errorCard}>
            <XCircle size={60} color="#ef4444" />
            <h2>Assignment Declined</h2>
            <p>The request has been sent back to the Resource Planner for re-evaluation.</p>
            <button onClick={() => setStatus('pending')} style={styles.backBtn}>Go Back</button>
          </div>
        )}
      </div>
    );
  }

  return (
    <div style={styles.container}>
      <header style={styles.header}>
        <ShieldCheck size={32} color="#4f46e5" />
        <h1>Approve Employee Assignment</h1>
      </header>

      <div style={styles.mainCard}>
        <div style={styles.infoGrid}>
          <div style={styles.infoBox}>
            <label style={styles.label}><Briefcase size={14} /> Project</label>
            <div style={styles.value}>{pendingConfirmation.project}</div>
          </div>
          <div style={styles.infoBox}>
            <label style={styles.label}><User size={14} /> Assigned Resource</label>
            <div style={styles.value}>{pendingConfirmation.employee}</div>
          </div>
          <div style={styles.infoBox}>
            <label style={styles.label}><Calendar size={14} /> Start Date</label>
            <div style={styles.value}>{pendingConfirmation.startDate}</div>
          </div>
          <div style={styles.infoBox}>
            <label style={styles.label}><AlertCircle size={14} /> Match Confidence</label>
            <div style={{...styles.value, color: '#10b981'}}>{pendingConfirmation.matchScore}</div>
          </div>
        </div>

        <hr style={styles.divider} />

        <div style={styles.summary}>
          <p>By approving this assignment, you confirm that <strong>{pendingConfirmation.employee}</strong> is the most suitable resource for <strong>{pendingConfirmation.project}</strong> and their workload capacity of <strong>{pendingConfirmation.workload}</strong> is verified.</p>
        </div>

        <div style={styles.actions}>
          <button onClick={() => handleFinalAction('decline')} style={styles.declineBtn}>Decline & Restart Search</button>
          <button onClick={() => handleFinalAction('approve')} style={styles.approveBtn}>Confirm Assignment</button>
        </div>
      </div>
    </div>
  );
};

const styles = {
  container: { maxWidth: '800px', margin: '0 auto', padding: '20px' },
  header: { display: 'flex', alignItems: 'center', gap: '15px', marginBottom: '30px' },
  mainCard: { background: 'white', padding: '40px', borderRadius: '15px', boxShadow: '0 10px 25px rgba(0,0,0,0.05)', border: '1px solid #e2e8f0' },
  infoGrid: { display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '30px', marginBottom: '30px' },
  label: { display: 'flex', alignItems: 'center', gap: '6px', fontSize: '12px', textTransform: 'uppercase', color: '#64748b', fontWeight: 'bold', marginBottom: '8px' },
  value: { fontSize: '18px', fontWeight: '600', color: '#1e293b' },
  divider: { border: '0', borderTop: '1px solid #f1f5f9', margin: '30px 0' },
  summary: { background: '#f8fafc', padding: '20px', borderRadius: '10px', fontSize: '14px', color: '#475569', lineHeight: '1.6', marginBottom: '30px' },
  actions: { display: 'flex', gap: '20px' },
  approveBtn: { flex: 2, padding: '16px', background: '#1e293b', color: 'white', border: 'none', borderRadius: '8px', fontWeight: 'bold', cursor: 'pointer', transition: '0.2s' },
  declineBtn: { flex: 1, padding: '16px', background: '#f1f5f9', color: '#ef4444', border: '1px solid #ef4444', borderRadius: '8px', fontWeight: 'bold', cursor: 'pointer' },
  resultContainer: { height: '80vh', display: 'flex', alignItems: 'center', justifyContent: 'center' },
  successCard: { textAlign: 'center', padding: '50px', background: 'white', borderRadius: '20px', boxShadow: '0 20px 25px -5px rgba(0,0,0,0.1)' },
  errorCard: { textAlign: 'center', padding: '50px', background: 'white', borderRadius: '20px', border: '2px solid #fee2e2' },
  backBtn: { marginTop: '20px', padding: '10px 20px', background: '#4f46e5', color: 'white', border: 'none', borderRadius: '5px', cursor: 'pointer' }
};

export default AssignmentConfirmation;