import React, { useState, useEffect, useCallback } from 'react';

const ApprovalDashboard = () => {
    const [activeTab, setActiveTab] = useState('pending');
    const [deptTasks, setDeptTasks] = useState([]);
    const [message, setMessage] = useState({ text: '', type: '' });
    const [isRefreshing, setIsRefreshing] = useState(false);
    const [pendingAction, setPendingAction] = useState(null);
    const [expandedInfo, setExpandedInfo] = useState(null); 
    const [lastSynced, setLastSynced] = useState(new Date().toLocaleTimeString());

    const userEmail = localStorage.getItem("email") || "bob@frauas.de";
    
    const fetchStaffingRequests = useCallback(async () => {
        setIsRefreshing(true);
        try {
            const response = await fetch(`http://localhost:8080/api/tasks/dept-head?email=${userEmail}`);
            const data = await response.json();
            const applicationsList = Array.isArray(data) ? data : (data.applications || []);
            setDeptTasks(applicationsList.filter(t => t.status === 'PENDING_APPROVAL'));
            setLastSynced(new Date().toLocaleTimeString());
        } catch (err) {
            setMessage({ text: "Error connecting to API.", type: 'error' });
        } finally {
            setIsRefreshing(false);
        }
    }, [userEmail]);

    useEffect(() => {
        fetchStaffingRequests();
    }, [fetchStaffingRequests]);

    const handleDecision = async (requestId, isApproved) => {
        if (!requestId) {
            setMessage({ text: "Error: Request ID is missing.", type: 'error' });
            return;
        }

        setPendingAction(`${isApproved}-${requestId}`);
        
        try {
            const queryParams = new URLSearchParams({
                requestId: requestId.toString(),
                email: userEmail,
                approved: isApproved.toString()
            });

            const url = `http://localhost:8080/api/tasks/dept-head/decision?${queryParams.toString()}`;

            const response = await fetch(url, { 
                method: 'POST', 
                headers: { 'accept': '*/*' }
            });

            if (response.ok) {
                // FIXED: Changed type to 'action' for rejections to avoid green styling
                setMessage({ 
                    text: `Request #${requestId} ${isApproved ? 'Approved' : 'Rejected'}.`, 
                    type: isApproved ? 'success' : 'action' 
                });
                fetchStaffingRequests();
            } else {
                const errorData = await response.json().catch(() => ({}));
                setMessage({ text: errorData.error || "Action failed on server.", type: 'error' });
            }
        } catch (err) {
            setMessage({ text: "Network error. Check if server is running.", type: 'error' });
        } finally {
            setPendingAction(null);
            setTimeout(() => setMessage({ text: '', type: '' }), 3000);
        }
    };

    return (
        <div style={styles.pageWrapper} onClick={() => setExpandedInfo(null)}>
            <main style={styles.container}>
                {/* Status Message Overlay with Dynamic Colors */}
                {message.text && (
                    <div style={{
                        ...styles.statusMessage, 
                        backgroundColor: 
                            message.type === 'error' ? '#fee2e2' : 
                            message.type === 'action' ? '#f1f5f9' : '#dcfce7',
                        color: 
                            message.type === 'error' ? '#991b1b' : 
                            message.type === 'action' ? '#475569' : '#166534',
                        border: `1px solid ${
                            message.type === 'error' ? '#fecaca' : 
                            message.type === 'action' ? '#e2e8f0' : '#bbf7d0'
                        }`
                    }}>
                        {message.text}
                    </div>
                )}

                <div style={styles.titleRow}>
                    <div style={styles.titleGroup}>
                        <h1 style={styles.mainTitle}>Career Portal</h1>
                        <p style={styles.subTitle}>Viewing <strong>Information Technology</strong> applications</p>
                    </div>
                    <div style={styles.syncGroup}>
                        <span style={styles.syncText}>Last synced: {lastSynced}</span>
                        <button 
                            onClick={(e) => { e.stopPropagation(); fetchStaffingRequests(); }} 
                            style={styles.refreshBtn}
                            disabled={isRefreshing}
                        >
                            {isRefreshing ? '...' : '↻'}
                        </button>
                    </div>
                </div>

                <div style={styles.tabContainer}>
                    <button 
                        style={{...styles.tab, ...(activeTab === 'pending' ? styles.activeTab : {})}} 
                        onClick={() => setActiveTab('pending')}
                    >
                        Staffing Request Approval ({deptTasks.length})
                    </button>
                    <button 
                        style={{...styles.tab, ...(activeTab === 'tracker' ? styles.activeTab : {})}} 
                        onClick={() => setActiveTab('tracker')}
                    >
                        Employee Assignment Approval
                    </button>
                </div>

                <div style={styles.list}>
                    {activeTab === 'pending' ? (
                        deptTasks.map((item) => (
                            <div key={item.requestId} style={styles.cardlessRow}>
                                <div style={styles.cardMain}>
                                    <div style={styles.cardHeader}>
                                        <div>
                                            <h2 style={styles.jobTitle}>{item.title}</h2>
                                            <p style={styles.subHeader}>
                                                <span style={styles.projectLink}>{item.project?.name}</span>
                                                <span style={styles.separator}>|</span>
                                                <span>{item.department?.name}</span>
                                            </p>
                                        </div>
                                        <div style={styles.priceGroup}>
                                            <span style={styles.wage}>€{item.wagePerHour}/hr</span>
                                            <div style={{ position: 'relative' }}>
                                                <button 
                                                    style={styles.infoCircle} 
                                                    onClick={(e) => {
                                                        e.stopPropagation();
                                                        setExpandedInfo(expandedInfo === item.requestId ? null : item.requestId);
                                                    }}
                                                >i</button>
                                                
                                                {expandedInfo === item.requestId && (
                                                    <div style={styles.floatingTab} onClick={(e) => e.stopPropagation()}>
                                                        <div style={styles.floatingGrid}>
                                                            <div>
                                                                <h5 style={styles.panelTitle}>Management</h5>
                                                                <p style={styles.floatText}><strong>Manager:</strong> {item.project?.managerUser?.employee?.firstName} {item.project?.managerUser?.employee?.lastName}</p>
                                                                <p style={styles.floatText}><strong>Email:</strong> {item.project?.managerUser?.email}</p>
                                                            </div>
                                                            <div>
                                                                <h5 style={styles.panelTitle}>Unit</h5>
                                                                <p style={styles.floatText}><strong>Department:</strong> {item.department?.name}</p>
                                                                <p style={styles.floatText}><strong>Planner:</strong> {item.department?.resourcePlanner?.email}</p>
                                                            </div>
                                                            <div>
                                                                <h5 style={styles.panelTitle}>Status</h5>
                                                                <p style={styles.floatText}><strong>Project:</strong> {item.project?.status}</p>
                                                                <p style={styles.floatText}><strong>Published:</strong> {item.project?.published ? "Yes" : "No"}</p>
                                                            </div>
                                                        </div>
                                                    </div>
                                                )}
                                            </div>
                                        </div>
                                    </div>

                                    <div style={styles.metaGrid}>
                                        <div style={styles.metaCol}>
                                            <div style={styles.metaItem}><strong>Req ID:</strong> {item.requestId}</div>
                                            <div style={styles.metaItem}><strong>Location:</strong> {item.project?.location}</div>
                                        </div>
                                        <div style={styles.metaCol}>
                                            <div style={styles.metaItem}><strong>Exp:</strong> {item.experienceYears} year(s)</div>
                                            <div style={styles.metaItem}><strong>Start:</strong> {item.project?.startDate}</div>
                                        </div>
                                        <div style={styles.metaCol}>
                                            <div style={styles.metaItem}><strong>Load:</strong> {item.availabilityHoursPerWeek} hrs/week</div>
                                            <div style={styles.metaItem}><strong>End:</strong> {item.project?.endDate}</div>
                                        </div>
                                    </div>

                                    <div style={styles.descriptionRow}>
                                        <div style={styles.descCol}>
                                            <span style={styles.smallLabel}>Position Description :</span>
                                            <p style={styles.descText}>{item.description}</p>
                                        </div>
                                        <div style={styles.descCol}>
                                            <span style={styles.smallLabel}>Project Context :</span>
                                            <p style={styles.descText}>{item.project?.description}</p>
                                        </div>
                                    </div>

                                    <div style={styles.skillRow}>
                                        {item.requiredSkills?.map(skill => (
                                            <span key={skill} style={styles.skillBadge}>{skill}</span>
                                        ))}
                                    </div>
                                </div>

                                <div style={styles.actionCol}>
                                    <button 
                                        onClick={(e) => { e.stopPropagation(); handleDecision(item.requestId, true); }} 
                                        style={styles.btnAccept}
                                        disabled={pendingAction !== null}
                                    >
                                        {pendingAction === `true-${item.requestId}` ? "Processing..." : "Accept"}
                                    </button>
                                    <button 
                                        onClick={(e) => { e.stopPropagation(); handleDecision(item.requestId, false); }} 
                                        style={styles.btnReject}
                                        disabled={pendingAction !== null}
                                    >
                                        {pendingAction === `false-${item.requestId}` ? "..." : "Reject"}
                                    </button>
                                </div>
                            </div>
                        ))
                    ) : (
                        <div style={styles.emptyState}>No data found for Employee Assignment.</div>
                    )}
                </div>
            </main>
        </div>
    );
};

