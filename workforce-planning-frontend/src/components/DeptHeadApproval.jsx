import React, { useState, useEffect, useCallback } from 'react';

const ApprovalDashboard = () => {
    const [activeTab, setActiveTab] = useState('staffing'); 
    const [staffingTasks, setStaffingTasks] = useState([]);
    const [assignmentTasks, setAssignmentTasks] = useState([]);
    const [completedAssignments, setCompletedAssignments] = useState([]); 
    const [employees, setEmployees] = useState([]); // NEW: Employee state
    const [departmentName, setDepartmentName] = useState("Loading..."); 
    const [message, setMessage] = useState({ text: '', type: '' });
    const [isRefreshing, setIsRefreshing] = useState(false);
    const [pendingAction, setPendingAction] = useState(null);
    const [expandedInfo, setExpandedInfo] = useState(null); 
    const [lastSynced, setLastSynced] = useState(new Date().toLocaleTimeString());

    const [showRejectModal, setShowRejectModal] = useState(false);
    const [rejectionReason, setRejectionReason] = useState("");
    const [targetRequestId, setTargetRequestId] = useState(null);

    const userEmail = localStorage.getItem("email") || "charlie@frauas.de";

    const calculateMatch = (required = [], candidate = []) => {
        if (required.length === 0) return 100;
        const matched = required.filter(skill => candidate.includes(skill));
        return Math.round((matched.length / required.length) * 100);
    };

    const fetchAllData = useCallback(async () => {
        setIsRefreshing(true);
        try {
            const endpoints = [
                `http://localhost:8080/api/tasks/dept-head?email=${userEmail}`,
                `http://localhost:8080/api/tasks/dept-head/employee-approval?email=${userEmail}`,
                `http://localhost:8080/api/workforce-overview/success-notifications?email=${userEmail}`,
                `http://localhost:8080/api/workforce-overview/all-employees?email=${userEmail}` // NEW: Employee endpoint
            ];

            const results = await Promise.allSettled(endpoints.map(url => fetch(url)));

            const getSafeData = async (result) => {
                if (result.status === 'fulfilled' && result.value.ok) {
                    try {
                        return await result.value.json();
                    } catch (e) {
                        console.error("JSON parsing error:", e);
                        return [];
                    }
                }
                return [];
            };

            const staffingData = await getSafeData(results[0]);
            const assignmentData = await getSafeData(results[1]);
            const successData = await getSafeData(results[2]);
            const employeeData = await getSafeData(results[3]); // NEW: Employee data

            setStaffingTasks(Array.isArray(staffingData) ? staffingData.filter(t => t.status === 'PENDING_APPROVAL') : []);
            setAssignmentTasks(Array.isArray(assignmentData) ? assignmentData : []);
            setCompletedAssignments(Array.isArray(successData) ? successData : []);
            setEmployees(Array.isArray(employeeData) ? employeeData : []); // NEW: Update employee state

            const names = { 
                "charlie@frauas.de": "IT Frontend", 
                "bob@frauas.de": "IT Backend", 
                "diana@frauas.de": "Finance & Management" 
            };
            setDepartmentName(names[userEmail] || "Department Dashboard");
            setLastSynced(new Date().toLocaleTimeString());
            
            setMessage({ text: '', type: '' });
        } catch (err) {
            console.error("Critical fetch error:", err);
            setMessage({ text: "Critical error connecting to backend.", type: 'error' });
        } finally {
            setIsRefreshing(false);
        }
    }, [userEmail]);

    useEffect(() => {
        fetchAllData();
    }, [fetchAllData]);

    const handleDecision = async (id, isApproved, reason = "") => {
        if (!isApproved && !reason.trim()) {
            setTargetRequestId(id);
            setShowRejectModal(true);
            return;
        }

        setPendingAction(`${isApproved}-${id}`);
        try {
            const queryParams = new URLSearchParams({
                requestId: id.toString(),
                email: userEmail,
                approved: isApproved.toString(),
                reason: reason 
            });

            const endpoint = activeTab === 'staffing' 
                ? 'api/tasks/dept-head/request-approval-decision' 
                : 'api/tasks/dept-head/employee-assigning-decision';

            const response = await fetch(`http://localhost:8080/${endpoint}?${queryParams.toString()}`, { 
                method: 'POST', 
                headers: { 'accept': '*/*' } 
            });

            if (response.ok) {
                setMessage({ 
                    text: `${activeTab === 'staffing' ? 'Request' : 'Assignment'} #${id} ${isApproved ? 'Approved' : 'Rejected'}.`, 
                    type: isApproved ? 'success' : 'action' 
                });
                setShowRejectModal(false);
                setRejectionReason("");
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

    const renderSuccessList = () => (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
            {completedAssignments.map(item => (
                <div key={item.requestId} style={{ ...styles.cardlessRow, borderLeft: '6px solid #10b981', paddingLeft: '30px' }}>
                    <div style={styles.cardMain}>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '15px', marginBottom: '10px' }}>
                            <h2 style={styles.jobTitle}>{item.employeeName}</h2>
                            <span style={{ ...styles.matchBadge, background: '#dcfce7', color: '#166534' }}>⭐ Performance: {item.performanceRating}</span>
                        </div>
                        <div style={{ background: '#f0fdf4', padding: '12px 16px', borderRadius: '8px', marginBottom: '15px', border: '1px solid #bbf7d0', color: '#166534', fontSize: '14px', fontWeight: '500' }}>
                            ✨ {item.congratsMessage}
                        </div>
                        <p style={styles.subHeader}>
                            <span style={styles.projectLink}>{item.projectName}</span>
                            <span style={styles.separator}>|</span>
                            <span>{item.jobTitle}</span>
                        </p>
                        <div style={styles.metaGrid}>
                            <div style={styles.metaCol}>
                                <div style={styles.metaItem}><strong>Employee ID:</strong> {item.employeeId}</div>
                                <div style={styles.metaItem}><strong>Location:</strong> {item.projectLocation}</div>
                            </div>
                            <div style={styles.metaCol}>
                                <div style={styles.metaItem}><strong>Wage:</strong> €{item.wagePerHour}/hr</div>
                                <div style={styles.metaItem}><strong>Timeline:</strong> {item.startDate} to {item.endDate}</div>
                            </div>
                            <div style={styles.metaCol}>
                                <div style={styles.skillRow}>
                                    {item.employeeSkills?.map(skill => (
                                        <span key={skill} style={styles.skillBadge}>{skill}</span>
                                    ))}
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            ))}
        </div>
    );

    // NEW: Employee List Renderer (Table View)
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
                        <tr key={emp.employeeId} style={styles.tableRow}>
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
                    <button style={{...styles.tab, ...(activeTab === 'employees' ? styles.activeTab : {})}} onClick={() => setActiveTab('employees')}>
                        Employee List ({employees.length})
                    </button>
                    <button style={{...styles.tab, ...(activeTab === 'success' ? styles.activeTab : {})}} onClick={() => setActiveTab('success')}>
                        Successful Assignments ({completedAssignments.length})
                    </button>
                </div>

                <div style={styles.list}>
                    {activeTab === 'success' ? (
                        completedAssignments.length > 0 ? renderSuccessList() : <div style={styles.emptyState}>No successful assignments found.</div>
                    ) : activeTab === 'employees' ? (
                        employees.length > 0 ? renderEmployeeList() : <div style={styles.emptyState}>No employees found in this department.</div>
                    ) : (
                        (activeTab === 'staffing' ? staffingTasks : assignmentTasks).length > 0 ? (
                            (activeTab === 'staffing' ? staffingTasks : assignmentTasks).map((item) => {
                                const itemId = item.requestId;
                                const emp = item.assignedUser?.employee;
                                const manager = item.project?.managerUser?.employee || item.createdBy;
                                const planner = item.department?.resourcePlanner;
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
                                                                <div style={styles.infoSection}>
                                                                    <h5 style={styles.panelTitle}>STAKEHOLDERS</h5>
                                                                    <p style={styles.floatText}><strong>Project Manager:</strong> {manager?.firstName} {manager?.lastName} ({manager?.email})</p>
                                                                    <p style={styles.floatText}><strong>Resource Planner:</strong> {planner?.email || 'Not Assigned'}</p>
                                                                    
                                                                    <h5 style={{...styles.panelTitle, marginTop: '12px'}}>PROJECT CONTEXT</h5>
                                                                    <p style={styles.floatText}>{item.project?.description || item.projectContext}</p>
                                                                </div>
                                                            </div>
                                                        )}
                                                    </div>
                                                </div>
                                            </div>

                                            <div style={styles.metaGrid}>
                                                <div style={styles.metaCol}>
                                                    <div style={styles.metaItem}><strong>ID:</strong> {itemId}</div>
                                                    <div style={styles.metaItem}><strong>Project Location:</strong> {item.projectLocation || item.project?.location}</div>
                                                </div>
                                                <div style={styles.metaCol}>
                                                    <div style={styles.metaItem}><strong>Mode:</strong> {item.workLocation}</div>
                                                    <div style={styles.metaItem}><strong>Schedule:</strong> {item.projectStartDate} to {item.projectEndDate}</div>
                                                </div>
                                                <div style={styles.metaCol}>
                                                    <div style={styles.metaItem}><strong>Utilization:</strong> {item.availabilityHoursPerWeek} hrs/week</div>
                                                    <div style={styles.metaItem}><strong>Experience:</strong> {item.experienceYears} year(s)</div>
                                                </div>
                                            </div>

                                            <div style={styles.descriptionRow}>
                                                <div style={styles.descCol}>
                                                    <span style={styles.smallLabel}>Role Description :</span>
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
                        )
                    )}
                </div>
            </main>

            {showRejectModal && (
                <div style={styles.modalOverlay} onClick={() => setShowRejectModal(false)}>
                    <div style={styles.modalContent} onClick={(e) => e.stopPropagation()}>
                        <h3 style={styles.modalTitle}>Reason for Rejection</h3>
                        <p style={styles.modalSub}>Provide a mandatory reason for this decision.</p>
                        <textarea 
                            style={styles.modalTextarea} 
                            placeholder="Type rejection reason..."
                            value={rejectionReason}
                            onChange={(e) => setRejectionReason(e.target.value)}
                        />
                        <div style={styles.modalActions}>
                            <button style={styles.modalCancel} onClick={() => setShowRejectModal(false)}>Cancel</button>
                            <button 
                                style={styles.modalConfirm} 
                                disabled={!rejectionReason.trim()}
                                onClick={() => handleDecision(targetRequestId, false, rejectionReason)}
                            >Confirm Rejection</button>
                        </div>
                    </div>
                </div>
            )}
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
    floatingTab: { position: 'absolute', top: '40px', right: '0', width: '320px', background: 'white', boxShadow: '0 10px 15px -3px rgba(0,0,0,0.1)', borderRadius: '12px', border: '1px solid #e2e8f0', padding: '16px', zIndex: 100 },
    infoSection: { padding: '0px' },
    floatText: { fontSize: '13px', color: '#475569', margin: '4px 0', lineHeight: '1.4' },
    panelTitle: { fontSize: '11px', letterSpacing: '0.05em', fontWeight: '800', color: '#1e293b', marginBottom: '10px', borderBottom: '2px solid #e2e8f0', paddingBottom: '4px' },
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
    emptyState: { textAlign: 'center', padding: '100px 0', color: '#94a3b8', fontSize: '18px' },
    modalOverlay: { position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, background: 'rgba(15, 23, 42, 0.7)', display: 'flex', justifyContent: 'center', alignItems: 'center', zIndex: 2000 },
    modalContent: { background: 'white', padding: '32px', borderRadius: '24px', width: '480px', boxShadow: '0 25px 50px -12px rgba(0,0,0,0.25)' },
    modalTitle: { fontSize: '24px', fontWeight: 'bold', color: '#1e293b', margin: '0 0 12px 0' },
    modalSub: { fontSize: '14px', color: '#64748b', marginBottom: '20px', lineHeight: '1.5' },
    modalTextarea: { width: '100%', height: '120px', padding: '16px', borderRadius: '12px', border: '1px solid #e2e8f0', fontFamily: 'inherit', fontSize: '14px', resize: 'none', marginBottom: '24px', boxSizing: 'border-box' },
    modalActions: { display: 'flex', gap: '12px' },
    modalCancel: { flex: 1, padding: '14px', borderRadius: '12px', border: '1px solid #e2e8f0', background: 'white', fontWeight: '600', cursor: 'pointer' },
    modalConfirm: { flex: 1, padding: '14px', borderRadius: '12px', border: 'none', background: '#ef4444', color: 'white', fontWeight: '600', cursor: 'pointer' },
    
    // NEW TABLE STYLES
    table: { width: '100%', borderCollapse: 'collapse', backgroundColor: '#fff', borderRadius: '12px', overflow: 'hidden' },
    tableHeader: { backgroundColor: '#f8fafc', borderBottom: '2px solid #e2e8f0' },
    th: { textAlign: 'left', padding: '16px', fontSize: '13px', fontWeight: '700', color: '#475569', textTransform: 'uppercase' },
    tableRow: { borderBottom: '1px solid #f1f5f9', transition: 'background 0.2s' },
    td: { padding: '16px', verticalAlign: 'middle' },
    skillTagSmall: { backgroundColor: '#f1f5f9', color: '#475569', padding: '2px 8px', borderRadius: '4px', fontSize: '11px', fontWeight: '600', border: '1px solid #e2e8f0' },
    statusPill: { padding: '4px 10px', borderRadius: '20px', fontSize: '11px', fontWeight: '700', display: 'inline-block', textAlign: 'center', minWidth: '80px' }
};

export default ApprovalDashboard;