import React, { useState, useEffect } from 'react';
import { Briefcase, Calendar, Clock, AlertCircle, Send, MapPin, List, FileText, CheckCircle } from 'lucide-react';

const StaffingRequest = () => {
    const [projects, setProjects] = useState([]);
    const [departments, setDepartments] = useState([]);
    const [isSubmitted, setIsSubmitted] = useState(false);
    const [formData, setFormData] = useState({
        title: '',
        description: '',
        experienceYears: '',
        projectId: '',
        availabilityHours: '',
        startDate: '',
        endDate: '',
        departmentId: '',
        wagePerHour: '',
        requiredSkills: '', 
        projectContext: '',
        projectLocation: '', 
        workLocation: ''    
    });

    const [loading, setLoading] = useState(false);
    const [message, setMessage] = useState({ type: '', text: '' });

    // 1. Fetch Projects and Departments for dropdowns
    useEffect(() => {
        const fetchData = async () => {
            try {
                const [projRes, deptRes] = await Promise.all([
                    fetch('http://localhost:8080/api/projects'),
                    fetch('http://localhost:8080/api/departments')
                ]);
                
                const projData = projRes.ok ? await projRes.json() : [];
                const deptData = deptRes.ok ? await deptRes.json() : [];
                
                setProjects(projData);
                setDepartments(deptData);

                if (!projRes.ok || !deptRes.ok) {
                    setMessage({ type: 'error', text: 'Data failed to load. Please refresh.' });
                }
            } catch (err) {
                setMessage({ type: 'error', text: 'Connection error: Backend is unreachable.' });
            }
        };
        fetchData();
    }, []);

    // 2. Dynamic Auto-fill for locations
    const handleProjectChange = (e) => {
        const id = e.target.value;
        const selectedProj = projects.find(p => p.id.toString() === id);
        
        setFormData({
            ...formData,
            projectId: id,
            projectLocation: selectedProj ? selectedProj.location : '',
            workLocation: selectedProj ? selectedProj.location : ''
        });
    };

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    // 3. Logic validation (Wage, Experience, Dates)
    const validateForm = () => {
        const start = new Date(formData.startDate);
        const end = new Date(formData.endDate);
        const wage = parseFloat(formData.wagePerHour);
        const experience = parseInt(formData.experienceYears);

        if (!formData.projectId || !formData.departmentId) return "Project and Department are required.";
        if (end < start) return "End date cannot be before start date.";
        if (wage > 40) return "Max wage allowed is 40.00 €.";
        if (experience > 25) return "Max experience allowed is 25 years.";
        return null;
    };

    // 4. Integrated Submission to Backend & Camunda Trigger
    const handleSubmit = async (e) => {
        e.preventDefault();
        setMessage({ type: '', text: '' });

        const error = validateForm();
        if (error) {
            setMessage({ type: 'error', text: error });
            window.scrollTo({ top: 0, behavior: 'smooth' });
            return;
        }

        setLoading(true);
        
        const payload = {
            ...formData,
            status: 'SUBMITTED', // Initial status before Worker validation
            experienceYears: parseInt(formData.experienceYears),
            availabilityHours: parseInt(formData.availabilityHours),
            wagePerHour: parseFloat(formData.wagePerHour),
            // Parse string into List<String> for Java
            requiredSkills: formData.requiredSkills.split(',').map(s => s.trim()).filter(s => s !== ""),
            projectId: parseInt(formData.projectId),
            departmentId: parseInt(formData.departmentId)
        };

        try {
            const response = await fetch('http://localhost:8080/api/requests/create', {
                method: 'POST',
                headers: { 
                    'Content-Type': 'application/json',
                    'X-User-ID': '1' // Replace with Auth state later
                },
                body: JSON.stringify(payload)
            });

            if (response.ok) {
                setIsSubmitted(true);
                setMessage({ type: 'success', text: 'Success! Workflow triggered.' });
            } else {
                setMessage({ type: 'error', text: 'Backend rejected the request.' });
            }
        } catch (err) {
            setMessage({ type: 'error', text: 'Network Error: Check if Spring Boot is running.' });
        } finally {
            setLoading(false);
        }
    };

    // Success View
    if (isSubmitted) {
        return (
            <div style={styles.container}>
                <div style={{...styles.glassCard, textAlign: 'center', padding: '60px'}}>
                    <CheckCircle size={64} color="#10b981" style={{marginBottom: '20px'}} />
                    <h2 style={styles.title}>Request Submitted</h2>
                    <p style={{color: '#6b7280', margin: '15px 0 30px'}}>
                        Your request is now being validated by the Camunda process engine. 
                        Status: <b>SUBMITTED</b>
                    </p>
                    <button onClick={() => window.location.reload()} style={styles.submitBtn}>
                        Create Another Request
                    </button>
                </div>
            </div>
        );
    }

    return (
        <div style={styles.container}>
            <div style={styles.glassCard}>
                <div style={styles.header}>
                    <Briefcase size={28} color="#4F46E5" />
                    <h2 style={styles.title}>New Staffing Request</h2>
                </div>

                {message.text && (
                    <div style={{...styles.msgBox, background: message.type === 'error' ? '#fee2e2' : '#dcfce7', color: message.type === 'error' ? '#dc2626' : '#166534'}}>
                        <AlertCircle size={20} />
                        <span>{message.text}</span>
                    </div>
                )}

                <form style={styles.form} onSubmit={handleSubmit}>
                    <div style={styles.inputGroup}>
                        <label style={styles.label}>Position Title</label>
                        <input name="title" style={styles.input} placeholder="e.g. Java Engineer" onChange={handleChange} required />
                    </div>

                    <div style={styles.inputGroup}>
                        <label style={styles.label}>Job Description</label>
                        <textarea name="description" style={{...styles.input, ...styles.textarea}} onChange={handleChange} required />
                    </div>

                    <div style={styles.row}>
                        <div style={{...styles.inputGroup, flex: 1}}>
                            <label style={styles.label}>Project</label>
                            <select name="projectId" style={styles.select} onChange={handleProjectChange} required>
                                <option value="">Select Project</option>
                                {projects.map(p => <option key={p.id} value={p.id}>{p.name}</option>)}
                            </select>
                        </div>
                        <div style={{...styles.inputGroup, flex: 1}}>
                            <label style={styles.label}>Department</label>
                            <select name="departmentId" style={styles.select} onChange={handleChange} required>
                                <option value="">Select Dept</option>
                                {departments.map(d => <option key={d.id} value={d.id}>{d.name}</option>)}
                            </select>
                        </div>
                    </div>

                    <div style={styles.row}>
                        <div style={{...styles.inputGroup, flex: 1}}>
                            <label style={styles.label}><MapPin size={14}/> Project Location</label>
                            <input name="projectLocation" value={formData.projectLocation} style={{...styles.input, background: '#f9fafb'}} readOnly />
                        </div>
                        <div style={{...styles.inputGroup, flex: 1}}>
                            <label style={styles.label}><MapPin size={14}/> Work Location</label>
                            <input name="workLocation" value={formData.workLocation} style={{...styles.input, background: '#f9fafb'}} readOnly />
                        </div>
                    </div>

                    <div style={styles.row}>
                        <div style={{...styles.inputGroup, flex: 1}}>
                            <label style={styles.label}>Experience (Years)</label>
                            <input name="experienceYears" type="number" style={styles.input} onChange={handleChange} required />
                        </div>
                        <div style={{...styles.inputGroup, flex: 1}}>
                            <label style={styles.label}>Wage / Hour (€)</label>
                            <input name="wagePerHour" type="number" step="0.01" style={styles.input} onChange={handleChange} required />
                        </div>
                    </div>

                    <div style={styles.inputGroup}>
                        <label style={styles.label}><List size={14}/> Required Skills (comma separated)</label>
                        <input name="requiredSkills" placeholder="React, Spring Boot, SQL" style={styles.input} onChange={handleChange} required />
                    </div>

                    <div style={styles.inputGroup}>
                        <label style={styles.label}><FileText size={14}/> Project Context</label>
                        <textarea name="projectContext" style={{...styles.input, ...styles.textarea}} onChange={handleChange} />
                    </div>

                    <div style={styles.row}>
                        <div style={{...styles.inputGroup, flex: 1}}>
                            <label style={styles.label}>Start Date</label>
                            <input name="startDate" type="date" style={styles.input} onChange={handleChange} required />
                        </div>
                        <div style={{...styles.inputGroup, flex: 1}}>
                            <label style={styles.label}>End Date</label>
                            <input name="endDate" type="date" style={styles.input} onChange={handleChange} required />
                        </div>
                        <div style={{...styles.inputGroup, flex: 1}}>
                            <label style={styles.label}>Hrs/Week</label>
                            <input name="availabilityHours" type="number" style={styles.input} onChange={handleChange} required />
                        </div>
                    </div>

                    <button type="submit" style={styles.submitBtn} disabled={loading}>
                        {loading ? 'Processing Workflow...' : <><Send size={18} /> Submit Request</>}
                    </button>
                </form>
            </div>
        </div>
    );
};

