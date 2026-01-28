import React, { useEffect, useState, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';

const API_BASE = process.env.REACT_APP_API_BASE_URL || "http://localhost:8080";
// --- Comprehensive Visual Mapping for all RequestStatus Enums ---
const STATUS_CONFIG = {
    'DRAFT': { color: '#f3f4f6', textColor: '#374151', label: 'Draft' },
    'SUBMITTED': { color: '#e0f2fe', textColor: '#0369a1', label: 'Correction Required' },
    'PM_RESUBMITTED': { color: '#e0e7ff', textColor: '#4338ca', label: 'PM Resubmitted' },
    'PM_CANCELLED': { color: '#111827', textColor: '#ffffff', label: 'PM Cancelled' },
    'PENDING_APPROVAL': { color: '#fef3c7', textColor: '#92400e', label: 'Pending Approval' },
    'REQUEST_REJECTED': { color: '#fee2e2', textColor: '#991b1b', label: 'Dept Head Rejected' },
    'APPROVED': { color: '#d1fae5', textColor: '#065f46', label: 'Approved/Open' },
    'REJECTED': { color: '#fee2e2', textColor: '#b91c1c', label: 'Rejected' },
    'EMPLOYEE_RESERVED': { color: '#fae8ff', textColor: '#86198f', label: 'Employee Reserved' },
    'INT_EMPLOYEE_APPROVED_BY_DH': { color: '#ecfdf5', textColor: '#047857', label: 'Internal Employee Approved' },
    'INT_EMPLOYEE_REJECTED_BY_DH': { color: '#fff1f2', textColor: '#be123c', label: 'Internal Employee Rejected by Dept. Head' },
    'INT_EMPLOYEE_REJECTED_BY_EMP': { color: '#fff1f2', textColor: '#be123c', label: 'Employee Declined' },
    'INT_EMPLOYEE_ASSIGNED': { color: '#dcfce7', textColor: '#166534', label: 'Internal Employee Assigned' },
    'EXTERNAL_SEARCH_TRIGGERED': { color: '#eff6ff', textColor: '#1d4ed8', label: 'External Search' },
    'EXTERNAL_RESPONSE_RECEIVED': { color: '#f0f9ff', textColor: '#075985', label: 'External Response' },
    'EXT_EMPLOYEE_REJECTED_BY_DH': { color: '#fef2f2', textColor: '#991b1b', label: 'External Employee Rejected by Dept. Head' },
    'NO_EXT_EMPLOYEE_FOUND': { color: '#f9fafb', textColor: '#4b5563', label: 'No External Employee Found' },
    'ASSIGNED': { color: '#f3e8ff', textColor: '#6b21a8', label: 'Staff Assigned' },
    'CANCELLED': { color: '#111827', textColor: '#ffffff', label: 'Cancelled' },
    'OPEN': { color: '#ecfdf5', textColor: '#047857', label: 'Active' },
    'CLOSED': { color: '#6b7280', textColor: '#ffffff', label: 'Closed' },
    'EXT EMPLOYEE APPROVED BY DH': { color: '#fae8ff', textColor: '#86198f', label: 'External Employee Approved by Dept. Head' },
};

const ManagerHome = () => {
    const navigate = useNavigate();
    const firstName = localStorage.getItem("firstName");
    const userEmail = localStorage.getItem("email");

    const [activeTab, setActiveTab] = useState('recent'); 
    const [requests, setRequests] = useState([]);
    const [rejectedRequests, setRejectedRequests] = useState([]);
    const [employees, setEmployees] = useState([]); 
    const [projects, setProjects] = useState([]); 
    const [successAssignments, setSuccessAssignments] = useState([]); 
    const [selectedRequest, setSelectedRequest] = useState(null); 
    const [isRefreshing, setIsRefreshing] = useState(false);
    const [lastSynced, setLastSynced] = useState(new Date().toLocaleTimeString());

    const [resubmitModal, setResubmitModal] = useState(null);
    const [editData, setEditData] = useState({});
    const [error, setError] = useState("");

    const [showProjectModal, setShowProjectModal] = useState(false);
    const [projectError, setProjectError] = useState(""); 
    const [newProject, setNewProject] = useState({
        name: "",
        description: "",
        startDate: "",
        endDate: "",
        location: ""
    });
// --- 1. VALIDATION LOGIC ---
const validateResubmission = (data, originalReq) => {
    const wage = parseFloat(data.wagePerHour);
    const experience = parseInt(data.experienceYears);
    
    let diffInDays = 100; 
    if(originalReq.projectStartDate && originalReq.projectEndDate) {
        const start = new Date(originalReq.projectStartDate);
        const end = new Date(originalReq.projectEndDate);
        const diffInMs = end - start;
        diffInDays = diffInMs / (1000 * 60 * 60 * 24);
    }

    if (!data.title || !data.description) return "Position title and description are required.";
    if (diffInDays < 60) return "Project duration must be at least 2 months (60 days).";
    if (isNaN(experience) || experience < 1 || experience > 25) return "Experience must be between 1 and 25 years.";
    if (isNaN(wage) || wage < 1 || wage > 40) return "Wage must be between 1.00 and 40.00 €.";
    if (!data.workLocation) return "Please select a work location.";
    
    return null;
};

// --- 2. DATA FETCHING ---
const fetchRequests = useCallback(async () => {
    setIsRefreshing(true);
    try {
        // Updated endpoints to match your request; Interceptor attaches token automatically
        const [recentRes, rejectedRes, empRes, projRes, successRes] = await Promise.all([
            axios.get(API_BASE + "/api/manager/all-staffing-requests"), 
            axios.get(API_BASE + "/api/manager/rejected-requests"),
            axios.get(API_BASE + "/api/workforce-overview/all-employees"),
            axios.get(API_BASE + "/api/projects"),
            axios.get(API_BASE + "/api/workforce-overview/success-notifications")
        ]);

        setRequests(recentRes.data || []);
        setRejectedRequests(rejectedRes.data || []);
        setEmployees(empRes.data || []);
        setProjects(projRes.data || []);
        setSuccessAssignments(successRes.data || []);
        
        setLastSynced(new Date().toLocaleTimeString());
    } catch (err) {
        console.error("Fetch failed", err);
        setRequests([]);
        setRejectedRequests([]);
    } finally {
        setIsRefreshing(false);
    }
}, [API_BASE]); 

// --- 3. AUTO-REFRESH EFFECT ---
useEffect(() => {
    fetchRequests();
    const interval = setInterval(fetchRequests, 30000);
    return () => clearInterval(interval);
}, [fetchRequests]); 

// --- 4. PROJECT CREATION ---
const handleCreateProject = async () => {
    setProjectError(""); 
    const { name, startDate, endDate, description, location } = newProject;

    if (!name || !startDate || !endDate || !description || !location) {
        setProjectError("All fields are required.");
        return;
    }

    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const start = new Date(startDate);
    const end = new Date(endDate);

    if (start < today) {
        setProjectError("Start date cannot be in the past.");
        return;
    }

    const minEndDate = new Date(start);
    minEndDate.setFullYear(start.getFullYear() + 1);

    if (end < minEndDate) {
        setProjectError("Project duration must be at least 1 year (12 months).");
        return;
    }

    try {
        // managerEmail removed; backend identifies user via JWT
        await axios.post(API_BASE + "/api/projects/create", newProject);
        setShowProjectModal(false);
        setNewProject({ name: "", description: "", startDate: "", endDate: "", location: "" });
        fetchRequests(); 
    } catch (err) {
        setProjectError("Failed to create project. Technical error occurred.");
    }
};

// --- 5. REVIEW DECISION ---
const handleDecision = async (id, isResubmit, originalReq = null) => {
    setError("");
    let payload = {};

    if (isResubmit) {
        const validationError = validateResubmission(editData, originalReq);
        if (validationError) {
            setError(validationError);
            return;
        }

        payload = {
            title: editData.title,
            description: editData.description,
            requiredSkills: typeof editData.requiredSkills === 'string' 
                ? editData.requiredSkills.split(',').map(s => s.trim()).filter(s => s !== "")
                : editData.requiredSkills,
            experienceYears: parseInt(editData.experienceYears),
            wagePerHour: parseFloat(editData.wagePerHour),
            workLocation: editData.workLocation,
            availabilityHoursPerWeek: parseInt(editData.availabilityHoursPerWeek)
        };
    }

    try {
        // email parameter removed; identity extracted from token by JwtAuthFilter
        const url = API_BASE + "/api/manager/staffing-request/review-decision" + 
                "?requestId=" + id + 
                "&isResubmit=" + isResubmit;
        await axios.post(url, payload);
        setResubmitModal(null);
        fetchRequests();
    } catch (err) {
        setError("Communication with server failed. Please try again.");
    }
};

// --- 6. MODAL CONTROL ---
const openResubmitModal = (req) => {
    setError("");
    setEditData({
        title: req.title,
        description: req.description,
        requiredSkills: req.requiredSkills?.join(', ') || "",
        experienceYears: req.experienceYears,
        wagePerHour: req.wagePerHour,
        workLocation: req.workLocation || "Remote",
        availabilityHoursPerWeek: req.availabilityHoursPerWeek || "40"
    });
    setResubmitModal(req);
};
    // Components like DetailRow and StatusBadge should be defined or used here
    const DetailRow = ({ label, value }) => (
        <div style={{ display: 'flex', justifyContent: 'space-between', padding: '8px 0', borderBottom: '1px solid #f3f4f6' }}>
            <span style={{ color: '#6b7280', fontSize: '13px' }}>{label}:</span>
            <span style={{ fontWeight: '600', fontSize: '13px', color: '#111827' }}>{value || 'N/A'}</span>
        </div>
    );

    const renderEmployeeList = () => (
        <div style={{ overflowX: 'auto', marginTop: '10px' }}>
            <table style={styles.table}>
                <thead>
                    <tr style={styles.tableHeader}>
                        <th style={styles.th}>ID & Name</th>
                        <th style={styles.th}>Languages</th>
                        <th style={styles.th}>Skills & Experience</th>
                        <th style={styles.th}>Rating</th>
                        <th style={styles.th}>Status</th>
                    </tr>
                </thead>
                <tbody>
                    {employees.map(emp => (
                        <tr key={emp.id} style={styles.tableRow}>
                            <td style={styles.td}>
                                <div style={{ fontSize: '11px', fontWeight: 'bold', color: '#6366f1' }}>{emp.employeeId}</div>
                                <div style={{ fontWeight: 'bold', color: '#111827' }}>{emp.fullName}</div>
                                <div style={{ fontSize: '12px', color: '#6b7280' }}>{emp.email}</div>
                            </td>
                            <td style={styles.td}>
                                <div style={{ display: 'flex', flexWrap: 'wrap', gap: '4px', marginBottom: '6px' }}>
                                    {emp.languages?.map(lang => (
                                        <span key={lang} style={styles.skillTagSmall}> {lang}</span>
                                    ))}
                                </div>
                            </td>
                            <td style={styles.td}>
                                <div style={{ display: 'flex', flexWrap: 'wrap', gap: '4px', marginBottom: '6px' }}>
                                    {emp.skills?.map(skill => (
                                        <span key={skill} style={styles.skillTagSmall}>{skill}</span>
                                    ))}
                                </div>
                                <div style={{ fontSize: '11px', color: '#6b7280', fontWeight: '600' }}>
                                    📅 {emp.experienceYears} Years Experience
                                </div>
                            </td>
                            <td style={styles.td}>
                                <div style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
                                    <span style={{ fontWeight: 'bold', color: '#f59e0b' }}>⭐ {emp.performanceRating}</span>
                                </div>
                            </td>
                            <td style={styles.td}>
                                <span style={{ 
                                    ...styles.statusPill, 
                                    backgroundColor: emp.availabilityStatus === 'AVAILABLE' ? '#dcfce7' : '#fee2e2',
                                    color: emp.availabilityStatus === 'AVAILABLE' ? '#166534' : '#991b1b'
                                }}>
                                    {emp.availabilityStatus}
                                </span>
                            </td>
                        </tr>
                    ))}
                </tbody>
            </table>
        </div>
    );

    const renderProjectList = () => (
        <div style={styles.projectListContainer}>
            {projects.map(proj => (
                <div key={proj.id} style={styles.projectRow}>
                    <div style={styles.projectMainInfo}>
                        <h3 style={styles.projectTitleText}>{proj.name}</h3>
                        <p style={styles.projectDescriptionText}>{proj.description}</p>
                    </div>
                    <div style={styles.projectMetaInfo}>
                        <div style={styles.metaItem}>
                            <span style={styles.metaLabel}>Location</span>
                            <span style={styles.metaValue}>📍 {proj.location}</span>
                        </div>
                        <div style={styles.metaItem}>
                            <span style={styles.metaLabel}>Timeline</span>
                            <span style={styles.metaValue}>📅 {proj.startDate} — {proj.endDate}</span>
                        </div>
                        <div style={styles.metaItem}>
                            <span style={styles.metaLabel}>Status</span>
                            <div style={{ marginTop: '4px' }}>
                                {new Date(proj.endDate) > new Date() ? (
                                    <span style={{...styles.badge, backgroundColor: '#ecfdf5', color: '#047857'}}>Active</span>
                                ) : (
                                    <span style={{...styles.badge, backgroundColor: '#f3f4f6', color: '#6b7280'}}>Closed</span>
                                )}
                            </div>
                        </div>
                    </div>
                </div>
            ))}
        </div>
    );

    const renderSuccessAssignments = () => {
    const assignments = Array.isArray(successAssignments) ? successAssignments : [];
        
    if (assignments.length === 0) {
        return (
            <div style={{
                textAlign: 'center',
                padding: '50px 20px',
                background: '#f9fafb',
                borderRadius: '12px',
                border: '2px dashed #e5e7eb',
                marginTop: '10px'
            }}>
                <h3 style={{ color: '#111827', margin: '0 0 8px 0', fontSize: '18px' }}>
                    No Successful Assignments Yet
                </h3>
                <p style={{ color: '#6b7280', margin: 0, fontSize: '14px', maxWidth: '500px', marginInline: 'auto' }}>
                    Once employees are successfully matched to Requests, their details will appear here.
                </p>
            </div>
        );
    }

    return (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '20px', marginTop: '10px' }}>
            {assignments.map(item => {
                const isExternal = item.contractType === 'EXTERNAL';

                return (
                    <div key={item.requestId} style={{
                        ...styles.projectRow, 
                        display: 'flex', 
                        flexDirection: 'row', 
                        alignItems: 'stretch', 
                        padding: '24px', 
                        // Indigo border for External, Green for Internal
                        borderLeft: `5px solid ${isExternal ? '#6366f1' : '#10b981'}`, 
                        backgroundColor: '#fff', 
                        borderRadius: '12px', 
                        boxShadow: '0 1px 3px rgba(0,0,0,0.1)'
                    }}>
                        <div style={{ flex: '1' }}>
                            <div style={{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '8px' }}>
                                <h3 style={{ ...styles.projectTitleText, margin: 0, fontSize: '24px' }}>{item.employeeName}</h3>
                                <span style={{ 
                                    background: isExternal ? '#e0e7ff' : '#dcfce7', 
                                    color: isExternal ? '#4338ca' : '#166534', 
                                    padding: '4px 10px', 
                                    borderRadius: '6px', 
                                    fontSize: '11px', 
                                    fontWeight: '800',
                                    display: 'flex',
                                    alignItems: 'center',
                                    gap: '4px'
                                }}>
                                    ⭐ PERFORMANCE: {item.performanceRating} {isExternal && `| ${item.contractType}`}
                                </span>
                            </div>
                            
                            <div style={{ 
                                background: isExternal ? '#f5f3ff' : '#f0fdf4', 
                                padding: '12px 16px', 
                                borderRadius: '8px', 
                                border: `1px solid ${isExternal ? '#ddd6fe' : '#bbf7d0'}`, 
                                color: isExternal ? '#5b21b6' : '#166534', 
                                fontSize: '13px', 
                                marginBottom: '16px', 
                                lineHeight: '1.4'
                            }}>
                                ✨ {item.congratsMessage}
                            </div>

                            <div style={{ display: 'flex', gap: '8px', fontSize: '15px', marginBottom: '16px', fontWeight: '500' }}>
                                <span style={{ color: '#4f46e5' }}>{item.projectName}</span>
                                <span style={{ color: '#d1d5db' }}>|</span>
                                <span style={{ color: '#6b7280' }}>{item.jobTitle}</span>
                            </div>

                            <div style={{ 
                                display: 'grid', 
                                gridTemplateColumns: '1.2fr 1.2fr 1.5fr', 
                                gap: '20px', 
                                background: '#f8fafc', 
                                padding: '16px', 
                                borderRadius: '8px', 
                                border: '1px solid #f1f5f9'
                            }}>
                                <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
                                    <div style={{ fontSize: '13px', color: '#475569' }}><strong>Employee ID:</strong> {item.employeeId}</div>
                                    <div style={{ fontSize: '13px', color: '#475569' }}><strong>Location:</strong> {item.projectLocation}</div>
                                    {/* Only show Provider for external */}
                                    {isExternal && (
                                        <div style={{ fontSize: '13px', color: '#6366f1', fontWeight: '600' }}>
                                            <strong>Provider:</strong> {item.primaryLocation}
                                        </div>
                                    )}
                                </div>
                                <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
                                    <div style={{ fontSize: '13px', color: '#475569' }}><strong>Wage:</strong> €{item.wagePerHour}/hr</div>
                                    <div style={{ fontSize: '13px', color: '#475569' }}><strong>Timeline:</strong> {item.startDate} to {item.endDate}</div>
                                    {/* Only show Manager for external */}
                                    {isExternal && (
                                        <div style={{ fontSize: '13px', color: '#475569' }}><strong>Manager:</strong> {item.managerName}</div>
                                    )}
                                </div>
                                <div style={{ display: 'flex', flexWrap: 'wrap', gap: '6px', alignContent: 'center' }}>
                                    {item.employeeSkills?.map(skill => (
                                        <span key={skill} style={{ 
                                            background: isExternal ? '#e0e7ff' : '#f1f5f9', 
                                            color: isExternal ? '#4338ca' : '#475569', 
                                            padding: '4px 10px', 
                                            borderRadius: '6px', 
                                            fontSize: '11px', 
                                            fontWeight: '700' 
                                        }}>
                                            {skill}
                                        </span>
                                    ))}
                                </div>
                            </div>
                        </div>
                    </div>
                );
            })}
        </div>
    );
};

    return (
        <div style={styles.container}>
            <div style={styles.contentWrapper}>
                <div style={styles.welcomeBox}>
                    <h1 style={{ margin: 0 }}>Hello, {firstName}! 👋</h1>
                    <p style={{ color: '#6b7280', margin: '10px 0 25px 0' }}>Manage your department staffing and track requests in real-time.</p>
                    <div style={{ display: 'flex', gap: '10px', justifyContent: 'center' }}>
                        <button onClick={() => navigate("/create-request")} style={styles.createBtn}>
                             New Staffing Request
                        </button>
                        <button onClick={() => { setProjectError(""); setShowProjectModal(true); }} style={{...styles.createBtn, background: '#10b981'}}>
                             Create New Project
                        </button>
                    </div>
                </div>

                <div style={styles.tabBar}>
                    <button style={{...styles.tabItem, ...(activeTab === 'recent' ? styles.activeTab : {})}} onClick={() => setActiveTab('recent')}>All Requests ({requests.length})</button>
                    <button style={{...styles.tabItem, ...(activeTab === 'rejected' ? styles.activeTab : {})}} onClick={() => setActiveTab('rejected')}>Rejected Requests ({rejectedRequests.length})</button>
                    <button style={{...styles.tabItem, ...(activeTab === 'employees' ? styles.activeTab : {})}} onClick={() => setActiveTab('employees')}>Employee List ({employees.length})</button>
                    <button style={{...styles.tabItem, ...(activeTab === 'projects' ? styles.activeTab : {})}} onClick={() => setActiveTab('projects')}>Project List ({projects.length})</button>
                    <button style={{...styles.tabItem, ...(activeTab === 'success' ? styles.activeTab : {})}} onClick={() => setActiveTab('success')}>Successful Assignments ({successAssignments.length})</button>
                </div>

                <div style={styles.listSection}>
                    <div style={styles.listHeader}>
                        <h2 style={{ margin: 0, fontSize: '18px' }}>
                            {activeTab === 'recent' ? 'Recent Activity' : 
                             activeTab === 'rejected' ? 'Action Required' : 
                             activeTab === 'employees' ? 'Internal Workforce' : 
                             activeTab === 'projects' ? 'Current Projects' : 'Successfully Staffed'}
                        </h2>
                        <div style={styles.syncContainer}>
                            <span style={styles.syncText}>Last synced: {lastSynced}</span>
                            <button onClick={fetchRequests} style={isRefreshing ? styles.refreshBtnSpin : styles.refreshBtn} disabled={isRefreshing}>↻</button>
                        </div>
                    </div>

                    {activeTab === 'employees' ? renderEmployeeList() : 
                     activeTab === 'projects' ? renderProjectList() : 
                     activeTab === 'success' ? renderSuccessAssignments() : (
                        <table style={styles.table}>
                            <thead>
                                <tr style={styles.tableHeader}>
                                    <th style={styles.th}>Position Title</th>
                                    <th style={styles.th}>Project</th>
                                    <th style={styles.th}>Status</th>
                                    <th style={styles.th}>Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                {((activeTab === 'recent' ? requests : rejectedRequests) || []).map((req) => (
                                    <tr key={req.requestId} style={styles.tableRow}>
                                        <td style={styles.td}>
                                            <strong>{req.title}</strong>
                                            {activeTab === 'rejected' && <div style={styles.reasonText}>Reason: {req.rejectionReason}</div>}
                                        </td>
                                        <td style={styles.td}>{req.project?.name || req.projectName || 'N/A'}</td>
                                        <td style={styles.td}><StatusBadge status={req.status} /></td>
                                        <td style={styles.td}>
                                            <div style={{display: 'flex', gap: '8px'}}>
                                                <button onClick={() => setSelectedRequest(req)} style={styles.infoBtn}>ⓘ</button>
                                                {activeTab === 'rejected' && (
                                                    <>
                                                        <button onClick={() => openResubmitModal(req)} style={styles.resubmitBtn}>Resubmit</button>
                                                        <button onClick={() => handleDecision(req.requestId, false)} style={styles.cancelBtn}>Cancel</button>
                                                    </>
                                                )}
                                            </div>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    )}
                </div>
            </div>

            {/* Create Project Modal */}
            {showProjectModal && (
                <div style={styles.modalOverlay}>
                    <div style={{...styles.modalContent, width: '500px'}}>
                        <div style={styles.modalHeader}>
                            <h3 style={{ margin: 0 }}>Create New Project</h3>
                            <button onClick={() => setShowProjectModal(false)} style={styles.closeBtn}>×</button>
                        </div>
                        {projectError && <div style={styles.errorBanner}>{projectError}</div>}
                        <div style={{...styles.modalBody, display: 'flex', flexDirection: 'column', gap: '15px'}}>
                            <div style={styles.inputGroup}>
                                <label style={styles.label}>Project Name *</label>
                                <input style={styles.input} value={newProject.name} onChange={e => setNewProject({...newProject, name: e.target.value})} />
                            </div>
                            <div style={styles.inputGroup}>
                                <label style={styles.label}>Description *</label>
                                <textarea style={{...styles.input, height: '60px'}} value={newProject.description} onChange={e => setNewProject({...newProject, description: e.target.value})} />
                            </div>
                            <div style={{display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '10px'}}>
                                <div style={styles.inputGroup}>
                                    <label style={styles.label}>Start Date *</label>
                                    <input type="date" min={new Date().toISOString().split("T")[0]} style={styles.input} value={newProject.startDate} onChange={e => setNewProject({...newProject, startDate: e.target.value})} />
                                </div>
                                <div style={styles.inputGroup}>
                                    <label style={styles.label}>End Date *</label>
                                    <input type="date" min={newProject.startDate || new Date().toISOString().split("T")[0]} style={styles.input} value={newProject.endDate} onChange={e => setNewProject({...newProject, endDate: e.target.value})} />
                                </div>
                            </div>
                            <div style={styles.inputGroup}>
                                <label style={styles.label}>Location *</label>
                                <input style={styles.input} value={newProject.location} onChange={e => setNewProject({...newProject, location: e.target.value})} />
                            </div>
                            <button onClick={handleCreateProject} style={{...styles.createBtn, marginTop: '10px', background: '#10b981'}}>Save Project</button>
                        </div>
                    </div>
                </div>
            )}

            {/* Detail Modal */}
            {selectedRequest && (
                <div style={styles.modalOverlay} onClick={() => setSelectedRequest(null)}>
                    <div style={{...styles.modalContent, width: '450px'}} onClick={e => e.stopPropagation()}>
                        <div style={styles.modalHeader}>
                            <h3 style={{ margin: 0 }}>Request Overview</h3>
                            <button onClick={() => setSelectedRequest(null)} style={styles.closeBtn}>×</button>
                        </div>
                        <div style={styles.modalBody}>
                            <DetailRow label="Request ID" value={`${selectedRequest.requestId}`} />
                            <DetailRow label="Title" value={selectedRequest.title} />
                            <DetailRow label="Project" value={selectedRequest.project?.name || selectedRequest.projectName} />
                            <DetailRow label="Status" value={selectedRequest.status} />
                            <DetailRow label="Location" value={selectedRequest.workLocation} />
                            <DetailRow label="Experience" value={`${selectedRequest.experienceYears} Years`} />
                            <DetailRow label="Hours/Week" value={selectedRequest.availabilityHoursPerWeek} />
                            <DetailRow label="Wage Budget" value={selectedRequest.wagePerHour ? `${Number(selectedRequest.wagePerHour).toFixed(2)} €/h` : 'N/A'} />
                            <DetailRow label="Required Skills:" value={selectedRequest.requiredSkills?.join(", ")} />

                            </div>
                    </div>
                </div>
            )}

            {/* Resubmit Modal */}
            {resubmitModal && (
                <div style={styles.modalOverlay}>
                    <div style={{...styles.modalContent, width: '550px'}}>
                        <div style={styles.modalHeader}>
                            <h3 style={{ margin: 0 }}>Edit & Resubmit Request</h3>
                            <button onClick={() => setResubmitModal(null)} style={styles.closeBtn}>×</button>
                        </div>
                        {error && <div style={styles.errorBanner}>{error}</div>}
                        <div style={{...styles.modalBody, display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '15px'}}>
                            <div style={{gridColumn: 'span 2', ...styles.inputGroup}}>
                                <label style={styles.label}>Position Title</label>
                                <input style={styles.input} value={editData.title} onChange={(e) => setEditData({...editData, title: e.target.value})} />
                            </div>
                            <div style={{gridColumn: 'span 2', ...styles.inputGroup}}>
                                <label style={styles.label}>Job Description</label>
                                <textarea style={{...styles.input, height: '70px'}} value={editData.description} onChange={(e) => setEditData({...editData, description: e.target.value})} />
                            </div>
                            <div style={styles.inputGroup}>
                                <label style={styles.label}>Exp. Years</label>
                                <input type="number" style={styles.input} value={editData.experienceYears} onChange={(e) => setEditData({...editData, experienceYears: e.target.value})} />
                            </div>
                            <div style={styles.inputGroup}>
                                <label style={styles.label}>Wage</label>
                                <input type="number" style={styles.input} value={editData.wagePerHour} onChange={(e) => setEditData({...editData, wagePerHour: e.target.value})} />
                            </div>
                            <div style={styles.inputGroup}>
                                <label style={styles.label}>Work Location</label>
                                <select style={styles.input} value={editData.workLocation} onChange={(e) => setEditData({...editData, workLocation: e.target.value})}>
                                    <option value="Remote">Remote</option>
                                    <option value="Onsite">Onsite</option>
                                </select>
                            </div>
                            <div style={styles.inputGroup}>
                                <label style={styles.label}>Hrs/Week</label>
                                <select style={styles.input} value={editData.availabilityHoursPerWeek} onChange={(e) => setEditData({...editData, availabilityHoursPerWeek: e.target.value})}>
                                    <option value="40">40 (Full Time)</option>
                                    <option value="20">20 (Part Time)</option>
                                </select>
                            </div>
                            <div style={{gridColumn: 'span 2', ...styles.inputGroup}}>
                                <label style={styles.label}>Skills </label>
                                <input style={styles.input} value={editData.requiredSkills} onChange={(e) => setEditData({...editData, requiredSkills: e.target.value})} />
                            </div>
                        </div>
                        <div style={{display: 'flex', gap: '10px', marginTop: '20px'}}>
                            <button onClick={() => handleDecision(resubmitModal.requestId, true, resubmitModal)} style={{...styles.createBtn, flex: 1}}>Confirm Resubmission</button>
                            <button onClick={() => setResubmitModal(null)} style={{...styles.cancelBtn, flex: 1, border: '1px solid #e5e7eb', background: '#fff', color: '#6b7280'}}>Dismiss</button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

const StatusBadge = ({ status }) => {
    // Force the status to uppercase to match the STATUS_CONFIG keys
    const lookupKey = status?.toUpperCase(); 
    
    const config = STATUS_CONFIG[lookupKey] || { 
        color: '#f3f4f6', 
        textColor: '#1f2937', 
        label: status?.replace(/_/g, ' ') 
    };

    return (
        <span style={{ 
            ...styles.badge,           
            backgroundColor: config.color, 
            color: config.textColor        
        }}>
            {config.label}
        </span>
    );
};
const DetailRow = ({ label, value }) => (
    <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '8px' }}>
        <span style={{ color: '#6b7280', fontSize: '13px' }}>{label}:</span>
        <span style={{ color: '#111827', fontSize: '13px', fontWeight: '600' }}>{value || 'N/A'}</span>
    </div>
);
const styles = {
    container: { padding: '50px 20px', backgroundColor: '#f9fafb', minHeight: '100vh', display: 'flex', justifyContent: 'center' },
    contentWrapper: { maxWidth: '1000px', width: '100%' },
    welcomeBox: { background: 'white', padding: '35px', borderRadius: '16px', boxShadow: '0 4px 6px rgba(0,0,0,0.03)', textAlign: 'center', marginBottom: '30px' },
    createBtn: { background: '#4f46e5', color: 'white', border: 'none', padding: '12px 24px', borderRadius: '8px', fontWeight: 'bold', cursor: 'pointer' },
    tabBar: { display: 'flex', gap: '20px', marginBottom: '20px', borderBottom: '1px solid #e5e7eb' },
    tabItem: { padding: '10px 20px', border: 'none', background: 'none', cursor: 'pointer', color: '#6b7280', fontWeight: '600', transition: '0.2s' },
    activeTab: { color: '#4f46e5', borderBottom: '2px solid #4f46e5' },
    listSection: { background: 'white', padding: '25px', borderRadius: '16px', boxShadow: '0 4px 6px rgba(0,0,0,0.03)' },
    listHeader: { display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' },
    syncContainer: { display: 'flex', alignItems: 'center', gap: '10px' },
    syncText: { fontSize: '12px', color: '#9ca3af' },
    refreshBtn: { background: 'none', border: 'none', color: '#4f46e5', fontSize: '20px', cursor: 'pointer' },
    refreshBtnSpin: { background: 'none', border: 'none', color: '#9ca3af', fontSize: '20px', animation: 'spin 1s linear infinite' },
    table: { width: '100%', borderCollapse: 'collapse' },
    tableHeader: { borderBottom: '1px solid #f3f4f6', textAlign: 'left' },
    th: { padding: '12px', color: '#9ca3af', fontSize: '12px', textTransform: 'uppercase', fontWeight: '600' },
    td: { padding: '16px 12px', fontSize: '14px', borderBottom: '1px solid #f9fafb' },
    badge: { padding: '4px 10px', borderRadius: '12px', fontSize: '10px', fontWeight: 'bold', textTransform: 'uppercase', whiteSpace: 'nowrap' },
    infoBtn: { color: '#9ca3af', border: '1px solid #e5e7eb', background: 'none', width: '28px', height: '28px', borderRadius: '50%', cursor: 'pointer' },
    resubmitBtn: { background: '#ecfdf5', color: '#059669', border: '1px solid #bbf7d0', padding: '4px 12px', borderRadius: '6px', fontSize: '12px', fontWeight: 'bold', cursor: 'pointer' },
    cancelBtn: { background: '#fff1f2', color: '#e11d48', border: '1px solid #fecaca', padding: '4px 12px', borderRadius: '6px', fontSize: '12px', fontWeight: 'bold', cursor: 'pointer' },
    reasonText: { fontSize: '11px', color: '#ef4444', marginTop: '4px', fontStyle: 'italic' },
    modalOverlay: { position: 'fixed', top: 0, left: 0, width: '100%', height: '100%', backgroundColor: 'rgba(0,0,0,0.4)', display: 'flex', justifyContent: 'center', alignItems: 'center', zIndex: 1000 },
    modalContent: { background: 'white', borderRadius: '16px', padding: '25px', boxShadow: '0 20px 25px rgba(0,0,0,0.1)', maxHeight: '90vh', overflowY: 'auto' },
    modalHeader: { display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px', borderBottom: '1px solid #f3f4f6', paddingBottom: '10px' },
    closeBtn: { background: 'none', border: 'none', fontSize: '20px', cursor: 'pointer', color: '#9ca3af' },
    hr: { border: '0', borderTop: '1px solid #f3f4f6', margin: '15px 0' },
    inputGroup: { display: 'flex', flexDirection: 'column', gap: '4px' },
    label: { fontSize: '12px', fontWeight: 'bold', color: '#4b5563' },
    input: { padding: '10px', borderRadius: '8px', border: '1px solid #d1d5db', fontSize: '14px', outline: 'none' },
    errorBanner: { background: '#fee2e2', color: '#b91c1c', padding: '10px', borderRadius: '8px', fontSize: '12px', marginBottom: '15px', border: '1px solid #fecaca' },
    projectListContainer: { display: 'flex', flexDirection: 'column', gap: '12px', marginTop: '10px' },
    projectRow: { display: 'flex', alignItems: 'center', background: '#fff', border: '1px solid #f3f4f6', borderRadius: '12px', padding: '24px', justifyContent: 'space-between', boxShadow: '0 2px 4px rgba(0,0,0,0.02)' },
    projectMainInfo: { flex: '1.5', display: 'flex', flexDirection: 'column', gap: '4px', paddingRight: '20px' },
    projectTitleText: { margin: 0, fontSize: '18px', color: '#111827', fontWeight: '700' },
    projectDescriptionText: { fontSize: '13px', color: '#6b7280', margin: 0, lineHeight: '1.5' },
    projectMetaInfo: { flex: '2.5', display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderLeft: '1px solid #f3f4f6', paddingLeft: '30px' },
    metaItem: { display: 'flex', flexDirection: 'column', gap: '6px', flex: '1' },
    metaLabel: { fontSize: '10px', textTransform: 'uppercase', color: '#9ca3af', fontWeight: '800', letterSpacing: '0.05em' },
    metaValue: { fontSize: '13px', color: '#374151', fontWeight: '600' },
    employeeGrid: { display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(350px, 1fr))', gap: '20px', marginTop: '10px' },
    employeeCard: { background: '#fdfdfd', border: '1px solid #f3f4f6', padding: '20px', borderRadius: '12px', boxShadow: '0 2px 4px rgba(0,0,0,0.02)', position: 'relative' },
    empRoleBadge: { fontSize: '11px', color: '#4f46e5', background: '#eef2ff', padding: '2px 8px', borderRadius: '4px', fontWeight: 'bold' },
    availabilityBadge: { fontSize: '10px', fontWeight: '800', padding: '4px 8px', borderRadius: '6px' },
    skillTagSmall: { fontSize: '11px', background: '#f3f4f6', color: '#4b5563', padding: '2px 8px', borderRadius: '4px' },
    detailBar: { display: 'flex', flexDirection: 'row', justifyContent: 'space-between', width: '100%',padding: '10px 0', borderTop: '1px solid #f3f4f6', borderBottom: '1px solid #f3f4f6', marginTop: '12px', marginBottom: '12px' },
    detailColumn: { display: 'flex', flexDirection: 'column', gap: '2px', flex: '1' },
    detailLabel: { fontSize: '9px', textTransform: 'uppercase', color: '#9ca3af', fontWeight: '800' },
    detailValue: { fontSize: '12px', color: '#374151', fontWeight: '600', whiteSpace: 'nowrap' },
    assignmentHighlight: { background: '#f0fdf4', padding: '12px', borderRadius: '8px', marginBottom: '12px', borderLeft: '3px solid #10b981', border: '1px solid #bbf7d0' }
};
export default ManagerHome;