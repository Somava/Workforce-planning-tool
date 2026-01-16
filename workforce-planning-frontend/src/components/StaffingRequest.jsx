import React, { useState, useEffect } from 'react';
import { Briefcase, Calendar, Clock, AlertCircle, Send, MapPin, List, CheckCircle } from 'lucide-react';

const StaffingRequest = () => {
    const [projects, setProjects] = useState([]);
    const [departments, setDepartments] = useState([]);
    const [isSubmitted, setIsSubmitted] = useState(false);
    const [formData, setFormData] = useState({
        title: '',
        description: '',
        experienceYears: '',
        projectId: '',
        availabilityHoursPerWeek: '',
        projectStartDate: '',
        projectEndDate: '',
        departmentId: '',
        wagePerHour: '', // Managed as a string for input, parsed on submit
        requiredSkills: '', 
        projectLocation: '', 
        workLocation: ''    
    });

    const [loading, setLoading] = useState(false);
    const [message, setMessage] = useState({ type: '', text: '' });

    useEffect(() => {
        const fetchProjects = async () => {
            try {
                const projRes = await fetch('http://localhost:8080/api/projects');
                if (projRes.ok) {
                    const projData = await projRes.json();
                    setProjects(projData);
                } else {
                    setMessage({ type: 'error', text: 'Projects failed to load. Check API.' });
                }
            } catch (err) {
                setMessage({ type: 'error', text: 'Connection error: Backend unreachable.' });
            }
        };
        fetchProjects();
    }, []);

    const handleProjectChange = async (e) => {
        const id = e.target.value;
        const selectedProj = projects.find(p => p.id.toString() === id);
        
        setDepartments([]);
        setFormData({
            ...formData,
            projectId: id,
            departmentId: '', 
            projectLocation: selectedProj ? selectedProj.location : '',
            workLocation: selectedProj ? selectedProj.location : ''
        });

        if (id) {
            try {
                const deptRes = await fetch(`http://localhost:8080/api/departments/project/${id}`);
                if (deptRes.ok) {
                    const deptData = await deptRes.json();
                    const uniqueDepts = deptData.reduce((acc, current) => {
                        const exists = acc.find(item => item.name === current.name);
                        if (!exists) return acc.concat([current]);
                        return acc;
                    }, []);
                    setDepartments(uniqueDepts);
                }
            } catch (err) {
                console.error("Error fetching project departments:", err);
            }
        }
    };

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const validateForm = () => {
        const today = new Date();
        today.setHours(0, 0, 0, 0); 
        
        const start = new Date(formData.projectStartDate);
        const end = new Date(formData.projectEndDate);
        
        // --- Wage Specific Validation ---
        const wage = parseFloat(formData.wagePerHour);
        
        const experience = parseInt(formData.experienceYears);
        const hours = parseInt(formData.availabilityHoursPerWeek);

        if (!formData.projectId || !formData.departmentId) return "Project and Department are required.";
        
        if (start < today) return "Start date cannot be in the past.";
        if (end < start) return "End date cannot be before start date.";

        const diffInMs = end - start;
        const diffInDays = diffInMs / (1000 * 60 * 60 * 24);
        if (diffInDays < 60) return "Project duration must be at least 2 months (60 days).";
        
        const maxSpanMs = 5 * 365.25 * 24 * 60 * 60 * 1000; 
        if (diffInMs > maxSpanMs) return "Project span cannot exceed 5 years.";
        
        if (isNaN(experience) || experience < 1 || experience > 25) return "Experience must be between 1 and 25 years.";
        
        // --- Wage Range Validation ---
        if (isNaN(wage) || wage <= 0) return "Wage per hour must be greater than zero.";
        if (wage > 40) return "Max wage allowed is 40.00 €.";
        
        if (isNaN(hours) || hours <= 0) return "Hours per week must be greater than zero.";
        if (hours > 40) return "Availability cannot exceed 40 hours per week.";
        
        return null;
    };

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
            experienceYears: parseInt(formData.experienceYears),
            availabilityHoursPerWeek: parseInt(formData.availabilityHoursPerWeek),
            
            // --- Final Wage Formatting for Payload ---
            wagePerHour: parseFloat(formData.wagePerHour),
            
            requiredSkills: formData.requiredSkills.split(',').map(s => s.trim()).filter(s => s !== ""),
            projectId: parseInt(formData.projectId),
            departmentId: parseInt(formData.departmentId)
        };

        try {
            const response = await fetch('http://localhost:8080/api/requests/create', {
                method: 'POST',
                headers: { 
                    'Content-Type': 'application/json',
                    'X-User-ID': '1' 
                },
                body: JSON.stringify(payload)
            });

            if (response.ok) {
                setIsSubmitted(true);
            } else {
                setMessage({ type: 'error', text: 'Backend rejected the request.' });
            }
        } catch (err) {
            setMessage({ type: 'error', text: 'Network Error: Check Spring Boot.' });
        } finally {
            setLoading(false);
        }
    };

    if (isSubmitted) {
        return (
            <div style={styles.container}>
                <div style={{...styles.glassCard, textAlign: 'center', padding: '60px'}}>
                    <CheckCircle size={64} color="#10b981" style={{marginBottom: '20px', marginInline: 'auto'}} />
                    <h2 style={styles.title}>Request Submitted</h2>
                    <p style={{color: '#6b7280', margin: '15px 0 30px'}}>
                        Your request has been sent and is now being processed.
                    </p>
                    <div style={{ display: 'flex', justifyContent: 'center', width: '100%' }}>
                        <button onClick={() => window.location.reload()} style={{...styles.submitBtn, width: 'auto', paddingInline: '40px'}}>
                            Create Another Request
                        </button>
                    </div>
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
                        <textarea name="description" style={{...styles.input, ...styles.textarea}} placeholder="Brief Description of the required position" onChange={handleChange} required />
                    </div>

                    <div style={styles.row}>
                        <div style={styles.flexItem}>
                            <label style={styles.label}>Project</label>
                            <select name="projectId" value={formData.projectId} style={styles.select} onChange={handleProjectChange} required>
                                <option value="">Select Project</option>
                                {projects.map(p => <option key={p.id} value={p.id}>{p.name}</option>)}
                            </select>
                        </div>
                        <div style={styles.flexItem}>
                            <label style={styles.label}>Department</label>
                            <select name="departmentId" value={formData.departmentId} style={styles.select} onChange={handleChange} required disabled={!formData.projectId}>
                                <option value="">{!formData.projectId ? "Select Project First" : "Select Dept"}</option>
                                {departments.map(d => <option key={d.id} value={d.id}>{d.name}</option>)}
                            </select>
                        </div>
                    </div>

                    <div style={styles.row}>
                        <div style={styles.flexItem}>
                            <label style={styles.label}><MapPin size={14}/> Project Location (Auto)</label>
                            <input name="projectLocation" value={formData.projectLocation} style={{...styles.input, background: '#f3f4f6', cursor: 'not-allowed'}} readOnly />
                        </div>
                        <div style={styles.flexItem}>
                            <label style={styles.label}><MapPin size={14}/> Work Location</label>
                            <input 
                                name="workLocation"  
                                style={styles.input} 
                                placeholder="Preferred Office Location"
                                onChange={handleChange} 
                                required 
                            />
                        </div>
                    </div>

                    <div style={styles.row}>
                        <div style={styles.flexItem}>
                            <label style={styles.label}>Experience (Years)</label>
                            <input 
                                name="experienceYears" 
                                type="text" 
                                value={formData.experienceYears}
                                style={styles.input} 
                                onChange={(e) => {
                                    if (/^\d*$/.test(e.target.value)) handleChange(e);
                                }} 
                                required 
                            />
                        </div>
                        <div style={styles.flexItem}>
                            <label style={styles.label}>Wage / Hour (€)</label>
                            {/* Input: Allowing decimals but keeping it numeric */}
                            <input 
                                name="wagePerHour" 
                                type="number" 
                                step="0.01" 
                                style={styles.input} 
                                onChange={handleChange} 
                                required 
                            />
                        </div>
                    </div>

                    <div style={styles.inputGroup}>
                        <label style={styles.label}><List size={14}/> Required Skills </label>
                        <input name="requiredSkills" placeholder="React, Spring Boot, SQL" style={styles.input} onChange={handleChange} required />
                    </div>

                    <div style={styles.row}>
                        <div style={styles.flexItem}>
                            <label style={styles.label}>Start Date</label>
                            <input name="projectStartDate" type="date" style={styles.input} onChange={handleChange} required />
                        </div>
                        <div style={styles.flexItem}>
                            <label style={styles.label}>End Date</label>
                            <input name="projectEndDate" type="date" style={styles.input} onChange={handleChange} required />
                        </div>
                        <div style={styles.flexItem}>
                            <label style={styles.label}>Hrs/Week</label>
                            <input 
                                name="availabilityHoursPerWeek" 
                                type="text" 
                                value={formData.availabilityHoursPerWeek}
                                style={styles.input} 
                                onChange={(e) => {
                                    if (/^\d*$/.test(e.target.value)) handleChange(e);
                                }} 
                                required 
                            />
                        </div>
                    </div>

                    <button type="submit" style={styles.submitBtn} disabled={loading}>
                        {loading ? 'Processing...' : <><Send size={18} /> Submit Request</>}
                    </button>
                </form>
            </div>
        </div>
    );
};

