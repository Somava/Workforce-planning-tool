import React, { useState } from 'react';
import { Search, Code, Clock, Send, CheckCircle } from 'lucide-react';

const EmployeeDashboard = () => {
  const [availableRequests] = useState([
    { id: 'REQ-001', project: 'Cloud Migration', skills: 'AWS, Docker, Terraform', workload: 40, start: 'Jan 2025' },
    { id: 'REQ-002', project: 'AI Chatbot', skills: 'Python, PyTorch, NLP', workload: 20, start: 'Feb 2025' },
    { id: 'REQ-003', project: 'Legacy Refactor', skills: 'Java 8, Spring, Oracle', workload: 40, start: 'Immediate' },
  ]);

  const [selectedProject, setSelectedProject] = useState(null);
  const [applicationData, setApplicationData] = useState({
    employeeSkills: '',
    applicationReason: ''
  });
  const [submitted, setSubmitted] = useState(false);

  const handleApply = (e) => {
    e.preventDefault();
    setSubmitted(true);
    setTimeout(() => {
      setSubmitted(false);
      setSelectedProject(null);
    }, 3000);
  };

  return (
    <div style={styles.container}>
      <header style={styles.header}>
        <h1>Find Your Next Project</h1>
        <p>Browse approved staffing requests and apply for roles.</p>
      </header>

      <div style={styles.mainLayout}>
        <div style={styles.listSection}>
          <div style={styles.searchBar}>
            <Search size={18} />
            <input type="text" placeholder="Search projects..." style={styles.searchInput} />
          </div>
          
          {availableRequests.map((req) => (
            <div 
              key={req.id} 
              style={{
                ...styles.projectCard, 
                border: selectedProject?.id === req.id ? '2px solid #4f46e5' : '1px solid #e2e8f0'
              }}
              onClick={() => setSelectedProject(req)}
            >
              <div style={styles.cardHeader}>
                <h3>{req.project}</h3>
                <span style={styles.badge}>{req.start}</span>
              </div>
              <div style={styles.cardDetail}><Code size={14} /> <span>{req.skills}</span></div>
              <div style={styles.cardDetail}><Clock size={14} /> <span>{req.workload} hrs/week</span></div>
            </div>
          ))}
        </div>

        <div style={styles.formSection}>
          {selectedProject ? (
            <div style={styles.formCard}>
              {submitted ? (
                <div style={styles.successState}>
                  <CheckCircle size={48} color="#10b981" />
                  <h2>Application Sent!</h2>
                </div>
              ) : (
                <form onSubmit={handleApply}>
                  <h2 style={styles.formTitle}>Apply for {selectedProject.project}</h2>
                  <div style={styles.inputGroup}>
                    <label style={styles.label}>Your Skills*</label>
                    <textarea 
                      required
                      style={styles.textarea}
                      onChange={(e) => setApplicationData({...applicationData, employeeSkills: e.target.value})}
                    />
                  </div>
                  <div style={styles.inputGroup}>
                    <label style={styles.label}>Reason for Applying</label>
                    <textarea 
                      style={styles.textarea}
                      onChange={(e) => setApplicationData({...applicationData, applicationReason: e.target.value})}
                    />
                  </div>
                  <button type="submit" style={styles.submitBtn}>
                    <Send size={18} /> Submit Application
                  </button>
                </form>
              )}
            </div>
          ) : (
            <div style={styles.emptyState}>
              <Search size={40} color="#94a3b8" />
              <p>Select a project from the left to view details and apply.</p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

const styles = {
  container: { maxWidth: '1200px', margin: '0 auto' },
  header: { marginBottom: '30px' },
  mainLayout: { display: 'flex', gap: '30px' },
  listSection: { flex: 1, display: 'flex', flexDirection: 'column', gap: '15px' },
  formSection: { flex: 1.2 },
  searchBar: { display: 'flex', alignItems: 'center', gap: '10px', background: 'white', padding: '10px 15px', borderRadius: '10px', border: '1px solid #e2e8f0', marginBottom: '10px' },
  searchInput: { border: 'none', outline: 'none', width: '100%', fontSize: '15px' },
  projectCard: { background: 'white', padding: '20px', borderRadius: '12px', cursor: 'pointer' },
  cardHeader: { display: 'flex', justifyContent: 'space-between', marginBottom: '10px' },
  badge: { display: 'inline-block', alignSelf: 'flex-start', background: '#f0fdf4', color: '#166534', padding: '4px 10px', borderRadius: '20px', fontSize: '12px', fontWeight: 'bold' },
  cardDetail: { display: 'flex', alignItems: 'center', gap: '8px', fontSize: '14px', color: '#64748b', marginBottom: '5px' },
  formCard: { background: 'white', padding: '40px', borderRadius: '15px', boxShadow: '0 4px 6px -1px rgba(0,0,0,0.1)' },
  formTitle: { marginBottom: '25px', color: '#1e293b' },
  inputGroup: { marginBottom: '20px' },
  label: { display: 'block', fontSize: '14px', fontWeight: '600', marginBottom: '8px', color: '#374151' },
  textarea: { width: '100%', padding: '12px', borderRadius: '8px', border: '1px solid #d1d5db', minHeight: '100px', outline: 'none' },
  submitBtn: { width: '100%', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '10px', padding: '14px', background: '#4f46e5', color: 'white', border: 'none', borderRadius: '8px', fontWeight: 'bold', cursor: 'pointer' },
  emptyState: { height: '400px', display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', background: '#f8fafc', border: '2px dashed #e2e8f0', borderRadius: '15px', color: '#64748b' },
  successState: { textAlign: 'center', padding: '20px' }
};

export default EmployeeDashboard;