const styles = {
    container: { minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center', background: '#f3f4f6', padding: '40px' },
    glassCard: { background: '#fff', padding: '40px', borderRadius: '12px', boxShadow: '0 10px 15px -3px rgba(0,0,0,0.1)', width: '100%', maxWidth: '800px' },
    header: { display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '25px' },
    title: { fontSize: '24px', fontWeight: 'bold', color: '#1f2937' },
    msgBox: { display: 'flex', alignItems: 'center', gap: '10px', padding: '12px', borderRadius: '8px', marginBottom: '20px', fontSize: '14px' },
    form: { display: 'flex', flexDirection: 'column', gap: '20px' },
    inputGroup: { display: 'flex', flexDirection: 'column', gap: '6px' },
    label: { fontSize: '14px', fontWeight: '600', color: '#374151', display: 'flex', alignItems: 'center', gap: '5px' },
    input: { width: '100%', padding: '12px', borderRadius: '8px', border: '1px solid #d1d5db', fontSize: '14px', outline: 'none' },
    select: { width: '100%', padding: '12px', borderRadius: '8px', border: '1px solid #d1d5db', background: '#fff' },
    textarea: { minHeight: '90px', resize: 'vertical' },
    row: { display: 'flex', gap: '15px' },
    submitBtn: { marginTop: '10px', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '10px', padding: '14px', borderRadius: '8px', border: 'none', background: '#4F46E5', color: 'white', fontWeight: 'bold', cursor: 'pointer', fontSize: '16px' }
};

export default StaffingRequest;