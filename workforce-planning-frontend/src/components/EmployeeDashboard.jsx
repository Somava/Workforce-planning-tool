import React, { useState, useEffect, useCallback } from 'react';

const EmployeeDashboard = () => {
    // State Management
    const [mainTab, setMainTab] = useState('assignments'); // Default to assignments to show opportunities
    const [activeTab, setActiveTab] = useState('browse');
    
    const [openPositions, setOpenPositions] = useState([]);
    const [myApplications, setMyApplications] = useState([]);
    const [assignedRequests, setAssignedRequests] = useState([]); 
    
    const [message, setMessage] = useState({ text: '', type: '', visible: false });
    const [pendingAction, setPendingAction] = useState(null); 

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
            const [posRes, appRes, empAssignRes] = await Promise.all([
                fetch(`http://localhost:8080/api/employee-portal/open-positions?email=${emailParam}`),
                fetch(`http://localhost:8080/api/employee-portal/my-applications?email=${emailParam}`),
                fetch(`http://localhost:8080/api/tasks/employee/assigned-requests?email=${emailParam}`)
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
        } catch (err) { 
            console.error("Sync Error:", err); 
        }
    }, [userEmail]);

    useEffect(() => { fetchData(); }, [fetchData]);

    // API Actions
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
        setPendingAction(requestId);
        try {
            const res = await fetch(`http://localhost:8080/api/tasks/employee/assignment-decision?requestId=${requestId}&email=${encodeURIComponent(userEmail)}&approved=${isApproved}`, { 
                method: 'POST' 
            });
            const text = await res.text();
            if (res.ok) {
                showNotification(text || (isApproved ? "Assignment Accepted!" : "Assignment Declined"), 'success');
                await fetchData();
            } else {
                showNotification(text || "Action failed", "error");
            }
        } catch (err) { showNotification("Connection error", "error"); }
        finally { setPendingAction(null); }
    };

    const availableJobs = openPositions.filter(pos => 
        !myApplications.some(app => String(app.requestId) === String(pos.requestId) && app.status !== 'WITHDRAWN')
    );

    return (
        <div style={styles.page}>
            <div style={styles.container}>
                <h1 style={{...styles.title, marginBottom: '30px'}}>My Employee Portal</h1>

                {/* Main Tabs */}
                <div style={{display: 'flex', gap: '40px', borderBottom: '1px solid #e2e8f0', marginBottom: '25px'}}>
                    <button onClick={() => setMainTab('assignments')} style={mainTab === 'assignments' ? styles.mainTabActive : styles.mainTab}>Project Assignment Confirmation</button>
                    <button onClick={() => setMainTab('career')} style={mainTab === 'career' ? styles.mainTabActive : styles.mainTab}>Career Portal</button>
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
                        <div style={styles.congratsBox}>
                            <h2 style={styles.congratsTitle}>Congratulations! 🎉</h2>
                            <p style={styles.congratsText}>Here are a few opportunities awaiting for you! Please review the details below and confirm your participation.</p>
                        </div>

                        <div style={styles.grid}>
                            {assignedRequests.length > 0 ? assignedRequests.map(item => (
                                <div key={item.requestId} style={styles.card}>
                                    <div style={styles.cardHeader}>
                                        <div>
                                            <span style={styles.deptBadge}>{item.project?.name || 'Assigned Project'}</span>
                                            <h2 style={styles.jobTitle}>{item.title}</h2>
                                            <div style={styles.statusLabelBadge}>
                                                Approved by dept head, awaiting employee confirmation
                                            </div>
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
                                        <h4 style={styles.sectionLabel}>Project Contacts</h4>
                                        <table style={styles.stakeholderTable}>
                                            <thead>
                                                <tr>
                                                    <th style={styles.sTh}>Role</th>
                                                    <th style={styles.sTh}>Name</th>
                                                    <th style={styles.sTh}>Contact</th>
                                                </tr>
                                            </thead>
                                            <tbody>
                                                <tr>
                                                    <td style={styles.sTd}>Resource Planner</td>
                                                    <td style={styles.sTd}>
                                                        {item.project?.managerUser?.employee?.firstName} {item.project?.managerUser?.employee?.lastName || "TBD"}
                                                    </td>
                                                    <td style={styles.sTd}>{item.project?.managerUser?.email || "N/A"}</td>
                                                </tr>
                                                <tr>
                                                    <td style={styles.sTd}>Dept. Head</td>
                                                    <td style={styles.sTd}>Internal Approval Verified</td>
                                                    <td style={styles.sTd}>—</td>
                                                </tr>
                                            </tbody>
                                        </table>
                                    </div>

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
                                                {pendingAction === item.requestId ? '...' : 'Accept Assignment'}
                                            </button>
                                            <button 
                                                onClick={() => handleEmployeeDecision(item.requestId, false)} 
                                                style={{...styles.applyBtn, background: '#ef4444'}} 
                                                disabled={pendingAction !== null}
                                            >
                                                Decline
                                            </button>
                                        </div>
                                    </div>
                                </div>
                            )) : (
                                <div style={styles.emptyState}>
                                    <p style={{color: '#64748b', fontSize: '16px'}}>No pending project assignments found.</p>
                                </div>
                            )}
                        </div>
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
                                            <tr key={app.applicationId} style={styles.tr}>
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
    mainTab: { padding: '12px 0', background: 'none', border: 'none', fontSize: '16px', fontWeight: '600', color: '#64748b', cursor: 'pointer' },
    mainTabActive: { padding: '12px 0', background: 'none', border: 'none', fontSize: '16px', fontWeight: '700', color: '#4f46e5', cursor: 'pointer', borderBottom: '2px solid #4f46e5' },
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
    statusLabelBadge: {
        marginTop: '8px',
        display: 'inline-block',
        padding: '6px 12px',
        background: '#fef3c7',
        color: '#92400e',
        borderRadius: '8px',
        fontSize: '13px',
        fontWeight: '700',
        border: '1px solid #fde68a'
    },
    wageBox: {
        textAlign: 'right',
        background: '#f0fdf4',
        padding: '10px 15px',
        borderRadius: '12px',
        border: '1px solid #dcfce7'
    },
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
    emptyState: { textAlign: 'center', padding: '60px', background: 'white', borderRadius: '20px', border: '1px solid #e2e8f0' }
};

export default EmployeeDashboard;