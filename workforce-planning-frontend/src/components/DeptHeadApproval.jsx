import React, { useState } from 'react';
import { CheckCircle, XCircle, MessageSquare, List, Info } from 'lucide-react';

const DeptHeadApproval = () => {
  // Mock data representing requests submitted by Managers
  const [requests, setRequests] = useState([
    { id: 'REQ-001', project: 'Cloud Migration', skills: 'AWS, Docker', date: '2025-01-15', workload: 40, status: 'Pending' },
    { id: 'REQ-002', project: 'AI Chatbot', skills: 'Python, NLP', date: '2025-02-01', workload: 20, status: 'Pending' }
  ]);

  const [selectedRequest, setSelectedRequest] = useState(null);
  const [decision, setDecision] = useState('');
  const [comments, setComments] = useState('');

  const handleDecision = (e) => {
    e.preventDefault();
    alert(`Request ${selectedRequest.id} has been ${decision}. Comments: ${comments}`);
    // Logic to remove from list after decision
    setRequests(requests.filter(r => r.id !== selectedRequest.id));
    setSelectedRequest(null);
  };

  return (
    <div style={styles.container}>
      <div style={styles.header}>
        <CheckCircle size={24} color="#10b981" />
        <h2 style={styles.title}>Department Head Approval Inbox</h2>
      </div>

      <div style={styles.layout}>
        {/* LEFT: List of Requests */}
        <div style={styles.listSide}>
          <h3 style={styles.sectionTitle}><List size={18} /> Pending Requests</h3>
          {requests.map(req => (
            <div 
              key={req.id} 
              style={{...styles.card, borderLeft: selectedRequest?.id === req.id ? '5px solid #4f46e5' : '1px solid #e2e8f0'}}
              onClick={() => setSelectedRequest(req)}
            >
              <strong>{req.project}</strong>
              <div style={styles.cardMeta}>{req.id} • {req.date}</div>
            </div>
          ))}
        </div>

        {/* RIGHT: Detailed Review & Form */}
        <div style={styles.formSide}>
          {selectedRequest ? (
            <div style={styles.glassContent}>
              <h3 style={styles.sectionTitle}><Info size={18} /> Review Details</h3>
              <div style={styles.detailsGrid}>
                <p><strong>Project:</strong> {selectedRequest.project}</p>
                <p><strong>Skills:</strong> {selectedRequest.skills}</p>
                <p><strong>Workload:</strong> {selectedRequest.workload} hrs/week</p>
                <p><strong>Start Date:</strong> {selectedRequest.date}</p>
              </div>

              <hr style={styles.hr} />

              <form onSubmit={handleDecision} style={styles.form}>
                <label style={styles.label}>Approval Decision</label>
                <div style={styles.radioGroup}>
                  <button 
                    type="button" 
                    onClick={() => setDecision('approved')}
                    style={{...styles.decisionBtn, background: decision === 'approved' ? '#10b981' : '#f3f4f6', color: decision === 'approved' ? 'white' : '#374151'}}
                  >
                    <CheckCircle size={16} /> Approve
                  </button>
                  <button 
                    type="button" 
                    onClick={() => setDecision('rejected')}
                    style={{...styles.decisionBtn, background: decision === 'rejected' ? '#ef4444' : '#f3f4f6', color: decision === 'rejected' ? 'white' : '#374151'}}
                  >
                    <XCircle size={16} /> Reject
                  </button>
                </div>

                <label style={styles.label}>Comments (Optional)</label>
                <div style={styles.inputWrapper}>
                  <MessageSquare size={16} style={styles.icon} />
                  <textarea 
                    style={styles.textarea} 
                    placeholder="Add feedback or reasons for rejection..."
                    onChange={(e) => setComments(e.target.value)}
                  />
                </div>

                <button type="submit" disabled={!decision} style={styles.submitBtn}>
                  Confirm Decision
                </button>
              </form>
            </div>
          ) : (
            <div style={styles.emptyState}>Select a request from the list to review.</div>
          )}
        </div>
      </div>
    </div>
  );
};

const styles = {
  container: { padding: '20px' },
  header: { display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '30px' },
  title: { margin: 0, fontSize: '24px', color: '#1e293b' },
  layout: { display: 'flex', gap: '30px' },
  listSide: { flex: 1, display: 'flex', flexDirection: 'column', gap: '15px' },
  formSide: { flex: 2 },
  sectionTitle: { fontSize: '16px', display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '15px', color: '#64748b' },
  card: { padding: '15px', background: 'white', borderRadius: '8px', cursor: 'pointer', boxShadow: '0 2px 4px rgba(0,0,0,0.05)', transition: 'all 0.2s' },
  cardMeta: { fontSize: '12px', color: '#94a3b8', marginTop: '5px' },
  glassContent: { background: 'white', padding: '30px', borderRadius: '12px', boxShadow: '0 10px 15px -3px rgba(0,0,0,0.1)', border: '1px solid #e2e8f0' },
  detailsGrid: { display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '10px', fontSize: '14px', color: '#334155' },
  hr: { margin: '20px 0', border: '0', borderTop: '1px solid #f1f5f9' },
  form: { display: 'flex', flexDirection: 'column', gap: '15px' },
  label: { fontSize: '13px', fontWeight: 'bold', color: '#475569' },
  radioGroup: { display: 'flex', gap: '15px' },
  decisionBtn: { flex: 1, display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '8px', padding: '10px', border: 'none', borderRadius: '8px', cursor: 'pointer', fontWeight: '600', transition: '0.2s' },
  inputWrapper: { position: 'relative' },
  icon: { position: 'absolute', top: '12px', left: '12px', color: '#94a3b8' },
  textarea: { width: '92%', padding: '10px 10px 10px 40px', borderRadius: '8px', border: '1px solid #cbd5e1', minHeight: '80px', outline: 'none' },
  submitBtn: { padding: '12px', background: '#1e293b', color: 'white', border: 'none', borderRadius: '8px', cursor: 'pointer', fontWeight: 'bold' },
  emptyState: { height: '300px', display: 'flex', alignItems: 'center', justifyContent: 'center', border: '2px dashed #cbd5e1', borderRadius: '12px', color: '#94a3b8' }
};

export default DeptHeadApproval;