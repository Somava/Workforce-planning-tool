import React, { useState, useEffect, useCallback } from 'react';

// --- STYLES SECTION ---
const statusStyles = {
    'APPLIED': { background: '#eff6ff', color: '#1e40af' },
    'APPROVED': { background: '#ecfdf5', color: '#065f46' },
    'REJECTED': { background: '#fee2e2', color: '#991b1b' },
    'WITHDRAWN': { background: '#f1f5f9', color: '#475569' }
};

const styles = {
    page: { background: '#f8fafc', minHeight: '100vh', width: '100%', fontFamily: '-apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif' },
    container: { maxWidth: '1100px', margin: '0 auto', padding: '40px 20px' },
    title: { fontSize: '28px', fontWeight: '800', color: '#0f172a' },
    mainTab: { padding: '12px 0', background: 'none', border: 'none', fontSize: '16px', fontWeight: '600', color: '#64748b', cursor: 'pointer', position: 'relative' },
    mainTabActive: { padding: '12px 0', background: 'none', border: 'none', fontSize: '16px', fontWeight: '700', color: '#4f46e5', cursor: 'pointer', borderBottom: '2px solid #4f46e5', position: 'relative' },
    notifDot: { position: 'absolute', top: '10px', right: '-15px', width: '10px', height: '10px', background: '#ef4444', borderRadius: '50%', border: '2px solid white' },
    
    profileHeader: { background: 'white', padding: '30px', borderRadius: '24px', border: '1px solid #e2e8f0', boxShadow: '0 4px 6px -1px rgba(0,0,0,0.05)', marginBottom: '20px' },
    profileName: { fontSize: '32px', fontWeight: '800', color: '#1e293b', margin: 0 },
    ratingBadge: { background: '#fef9c3', color: '#854d0e', padding: '4px 10px', borderRadius: '8px', fontSize: '14px', fontWeight: '700' },
    profileSub: { color: '#6366f1', fontWeight: '600', marginTop: '4px' },
    wageHighlight: { fontSize: '28px', fontWeight: '800', color: '#10b981' },
    availabilityStatus: { display: 'block', color: '#059669', fontSize: '12px', fontWeight: '700', textTransform: 'uppercase', marginTop: '4px' },
    profileStatsGrid: { display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '15px', marginTop: '25px', padding: '20px', background: '#f8fafc', borderRadius: '16px' },
    statItem: { fontSize: '13px', color: '#64748b' },
    skillBadge: { background: '#e0e7ff', color: '#4338ca', padding: '6px 14px', borderRadius: '8px', fontSize: '12px', fontWeight: '600', marginRight: '8px', marginBottom: '8px', display: 'inline-block' },
    langBadge: { background: '#f1f5f9', color: '#475569', padding: '6px 14px', borderRadius: '8px', fontSize: '12px', fontWeight: '600', marginRight: '8px', marginBottom: '8px', display: 'inline-block' },
    orgInfoBox: { background: '#fcfdff', padding: '15px', borderRadius: '12px', border: '1px solid #eff2ff' },
    orgDetail: { fontSize: '13px', color: '#475569', marginBottom: '4px' },

    congratsBox: {
        background: 'linear-gradient(135deg, #6366f1 0%, #4f46e5 100%)',
        padding: '30px',
        borderRadius: '20px',
        marginBottom: '30px',
        color: 'white',
        boxShadow: '0 10px 15px -3px rgba(79, 70, 229, 0.2)'
    },
    congratsTitle: { margin: '0 0 10px 0', fontSize: '24px', fontWeight: '800' },
    congratsText: { margin: 0, fontSize: '16px', opacity: 0.9, lineHeight: '1.5' },
    statusLabelBadge: { marginTop: '8px', display: 'inline-block', padding: '6px 12px', background: '#fef3c7', color: '#92400e', borderRadius: '8px', fontSize: '13px', fontWeight: '700', border: '1px solid #fde68a' },
    wageBox: { textAlign: 'right', background: '#f0fdf4', padding: '10px 15px', borderRadius: '12px', border: '1px solid #dcfce7' },
    wageText: { fontSize: '22px', fontWeight: '800', color: '#166534', display: 'block' },
    wageUnit: { fontSize: '14px', color: '#166534', fontWeight: '600' },
    header: { display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' },
    tabBar: { display: 'flex', background: '#e2e8f0', padding: '4px', borderRadius: '12px' },
    tab: { padding: '10px 20px', border: 'none', background: 'none', cursor: 'pointer', fontWeight: '600', color: '#64748b' },
    activeTab: { padding: '10px 20px', border: 'none', background: 'white', borderRadius: '8px', color: '#4f46e5', fontWeight: '700', boxShadow: '0 2px 4px rgba(0,0,0,0.05)' },
    alert: { padding: '15px', borderRadius: '12px', marginBottom: '20px', border: '1px solid #e2e8f0', textAlign: 'center', fontWeight: '600' },
    grid: { display: 'flex', flexDirection: 'column', gap: '20px' },
    card: { background: 'white', padding: '30px', borderRadius: '20px', border: '1px solid #e2e8f0', boxShadow: '0 4px 6px -1px rgba(0,0,0,0.05)' },
    cardHeader: { display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '20px' },
    deptBadge: { fontSize: '11px', textTransform: 'uppercase', letterSpacing: '0.05em', color: '#6366f1', fontWeight: '700' },
    jobTitle: { fontSize: '22px', margin: '4px 0 0 0', color: '#1e293b', fontWeight: '700' },
    priceTag: { fontSize: '24px', fontWeight: '800', color: '#10b981' },
    descriptionSection: { marginBottom: '20px' },
    requestDesc: { fontSize: '15px', color: '#475569', lineHeight: '1.6', margin: '5px 0 0 0' },
    metaGrid: { display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '10px', marginBottom: '20px' },
    metaItem: { fontSize: '14px', color: '#64748b' },
    desc: { fontSize: '15px', color: '#475569', lineHeight: '1.6', background: '#f8fafc', padding: '15px', borderRadius: '12px' },
    footer: { display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: '20px' },
    skillList: { display: 'flex', gap: '8px', flexWrap: 'wrap' },
    skill: { background: '#eef2ff', color: '#4338ca', padding: '5px 12px', borderRadius: '20px', fontSize: '12px', fontWeight: '600' },
    applyBtn: { padding: '12px 24px', background: '#4f46e5', color: 'white', border: 'none', borderRadius: '10px', fontWeight: '700', cursor: 'pointer' },
    tableWrap: { background: 'white', borderRadius: '20px', border: '1px solid #e2e8f0', overflow: 'hidden' },
    table: { width: '100%', borderCollapse: 'collapse' },
    thead: { background: '#f8fafc', textAlign: 'left' },
    th: { padding: '18px', color: '#64748b', fontSize: '13px' },
    td: { padding: '18px', borderBottom: '1px solid #f1f5f9', fontSize: '14px' },
    status: { padding: '4px 10px', borderRadius: '20px', fontSize: '12px', fontWeight: '700' },
    withdrawBtn: { color: '#ef4444', background: 'none', border: 'none', cursor: 'pointer', fontWeight: '600' },
    disabledBtn: { color: '#cbd5e1', background: 'none', border: 'none', cursor: 'not-allowed' },
    infoBoxBlue: { background: '#f0f9ff', padding: '15px', borderRadius: '12px', border: '1px solid #e0f2fe', marginBottom: '10px' },
    infoBoxGray: { background: '#f8fafc', padding: '15px', borderRadius: '12px', border: '1px solid #f1f5f9', marginBottom: '20px' },
    sectionLabel: { fontSize: '11px', textTransform: 'uppercase', color: '#94a3b8', margin: '0 0 8px 0', letterSpacing: '0.05em' },
    stakeholderTable: { width: '100%', borderCollapse: 'collapse', marginTop: '10px' },
    sTh: { textAlign: 'left', fontSize: '12px', color: '#94a3b8', paddingBottom: '8px' },
    sTd: { padding: '8px 0', fontSize: '14px', color: '#1e293b', borderBottom: '1px solid #f1f5f9' },
    emptyState: { textAlign: 'center', padding: '60px', background: 'white', borderRadius: '20px', border: '1px solid #e2e8f0' },
    
    // Success View Additions
    successContainer: { background: 'white', borderRadius: '24px', padding: '40px', border: '1px solid #e2e8f0', boxShadow: '0 20px 25px -5px rgba(0, 0, 0, 0.1)', textAlign: 'center' },
    congratsBanner: { color: 'white', padding: '30px', borderRadius: '20px', marginBottom: '30px' },
    successDetailsGrid: { display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '20px', marginBottom: '30px', textAlign: 'left' },
    successCard: { background: '#f8fafc', padding: '20px', borderRadius: '16px', border: '1px solid #f1f5f9' },
    backBtn: { padding: '12px 30px', background: '#f1f5f9', color: '#475569', border: 'none', borderRadius: '10px', fontWeight: '700', cursor: 'pointer' },
    reasonInputBox: { marginTop: '15px', padding: '15px', background: '#fff1f2', borderRadius: '8px', border: '1px solid #fecdd3' },
    textArea: { width: '100%', padding: '10px', borderRadius: '6px', border: '1px solid #fda4af', fontSize: '14px', outline: 'none', fontFamily: 'inherit' }
};

// --- COMPONENT SECTION ---
const EmployeeDashboard = () => {
    // State Management
    const [mainTab, setMainTab] = useState('career');
    const [activeTab, setActiveTab] = useState('browse');
   
    const [userProfile, setUserProfile] = useState(null);
    const [openPositions, setOpenPositions] = useState([]);
    const [myApplications, setMyApplications] = useState([]);
    const [assignedRequests, setAssignedRequests] = useState([]);
   
    const [message, setMessage] = useState({ text: '', type: '', visible: false });
    const [pendingAction, setPendingAction] = useState(null);
    const [decisionMessage, setDecisionMessage] = useState(null);

    // Track per requestId for Decline Reason
    const [showReasonInput, setShowReasonInput] = useState({}); 
    const [declineReason, setDeclineReason] = useState({}); 

    const userEmail = localStorage.getItem("email") || "hoffmann.rd@frauas.de";

    // Logic Helpers
    const getDeptName = (id) => {
        if (id <= 4) return "Information Technology";
        if (id <= 8) return "Research & Development";
        if (id <= 12) return "Human Resource";
        return "General Operations";
    };

    const getDurationInfo = (start, end) => {
        if (!start || !end) return "Flexible Duration";
        const d1 = new Date(start);
        const d2 = new Date(end);
        const months = (d2.getFullYear() - d1.getFullYear()) * 12 + (d2.getMonth() - d1.getMonth());
        const startStr = d1.toLocaleDateString('en-US', { month: 'short', year: 'numeric' });
        const endStr = d2.toLocaleDateString('en-US', { month: 'short', year: 'numeric' });
        return `${months} Months (${startStr} — ${endStr})`;
    };

    // Global Styles & Animations
    useEffect(() => {
        const style = document.createElement('style');
        style.innerHTML = `
            html, body { margin: 0 !important; padding: 0 !important; background-color: #f8fafc; }
            @keyframes fadeIn { from { opacity: 0; transform: translateY(-10px); } to { opacity: 1; transform: translateY(0); } }
            @keyframes fadeOut { from { opacity: 1; transform: translateY(0); } to { opacity: 0; transform: translateY(-10px); } }
            .alert-fade-in { animation: fadeIn 0.4s ease forwards; }
            .alert-fade-out { animation: fadeOut 0.4s ease forwards; }
        `;
        document.head.appendChild(style);
        return () => document.head.removeChild(style);
    }, []);

    const showNotification = (text, type) => {
        setMessage({ text, type, visible: true });
        setTimeout(() => setMessage(prev => ({ ...prev, visible: false })), 4600);
    };

    // Data Fetching
    const fetchData = useCallback(async () => {
        const emailParam = encodeURIComponent(userEmail);
        try {
            const [posRes, appRes, empAssignRes, profileRes] = await Promise.all([
                fetch(`http://localhost:8080/api/employee-portal/open-positions?email=${emailParam}`),
                fetch(`http://localhost:8080/api/employee-portal/my-applications?email=${emailParam}`),
                fetch(`http://localhost:8080/api/tasks/employee/assigned-requests?email=${emailParam}`),
                fetch(`http://localhost:8080/api/employee-portal/my-profile?email=${emailParam}`)
            ]);

            if (posRes.ok) {
                const text = await posRes.text();
                if (text) setOpenPositions(JSON.parse(text));
            }
            if (appRes.ok) {
                const text = await appRes.text();
                if (text) setMyApplications(JSON.parse(text));
            }
            if (empAssignRes.ok) {
                const data = await empAssignRes.json();
                setAssignedRequests(Array.isArray(data) ? data : []);
            }
            if (profileRes.ok) {
                const profileData = await profileRes.json();
                setUserProfile(profileData);
            }
        } catch (err) {
            console.error("Sync Error:", err);
        }
    }, [userEmail]);

    useEffect(() => { fetchData(); }, [fetchData]);

    const handleApply = async (requestId) => {
        setPendingAction(requestId);
        try {
            const res = await fetch(`http://localhost:8080/api/employee-portal/apply?requestId=${requestId}&email=${encodeURIComponent(userEmail)}`, { method: 'POST' });
            const text = await res.text();
            showNotification(text, res.ok ? 'success' : 'error');
            await fetchData();
            if (res.ok) setTimeout(() => setActiveTab('applied'), 1000);
        } catch (err) { showNotification("Connection failed", "error"); }
        finally { setPendingAction(null); }
    };

    const handleWithdraw = async (applicationId) => {
        setPendingAction(applicationId);
        try {
            const res = await fetch(`http://localhost:8080/api/employee-portal/withdraw?applicationId=${applicationId}&email=${encodeURIComponent(userEmail)}`, { method: 'POST' });
            showNotification("Application Withdrawn", 'success');
            await fetchData();
        } catch (err) { showNotification("Withdraw failed", "error"); }
        finally { setPendingAction(null); }
    };

    const handleEmployeeDecision = async (requestId, isApproved) => {
        const reason = declineReason[requestId] || "";
        
        if (!isApproved && !reason.trim()) {
            showNotification("Please provide a reason before declining.", "error");
            return;
        }

        setPendingAction(requestId);
        try {
            const url = `http://localhost:8080/api/employee-portal/employee/assignment-decision?requestId=${requestId}&email=${encodeURIComponent(userEmail)}&approved=${isApproved}&reason=${encodeURIComponent(reason)}`;
            
            const res = await fetch(url, { method: 'POST' });
            
            if (res.ok) {
                setDecisionMessage(isApproved ?
                    { title: "Assignment Accepted! 🎉", text: "You have been successfully added to the project team.", type: 'success' } :
                    { title: "Assignment Declined", text: "The request has been removed. You can continue browsing the career portal.", type: 'info' }
                );
                await fetchData();
            } else {
                const text = await res.text();
                showNotification(text || "Action failed", "error");
            }
        } catch (err) {
            showNotification("Connection error", "error");
        } finally {
            setPendingAction(null);
        }
    };

    const availableJobs = openPositions.filter(pos =>
        !myApplications.some(app => String(app.requestId) === String(pos.requestId) && app.status !== 'WITHDRAWN')
    );

    return (
        <div style={styles.page}>
            <div style={styles.container}>
                
                {/* 1. Integrated Profile Section */}
                {userProfile && (
                    <div style={styles.profileHeader} className="alert-fade-in">
                        <div style={{display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start'}}>
                            <div>
                                <div style={{display: 'flex', alignItems: 'center', gap: '12px'}}>
                                    <h1 style={styles.profileName}>{userProfile.firstName} {userProfile.lastName}</h1>
                                    <span style={styles.ratingBadge}>★ {userProfile.performanceRating}</span>
                                </div>
                                <p style={styles.profileSub}>{userProfile.jobRoleName} | {userProfile.departmentName}</p>
                                <p style={{fontSize: '14px', color: '#64748b', margin: '4px 0'}}>
                                    📧 {userProfile.email} | 📞 {userProfile.emergencyContact}
                                </p>
                            </div>
                            <div style={{textAlign: 'right'}}>
                                <div style={styles.wageHighlight}>€{userProfile.wagePerHour}<span style={{fontSize: '16px'}}> /hr</span></div>
                                <span style={styles.availabilityStatus}>{userProfile.matchingAvailability}</span>
                            </div>
                        </div>
                        
                        <div style={styles.profileStatsGrid}>
                            <div style={styles.statItem}><strong>Employee ID:</strong> {userProfile.employeeId}</div>
                            <div style={styles.statItem}><strong>Experience:</strong> {userProfile.experienceYears} Years</div>
                            <div style={styles.statItem}><strong>Working Hours:</strong> {userProfile.totalHoursPerWeek}h/week</div>
                            <div style={styles.statItem}><strong>Location:</strong> {userProfile.primaryLocation}</div>
                            <div style={styles.statItem}><strong>Contract:</strong> {userProfile.contractType?.replace('_', ' ')}</div>
                            <div style={styles.statItem}><strong>Window:</strong> {userProfile.availabilityStart} to {userProfile.availabilityEnd}</div>
                        </div>

                        <div style={{display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '30px', marginTop: '20px'}}>
                            <div>
                                <p style={styles.sectionLabel}>Technical Skills</p>
                                <div style={styles.skillList}>
                                    {userProfile.skills?.map(skill => <span key={skill} style={styles.skillBadge}>{skill}</span>)}
                                </div>
                                <p style={{...styles.sectionLabel, marginTop: '15px'}}>Languages</p>
                                <div style={styles.skillList}>
                                    {userProfile.languages?.map(lang => <span key={lang} style={styles.langBadge}>{lang}</span>)}
                                </div>
                            </div>
                            <div style={styles.orgInfoBox}>
                                <p style={styles.sectionLabel}>Organization & Preferences</p>
                                <div style={styles.orgDetail}><strong>Supervisor:</strong> {userProfile.supervisorName} ({userProfile.supervisorEmail})</div>
                                <div style={styles.orgDetail}><strong>Dept Head:</strong> {userProfile.departmentHeadName}</div>
                                <div style={{...styles.orgDetail, color: '#4f46e5', marginTop: '8px'}}>
                                    <strong>Preferences:</strong> {userProfile.projectPreferences} | <strong>Interests:</strong> {userProfile.interests}
                                </div>
                            </div>
                        </div>
                    </div>
                )}

                <h1 style={{...styles.title, marginBottom: '30px', marginTop: '40px'}}>My Employee Portal</h1>

                {/* Main Tabs */}
                <div style={{display: 'flex', gap: '40px', borderBottom: '1px solid #e2e8f0', marginBottom: '25px'}}>
                    <button onClick={() => setMainTab('career')} style={mainTab === 'career' ? styles.mainTabActive : styles.mainTab}>Career Portal</button>
                    <button onClick={() => setMainTab('assignments')} style={mainTab === 'assignments' ? styles.mainTabActive : styles.mainTab}>
                        Project Assignment Confirmation {assignedRequests.length > 0 && <span style={styles.notifDot}></span>}
                    </button>
                </div>

                {/* Notifications */}
                {message.text && (
                    <div className={message.visible ? 'alert-fade-in' : 'alert-fade-out'}
                         style={{...styles.alert, backgroundColor: message.type === 'success' ? '#ecfdf5' : '#fef2f2', color: message.type === 'success' ? '#065f46' : '#991b1b'}}>
                        {message.text}
                    </div>
                )}

                {/* Assignment Confirmation Section */}
                {mainTab === 'assignments' && (
                    <div className="alert-fade-in">
                        {/* CASE 1: Action just completed (Success View) */}
                        {!assignedRequests.length && decisionMessage ? (
                            <div style={styles.successContainer} className="alert-fade-in">
                                <div style={{...styles.congratsBanner, background: decisionMessage.type === 'success' ? '#10b981' : '#64748b'}}>
                                    <h2 style={{ margin: 0, fontSize: '24px' }}>{decisionMessage.title}</h2>
                                    <p style={{ margin: '8px 0 0 0', opacity: 0.9 }}>{decisionMessage.text}</p>
                                </div>

                                {decisionMessage.type === 'success' && (
                                    <div style={styles.successDetailsGrid}>
                                        <div style={styles.successCard}>
                                            <p style={styles.sectionLabel}>Next Steps</p>
                                            <p style={{ fontSize: '14px', color: '#475569', lineHeight: '1.5' }}>
                                                Your profile has been officially linked to the project. You will receive an onboarding email from your Project Manager shortly regarding the kickoff meeting and resource access.
                                            </p>
                                        </div>
                                        <div style={styles.successCard}>
                                            <p style={styles.sectionLabel}>Quick Actions</p>
                                            <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
                                                <div style={{ fontSize: '13px', color: '#4f46e5', fontWeight: '600', cursor: 'pointer' }}>📅 Add to Calendar</div>
                                                <div style={{ fontSize: '13px', color: '#4f46e5', fontWeight: '600', cursor: 'pointer' }}>📄 Download Assignment PDF</div>
                                            </div>
                                        </div>
                                    </div>
                                )}

                                <div style={{ textAlign: 'center', marginTop: '20px' }}>
                                    <button onClick={() => setDecisionMessage(null)} style={styles.backBtn}>
                                        Return to Portal
                                    </button>
                                </div>
                            </div>
                        ) : assignedRequests.length > 0 ? (
                            /* CASE 2: Pending Assignments List */
                            <>
                                <div style={styles.congratsBox}>
                                    <h2 style={styles.congratsTitle}>Congratulations! 🎉</h2>
                                    <p style={styles.congratsText}>Here are a few opportunities awaiting for you! Please review the details below and confirm your participation.</p>
                                </div>
                                <div style={styles.grid}>
                                    {assignedRequests.map(item => (
                                        <div key={item.requestId} style={styles.card}>
                                            <div style={styles.cardHeader}>
                                                <div>
                                                    <span style={styles.deptBadge}>{item.project?.name || 'Assigned Project'}</span>
                                                    <h2 style={styles.jobTitle}>{item.title}</h2>
                                                    <div style={styles.statusLabelBadge}>Approved by dept head, awaiting employee confirmation</div>
                                                </div>
                                                <div style={styles.wageBox}>
                                                    <span style={styles.wageText}>€{item.wagePerHour}</span>
                                                    <span style={styles.wageUnit}>/hr</span>
                                                </div>
                                            </div>

                                            <div style={styles.descriptionSection}>
                                                <h4 style={styles.sectionLabel}>Description of the Staffing Request</h4>
                                                <p style={styles.requestDesc}>{item.description || "No description provided."}</p>
                                            </div>

                                            <div style={styles.infoBoxBlue}>
                                                <h4 style={styles.sectionLabel}>Project Context</h4>
                                                <p style={{fontSize: '17px', fontWeight: '700', color: '#1e293b', margin: '0 0 8px 0'}}>{item.project?.name}</p>
                                                <p style={{fontSize: '14px', color: '#475569', lineHeight: '1.6', marginBottom: '15px'}}>{item.project?.description}</p>
                                                <div style={styles.metaGrid}>
                                                    <div style={styles.metaItem}><strong>📍 Work Location:</strong> {item.workLocation || item.project?.location }</div>
                                                    <div style={styles.metaItem}><strong>🕒 Engagement:</strong> {item.availabilityHoursPerWeek} hrs/week</div>
                                                    <div style={styles.metaItem}><strong>📅 Start:</strong> {item.project?.startDate}</div>
                                                    <div style={styles.metaItem}><strong>📅 End:</strong> {item.project?.endDate}</div>
                                                </div>
                                            </div>

                                            <div style={styles.infoBoxGray}>
                                                <h4 style={styles.sectionLabel}>Stakeholders</h4>
                                                <table style={styles.stakeholderTable}>
                                                    <thead>
                                                        <tr>
                                                            <th style={styles.sTh}>Role</th>
                                                            <th style={styles.sTh}>Name</th>
                                                        </tr>
                                                    </thead>
                                                    <tbody>
                                                        <tr>
                                                            <td style={styles.sTd}>Project Manager</td>
                                                            <td style={styles.sTd}>{item.project?.managerUser?.email}</td>
                                                        </tr>
                                                        <tr>
                                                            <td style={styles.sTd}>Dept. Head</td>
                                                            <td style={styles.sTd}>{item.department?.departmentHead?.email || "Internal Approved"}</td>
                                                        </tr>
                                                        <tr>
                                                            <td style={styles.sTd}>Resource Planner</td>
                                                            <td style={styles.sTd}>{item.department?.resourcePlanner?.email || "Eve (HR)"}</td>
                                                        </tr>
                                                    </tbody>
                                                </table>
                                            </div>
                                            
                                            {showReasonInput[item.requestId] && (
                                                <div style={styles.reasonInputBox}>
                                                    <label style={{fontSize: '13px', fontWeight: '600', color: '#991b1b', display: 'block', marginBottom: '8px'}}>Reason for Declining:</label>
                                                    <textarea
                                                        style={styles.textArea}
                                                        placeholder="Please explain why you cannot join this project..."
                                                        value={declineReason[item.requestId] || ""}
                                                        onChange={(e) => setDeclineReason({...declineReason, [item.requestId]: e.target.value})}
                                                    />
                                                </div>
                                            )}

                                            <div style={styles.footer}>
                                                <div style={styles.skillList}>
                                                    {item.requiredSkills?.map(s => <span key={s} style={styles.skill}>{s}</span>)}
                                                </div>
                                                <div style={{display: 'flex', gap: '12px'}}>
                                                    <button onClick={() => handleEmployeeDecision(item.requestId, true)} style={{...styles.applyBtn, background: '#10b981'}} disabled={pendingAction !== null}>
                                                        {pendingAction === item.requestId ? '...' : 'Accept Assignment'}
                                                    </button>
                                                    <button
                                                        onClick={() => {
                                                            if (!showReasonInput[item.requestId]) {
                                                                setShowReasonInput({...showReasonInput, [item.requestId]: true});
                                                            } else {
                                                                handleEmployeeDecision(item.requestId, false);
                                                            }
                                                        }}
                                                        style={{...styles.applyBtn, background: showReasonInput[item.requestId] ? '#991b1b' : '#ef4444'}}
                                                        disabled={pendingAction !== null}
                                                    >
                                                        {showReasonInput[item.requestId] ? 'Confirm Decline' : 'Decline'}
                                                    </button>
                                                </div>
                                            </div>
                                        </div>
                                    ))}
                                </div>
                            </>
                        ) : (
                            /* CASE 3: Empty State */
                            <div style={styles.emptyState}>
                                <p style={{color: '#64748b', fontSize: '16px'}}>No pending project assignments found.</p>
                            </div>
                        )}
                    </div>
                )}

                {/* Career Portal Section */}
                {mainTab === 'career' && (
                    <div className="alert-fade-in">
                        <header style={styles.header}>
                            <div style={styles.tabBar}>
                                <button onClick={() => setActiveTab('browse')} style={activeTab === 'browse' ? styles.activeTab : styles.tab}>Browse Jobs ({availableJobs.length})</button>
                                <button onClick={() => setActiveTab('applied')} style={activeTab === 'applied' ? styles.activeTab : styles.tab}>My Applications ({myApplications.length})</button>
                            </div>
                        </header>
                        
                        {activeTab === 'browse' && (
                            <div style={styles.grid}>
                                {availableJobs.map(job => (
                                    <div key={job.requestId} style={styles.card}>
                                        <div style={styles.cardHeader}>
                                            <div>
                                                <span style={styles.deptBadge}>{getDeptName(job.departmentId)}</span>
                                                <h2 style={styles.jobTitle}>{job.title}</h2>
                                            </div>
                                            <div style={styles.priceTag}>€{job.wagePerHour}<small>/hr</small></div>
                                        </div>
                                        <div style={styles.metaGrid}>
                                            <div style={styles.metaItem}><strong>📍 Location:</strong> {job.workLocation}</div>
                                            <div style={styles.metaItem}><strong>⏳ Engagement:</strong> {getDurationInfo(job.projectStartDate, job.projectEndDate)}</div>
                                            <div style={styles.metaItem}><strong>💪 Experience:</strong> {job.experienceYears} year(s)</div>
                                            <div style={styles.metaItem}><strong>🕒 Weekly:</strong> {job.availabilityHoursPerWeek} hrs/week</div>
                                        </div>
                                        <p style={styles.desc}>{job.description}</p>
                                        <div style={styles.footer}>
                                            <div style={styles.skillList}>
                                                {job.requiredSkills?.map(s => <span key={s} style={styles.skill}>{s}</span>)}
                                            </div>
                                            <button onClick={() => handleApply(job.requestId)} style={styles.applyBtn} disabled={pendingAction !== null}>
                                                {pendingAction === job.requestId ? '...' : 'Apply Now'}
                                            </button>
                                        </div>
                                    </div>
                                ))}
                                {availableJobs.length === 0 && <div style={styles.emptyState}>No open positions available.</div>}
                            </div>
                        )}

                        {activeTab === 'applied' && (
                            <div style={styles.tableWrap}>
                                <table style={styles.table}>
                                    <thead style={styles.thead}>
                                        <tr>
                                            <th style={styles.th}>Project Title</th>
                                            <th style={styles.th}>Status</th>
                                            <th style={styles.th}>Applied On</th>
                                            <th style={styles.th}>Action</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {myApplications.map(app => (
                                            <tr key={app.applicationId}>
                                                <td style={styles.td}><strong>{app.projectTitle}</strong></td>
                                                <td style={styles.td}><span style={{...styles.status, ...statusStyles[app.status]}}>{app.status}</span></td>
                                                <td style={styles.td}>{new Date(app.appliedAt).toLocaleDateString()}</td>
                                                <td style={styles.td}>
                                                    <button
                                                        disabled={app.status !== 'APPLIED' || pendingAction !== null}
                                                        onClick={() => handleWithdraw(app.applicationId)}
                                                        style={app.status === 'APPLIED' ? styles.withdrawBtn : styles.disabledBtn}
                                                    >Withdraw</button>
                                                </td>
                                            </tr>
                                        ))}
                                    </tbody>
                                </table>
                            </div>
                        )}
                    </div>
                )}
            </div>
        </div>
    );
};

export default EmployeeDashboard;