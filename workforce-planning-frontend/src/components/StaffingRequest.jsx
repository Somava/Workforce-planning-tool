import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { Briefcase, Calendar, Clock, AlertCircle, Send, MapPin, List, CheckCircle } from 'lucide-react';

const StaffingRequest = () => {
    // Force scroll to top on component mount
    useEffect(() => {
        window.scrollTo(0, 0);
    }, []);

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
        wagePerHour: '', 
        requiredSkills: '', 
        projectLocation: '', 
        workLocation: ''    
    });

    const [loading, setLoading] = useState(false);
    const [message, setMessage] = useState({ type: '', text: '' });

    // --- FETCH PROJECTS ---
    useEffect(() => {
        const fetchProjects = async () => {
            try {
                // Using axios - interceptor handles the token automatically
                const res = await axios.get('http://localhost:8080/api/projects');
                setProjects(res.data || []);
            } catch (err) {
                setMessage({ type: 'error', text: 'Projects failed to load. Check API.' });
            }
        };
        fetchProjects();
    }, []);

    // --- HANDLE PROJECT CHANGE ---
    const handleProjectChange = async (e) => {
        const id = e.target.value;
        const selectedProj = projects.find(p => p.id.toString() === id);
        
        setDepartments([]);
        setFormData({
            ...formData,
            projectId: id,
            departmentId: '', 
            projectLocation: selectedProj ? selectedProj.location : '',
            workLocation: '' 
        });

        if (id) {
            try {
                const res = await axios.get(`http://localhost:8080/api/department-head/all-departments`);
                const deptData = res.data || [];
                
                // Logic to ensure unique department names
                const uniqueDepts = deptData.reduce((acc, current) => {
                    const exists = acc.find(item => item.name === current.name);
                    if (!exists) return acc.concat([current]);
                    return acc;
                }, []);
                
                setDepartments(uniqueDepts);
            } catch (err) {
                console.error("Error fetching departments:", err);
            }
        }
    };

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    // --- VALIDATION LOGIC ---
    const validateForm = () => {
        const today = new Date();
        today.setHours(0, 0, 0, 0); 
        
        const start = new Date(formData.projectStartDate);
        const end = new Date(formData.projectEndDate);
        
        const wage = parseFloat(formData.wagePerHour);
        const experience = parseInt(formData.experienceYears);

        if (!formData.projectId || !formData.departmentId) return "Project and Department are required.";
        if (!formData.workLocation) return "Please select a Work Location.";
        if (!formData.availabilityHoursPerWeek) return "Please select contract hours.";
        
        if (start < today) return "Start date cannot be in the past.";
        if (end < start) return "End date cannot be before start date.";

        const diffInMs = end - start;
        const diffInDays = diffInMs / (1000 * 60 * 60 * 24);
        if (diffInDays < 60) return "Project duration must be at least 2 months (60 days).";
        
        if (isNaN(experience) || experience < 1 || experience > 25) return "Experience must be between 1 and 25 years.";
        if (isNaN(wage) || wage <= 0 || wage > 40) return "Wage must be between 0.01 and 40.00 €.";
        
        return null;
    };

    // --- SUBMIT REQUEST ---
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
            title: formData.title,
            description: formData.description,
            projectId: parseInt(formData.projectId),
            departmentId: parseInt(formData.departmentId),
            experienceYears: parseInt(formData.experienceYears),
            availabilityHoursPerWeek: parseInt(formData.availabilityHoursPerWeek), 
            projectStartDate: formData.projectStartDate,
            projectEndDate: formData.projectEndDate,
            wagePerHour: parseFloat(formData.wagePerHour),
            projectLocation: formData.projectLocation,
            workLocation: formData.workLocation,
            requiredSkills: formData.requiredSkills.split(',').map(s => s.trim()).filter(s => s !== "")
        };

        try {
            // Interceptor handles headers. Data is passed as second argument.
            const response = await axios.post('http://localhost:8080/api/manager/create-staffing-requests', payload);

            if (response.status === 200 || response.status === 201) {
                setIsSubmitted(true);
            }
        } catch (err) {
            const status = err.response?.status;
            if (status === 401) {
                setMessage({ type: 'error', text: 'Session expired. Please log in again.' });
            } else if (status === 403) {
                setMessage({ type: 'error', text: 'Insufficient permissions to create requests.' });
            } else {
                setMessage({ type: 'error', text: 'Backend rejected the request. Please check input data.' });
            }
        } finally {
            setLoading(false);
        }
    };

    // --- SUCCESS SCREEN ---
    if (isSubmitted) {
        return (
            <div style={styles.container}>
                <div style={{...styles.glassCard, textAlign: 'center', padding: '60px', marginTop: '20px'}}>
                    <CheckCircle size={64} color="#10b981" style={{marginBottom: '20px', marginInline: 'auto'}} />
                    <h2 style={styles.title}>Request Submitted</h2>
                    <p style={{color: '#6b7280', margin: '15px 0 30px'}}>
                        Your request has been sent and is now being processed by the resource planner.
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
                        <textarea name="description" style={{...styles.input, ...styles.textarea}} placeholder="Brief Description..." onChange={handleChange} required />
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
                            <label style={styles.label}><MapPin size={14}/> Project Location</label>
                            <input name="projectLocation" value={formData.projectLocation} style={{...styles.input, background: '#f3f4f6', cursor: 'not-allowed'}} readOnly />
                        </div>
                        <div style={styles.flexItem}>
                            <label style={styles.label}><MapPin size={14}/> Work Location</label>
                            <select name="workLocation" style={styles.select} value={formData.workLocation} onChange={handleChange} required>
                                <option value="">Select Location</option>
                                <option value="Onsite">Onsite</option>
                                <option value="Remote">Remote</option>
                            </select>
                        </div>
                    </div>

                    <div style={styles.row}>
                        <div style={styles.flexItem}>
                            <label style={styles.label}>Experience (Years)</label>
                            <input name="experienceYears" type="number" style={styles.input} onChange={handleChange} required />
                        </div>
                        <div style={styles.flexItem}>
                            <label style={styles.label}>Wage / Hour (€)</label>
                            <input name="wagePerHour" type="number" step="0.1" style={styles.input} onChange={handleChange} required />
                        </div>
                    </div>

                    <div style={styles.inputGroup}>
                        <label style={styles.label}><List size={14}/> Required Skills</label>
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
                            <label style={styles.label}>Contract Type</label>
                            <select name="availabilityHoursPerWeek" style={styles.select} value={formData.availabilityHoursPerWeek} onChange={handleChange} required>
                                <option value="">Select Hours</option>
                                <option value="40">Full time (40 hrs/week)</option>
                                <option value="20">Part time (20 hrs/week)</option>
                            </select>
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

// ... Styles stay exactly the same ...
const styles = {
    container: { minHeight: '100vh', display: 'flex', alignItems: 'flex-start', justifyContent: 'center', background: '#f3f4f6', padding: '30px 20px' },
    glassCard: { background: '#fff', padding: '30px', borderRadius: '12px', boxShadow: '0 10px 15px -3px rgba(0,0,0,0.1)', width: '100%', maxWidth: '850px', marginTop: '10px' },
    header: { display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '20px' },
    title: { fontSize: '24px', fontWeight: 'bold', color: '#1f2937' },
    msgBox: { display: 'flex', alignItems: 'center', gap: '10px', padding: '12px', borderRadius: '8px', marginBottom: '20px', fontSize: '14px' },
    form: { display: 'flex', flexDirection: 'column', gap: '20px' },
    inputGroup: { display: 'flex', flexDirection: 'column', gap: '6px' },
    row: { display: 'flex', flexWrap: 'wrap', gap: '20px', width: '100%' },
    flexItem: { flex: '1 1 200px', display: 'flex', flexDirection: 'column', gap: '6px', minWidth: '0' },
    label: { fontSize: '14px', fontWeight: '600', color: '#374151', display: 'flex', alignItems: 'center', gap: '5px' },
    input: { width: '100%', padding: '12px', borderRadius: '8px', border: '1px solid #d1d5db', fontSize: '14px', outline: 'none', boxSizing: 'border-box' },
    select: { width: '100%', padding: '12px', borderRadius: '8px', border: '1px solid #d1d5db', background: '#fff', boxSizing: 'border-box', outline: 'none', cursor: 'pointer' },
    textarea: { minHeight: '90px', resize: 'vertical' },
    submitBtn: { marginTop: '10px', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '10px', padding: '14px', borderRadius: '8px', border: 'none', background: '#4F46E5', color: 'white', fontWeight: 'bold', cursor: 'pointer', fontSize: '16px' }
};

export default StaffingRequest;