import React, { useState, useEffect, useCallback } from 'react';

const EmployeeDashboard = () => {
    const [activeTab, setActiveTab] = useState('browse');
    const [openPositions, setOpenPositions] = useState([]);
    const [myApplications, setMyApplications] = useState([]);
    const [message, setMessage] = useState({ text: '', type: '', visible: false });
    const [pendingAction, setPendingAction] = useState(null); 

    const userEmail = localStorage.getItem("email") || "becker.it@frauas.de";

    // Department Mapping Logic
    const getDeptName = (id) => {
        if (id <= 4) return "Information Technology";
        if (id <= 8) return "Research & Development";
        if (id <= 12) return "Human Resource";
        return "General Operations";
    };

    // Creative Date Logic
    const getDurationInfo = (start, end) => {
        if (!start || !end) return "Flexible Duration";
        const d1 = new Date(start);
        const d2 = new Date(end);
        const months = (d2.getFullYear() - d1.getFullYear()) * 12 + (d2.getMonth() - d1.getMonth());
        const startStr = d1.toLocaleDateString('en-US', { month: 'short', year: 'numeric' });
        const endStr = d2.toLocaleDateString('en-US', { month: 'short', year: 'numeric' });
        return `${months} Months (${startStr} — ${endStr})`;
    };

    // Global Styles: Animations & Layout
    useEffect(() => {
        const style = document.createElement('style');
        style.innerHTML = `
            html, body { margin: 0 !important; padding: 0 !important; background-color: #f8fafc; }
            @keyframes fadeIn {
                from { opacity: 0; transform: translateY(-10px); }
                to { opacity: 1; transform: translateY(0); }
            }
            @keyframes fadeOut {
                from { opacity: 1; transform: translateY(0); }
                to { opacity: 0; transform: translateY(-10px); }
            }
            .alert-fade-in { animation: fadeIn 0.4s ease forwards; }
            .alert-fade-out { animation: fadeOut 0.4s ease forwards; }
        `;
        document.head.appendChild(style);
        return () => document.head.removeChild(style);
    }, []);

    // Handle Message Visibility Timer
    useEffect(() => {
        if (message.text && message.visible) {
            const timer = setTimeout(() => {
                setMessage(prev => ({ ...prev, visible: false }));
            }, 4600); // Start fade out slightly before 5s
            return () => clearTimeout(timer);
        }
    }, [message.text, message.visible]);

    const fetchData = useCallback(async () => {
        try {
            const [posRes, appRes] = await Promise.all([
                fetch(`http://localhost:8080/api/employee-portal/open-positions?email=${userEmail}`),
                fetch(`http://localhost:8080/api/employee-portal/my-applications?email=${userEmail}`)
            ]);
            if (posRes.ok) setOpenPositions(await posRes.json());
            if (appRes.ok) setMyApplications(await appRes.json());
        } catch (err) { console.error("Sync Error", err); }
    }, [userEmail]);

    useEffect(() => { fetchData(); }, [fetchData]);

    const showNotification = (text, type) => {
        setMessage({ text, type, visible: true });
    };

    const handleApply = async (requestId) => {
        setPendingAction(requestId);
        try {
            const res = await fetch(`http://localhost:8080/api/employee-portal/apply?requestId=${requestId}&email=${userEmail}`, { method: 'POST' });
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
            const res = await fetch(`http://localhost:8080/api/employee-portal/withdraw?applicationId=${applicationId}&email=${userEmail}`, { method: 'POST' });
            const text = await res.text();
            showNotification(text || "Application Withdrawn", 'success');
            await fetchData(); 
        } catch (err) { showNotification("Withdraw failed", "error"); }
        finally { setPendingAction(null); }
    };

    const availableJobs = openPositions.filter(pos => 
        !myApplications.some(app => String(app.requestId) === String(pos.requestId) && app.status !== 'WITHDRAWN')
    );

    return (
        <div style={styles.page}>
            <div style={styles.container}>
                <header style={styles.header}>
                    <h1 style={styles.title}>Career Portal</h1>
                    <div style={styles.tabBar}>
                        <button onClick={() => setActiveTab('browse')} style={activeTab === 'browse' ? styles.activeTab : styles.tab}>Browse Jobs ({availableJobs.length})</button>
                        <button onClick={() => setActiveTab('applied')} style={activeTab === 'applied' ? styles.activeTab : styles.tab}>My Applications ({myApplications.length})</button>
                    </div>
                </header>

                {message.text && (
                    <div className={message.visible ? 'alert-fade-in' : 'alert-fade-out'} 
                         style={{...styles.alert, 
                                 backgroundColor: message.type === 'success' ? '#ecfdf5' : '#fef2f2',
                                 display: (message.text === "" && !message.visible) ? 'none' : 'block'
                         }}>
                        {message.text}
                    </div>
                )}

                {activeTab === 'browse' ? (
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
                                    <div style={styles.metaItem}><strong>📍 Location:</strong> {job.workLocation} ({job.projectLocation})</div>
                                    <div style={styles.metaItem}><strong>⏳ Engagement:</strong> {getDurationInfo(job.projectStartDate, job.projectEndDate)}</div>
                                    <div style={styles.metaItem}><strong>💪 Experience:</strong> {job.experienceYears} year(s)</div>
                                    <div style={styles.metaItem}><strong>🕒 Weekly:</strong> {job.availabilityHoursPerWeek} hrs/week</div>
                                </div>

                                <p style={styles.desc}><strong>Context:</strong> {job.projectContext}<br/>{job.description}</p>

                                <div style={styles.footer}>
                                    <div style={styles.skillList}>
                                        {job.requiredSkills && job.requiredSkills.map(s => <span key={s} style={styles.skill}>{s}</span>)}
                                    </div>
                                    <button 
                                        onClick={() => handleApply(job.requestId)}
                                        style={styles.applyBtn}
                                        disabled={pendingAction !== null}
                                    >
                                        {pendingAction === job.requestId ? '...' : 'Apply Now'}
                                    </button>
                                </div>
                            </div>
                        ))}
                    </div>
                ) : (
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
                                        <td style={styles.td}>
                                            <span style={{...styles.status, ...statusStyles[app.status]}}>{app.status}</span>
                                        </td>
                                        <td style={styles.td}>{new Date(app.appliedAt).toLocaleDateString()}</td>
                                        <td style={styles.td}>
                                            <button 
                                                disabled={app.status !== 'APPLIED' || pendingAction !== null}
                                                onClick={() => handleWithdraw(app.applicationId)}
                                                style={app.status === 'APPLIED' ? styles.withdrawBtn : styles.disabledBtn}
                                            >
                                                Withdraw
                                            </button>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
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
    container: { maxWidth: '1100px', margin: '0 auto', padding: '10px 20px' }, // Reduced padding to kill top gap
    header: { display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }, // Reduced margin
    title: { fontSize: '28px', fontWeight: '800', color: '#0f172a' },
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
    disabledBtn: { color: '#cbd5e1', background: 'none', border: 'none', cursor: 'not-allowed' }
};

export default EmployeeDashboard;