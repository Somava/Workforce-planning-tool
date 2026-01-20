import React, { useState, useEffect, useCallback } from 'react';

const ApprovalDashboard = () => {
    const [activeTab, setActiveTab] = useState('staffing'); 
    const [staffingTasks, setStaffingTasks] = useState([]);
    const [assignmentTasks, setAssignmentTasks] = useState([]);
    const [departmentName, setDepartmentName] = useState("Loading..."); 
    const [message, setMessage] = useState({ text: '', type: '' });
    const [isRefreshing, setIsRefreshing] = useState(false);
    const [pendingAction, setPendingAction] = useState(null);
    const [expandedInfo, setExpandedInfo] = useState(null); 
    const [lastSynced, setLastSynced] = useState(new Date().toLocaleTimeString());

    const userEmail = localStorage.getItem("email") || "charlie@frauas.de";

    // Helper to calculate skill match percentage
    const calculateMatch = (required = [], candidate = []) => {
        if (required.length === 0) return 100;
        const matched = required.filter(skill => candidate.includes(skill));
        return Math.round((matched.length / required.length) * 100);
    };

    const fetchAllData = useCallback(async () => {
        setIsRefreshing(true);
        try {
            const [staffingRes, assignmentRes] = await Promise.all([
                fetch(`http://localhost:8080/api/tasks/dept-head?email=${userEmail}`),
                fetch(`http://localhost:8080/api/tasks/dept-head/int-employee-approval?email=${userEmail}`)
            ]);

            const staffingData = await staffingRes.json();
            const assignmentData = await assignmentRes.json();

            setStaffingTasks(Array.isArray(staffingData) ? staffingData.filter(t => t.status === 'PENDING_APPROVAL') : []);
            setAssignmentTasks(Array.isArray(assignmentData) ? assignmentData : []);

            const names = { 
                "charlie@frauas.de": "Research & Development", 
                "bob@frauas.de": "Information Technology", 
                "diana@frauas.de": "Human Resources" 
            };
            setDepartmentName(names[userEmail] || "Department Dashboard");
            setLastSynced(new Date().toLocaleTimeString());
        } catch (err) {
            setMessage({ text: "Error connecting to backend services.", type: 'error' });
        } finally {
            setIsRefreshing(false);
        }
    }, [userEmail]);

    useEffect(() => {
        fetchAllData();
    }, [fetchAllData]);

    const handleDecision = async (id, isApproved) => {
        setPendingAction(`${isApproved}-${id}`);
        try {
            const queryParams = new URLSearchParams({
                requestId: id.toString(),
                email: userEmail,
                approved: isApproved.toString()
            });

            const endpoint = activeTab === 'staffing' 
                ? 'api/tasks/dept-head/decision' 
                : 'api/tasks/dept-head/int-employee-decision';

            const response = await fetch(`http://localhost:8080/${endpoint}?${queryParams.toString()}`, { 
                method: 'POST', 
                headers: { 'accept': '*/*' } 
            });

            if (response.ok) {
                setMessage({ 
                    text: `${activeTab === 'staffing' ? 'Request' : 'Assignment'} #${id} ${isApproved ? 'Approved' : 'Rejected'}.`, 
                    type: isApproved ? 'success' : 'action' 
                });
                fetchAllData();
            } else {
                setMessage({ text: "Server failed to process decision.", type: 'error' });
            }
        } catch (err) {
            setMessage({ text: "Network error.", type: 'error' });
        } finally {
            setPendingAction(null);
            setTimeout(() => setMessage({ text: '', type: '' }), 3000);
        }
    };

    const currentTasks = activeTab === 'staffing' ? staffingTasks : assignmentTasks;

    return (
        <div style={styles.pageWrapper} onClick={() => setExpandedInfo(null)}>
            <main style={styles.container}>
                {message.text && (
                    <div style={{
                        ...styles.statusMessage, 
                        backgroundColor: message.type === 'error' ? '#fee2e2' : message.type === 'action' ? '#f1f5f9' : '#dcfce7',
                        color: message.type === 'error' ? '#991b1b' : message.type === 'action' ? '#475569' : '#166534',
                        border: `1px solid ${message.type === 'error' ? '#fecaca' : message.type === 'action' ? '#e2e8f0' : '#bbf7d0'}`
                    }}>
                        {message.text}
                    </div>
                )}

                <div style={styles.titleRow}>
                    <div style={styles.titleGroup}>
                        <h1 style={styles.mainTitle}>Department Portal</h1>
                        <p style={styles.subTitle}>Approvals for <strong>{departmentName}</strong></p>
                    </div>
                    <div style={styles.syncGroup}>
                        <span style={styles.syncText}>Last synced: {lastSynced}</span>
                        <button onClick={(e) => { e.stopPropagation(); fetchAllData(); }} style={styles.refreshBtn} disabled={isRefreshing}>
                            {isRefreshing ? '...' : '↻'}
                        </button>
                    </div>
                </div>

                <div style={styles.tabContainer}>
                    <button style={{...styles.tab, ...(activeTab === 'staffing' ? styles.activeTab : {})}} onClick={() => setActiveTab('staffing')}>
                        Staffing Requests ({staffingTasks.length})
                    </button>
                    <button style={{...styles.tab, ...(activeTab === 'assignment' ? styles.activeTab : {})}} onClick={() => setActiveTab('assignment')}>
                        Employee Assignments ({assignmentTasks.length})
                    </button>
                </div>

                <div style={styles.list}>
                    {currentTasks.length > 0 ? (
                        currentTasks.map((item) => {
                            const itemId = item.requestId;
                            const emp = item.assignedUser?.employee;
                            const manager = item.project?.managerUser?.employee;
                            const matchScore = calculateMatch(item.requiredSkills, emp?.skills);

                            return (
                                <div key={itemId} style={styles.cardlessRow}>
                                    <div style={styles.cardMain}>
                                        <div style={styles.cardHeader}>
                                            <div>
                                                <div style={{display: 'flex', alignItems: 'center', gap: '15px'}}>
                                                    <h2 style={styles.jobTitle}>{item.title}</h2>
                                                    {activeTab === 'assignment' && (
                                                        <span style={{
                                                            ...styles.matchBadge, 
                                                            background: matchScore > 75 ? '#dcfce7' : '#fef9c3',
                                                            color: matchScore > 75 ? '#166534' : '#854d0e'
                                                        }}>
                                                            {matchScore}% Match
                                                        </span>
                                                    )}
                                                </div>
                                                <p style={styles.subHeader}>
                                                    <span style={styles.projectLink}>{item.project?.name || item.projectName}</span>
                                                    <span style={styles.separator}>|</span>
                                                    <span style={{fontWeight: '700', color: activeTab === 'staffing' ? '#64748b' : '#4338ca'}}>
                                                        {activeTab === 'staffing' ? (item.department?.name || "Pending") : `Candidate: ${emp?.firstName} ${emp?.lastName}`}
                                                    </span>
                                                </p>
                                            </div>
                                            <div style={styles.priceGroup}>
                                                <span style={styles.wage}>€{item.wagePerHour}/hr</span>
                                                <div style={{ position: 'relative' }}>
                                                    <button 
                                                        style={styles.infoCircle} 
                                                        onClick={(e) => {
                                                            e.stopPropagation();
                                                            setExpandedInfo(expandedInfo === itemId ? null : itemId);
                                                        }}
                                                    >i</button>
                                                    
                                                    {expandedInfo === itemId && (
                                                        <div style={styles.floatingTab} onClick={(e) => e.stopPropagation()}>
                                                            <div style={styles.floatingGrid}>
                                                                <div style={styles.infoSection}>
                                                                    <h5 style={styles.panelTitle}>THE REQUEST (DEMAND)</h5>
                                                                    <p style={styles.floatText}><strong>Role:</strong> {item.title}</p>
                                                                    <p style={styles.floatText}><strong>Location:</strong> {item.workLocation || item.project?.location}</p>
                                                                    <p style={styles.floatText}><strong>Manager:</strong> {manager?.firstName} {manager?.lastName}</p>
                                                                    <p style={styles.floatText}><strong>Required Skills:</strong> {item.requiredSkills?.join(', ')}</p>
                                                                </div>

                                                                <div style={styles.infoSectionHighlight}>
                                                                    <h5 style={styles.panelTitle}>FETCHED EMPLOYEE </h5>
                                                                    {emp ? (
                                                                        <>
                                                                            <p style={styles.floatText}><strong>Name:</strong> {emp.firstName} {emp.lastName}</p>
                                                                            <p style={styles.floatText}><strong>Home Base:</strong> {emp.primaryLocation}</p>
                                                                            <p style={styles.floatText}><strong>Rating:</strong> {emp.performanceRating} / 5.0</p>
                                                                            <p style={{...styles.floatText, fontWeight: 'bold', color: '#4338ca'}}>
                                                                                <strong>Skill Match:</strong> {matchScore}%
                                                                            </p>
                                                                        </>
                                                                    ) : (
                                                                        <p style={styles.floatText}>No specific employee yet.</p>
                                                                    )}
                                                                </div>
                                                            </div>
                                                        </div>
                                                    )}
                                                </div>
                                            </div>
                                        </div>

                                        <div style={styles.metaGrid}>
                                            <div style={styles.metaCol}>
                                                <div style={styles.metaItem}><strong>Request ID:</strong> {itemId}</div>
                                                <div style={styles.metaItem}><strong>Employee Base:</strong> {emp?.primaryLocation || 'N/A'}</div>
                                            </div>
                                            <div style={styles.metaCol}>
                                                <div style={styles.metaItem}><strong>Mode:</strong> {item.workLocation}</div>
                                                <div style={styles.metaItem}><strong>Start:</strong> {item.projectStartDate}</div>
                                            </div>
                                            <div style={styles.metaCol}>
                                                <div style={styles.metaItem}><strong>Hrs/Week:</strong> {item.availabilityHoursPerWeek}</div>
                                                <div style={styles.metaItem}><strong>End:</strong> {item.projectEndDate}</div>
                                            </div>
                                        </div>

                                        <div style={styles.descriptionRow}>
                                            <div style={styles.descCol}>
                                                <span style={styles.smallLabel}>Job Description :</span>
                                                <p style={styles.descText}>{item.description}</p>
                                            </div>
                                            <div style={styles.descCol}>
                                                <span style={styles.smallLabel}>
                                                    {activeTab === 'staffing' ? 'Required Skills :' : 'Candidate Skills :'}
                                                </span>
                                                <div style={styles.skillRow}>
                                                    {(activeTab === 'staffing' ? item.requiredSkills : emp?.skills || []).map(skill => (
                                                        <span key={skill} style={{
                                                            ...styles.skillBadge,
                                                            border: (activeTab === 'assignment' && item.requiredSkills?.includes(skill)) ? '1.5px solid #4338ca' : 'none'
                                                        }}>
                                                            {skill}
                                                        </span>
                                                    ))}
                                                </div>
                                            </div>
                                        </div>
                                    </div>

                                    <div style={styles.actionCol}>
                                        <button 
                                            onClick={(e) => { e.stopPropagation(); handleDecision(itemId, true); }} 
                                            style={styles.btnAccept}
                                            disabled={pendingAction !== null}
                                        >
                                            {pendingAction === `true-${itemId}` ? "..." : "Approve"}
                                        </button>
                                        <button 
                                            onClick={(e) => { e.stopPropagation(); handleDecision(itemId, false); }} 
                                            style={styles.btnReject}
                                            disabled={pendingAction !== null}
                                        >
                                            {pendingAction === `false-${itemId}` ? "..." : "Reject"}
                                        </button>
                                    </div>
                                </div>
                            );
                        })
                    ) : (
                        <div style={styles.emptyState}>No pending tasks in this category.</div>
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
    matchBadge: { padding: '4px 12px', borderRadius: '20px', fontSize: '12px', fontWeight: '800', textTransform: 'uppercase' },
    subHeader: { display: 'flex', gap: '10px', fontSize: '15px', marginTop: '6px', color: '#64748b' },
    projectLink: { color: '#6366f1', fontWeight: '700' },
    separator: { color: '#cbd5e1' },
    priceGroup: { display: 'flex', alignItems: 'center', gap: '18px' },
    wage: { color: '#059669', fontWeight: '800', fontSize: '30px' },
    infoCircle: { width: '28px', height: '28px', borderRadius: '50%', border: '1.5px solid #cbd5e1', background: 'none', color: '#1e293b', cursor: 'pointer', fontWeight: 'bold', fontSize: '14px' },
    floatingTab: { position: 'absolute', top: '40px', right: '0', width: '600px', background: 'white', boxShadow: '0 20px 25px -5px rgba(0,0,0,0.2)', borderRadius: '12px', border: '1px solid #e2e8f0', padding: '24px', zIndex: 100 },
    floatingGrid: { display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '30px' },
    infoSection: { padding: '10px' },
    infoSectionHighlight: { padding: '15px', background: '#f8fafc', borderRadius: '8px', border: '1px solid #e2e8f0' },
    floatText: { fontSize: '12px', color: '#475569', margin: '6px 0', lineHeight: '1.4' },
    panelTitle: { fontSize: '11px', letterSpacing: '0.05em', fontWeight: '800', color: '#1e293b', marginBottom: '12px', borderBottom: '2px solid #e2e8f0', paddingBottom: '6px' },
    metaGrid: { display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: '12px', marginBottom: '25px', background: '#f8fafc', padding: '25px', borderRadius: '12px' },
    metaCol: { display: 'flex', flexDirection: 'column', gap: '8px' },
    metaItem: { fontSize: '13px', color: '#475569' },
    descriptionRow: { display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '40px', marginBottom: '25px' },
    descCol: { display: 'flex', flexDirection: 'column', gap: '6px' },
    smallLabel: { fontSize: '14px', fontWeight: '700', color: '#1e293b', marginBottom: '8px' },
    descText: { fontSize: '14px', color: '#64748b', lineHeight: '1.6', margin: 0 },
    skillRow: { display: 'flex', gap: '8px', flexWrap: 'wrap' },
    skillBadge: { background: '#e0e7ff', color: '#4338ca', padding: '4px 12px', borderRadius: '6px', fontSize: '12px', fontWeight: '700' },
    actionCol: { display: 'flex', flexDirection: 'column', gap: '15px', minWidth: '200px', paddingTop: '10px' },
    btnAccept: { background: '#059669', color: 'white', border: 'none', padding: '18px', borderRadius: '12px', fontWeight: 'bold', cursor: 'pointer', fontSize: '16px' },
    btnReject: { background: 'transparent', color: '#ef4444', border: '1px solid #fee2e2', padding: '17px', borderRadius: '12px', fontWeight: 'bold', cursor: 'pointer', fontSize: '16px' },
    emptyState: { textAlign: 'center', padding: '100px 0', color: '#94a3b8', fontSize: '18px' }
};

export default ApprovalDashboard;