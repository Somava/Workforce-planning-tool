import React, { useState, useEffect, useCallback } from 'react';
import axios from 'axios';

const API_BASE = process.env.REACT_APP_API_BASE_URL || "http://localhost:8080";
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
    wageBox: { textAlign: 'right', background: '#f0fdf4', padding: '10px 15px', borderRadius: '12px', border: '1px solid #dcfce7' },
    wageText: { fontSize: '22px', fontWeight: '800', color: '#166534', display: 'block' },
    wageUnit: { fontSize: '14px', color: '#166534', fontWeight: '600' },
    availabilityStatus: { display: 'block', color: '#059669', fontSize: '12px', fontWeight: '700', textTransform: 'uppercase', marginTop: '4px' },
    profileStatsGrid: { display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '15px', marginTop: '25px', padding: '20px', background: '#f8fafc', borderRadius: '16px' },
    statItem: { fontSize: '13px', color: '#64748b' },
    skillBadge: { background: '#e0e7ff', color: '#4338ca', padding: '6px 14px', borderRadius: '8px', fontSize: '12px', fontWeight: '600', marginRight: '8px', marginBottom: '8px', display: 'inline-block' },
    skillList: { display: 'flex', gap: '8px', flexWrap: 'wrap' },
    skill: { background: '#eef2ff', color: '#4338ca', padding: '5px 12px', borderRadius: '20px', fontSize: '12px', fontWeight: '600' },
    congratsBox: { background: 'linear-gradient(135deg, #6366f1 0%, #4f46e5 100%)', padding: '30px', borderRadius: '20px', marginBottom: '30px', color: 'white' },
    congratsTitle: { margin: '0 0 10px 0', fontSize: '24px', fontWeight: '800' },
    congratsText: { margin: 0, fontSize: '16px', opacity: 0.9 },
    header: { display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' },
    tabBar: { display: 'flex', background: '#e2e8f0', padding: '4px', borderRadius: '12px' },
    tab: { padding: '10px 20px', border: 'none', background: 'none', cursor: 'pointer', fontWeight: '600', color: '#64748b' },
    activeTab: { padding: '10px 20px', border: 'none', background: 'white', borderRadius: '8px', color: '#4f46e5', fontWeight: '700' },
    grid: { display: 'flex', flexDirection: 'column', gap: '20px' },
    card: { background: 'white', padding: '30px', borderRadius: '20px', border: '1px solid #e2e8f0' },
    cardHeader: { display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '20px' },
    deptBadge: { fontSize: '11px', textTransform: 'uppercase', color: '#6366f1', fontWeight: '700' },
    jobTitle: { fontSize: '22px', margin: '4px 0 0 0', color: '#1e293b', fontWeight: '700' },
    priceTag: { fontSize: '24px', fontWeight: '800', color: '#10b981' },
    desc: { fontSize: '15px', color: '#475569', lineHeight: '1.6', background: '#f8fafc', padding: '15px', borderRadius: '12px' },
    footer: { display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: '20px' },
    applyBtn: { padding: '12px 24px', background: '#4f46e5', color: 'white', border: 'none', borderRadius: '10px', fontWeight: '700', cursor: 'pointer' },
    tableWrap: { background: 'white', borderRadius: '20px', border: '1px solid #e2e8f0', overflow: 'hidden' },
    table: { width: '100%', borderCollapse: 'collapse' },
    thead: { background: '#f8fafc', textAlign: 'left' },
    th: { padding: '18px', color: '#64748b', fontSize: '13px' },
    td: { padding: '18px', borderBottom: '1px solid #f1f5f9' },
    status: { padding: '4px 10px', borderRadius: '20px', fontSize: '12px', fontWeight: '700' },
    withdrawBtn: { color: '#ef4444', background: 'none', border: 'none', cursor: 'pointer', fontWeight: '600' },
    disabledBtn: { color: '#cbd5e1', background: 'none', border: 'none', cursor: 'not-allowed' },
    infoBoxBlue: { background: '#f0f9ff', padding: '15px', borderRadius: '12px', border: '1px solid #e0f2fe' },
    metaGrid: { display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '10px', marginTop: '10px' },
    metaItem: { fontSize: '14px', color: '#64748b' },
    emptyState: { textAlign: 'center', padding: '60px', background: 'white', borderRadius: '20px', border: '1px solid #e2e8f0' },
    successContainer: { background: 'white', borderRadius: '24px', padding: '40px', textAlign: 'center' },
    congratsBanner: { color: 'white', padding: '30px', borderRadius: '20px', marginBottom: '30px' },
    backBtn: { padding: '12px 30px', background: '#f1f5f9', color: '#475569', border: 'none', borderRadius: '10px', fontWeight: '700' },
    stakeholderTable: {
        width: '100%',
        borderCollapse: 'collapse', // Important: removes gaps between cells
        marginTop: '10px',
    },
    sTh: {
        textAlign: 'left',
        fontSize: '14px',
        color: '#64748b',
        fontWeight: '700',
        paddingBottom: '10px',
        borderBottom: '1px solid #e2e8f0', // Adds a nice underline to the header
    },
    sTd: {
        textAlign: 'left',
        padding: '12px 0', // Vertical spacing for readability
        fontSize: '15px',
        color: '#1e293b',
        borderBottom: '1px solid #f1f5f9', // Optional: very light line between rows
    },
    sectionLabel: {
        fontSize: '12px',
        textTransform: 'uppercase',
        letterSpacing: '0.05em',
        color: '#94a3b8',
        marginBottom: '10px',
        fontWeight: '700',
    },
    infoBoxGray: {
        backgroundColor: '#f8fafc', 
        borderRadius: '12px',      
        padding: '20px',           
        marginTop: '20px',         
        border: '1px solid #e2e8f0'
    },
    // NEW ACTIVE PROJECT STYLES
    activeProjectCard: { background: 'white', borderRadius: '24px', border: '1px solid #e2e8f0', overflow: 'hidden', display: 'flex', boxShadow: '0 4px 6px -1px rgba(0,0,0,0.05)' },
    activeCardSidebar: { width: '8px', background: '#10b981' },
    activeSuccessBanner: { background: '#f0fdfa', border: '1px solid #ccfbf1', color: '#134e4a', padding: '15px', borderRadius: '12px', marginTop: '15px', fontSize: '14px' },
    activeStatusBox: { border: '1px solid #e2e8f0', padding: '20px', borderRadius: '16px', textAlign: 'center', minWidth: '140px', background: '#fcfdff' },
    sectionLabel: { fontSize: '11px', textTransform: 'uppercase', color: '#94a3b8', margin: '0 0 8px 0', letterSpacing: '0.05em' },
    statusLabelBadge: { padding: '4px 10px', background: '#fef3c7', color: '#92400e', borderRadius: '8px', fontSize: '13px', fontWeight: '700' }
};

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
    const [successAssignments, setSuccessAssignments] = useState([]);

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

    // Status Styles for Table
    const statusStyles = {
        'APPLIED': { backgroundColor: '#e0f2fe', color: '#0369a1' },
        'ASSIGNED': { backgroundColor: '#dcfce7', color: '#166534' },
        'WITHDRAWN': { backgroundColor: '#f1f5f9', color: '#475569' },
        'REJECTED': { backgroundColor: '#fee2e2', color: '#991b1b' }
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
        setTimeout(() => setMessage(prev => ({ ...prev, visible: false })), 8000);
    };

    // Data Fetching
    const fetchData = useCallback(async () => {
    try {
        const [posRes, appRes, empAssignRes, profileRes, successRes] = await Promise.all([
            axios.get(API_BASE + '/api/employee/open-positions'),
            axios.get(API_BASE + '/api/employee/my-applications'), 
            axios.get(API_BASE + '/api/employee/assigned-requests'),
            axios.get(API_BASE + '/api/employee/my-profile'),
            axios.get(API_BASE + '/api/workforce-overview/success-notifications')
        ]);

        setOpenPositions(Array.isArray(posRes.data) ? posRes.data : []);
        setMyApplications(Array.isArray(appRes.data) ? appRes.data : []);
        setAssignedRequests(Array.isArray(empAssignRes.data) ? empAssignRes.data : []);
        setUserProfile(profileRes.data || null);

        // UPDATE THIS: Use the data directly from the new success-notifications API
        // This matches the exact structure shown in your Swagger screenshot
        setSuccessAssignments(Array.isArray(successRes.data) ? successRes.data : []);

    } catch (err) {
        console.error("Sync Error:", err);
    }
}, [API_BASE]);
    useEffect(() => { 
        fetchData(); 
    }, [fetchData]);

    // 2. Handle Application
    const handleApply = async (requestId) => {
        setPendingAction(requestId);
        try {
            // Using params object for cleaner URL construction
            const res = await axios.post(API_BASE + '/api/employee/apply', null, {
                params: { requestId }
            });
            
            showNotification(res.data || "Application submitted!", 'success');
            await fetchData();
            if (res.status === 200) setTimeout(() => setActiveTab('applied'), 1000);
        } catch (err) { 
            const errorMsg = err.response?.data || "Connection failed";
            showNotification(errorMsg, "error"); 
        } finally { 
            setPendingAction(null); 
        }
    };

    // 3. Handle Withdrawal
    const handleWithdraw = async (applicationId) => {
        setPendingAction(applicationId);
        try {
            await axios.post(API_BASE + '/api/employee/withdraw', null, {
                params: { applicationId }
            });
            showNotification("Application Withdrawn", 'success');
            await fetchData();
        } catch (err) { 
            showNotification("Withdraw failed", "error"); 
        } finally { 
            setPendingAction(null); 
        }
    };

        const handleEmployeeDecision = async (id, isApproved) => {
    if (!id) {
        console.error("Missing ID!");
        showNotification("Error: Request ID not found.", "error");
        return;
    }

    const reason = declineReason[id] || "";
    if (!isApproved && !reason.trim()) {
        showNotification("Please provide a reason before declining.", "error");
        return;
    }

    setPendingAction(id);

    try {
        const token = localStorage.getItem('token'); 
        const url = API_BASE + "/api/employee/assignment-decision";
        const res = await axios.post(url, null, {
            params: {
                requestId: id,
                approved: isApproved,
                reason: isApproved ? "Accepted via Employee Portal" : reason
            },
            headers: { 'Authorization': `Bearer ${token}` }
        });
        
        if (res.status === 200) {
            await fetchData(); 

            if (isApproved) {
                // ACCEPT FLOW: No "Thank You" screen, just notification and tab switch
                setDecisionMessage({
                    requestId: id,
                    text: "Accepted!",
                    isRejection: false // Ensure flag is false
                });
                setTimeout(() => {
                    setMainTab('active');
                    setDecisionMessage(null);
                }, 2000); 
            } else {
                // REJECT FLOW: Trigger the "Return to Portal" screen
                setDecisionMessage({
                    requestId: id,
                    text: "declined",
                    isRejection: true // Trigger the special UI
                });

                // Stay for 6 seconds then reset
                setTimeout(() => {
                    setDecisionMessage(null);
                }, 6000);
            }
        }
    } catch (err) {
        console.error("Error:", err.response);
        showNotification(err.response?.data || "Authorization error", "error");
    } finally {
        setPendingAction(null);
        if (!isApproved) {
            setDeclineReason(prev => {
                const newState = { ...prev };
                delete newState[id];
                return newState;
            });
            setShowReasonInput(prev => ({...prev, [id]: false}));
        }
    }
};
    const availableJobs = openPositions.filter(pos =>
        !myApplications.some(app => String(app.requestId) === String(pos.requestId) && app.status !== 'WITHDRAWN')
    );

    // Check if your backend uses 'ASSIGNED' or 'ACCEPTED'
        const activeProjects = myApplications.filter(app => 
            app.status === 'ASSIGNED' || app.status === 'ACCEPTED'
        );
        const renderSuccessAssignments = () => (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '20px', marginTop: '10px' }}>
            {successAssignments.map(item => (
                <div key={item.requestId} style={{
                    ...styles.projectRow, 
                    display: 'flex', 
                    flexDirection: 'row', 
                    alignItems: 'stretch', 
                    padding: '24px', 
                    borderLeft: '5px solid #10b981', 
                    backgroundColor: '#fff', 
                    borderRadius: '12px', 
                    boxShadow: '0 1px 3px rgba(0,0,0,0.1)'
                }}>
                    <div style={{ flex: '1' }}>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '8px' }}>
                            <h3 style={{ ...styles.projectTitleText, margin: 0, fontSize: '24px' }}>{item.employeeName}</h3>
                            <span style={{ 
                                background: '#dcfce7', 
                                color: '#166534', 
                                padding: '4px 10px', 
                                borderRadius: '6px', 
                                fontSize: '11px', 
                                fontWeight: '800', 
                                display: 'flex', 
                                alignItems: 'center', 
                                gap: '4px'
                            }}>
                                ⭐ PERFORMANCE: {item.performanceRating}
                            </span>
                        </div>
                        
                        <div style={{ 
                            background: '#f0fdf4', 
                            padding: '12px 16px', 
                            borderRadius: '8px', 
                            border: '1px solid #bbf7d0', 
                            color: '#166534', 
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
                            </div>
                            <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
                                <div style={{ fontSize: '13px', color: '#475569' }}><strong>Wage:</strong> €{item.wagePerHour}/hr</div>
                                <div style={{ fontSize: '13px', color: '#475569' }}><strong>Timeline:</strong> {item.startDate} to {item.endDate}</div>
                            </div>
                            <div style={{ display: 'flex', flexWrap: 'wrap', gap: '6px', alignContent: 'center' }}>
                                {item.employeeSkills?.map(skill => (
                                    <span key={skill} style={{ 
                                        background: '#e0e7ff', 
                                        color: '#4338ca', 
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
            ))}
        </div>
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
                                <p style={styles.profileSub}>{userProfile.departmentName}</p>
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
                            <div style={styles.statItem}><strong>Required Hours:</strong> {userProfile.totalHoursPerWeek}h/week</div>
                            <div style={styles.statItem}><strong>Location:</strong> {userProfile.primaryLocation}</div>
                            <div style={styles.statItem}><strong>Contract:</strong> {userProfile.contractType?.replace('_', ' ')}</div>
                            <div style={styles.statItem}><strong>Timeline:</strong> {userProfile.availabilityStart} to {userProfile.availabilityEnd}</div>
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
                                <div style={{...styles.orgDetail, marginTop: '8px'}}>
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
                    <button onClick={() => setMainTab('active')} style={mainTab === 'active' ? styles.mainTabActive : styles.mainTab}>
                        Active Projects {activeProjects.length > 0 && `(${activeProjects.length})`}
                    </button>
                </div>

                {/* Notifications */}
                {message.text && (
                    <div className={message.visible ? 'alert-fade-in' : 'alert-fade-out'}
                         style={{...styles.alert, backgroundColor: message.type === 'success' ? '#ecfdf5' : '#fef2f2', color: message.type === 'success' ? '#065f46' : '#991b1b'}}>
                        {message.text}
                    </div>
                )}

             {mainTab === 'active' && (
                    <div className="alert-fade-in">
                        {renderSuccessAssignments()}
                    </div>
                )}

                {/* Assignment Confirmation Section */}
                {mainTab === 'assignments' && (
                        <div className="alert-fade-in">
                            {/* PERSISTENT CONGRATS BANNER */}
                            {successAssignments.length > 0 && (
                                <div style={styles.successContainer} className="alert-fade-in">
                                    <div style={{...styles.congratsBanner, background: '#10b981', marginBottom: '20px'}}>
                                        <h2 style={{ margin: 0, fontSize: '24px' }}>Congratulations! 🎉</h2>
                                        <p style={{ margin: '8px 0 0 0', opacity: 0.9 }}>
                                            You have {successAssignments.length} successfully confirmed assignment. 
                                            Check the "Active Projects" tab for full details.
                                        </p>
                                    </div>
                                </div>
                            )}

                            {/* Show pending requests if there are any */}
                            {assignedRequests.length > 0 ? (
                                <>
                                    <div style={styles.congratsBox}>
                                        <h2 style={styles.congratsTitle}>New Opportunities! 🔔</h2>
                                        <p style={styles.congratsText}>Please review the details below and confirm your participation for these new requests.</p>
                                    </div>
                                    <div style={styles.grid}>
                                        {assignedRequests.map(item => (
                                            <div key={item.requestId} style={styles.card}>
                                                {decisionMessage && decisionMessage.requestId === item.requestId && (
                                                    <div style={{
                                                        backgroundColor: '#f0fdf4', border: '1px solid #10b981', color: '#15803d',
                                                        padding: '12px 16px', borderRadius: '10px', marginBottom: '20px',
                                                        fontSize: '14px', fontWeight: '600', display: 'flex', alignItems: 'center', gap: '8px'
                                                    }}>
                                                        <span>✅</span> {decisionMessage.text}
                                                    </div>
                                                )}

                                                <div style={styles.cardHeader}>
                                                    <div>
                                                        <span style={styles.deptBadge}>{item.project?.name || 'Assigned Project'}</span>
                                                        <h2 style={styles.jobTitle}>{item.title}</h2>
                                                        <div style={styles.statusLabelBadge}>Approved by dept head, awaiting employee confirmation</div>
                                                    </div>
                                                    <div style={styles.wageBox}>
                                                        <span style={styles.wageText}>€{item.wagePerHour}/hr</span>
                                                    </div>
                                                </div>

                                                <div style={styles.descriptionSection}>
                                                    <h4 style={styles.sectionLabel}>Description</h4>
                                                    <p style={styles.requestDesc}>{item.description || "No description provided."}</p>
                                                </div>

                                                <div style={styles.infoBoxBlue}>
                                                    <h4 style={styles.sectionLabel}>Project Context</h4>
                                                    <div style={styles.metaGrid}>
                                                        <div style={styles.metaItem}><strong>📍 Location:</strong> {item.workLocation || item.project?.location }</div>
                                                        <div style={styles.metaItem}><strong>🕒 Engagement:</strong> {item.availabilityHoursPerWeek} hrs/week</div>
                                                        <div style={styles.metaItem}><strong>📅 Start:</strong> {item.project?.startDate}</div>
                                                        <div style={styles.metaItem}><strong>📅 End:</strong> {item.project?.endDate}</div>
                                                    </div>
                                                </div>

                                                {showReasonInput[item.requestId] && (
                                                    <div style={{
                                                        position: 'fixed',
                                                        top: 0,
                                                        left: 0,
                                                        right: 0,
                                                        bottom: 0,
                                                        backgroundColor: 'rgba(15, 23, 42, 0.7)', // The exact dark tint from your styles
                                                        display: 'flex',
                                                        justifyContent: 'center',
                                                        alignItems: 'center',
                                                        zIndex: 9999,
                                                        backdropFilter: 'blur(4px)'
                                                    }} onClick={() => setShowReasonInput({...showReasonInput, [item.requestId]: false})}>
                                                        
                                                        <div style={{
                                                            backgroundColor: 'white',
                                                            padding: '40px',
                                                            borderRadius: '32px', // Force the deep curve
                                                            width: '100%',
                                                            maxWidth: '520px',
                                                            boxShadow: '0 25px 50px -12px rgba(0,0,0,0.25)',
                                                            border: 'none'
                                                        }} onClick={(e) => e.stopPropagation()}>
                                                            
                                                            <h3 style={{ 
                                                                fontSize: '32px', 
                                                                fontWeight: '800', 
                                                                color: '#1e293b', 
                                                                margin: '0 0 10px 0',
                                                                fontFamily: 'sans-serif' 
                                                            }}>Reason for Rejection</h3>
                                                            
                                                            <p style={{ 
                                                                fontSize: '16px', 
                                                                color: '#64748b', 
                                                                marginBottom: '24px', 
                                                                lineHeight: '1.5' 
                                                            }}>Provide a mandatory reason for this decision.</p>
                                                            
                                                            <textarea 
                                                                style={{
                                                                    width: '100%',
                                                                    height: '160px',
                                                                    padding: '20px',
                                                                    borderRadius: '16px',
                                                                    border: '1px solid #e2e8f0',
                                                                    backgroundColor: '#f8fafc', // Light grey background
                                                                    fontFamily: 'inherit',
                                                                    fontSize: '16px',
                                                                    resize: 'none',
                                                                    marginBottom: '32px',
                                                                    boxSizing: 'border-box',
                                                                    outline: 'none'
                                                                }} 
                                                                placeholder="Type rejection reason..."
                                                                value={declineReason[item.requestId] || ""}
                                                                onChange={(e) => setDeclineReason({...declineReason, [item.requestId]: e.target.value})}
                                                            />

                                                            <div style={{ display: 'flex', gap: '16px' }}>
                                                                <button 
                                                                    style={{
                                                                        flex: 1,
                                                                        padding: '16px',
                                                                        borderRadius: '14px',
                                                                        border: '1px solid #e2e8f0',
                                                                        backgroundColor: 'white',
                                                                        color: '#1e293b',
                                                                        fontWeight: '700',
                                                                        cursor: 'pointer',
                                                                        fontSize: '16px'
                                                                    }} 
                                                                    onClick={() => setShowReasonInput({...showReasonInput, [item.requestId]: false})}
                                                                >
                                                                    Cancel
                                                                </button>
                                                                <button 
                                                                    style={{
                                                                        flex: 1,
                                                                        padding: '16px',
                                                                        borderRadius: '14px',
                                                                        border: 'none',
                                                                        color: 'white',
                                                                        fontWeight: '700',
                                                                        fontSize: '16px',
                                                                        backgroundColor: (declineReason[item.requestId] || "").trim() ? '#ef4444' : '#fca5a5',
                                                                        cursor: (declineReason[item.requestId] || "").trim() ? 'pointer' : 'not-allowed'
                                                                    }} 
                                                                    disabled={!(declineReason[item.requestId] || "").trim()}
                                                                    onClick={() => handleEmployeeDecision(item.requestId, false)}
                                                                >
                                                                    Confirm Rejection
                                                                </button>
                                                            </div>
                                                        </div>
                                                    </div>
                                                )}
                                                <div style={styles.footer}>
                                                    <div style={styles.skillList}>
                                                        {item.requiredSkills?.map(s => <span key={s} style={styles.skill}>{s}</span>)}
                                                    </div>
                                                    <div style={{display: 'flex', gap: '12px'}}>
                                                        <button 
                                                            onClick={() => handleEmployeeDecision(item.requestId, true)} 
                                                            style={{...styles.applyBtn, background: '#10b981'}} 
                                                            disabled={pendingAction !== null}
                                                        >
                                                            Accept
                                                        </button>
                                                        <button 
                                                            onClick={() => setShowReasonInput({...showReasonInput, [item.requestId]: true})} 
                                                            style={{...styles.applyBtn, background: '#ef4444'}} 
                                                            disabled={pendingAction !== null}
                                                        >
                                                            Decline
                                                        </button>
                                                    </div>
                                                </div>
                                            </div>
                                        ))}
                                    </div>
                                </>
                            ) : (
                                /* CLEAN SECTION: No outer styles.emptyState box unless we actually need to show content */
                                <div style={{ textAlign: 'center' }}>
                                    {decisionMessage?.isRejection ? (
                                        <div className="alert-fade-in" style={{ animation: 'fadeIn 0.5s', padding: '40px 0' }}>
                                            <div style={{ fontSize: '60px', marginBottom: '20px' }}>📩</div>
                                            <h3 style={{ color: '#1f2937', fontSize: '24px', fontWeight: 'bold', marginBottom: '12px' }}>Decision Recorded</h3>
                                            <p style={{ color: '#4b5563', maxWidth: '450px', margin: '0 auto 30px auto', lineHeight: '1.6', fontSize: '16px' }}>
                                                Thank you for your decision. You can explore other exciting opportunities from the career portal.
                                            </p>
                                            <button 
                                                onClick={() => window.location.href = '/employee-dashboard'}
                                                style={{
                                                    padding: '14px 28px',
                                                    backgroundColor: '#2563eb',
                                                    color: 'white',
                                                    border: 'none',
                                                    borderRadius: '10px',
                                                    fontWeight: '600',
                                                    cursor: 'pointer'
                                                }}
                                            >
                                                Return to Career Portal
                                            </button>
                                        </div>
                                    ) : (
                                        /* Only show "No assignments" if there are also no success banners */
                                        successAssignments.length === 0 && (
                                            <div style={{...styles.emptyState, minHeight: '200px', display: 'flex', alignItems: 'center', justifyContent: 'center'}}>
                                                <p style={{ color: '#9ca3af', fontSize: '18px', margin: 0 }}>
                                                    {decisionMessage?.type === 'success' ? "Redirecting to active projects..." : "No pending project assignments found."}
                                                </p>
                                            </div>
                                        )
                                    )}
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
                                            <div style={styles.priceTag}>€{job.wagePerHour}/hr</div>
                                        </div>
                                        <div style={styles.metaGrid}>
                                            <div style={styles.metaItem}>📍 {job.workLocation}</div>
                                            <div style={styles.metaItem}>⏳ {getDurationInfo(job.projectStartDate, job.projectEndDate)}</div>
                                            <div style={styles.metaItem}>💪 {job.experienceYears} year(s)</div>
                                            <div style={styles.metaItem}>🕒 {job.availabilityHoursPerWeek} hrs/week</div>
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