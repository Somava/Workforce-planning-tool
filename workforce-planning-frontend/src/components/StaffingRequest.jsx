import React, { useState } from 'react';
import { User, Briefcase, Calendar, Clock, AlertCircle, Send } from 'lucide-react'; // Install lucide-react for icons

const StaffingRequest = () => {
  const [formData, setFormData] = useState({
    projectName: '',
    requiredSkills: '',
    startDate: '',
    workload: ''
  });

  const [validationMessage, setValidationMessage] = useState(null);

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  return (
    <div style={styles.container}>
      <div style={styles.glassCard}>
        <div style={styles.header}>
          <Briefcase size={28} color="#4F46E5" />
          <h2 style={styles.title}>Create Staffing Request</h2>
        </div>
        <p style={styles.subtitle}>Fill in the project details to initiate the resource allocation process.</p>

        {/* ERROR: Validation Failed display */}
        {validationMessage && (
          <div style={styles.errorBox}>
            <AlertCircle size={20} />
            <span>The following issues were found: <strong>{validationMessage}</strong></span>
          </div>
        )}

        <form style={styles.form}>
          {/* Project Name */}
          <div style={styles.inputGroup}>
            <label style={styles.label}>Project Name</label>
            <div style={styles.inputWrapper}>
              <User style={styles.icon} size={18} />
              <input 
                name="projectName"
                type="text" 
                placeholder="Enter project title"
                style={styles.input}
                onChange={handleChange}
                required
              />
            </div>
          </div>

          {/* Required Skills */}
          <div style={styles.inputGroup}>
            <label style={styles.label}>Required Skills</label>
            <textarea 
              name="requiredSkills"
              placeholder="List technical skills (e.g. React, Spring Boot, Azure)"
              style={{...styles.input, ...styles.textarea}}
              onChange={handleChange}
              required
            />
          </div>

          <div style={styles.row}>
            {/* Start Date */}
            <div style={{...styles.inputGroup, flex: 1}}>
              <label style={styles.label}>Start Date</label>
              <div style={styles.inputWrapper}>
                <Calendar style={styles.icon} size={18} />
                <input 
                  name="startDate"
                  type="date" 
                  style={styles.input}
                  onChange={handleChange}
                  required
                />
              </div>
            </div>

            {/* Workload */}
            <div style={{...styles.inputGroup, flex: 1}}>
              <label style={styles.label}>Workload (hrs/week)</label>
              <div style={styles.inputWrapper}>
                <Clock style={styles.icon} size={18} />
                <input 
                  name="workload"
                  type="number" 
                  placeholder="40"
                  style={styles.input}
                  onChange={handleChange}
                  required
                />
              </div>
            </div>
          </div>

          <button type="submit" style={styles.submitBtn}>
            <Send size={18} />
            Submit Staffing Request
          </button>
        </form>
      </div>
    </div>
  );
};

const styles = {
  container: {
    minHeight: '100vh',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    background: 'linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%)',
    padding: '20px'
  },
  glassCard: {
    background: 'rgba(255, 255, 255, 0.95)',
    padding: '40px',
    borderRadius: '20px',
    boxShadow: '0 10px 25px rgba(0,0,0,0.1)',
    width: '100%',
    maxWidth: '600px',
  },
  header: { display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '8px' },
  title: { margin: 0, fontSize: '24px', color: '#1f2937' },
  subtitle: { color: '#6b7280', marginBottom: '30px', fontSize: '14px' },
  errorBox: {
    display: 'flex', alignItems: 'center', gap: '10px',
    background: '#fee2e2', color: '#dc2626',
    padding: '12px', borderRadius: '8px', marginBottom: '20px', fontSize: '14px'
  },
  form: { display: 'flex', flexDirection: 'column', gap: '20px' },
  inputGroup: { display: 'flex', flexDirection: 'column', gap: '8px' },
  label: { fontSize: '14px', fontWeight: '600', color: '#374151' },
  inputWrapper: { position: 'relative', display: 'flex', alignItems: 'center' },
  icon: { position: 'absolute', left: '12px', color: '#9ca3af' },
  input: {
    width: '100%', padding: '12px 12px 12px 40px',
    borderRadius: '10px', border: '1px solid #d1d5db',
    fontSize: '15px', outline: 'none', transition: 'border 0.2s',
  },
  textarea: { padding: '12px', minHeight: '100px', resize: 'vertical' },
  row: { display: 'flex', gap: '20px' },
  submitBtn: {
    marginTop: '10px', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '10px',
    padding: '14px', borderRadius: '10px', border: 'none',
    background: '#4F46E5', color: 'white', fontWeight: 'bold',
    cursor: 'pointer', transition: 'background 0.3s'
  }
};

export default StaffingRequest;