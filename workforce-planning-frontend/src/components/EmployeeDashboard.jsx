import React, { useState, useEffect } from 'react';

const EmployeeDashboard = () => {
    const [activeTab, setActiveTab] = useState('browse');
    const [openPositions, setOpenPositions] = useState([]);
    const [myApplications, setMyApplications] = useState([]);
    const [message, setMessage] = useState({ text: '', type: '' });
    
    const employeeId = localStorage.getItem("employeeHrid");
    const firstName = localStorage.getItem("firstName") || "User";

    useEffect(() => {
        fetchPositions();
        fetchMyApplications();
    }, []);

    const fetchPositions = async () => {
        try {
            const response = await fetch('http://localhost:8080/api/employee-portal/open-positions');
            const data = await response.json();
            setOpenPositions(Array.isArray(data) ? data : []);
        } catch (err) { console.error("Error fetching positions:", err); }
    };

    const fetchMyApplications = async () => {
        try {
            const response = await fetch(`http://localhost:8080/api/employee-portal/my-applications/${employeeId}`);
            const data = await response.json();
            setMyApplications(Array.isArray(data) ? data : []);
        } catch (err) { console.error("Error fetching applications:", err); }
    };

    const handleApply = async (reqId) => {
        setMessage({ text: '', type: '' });
        try {
            const response = await fetch(`http://localhost:8080/api/employee-portal/apply/${reqId}?employeeId=${employeeId}`, {
                method: 'POST'
            });
            if (response.ok) {
                setMessage({ text: "Application submitted successfully!", type: 'success' });
                fetchMyApplications(); // Refresh the applied list
                setTimeout(() => setActiveTab('applied'), 1500); // Switch tab automatically
            } else {
                const errorText = await response.text();
                setMessage({ text: errorText || "Application failed.", type: 'error' });
            }
        } catch (err) { setMessage({ text: "Network error.", type: 'error' }); }
    };

    // [NEW] Withdraw Logic using DELETE API
    const handleWithdraw = async (applicationId) => {
        if (!window.confirm("Are you sure you want to withdraw this application?")) return;

        try {
            const response = await fetch(`http://localhost:8080/api/employee-portal/withdraw/${applicationId}?employeeId=${employeeId}`, {
                method: 'DELETE'
            });

            if (response.ok) {
                setMessage({ text: "Application withdrawn successfully.", type: 'success' });
                fetchMyApplications(); // Refresh list to show updated status
            } else {
                setMessage({ text: "Withdrawal failed.", type: 'error' });
            }
        } catch (err) { setMessage({ text: "Server error during withdrawal.", type: 'error' }); }
    };

    return (
        <div style={styles.pageWrapper}>
            <div style={styles.container}>
                <header style={styles.header}>
                    <div>
                        <h1 style={styles.welcomeText}>Career Portal</h1>
                        <p style={styles.subText}>Welcome back, {firstName}. Manage your opportunities here.</p>
                    </div>
                    <div style={styles.tabGroup}>
                        <button onClick={() => setActiveTab('browse')} style={activeTab === 'browse' ? styles.activeBtn : styles.tabBtn}>Browse Jobs</button>
                        <button onClick={() => setActiveTab('applied')} style={activeTab === 'applied' ? styles.activeBtn : styles.tabBtn}>My Applications ({myApplications.length})</button>
                    </div>
                </header>

                {message.text && (
                    <div style={{ ...styles.alert, backgroundColor: message.type === 'success' ? '#ecfdf5' : '#fef2f2', color: message.type === 'success' ? '#059669' : '#b91c1c' }}>
                        {message.text}
                    </div>
                )}

                <div style={styles.scrollArea}>
                    {activeTab === 'browse' ? (
                        <div style={styles.list}>
                            {openPositions.map((pos) => (
                                <div key={pos.requestId} style={styles.horizontalCard}>
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
                                        <button onClick={() => handleApply(pos.requestId)} style={styles.applyBtn}>Apply Now</button>
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
                                        <th style={styles.th}>Actions</th>
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
                                                {/* Only show withdraw if status is not already withdrawn */}
                                                {app.canWithdraw && (
                                                    <button 
                                                        onClick={() => handleWithdraw(app.applicationId)} 
                                                        style={styles.withdrawBtn}
                                                    >
                                                        Withdraw
                                                    </button>
                                                )}
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                            {myApplications.length === 0 && <p style={styles.emptyText}>You haven't applied to any positions yet.</p>}
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
};

const getStatusStyle = (status) => {
    switch (status) {
        case 'WITHDRAWN': return { background: '#f1f5f9', color: '#64748b' };
        case 'APPROVED': return { background: '#dcfce7', color: '#166534' };
        default: return { background: '#eff6ff', color: '#3b82f6' };
    }
};

const styles = {
    pageWrapper: { background: '#f8fafc', minHeight: '100vh', width: '100%' },
    container: { maxWidth: '1100px', margin: '0 auto', padding: '50px 20px' },
    header: { display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '40px' },
    welcomeText: { fontSize: '32px', fontWeight: '800', color: '#1e293b', margin: 0 },
    subText: { color: '#64748b', fontSize: '16px', marginTop: '5px' },
    tabGroup: { display: 'flex', background: '#e2e8f0', padding: '5px', borderRadius: '12px' },
    tabBtn: { padding: '10px 20px', border: 'none', background: 'none', cursor: 'pointer', fontWeight: '600', color: '#475569' },
    activeBtn: { padding: '10px 20px', border: 'none', background: 'white', color: '#4f46e5', borderRadius: '8px', fontWeight: '700', boxShadow: '0 2px 4px rgba(0,0,0,0.05)' },
    scrollArea: { height: 'calc(100vh - 250px)', overflowY: 'auto' },
    list: { display: 'flex', flexDirection: 'column', gap: '20px' },
    horizontalCard: { background: 'white', padding: '30px', borderRadius: '16px', border: '1px solid #e2e8f0', display: 'flex', alignItems: 'center' },
    cardInfo: { flex: 1, paddingRight: '30px' },
    cardHeaderRow: { display: 'flex', justifyContent: 'space-between', marginBottom: '10px' },
    jobTitle: { fontSize: '22px', fontWeight: '800', margin: 0 },
    wage: { color: '#059669', fontWeight: '800', fontSize: '20px' },
    metaRow: { display: 'flex', gap: '15px', marginBottom: '15px', color: '#64748b' },
    cardDesc: { fontSize: '15px', color: '#475569', lineHeight: '1.6', marginBottom: '18px' },
    skillTag: { fontSize: '11px', background: '#eef2ff', color: '#4f46e5', padding: '5px 12px', borderRadius: '20px', fontWeight: '700', marginRight: '8px' },
    applyBtn: { width: '160px', padding: '14px', background: '#4f46e5', color: 'white', border: 'none', borderRadius: '10px', fontWeight: '700', cursor: 'pointer' },
    
    // Applied Table Styles
    tableCard: { background: 'white', borderRadius: '16px', border: '1px solid #e2e8f0', overflow: 'hidden' },
    table: { width: '100%', borderCollapse: 'collapse' },
    th: { textAlign: 'left', padding: '18px', background: '#f8fafc', color: '#64748b', fontWeight: '600', borderBottom: '1px solid #e2e8f0' },
    td: { padding: '18px', borderBottom: '1px solid #f1f5f9' },
    statusBadge: { padding: '5px 12px', borderRadius: '20px', fontSize: '12px', fontWeight: '700' },
    withdrawBtn: { padding: '6px 14px', background: 'none', border: '1px solid #ef4444', color: '#ef4444', borderRadius: '6px', fontWeight: '600', cursor: 'pointer', fontSize: '13px' },
    alert: { padding: '15px', borderRadius: '10px', marginBottom: '20px', textAlign: 'center', fontWeight: '600' },
    emptyText: { textAlign: 'center', padding: '40px', color: '#94a3b8' }
};

export default EmployeeDashboard;