const styles = {
    pageWrapper: { background: '#ffffff', minHeight: '100vh', fontFamily: 'Inter, sans-serif', position: 'relative' },
    statusMessage: { position: 'fixed', top: '20px', left: '50%', transform: 'translateX(-50%)', padding: '12px 24px', borderRadius: '8px', fontWeight: '600', zIndex: 1000, boxShadow: '0 4px 12px rgba(0,0,0,0.1)' },
    container: { maxWidth: '1200px', margin: '0 auto', padding: '60px 40px' },
    titleRow: { display: 'flex', justifyContent: 'space-between', alignItems: 'flex-end', marginBottom: '40px' },
    mainTitle: { fontSize: '38px', fontWeight: '800', color: '#1e293b', margin: 0 },
    subTitle: { color: '#64748b', fontSize: '16px', marginTop: '6px' },
    syncGroup: { display: 'flex', alignItems: 'center', gap: '12px' },
    syncText: { fontSize: '13px', color: '#94a3b8' },
    refreshBtn: { background: 'white', border: '1px solid #e2e8f0', width: '36px', height: '36px', borderRadius: '50%', cursor: 'pointer' },
    tabContainer: { display: 'flex', gap: '40px', marginBottom: '50px', borderBottom: '1px solid #e2e8f0' },
    tab: { padding: '12px 0', border: 'none', background: 'none', cursor: 'pointer', color: '#94a3b8', fontWeight: '600', fontSize: '15px' },
    activeTab: { color: '#4f46e5', borderBottom: '3px solid #4f46e5' },
    cardlessRow: { display: 'flex', justifyContent: 'space-between', gap: '50px', marginBottom: '60px', paddingBottom: '40px', borderBottom: '1px solid #f1f5f9' }, 
    cardMain: { flex: 1 },
    cardHeader: { display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '25px' },
    jobTitle: { fontSize: '30px', fontWeight: 'bold', color: '#1e293b', margin: 0 },
    subHeader: { display: 'flex', gap: '10px', fontSize: '15px', marginTop: '6px', color: '#64748b' },
    projectLink: { color: '#6366f1', fontWeight: '700' },
    separator: { color: '#cbd5e1' },
    priceGroup: { display: 'flex', alignItems: 'center', gap: '18px' },
    wage: { color: '#059669', fontWeight: '800', fontSize: '30px' },
    infoCircle: { width: '28px', height: '28px', borderRadius: '50%', border: '1.5px solid #cbd5e1', background: 'none', color: '#1e293b', cursor: 'pointer', fontWeight: 'bold', fontSize: '14px' },
    floatingTab: { position: 'absolute', top: '40px', right: '0', width: '500px', background: 'white', boxShadow: '0 20px 25px -5px rgba(0,0,0,0.1), 0 10px 10px -5px rgba(0,0,0,0.04)', borderRadius: '12px', border: '1px solid #e2e8f0', padding: '20px', zIndex: 100 },
    floatingGrid: { display: 'grid', gridTemplateColumns: '1.5fr 1fr 1fr', gap: '20px' },
    floatText: { fontSize: '12px', color: '#475569', margin: '4px 0' },
    panelTitle: { fontSize: '13px', fontWeight: 'bold', color: '#1e293b', marginBottom: '8px', borderBottom: '1px solid #f1f5f9', paddingBottom: '4px' },
    metaGrid: { display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: '20px', marginBottom: '25px', background: '#f8fafc', padding: '25px', borderRadius: '12px' },
    metaCol: { display: 'flex', flexDirection: 'column', gap: '8px' },
    metaItem: { fontSize: '14px', color: '#475569' },
    descriptionRow: { display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '40px', marginBottom: '25px' },
    descCol: { display: 'flex', flexDirection: 'column', gap: '6px' },
    smallLabel: { fontSize: '14px', fontWeight: '700', color: '#1e293b' },
    descText: { fontSize: '14px', color: '#64748b', lineHeight: '1.6', margin: 0 },
    skillRow: { display: 'flex', gap: '10px', flexWrap: 'wrap' },
    skillBadge: { background: '#e0e7ff', color: '#4338ca', padding: '6px 14px', borderRadius: '8px', fontSize: '13px', fontWeight: '700' },
    actionCol: { display: 'flex', flexDirection: 'column', gap: '15px', minWidth: '200px', paddingTop: '10px' },
    btnAccept: { background: '#059669', color: 'white', border: 'none', padding: '18px', borderRadius: '12px', fontWeight: 'bold', cursor: 'pointer', fontSize: '16px' },
    btnReject: { background: 'transparent', color: '#ef4444', border: '1px solid #fee2e2', padding: '17px', borderRadius: '12px', fontWeight: 'bold', cursor: 'pointer', fontSize: '16px' },
    emptyState: { textAlign: 'center', padding: '100px 0', color: '#94a3b8', fontSize: '18px' }
};

export default ApprovalDashboard;