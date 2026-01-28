import React, { useState, useEffect, useCallback } from 'react';
import axios from 'axios';

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

    // 1. Change the timer to a Ref at the top of your component
        const messageTimerRef = React.useRef(null); 

        const fetchAllData = useCallback(async () => {
            setIsRefreshing(true);
            try {
                const [staffingRes, assignmentRes, successRes, employeeRes] = await Promise.all([
                    axios.get(`http://localhost:8080/api/department-head/pending-requests-approval`),
                    axios.get(`http://localhost:8080/api/department-head/pending-employees-approval`),
                    axios.get(`http://localhost:8080/api/workforce-overview/success-notifications`),
                    axios.get(`http://localhost:8080/api/workforce-overview/all-employees`)
                ]);

                setStaffingTasks(Array.isArray(staffingRes.data) ? staffingRes.data.filter(t => t.status === 'PENDING_APPROVAL') : []);
                setAssignmentTasks(Array.isArray(assignmentRes.data) ? assignmentRes.data : []);
                setCompletedAssignments(Array.isArray(successRes.data) ? successRes.data : []);
                setEmployees(Array.isArray(employeeRes.data) ? employeeRes.data : []);

                const names = { 
                    "charlie@frauas.de": "IT Frontend", 
                    "bob@frauas.de": "IT Backend", 
                    "diana@frauas.de": "Finance & Management" 
                };
                setDepartmentName(names[userEmail] || "Department Dashboard");
                setLastSynced(new Date().toLocaleTimeString());

                // REMOVED: setMessage({ text: '', type: '' }); <--- THIS WAS KILLING YOUR MESSAGE

            } catch (err) {
                console.error("Critical fetch error:", err);
                setMessage({ text: "Critical error connecting to backend.", type: 'error', visible: true });
            } finally {
                setIsRefreshing(false);
            }
        }, [userEmail]);
          useEffect(() => {
        fetchAllData();
        }, [fetchAllData]);
        const handleDecision = async (id, isApproved, reason = "") => {
            const safeReason = (typeof reason === 'string') ? reason.trim() : "";

            if (!isApproved && !safeReason) {
                setTargetRequestId(id);
                setShowRejectModal(true);
                return;
            }

            setPendingAction(`${isApproved}-${id}`);

            try {
                const params = {
                    requestId: id,
                    approved: isApproved,
                    reason: safeReason 
                };

                const endpoint = activeTab === 'staffing' 
                    ? 'api/department-head/requests-approval-decision' 
                    : 'api/department-head/employee-assigning-decision';

                await axios.post(`http://localhost:8080/${endpoint}`, null, { params });

                // Show the success message
                setMessage({ 
                    text: `${activeTab === 'staffing' ? 'Request' : 'Assignment'} #${id} ${isApproved ? 'Approved' : 'Rejected'}.`, 
                    type: isApproved ? 'success' : 'action',
                    visible: true 
                });
                
                setShowRejectModal(false);
                setRejectionReason("");
                
                // Refresh data (Now it won't clear the message)
                await fetchAllData(); 

            } catch (err) {
                console.error("Decision failed:", err);
                setMessage({ text: "Server failed to process decision.", type: 'error', visible: true });
            } finally {
                setPendingAction(null);

                // Handle the 8-second timer using the Ref
                if (messageTimerRef.current) {
                    clearTimeout(messageTimerRef.current);
                }

                messageTimerRef.current = setTimeout(() => {
                    setMessage({ text: '', type: '', visible: false });
                }, 8000);
            }
        };
    const renderSuccessList = () => (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '20px', marginTop: '10px' }}>
            {completedAssignments.map(item => (
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

    // NEW: Employee List Renderer (Table View)
    const renderEmployeeList = () => {
    // Specifically tuned to match the "Internal Workforce" UI in image_161d15.png
    const styles = {
        tableContainer: {
            backgroundColor: '#fff',
            borderRadius: '12px',
            padding: '24px',
            marginTop: '10px',
            boxShadow: '0 2px 8px rgba(0,0,0,0.05)',
        },
        table: {
            width: '100%',
            borderCollapse: 'collapse',
            fontFamily: 'inherit',
        },
        tableHeader: {
            backgroundColor: '#fcfcfc', // Very light grey as seen in screenshot
        },
        th: {
            padding: '12px 8px',
            textAlign: 'left',
            fontSize: '13px',
            fontWeight: '600',
            color: '#6b7280',
            textTransform: 'uppercase',
            borderBottom: '1px solid #f3f4f6',
        },
        tableRow: {
            borderBottom: '1px solid #f9fafb',
        },
        td: {
            padding: '16px 8px',
            verticalAlign: 'top',
        },
        skillTagSmall: {
            display: 'inline-block',
            padding: '4px 10px',
            borderRadius: '6px',
            fontSize: '12px',
            backgroundColor: '#f3f4f6', // Light grey tags from image
            color: '#374151',
            margin: '2px',
        },
        statusPill: {
            display: 'inline-flex',
            alignItems: 'center',
            justifyContent: 'center',
            padding: '4px 12px',
            borderRadius: '4px',
            fontSize: '12px',
            fontWeight: 'bold',
            letterSpacing: '0.5px',
        }
    };

    return (
        <div style={styles.tableContainer}>
            <div style={{ overflowX: 'auto' }}>
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
                                    <div style={{ fontSize: '12px', fontWeight: '600', color: '#4f46e5', marginBottom: '2px' }}>{emp.employeeId}</div>
                                    <div style={{ fontSize: '15px', fontWeight: '700', color: '#111827' }}>{emp.fullName}</div>
                                    <div style={{ fontSize: '13px', color: '#6b7280' }}>{emp.email}</div>
                                </td>
                                <td style={styles.td}>
                                    <div style={{ display: 'flex', flexWrap: 'wrap', gap: '4px' }}>
                                        {emp.languages?.map(lang => (
                                            <span key={lang} style={styles.skillTagSmall}>{lang}</span>
                                        ))}
                                    </div>
                                </td>
                                <td style={styles.td}>
                                    <div style={{ display: 'flex', flexWrap: 'wrap', gap: '6px', marginBottom: '8px' }}>
                                        {emp.skills?.map(skill => (
                                            <span key={skill} style={styles.skillTagSmall}>{skill}</span>
                                        ))}
                                    </div>
                                    <div style={{ fontSize: '12px', color: '#6b7280', display: 'flex', alignItems: 'center', gap: '4px' }}>
                                        📅 <span style={{ fontWeight: '600' }}>{emp.experienceYears} Years Experience</span>
                                    </div>
                                </td>
                                <td style={styles.td}>
                                    <div style={{ display: 'flex', alignItems: 'center', gap: '4px', color: '#f59e0b', fontWeight: '700' }}>
                                        ⭐ {emp.performanceRating}
                                    </div>
                                </td>
                                <td style={styles.td}>
                                    <span style={{ 
                                        ...styles.statusPill, 
                                        backgroundColor: emp.availabilityStatus === 'AVAILABLE' ? '#ecfdf5' : '#fef2f2',
                                        color: emp.availabilityStatus === 'AVAILABLE' ? '#059669' : '#dc2626'
                                    }}>
                                        {emp.availabilityStatus}
                                    </span>
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>
        </div>
    );
};
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

                <div>
                    <div style={styles.tabContainer}>
                        <button 
                            style={{...styles.tab, ...(activeTab === 'staffing' ? styles.activeTab : {})}} 
                            onClick={() => setActiveTab('staffing')}
                        >
                            Staffing Requests ({staffingTasks.length})
                        </button>
                        <button 
                            style={{...styles.tab, ...(activeTab === 'assignment' ? styles.activeTab : {})}} 
                            onClick={() => setActiveTab('assignment')}
                        >
                            Employee Assignments ({assignmentTasks.length})
                        </button>
                        <button 
                            style={{...styles.tab, ...(activeTab === 'employees' ? styles.activeTab : {})}} 
                            onClick={() => setActiveTab('employees')}
                        >
                            Employee List ({employees.length})
                        </button>
                        <button 
                            style={{...styles.tab, ...(activeTab === 'success' ? styles.activeTab : {})}} 
                            onClick={() => setActiveTab('success')}
                        >
                            Successful Assignments ({completedAssignments.length})
                        </button>
                    </div>

                    <div style={{ 
                        padding: '20px 0 10px 0', 
                        display: 'flex', 
                        justifyContent: 'space-between', 
                        alignItems: 'baseline' 
                    }}>
                        <h2 style={{ 
                            fontSize: '20px', 
                            fontWeight: '800', 
                            color: '#111827', 
                            margin: 0,
                            fontFamily: 'inherit'
                        }}>
                            {activeTab === 'staffing' && "Action Required : Staffing Request Approval"}
                            {activeTab === 'assignment' && "Action Required : Employee Assignments"}
                            {activeTab === 'employees' && "Internal Workforce"}
                            {activeTab === 'success' && "Successfully Staffed"}
                            
                            <span style={{ fontSize: '14px', color: '#6b7280', fontWeight: '400', marginLeft: '8px' }}>
                                {departmentName ? `Viewing ${departmentName} operations` : ''}
                            </span>
                        </h2>
                    </div>
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
                                const isExternal = !!item.externalEmployee;
                                const emp = isExternal ? item.externalEmployee : item.assignedUser?.employee;
                                const manager = item.project?.managerUser?.employee || item.createdBy;
                                
                                const matchScore = isExternal 
                                    ? Math.round(item.externalEmployee.evaluationScore) 
                                    : calculateMatch(item.requiredSkills, emp?.skills);

                                const candidateName = emp ? `${emp.firstName} ${emp.lastName}` : "Pending Assignment";
                                const experience = isExternal ? emp?.experienceYears : item.experienceYears;

                                return (
                                    <div key={itemId} style={{
                                        ...styles.cardlessRow,
                                        borderLeft: activeTab === 'assignment' ? `5px solid ${isExternal ? '#0ea5e9' : '#6366f1'}` : 'none',
                                        marginBottom: '16px',
                                        padding: '24px'
                                    }}>
                                        <div style={styles.cardMain}>
                                            <div style={{ ...styles.cardHeader, alignItems: 'flex-start' }}>
                                                <div style={{ flex: 1 }}>
                                                    {/* TAGS & TITLE ROW */}
                                                    <div style={{ display: 'flex', alignItems: 'center', gap: '10px', flexWrap: 'wrap', marginBottom: '8px' }}>
                                                        {isExternal && (
                                                            <>
                                                                <span style={{
                                                                    fontSize: '11px', fontWeight: 'bold', padding: '3px 10px', borderRadius: '6px',
                                                                    backgroundColor: '#0f172a', color: '#fff', textTransform: 'uppercase'
                                                                }}>EXTERNAL</span>
                                                                <span style={{
                                                                    fontSize: '11px', fontWeight: '600', padding: '2px 10px', borderRadius: '6px',
                                                                    backgroundColor: '#f1f5f9', color: '#475569', border: '1px solid #e2e8f0'
                                                                }}>Provider: {item.externalEmployee.provider}</span>
                                                            </>
                                                        )}
                                                        <h2 style={{ ...styles.jobTitle, margin: 0, fontSize: '22px' }}>{item.title}</h2>
                                                        
                                                        {activeTab === 'assignment' && (
                                                            <span style={{
                                                                ...styles.matchBadge, 
                                                                background: matchScore > 75 ? '#dcfce7' : '#fef9c3',
                                                                color: matchScore > 75 ? '#166534' : '#854d0e',
                                                                borderRadius: '20px', padding: '4px 12px'
                                                            }}>
                                                                {matchScore}% Match of Skills
                                                            </span>
                                                        )}
                                                    </div>
                                                    
                                                    {/* PROJECT INFO SUBHEADER */}
                                                    <p style={{ ...styles.subHeader, margin: 0, display: 'flex', alignItems: 'center', gap: '8px' }}>
                                                        <span style={{ ...styles.projectLink, color: '#6366f1', fontWeight: '600' }}>{item.project?.name || item.projectName}</span>
                                                        <span style={styles.separator}>•</span>
                                                        <span style={{ fontWeight: '500', color: '#4b5563' }}>
                                                            {activeTab === 'staffing' ? "Resource Request" : `Candidate: ${candidateName}`}
                                                        </span>
                                                    </p>
                                                </div>

                                                <div style={{ display: 'flex', alignItems: 'center', gap: '12px', position: 'relative' }}>
                                                <span style={styles.wage}>€{isExternal ? emp.wagePerHour : item.wagePerHour}/hr</span>
    
                                                {/* Relative wrapper keeps the panel anchored to the button's position */}
                                                <div style={{ position: 'relative', display: 'flex', alignItems: 'center' }}>
                                                    <button 
                                                        style={{ ...styles.infoCircle, width: '28px', height: '28px', fontSize: '14px', cursor: 'pointer' }} 
                                                        onClick={(e) => {
                                                            e.stopPropagation();
                                                            setExpandedInfo(expandedInfo === itemId ? null : itemId);
                                                        }}
                                                    >
                                                        i
                                                    </button>

                                                    {expandedInfo === itemId && (
                                                        <div 
                                                            style={{
                                                                ...styles.floatingTab,
                                                                position: 'absolute',
                                                                top: '35px',        // Positions it just below the button
                                                                right: '0',         // Aligns right edge with button
                                                                width: '280px',
                                                                backgroundColor: '#fff',
                                                                borderRadius: '8px',
                                                                boxShadow: '0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05)',
                                                                border: '1px solid #e2e8f0',
                                                                padding: '16px',
                                                                zIndex: 999,        // Ensures it stays on top of other content
                                                                textAlign: 'left'
                                                            }} 
                                                            onClick={(e) => e.stopPropagation()}
                                                        >
                                                            <div style={styles.infoSection}>
                                                                <h5 style={{ ...styles.panelTitle, margin: '0 0 8px 0', fontSize: '11px', color: '#64748b', fontWeight: 'bold' }}>STAKEHOLDERS</h5>
                                                                <p style={{ ...styles.floatText, margin: '0 0 4px 0', fontSize: '13px' }}>
                                                                    <strong>Project Manager:</strong> {manager?.firstName} {manager?.lastName}
                                                                </p>                                    
                                                                <p style={{ ...styles.floatText, margin: '0 0 12px 0', fontSize: '13px' }}>
                                                                    <strong>Email:</strong> {manager?.email}
                                                                </p>                                    
                                                                
                                                                <h5 style={{ ...styles.panelTitle, margin: '0 0 8px 0', fontSize: '11px', color: '#64748b', fontWeight: 'bold' }}>PROJECT CONTEXT</h5>
                                                                <p style={{ ...styles.floatText, margin: 0, fontSize: '13px', lineHeight: '1.5' }}>
                                                                    {item.project?.description || item.projectContext}
                                                                </p>
                                                            </div>
                                                        </div>
                                                    )}
                                                </div>
                                            </div>
                                            </div>

                                            {/* DATA GRID */}
                                            <div style={{ ...styles.metaGrid, marginTop: '20px', backgroundColor: '#f8fafc', padding: '15px', borderRadius: '12px' }}>
                                                <div style={styles.metaCol}>
                                                    <div style={styles.metaItem}><span style={{ color: '#64748b' }}>ID:</span> <span style={{ fontWeight: '600' }}>{isExternal ? item.externalEmployee.externalEmployeeId : itemId}</span></div>
                                                    <div style={styles.metaItem}><span style={{ color: '#64748b' }}>Location:</span> <span style={{ fontWeight: '600' }}>{emp?.location || item.projectLocation}</span></div>
                                                </div>
                                                <div style={styles.metaCol}>
                                                    <div style={styles.metaItem}><span style={{ color: '#64748b' }}>Mode:</span> <span style={{ fontWeight: '600' }}>{item.workLocation}</span></div>
                                                    <div style={styles.metaItem}><span style={{ color: '#64748b' }}>Schedule:</span> <span style={{ fontWeight: '600' }}>{item.projectStartDate}</span></div>
                                                </div>
                                                <div style={styles.metaCol}>
                                                    <div style={styles.metaItem}><span style={{ color: '#64748b' }}>Util:</span> <span style={{ fontWeight: '600' }}>{item.availabilityHoursPerWeek} hrs/wk</span></div>
                                                    <div style={styles.metaItem}><span style={{ color: '#64748b' }}>Exp:</span> <span style={{ fontWeight: '600' }}>{experience} yr(s)</span></div>
                                                </div>
                                            </div>

                                            {/* DESCRIPTION & SKILLS */}
                                            <div style={{ ...styles.descriptionRow, marginTop: '20px', gap: '40px' }}>
                                                <div style={{ flex: 1 }}>
                                                    <span style={{ ...styles.smallLabel, color: '#1e293b', fontWeight: '700', textTransform: 'uppercase', fontSize: '11px', letterSpacing: '0.05em' }}>Description</span>
                                                    <p style={{ ...styles.descText, marginTop: '8px', color: '#475569', lineHeight: '1.6' }}>{item.description || item.jobDescription}</p>
                                                </div>
                                                <div style={{ flex: 1 }}>
                                                    <span style={{ ...styles.smallLabel, color: '#1e293b', fontWeight: '700', textTransform: 'uppercase', fontSize: '11px', letterSpacing: '0.05em' }}>
                                                        {activeTab === 'staffing' ? 'Required Skills' : 'Candidate Profile'}
                                                    </span>
                                                    <div style={{ ...styles.skillRow, marginTop: '8px' }}>
                                                        {(activeTab === 'staffing' ? item.requiredSkills : emp?.skills || item.employeeSkills || []).map(skill => (
                                                            <span key={skill} style={{
                                                                ...styles.skillBadge,
                                                                padding: '4px 12px',
                                                                backgroundColor: '#fff',
                                                                border: (activeTab === 'assignment' && item.requiredSkills?.includes(skill)) ? `2px solid ${isExternal ? '#0ea5e9' : '#6366f1'}` : '1px solid #e2e8f0',
                                                                color: '#334155',
                                                                fontWeight: '500'
                                                            }}>
                                                                {skill}
                                                            </span>
                                                        ))}
                                                    </div>
                                                </div>
                                            </div>
                                        </div>

                                        <div style={{ ...styles.actionCol, justifyContent: 'center', gap: '12px', minWidth: '140px' }}>
                                            <button 
                                                onClick={(e) => { e.stopPropagation(); handleDecision(itemId, true, isExternal); }} 
                                                style={{ ...styles.btnAccept, width: '100%', padding: '12px' }}
                                                disabled={pendingAction !== null}
                                            >
                                                {pendingAction === `true-${itemId}` ? "..." : "Approve"}
                                            </button>
                                            <button 
                                                onClick={(e) => { e.stopPropagation(); setTargetRequestId(itemId); setShowRejectModal(true); }} 
                                                style={{ ...styles.btnReject, width: '100%', padding: '12px' }}
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