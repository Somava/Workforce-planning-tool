import React, { useState, useEffect, useCallback } from 'react';

const EmployeeDashboard = () => {
    const [activeTab, setActiveTab] = useState('browse');
    const [openPositions, setOpenPositions] = useState([]);
    const [myApplications, setMyApplications] = useState([]);
    const [message, setMessage] = useState({ text: '', type: '' });
    
    const [pendingAction, setPendingAction] = useState(null); 
    const [lastSynced, setLastSynced] = useState(new Date().toLocaleTimeString());
    const [isRefreshing, setIsRefreshing] = useState(false);
    
    const userEmail = localStorage.getItem("email"); 
    const firstName = localStorage.getItem("firstName") || "User";

    useEffect(() => {
        if (message.text) {
            const timer = setTimeout(() => setMessage({ text: '', type: '' }), 5000);
            return () => clearTimeout(timer);
        }
    }, [message]);

    const fetchPositions = useCallback(async () => {
        setIsRefreshing(true);
        try {
            const response = await fetch('http://localhost:8080/api/employee-portal/open-positions');
            const data = await response.json();
            setOpenPositions(Array.isArray(data) ? data : []);
            setLastSynced(new Date().toLocaleTimeString());
        } catch (err) { console.error(err); } 
        finally { setIsRefreshing(false); }
    }, []);

    const fetchMyApplications = useCallback(async () => {
        if (!userEmail) return;
        try {
            const response = await fetch(`http://localhost:8080/api/employee-portal/my-applications?email=${userEmail}`);
            const data = await response.json();
            setMyApplications(Array.isArray(data) ? data : []);
        } catch (err) { console.error(err); }
    }, [userEmail]);

    useEffect(() => {
        fetchPositions();
        fetchMyApplications();
        const interval = setInterval(() => {
            fetchPositions();
            fetchMyApplications();
        }, 30000); 
        return () => clearInterval(interval);
    }, [fetchPositions, fetchMyApplications]);

    const handleApply = async (id) => {
        setPendingAction(`apply-${id}`);
        setMessage({ text: '', type: '' });
        try {
            const url = `http://localhost:8080/api/employee-portal/apply?requestId=${id}&email=${userEmail}`;
            const response = await fetch(url, { method: 'POST' });
            if (response.ok) {
                setMessage({ text: "Successfully applied for position!", type: 'success' });
                fetchMyApplications();
                setTimeout(() => setActiveTab('applied'), 2000);
            } else {
                setMessage({ text: "You have already applied for this role.", type: 'error' });
            }
        } catch (err) { 
            setMessage({ text: "Server error.", type: 'error' }); 
        } finally {
            setPendingAction(null);
        }
    };
    
    const handleWithdraw = async (applicationId) => {
        setPendingAction(`withdraw-${applicationId}`);
        try {
            const url = `http://localhost:8080/api/employee-portal/withdraw?applicationId=${applicationId}&email=${userEmail}`;
            const response = await fetch(url, { method: 'POST' });
            if (response.ok) {
                const successText = await response.text();
                setMessage({ text: successText, type: 'success' });
                fetchMyApplications(); 
            }
        } catch (err) { console.error(err); } 
        finally { setPendingAction(null); }
    };

    return (
        <div style={styles.pageWrapper}>
            <div style={styles.contentArea}>
                <header style={styles.header}>
                    <div>
                        <h1 style={styles.welcomeText}>Career Portal</h1>
                        <p style={styles.subText}>Welcome, {firstName} ({userEmail})</p>
                    </div>
                    <div style={styles.syncSection}>
                        <div style={styles.syncInfo}>
                            <span style={styles.syncTime}>Last synced: {lastSynced}</span>
                            <button 
                                onClick={() => { fetchPositions(); fetchMyApplications(); }} 
                                style={isRefreshing ? styles.refreshBtnSpin : styles.refreshBtn}
                            >
                                ↻
                            </button>
                        </div>
                        <div style={styles.tabGroup}>
                            <button onClick={() => setActiveTab('browse')} style={activeTab === 'browse' ? styles.activeBtn : styles.tabBtn}>Browse Jobs</button>
                            <button onClick={() => setActiveTab('applied')} style={activeTab === 'applied' ? styles.activeBtn : styles.tabBtn}>My Applications ({myApplications.length})</button>
                        </div>
                    </div>
                </header>

                {message.text && (
                    <div style={{ 
                        ...styles.alert, 
                        backgroundColor: message.type === 'success' ? '#f0fdf4' : '#fef2f2', 
                        color: message.type === 'success' ? '#166534' : '#991b1b',
                        border: `1px solid ${message.type === 'success' ? '#bbf7d0' : '#fecaca'}`
                    }}>
                        <span style={styles.alertIcon}>{message.type === 'success' ? '✓' : '✕'}</span>
                        {message.text}
                    </div>
                )}

                <div style={styles.scrollArea}>
                    {activeTab === 'browse' ? (
                        <div style={styles.list}>
                            {openPositions.map((pos, index) => (
                                <div key={index} style={styles.horizontalCard}>
                                    <div style={styles.cardInfo}>
                                        <div style={styles.cardHeaderRow}>
                                            <h3 style={styles.jobTitle}>{pos.title}</h3>
                                            <span style={styles.wage}>€{pos.wagePerHour}/hr</span>
                                        </div>
                                        <div style={styles.metaRow}>
                                            <span><strong>Exp:</strong> {pos.experienceYears}y</span>
                                            <span>•</span>
                                            <span><strong>Load:</strong> {pos.availabilityHoursPerWeek}h/wk</span>
                                        </div>
                                        <p style={styles.cardDesc}>{pos.description}</p>
                                        <div style={styles.skillList}>
                                            {pos.requiredSkills?.map(s => <span key={s} style={styles.skillTag}>{s}</span>)}
                                        </div>
                                    </div>
                                    <div style={styles.actionSection}>
                                        <button 
                                            onClick={() => handleApply(pos.projectId)} 
                                            style={pendingAction === `apply-${pos.projectId}` ? styles.applyBtnLoading : styles.applyBtn}
                                            disabled={pendingAction !== null}
                                        >
                                            {pendingAction === `apply-${pos.projectId}` ? "..." : "Apply Now"}
                                        </button>
                                    </div>
                                </div>
                            ))}
                        </div>
                    ) : (
                        <div style={styles.tableCard}>
                            <table style={styles.table}>
                                <thead>
                                    <tr>
                                        <th style={styles.th}>Project Title</th>
                                        <th style={styles.th}>Status</th>
                                        <th style={styles.th}>Applied Date</th>
                                        <th style={styles.th}>Action</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {myApplications.map((app) => (
                                        <tr key={app.applicationId} style={styles.tr}>
                                            <td style={styles.td}><strong>{app.projectTitle}</strong></td>
                                            <td style={styles.td}>
                                                <span style={{...styles.statusBadge, ...getStatusStyle(app.status)}}>
                                                    {app.status}
                                                </span>
                                            </td>
                                            <td style={styles.td}>{new Date(app.appliedAt).toLocaleDateString()}</td>
                                            <td style={styles.td}>
                                                <button 
                                                    onClick={() => handleWithdraw(app.applicationId)}
                                                    disabled={app.status !== 'APPLIED' || pendingAction !== null}
                                                    style={
                                                        app.status === 'APPLIED' 
                                                        ? (pendingAction === `withdraw-${app.applicationId}` ? styles.withdrawBtnLoading : styles.withdrawBtn)
                                                        : styles.disabledBtn
                                                    }
                                                >
                                                    {pendingAction === `withdraw-${app.applicationId}` ? "..." : "Withdraw"}
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
        </div>
    );
};

const getStatusStyle = (status) => {
    switch (status) {
        case 'APPROVED': return { background: '#dcfce7', color: '#166534', border: '1px solid #bbf7d0' };
        case 'REJECTED': return { background: '#fee2e2', color: '#991b1b', border: '1px solid #fecaca' };
        case 'WITHDRAWN': return { background: '#f1f5f9', color: '#475569', border: '1px solid #e2e8f0' };
        default: return { background: '#eff6ff', color: '#1e40af', border: '1px solid #bfdbfe' };
    }
};

const styles = {
    pageWrapper: { background: '#f8fafc', minHeight: '100vh', width: '100%', padding: 0, margin: 0 },
    contentArea: { maxWidth: '1400px', margin: '0 auto', padding: '40px 30px' }, // "A little padding" on sides
    header: { display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '30px' },
    welcomeText: { fontSize: '32px', fontWeight: '800', color: '#1e293b', margin: 0 },
    subText: { color: '#64748b', fontSize: '16px', marginTop: '5px' },
    syncSection: { display: 'flex', flexDirection: 'column', alignItems: 'flex-end', gap: '10px' },
    syncInfo: { display: 'flex', alignItems: 'center', gap: '8px' },
    syncTime: { fontSize: '12px', color: '#94a3b8' },
    refreshBtn: { background: 'none', border: 'none', color: '#4f46e5', fontSize: '20px', cursor: 'pointer' },
    refreshBtnSpin: { background: 'none', border: 'none', color: '#94a3b8', fontSize: '20px', cursor: 'wait' },
    tabGroup: { display: 'flex', background: '#e2e8f0', padding: '5px', borderRadius: '12px' },
    tabBtn: { padding: '10px 20px', border: 'none', background: 'none', cursor: 'pointer', fontWeight: '600', color: '#475569' },
    activeBtn: { padding: '10px 20px', border: 'none', background: 'white', color: '#4f46e5', borderRadius: '8px', fontWeight: '700', boxShadow: '0 2px 4px rgba(0,0,0,0.05)' },
    scrollArea: { overflowY: 'visible' }, // No nested scroll border
    list: { display: 'flex', flexDirection: 'column', gap: '20px' },
    horizontalCard: { background: 'white', padding: '30px', borderRadius: '16px', border: '1px solid #e2e8f0', display: 'flex', alignItems: 'center', boxShadow: '0 1px 3px rgba(0,0,0,0.02)' },
    cardInfo: { flex: 1, paddingRight: '30px' },
    cardHeaderRow: { display: 'flex', justifyContent: 'space-between', marginBottom: '10px' },
    jobTitle: { fontSize: '22px', fontWeight: '800', margin: 0, color: '#1e293b' },
    wage: { color: '#059669', fontWeight: '800', fontSize: '20px' },
    metaRow: { display: 'flex', gap: '15px', marginBottom: '15px', color: '#64748b', fontSize: '14px' },
    cardDesc: { fontSize: '15px', color: '#475569', lineHeight: '1.6' },
    skillList: { display: 'flex', gap: '8px', marginTop: '10px' },
    skillTag: { background: '#eef2ff', color: '#4f46e5', padding: '4px 10px', borderRadius: '20px', fontSize: '12px', fontWeight: '600' },
    applyBtn: { width: '140px', padding: '12px', background: '#4f46e5', color: 'white', border: 'none', borderRadius: '10px', fontWeight: '700', cursor: 'pointer' },
    applyBtnLoading: { width: '140px', padding: '12px', background: '#94a3b8', color: 'white', border: 'none', borderRadius: '10px', fontWeight: '700', cursor: 'wait' },
    withdrawBtn: { padding: '8px 16px', background: '#fee2e2', color: '#b91c1c', border: '1px solid #fecaca', borderRadius: '8px', cursor: 'pointer', fontWeight: '600' },
    withdrawBtnLoading: { padding: '8px 16px', background: '#f1f5f9', color: '#94a3b8', border: '1px solid #e2e8f0', borderRadius: '8px', cursor: 'wait' },
    disabledBtn: { padding: '8px 16px', background: '#f1f5f9', color: '#94a3b8', border: '1px solid #e2e8f0', borderRadius: '8px', cursor: 'not-allowed' },
    tableCard: { background: 'white', borderRadius: '16px', border: '1px solid #e2e8f0', overflow: 'hidden' },
    table: { width: '100%', borderCollapse: 'collapse' },
    th: { textAlign: 'left', padding: '18px', background: '#f8fafc', color: '#64748b', fontWeight: '600', borderBottom: '1px solid #e2e8f0' },
    td: { padding: '18px', borderBottom: '1px solid #f1f5f9' },
    statusBadge: { padding: '5px 12px', borderRadius: '20px', fontSize: '11px', fontWeight: '800', textTransform: 'uppercase' },
    alert: { padding: '16px 24px', borderRadius: '12px', marginBottom: '25px', textAlign: 'center', fontWeight: '600', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '10px', boxShadow: '0 4px 6px -1px rgba(0, 0, 0, 0.1)' },
    alertIcon: { fontSize: '18px', display: 'inline-block' }
};

export default EmployeeDashboard;