const styles = {
    container: { minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center', background: '#f3f4f6', padding: '20px' },
    glassCard: { background: '#fff', padding: '30px', borderRadius: '12px', boxShadow: '0 10px 15px -3px rgba(0,0,0,0.1)', width: '100%', maxWidth: '850px' },
    header: { display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '20px' },
    title: { fontSize: '24px', fontWeight: 'bold', color: '#1f2937' },
    msgBox: { display: 'flex', alignItems: 'center', gap: '10px', padding: '12px', borderRadius: '8px', marginBottom: '20px', fontSize: '14px' },
    form: { display: 'flex', flexDirection: 'column', gap: '20px' },
    inputGroup: { display: 'flex', flexDirection: 'column', gap: '6px' },
    row: { display: 'flex', flexWrap: 'wrap', gap: '20px', width: '100%' },
    flexItem: { flex: '1 1 200px', display: 'flex', flexDirection: 'column', gap: '6px', minWidth: '0' },
    label: { fontSize: '14px', fontWeight: '600', color: '#374151', display: 'flex', alignItems: 'center', gap: '5px' },
    input: { width: '100%', padding: '12px', borderRadius: '8px', border: '1px solid #d1d5db', fontSize: '14px', outline: 'none', boxSizing: 'border-box' },
    select: { width: '100%', padding: '12px', borderRadius: '8px', border: '1px solid #d1d5db', background: '#fff', boxSizing: 'border-box' },
    textarea: { minHeight: '90px', resize: 'vertical' },
    submitBtn: { marginTop: '10px', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '10px', padding: '14px', borderRadius: '8px', border: 'none', background: '#4F46E5', color: 'white', fontWeight: 'bold', cursor: 'pointer', fontSize: '16px' }
};

export default StaffingRequest;