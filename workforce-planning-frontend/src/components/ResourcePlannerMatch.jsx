import React, { useState, useEffect, useCallback } from 'react';
import { ChevronRight, ArrowLeft, RefreshCw, UserCheck, Cpu, Zap, Globe, Award, Phone, AlertCircle, CheckCircle, Users, Mail, MapPin, Briefcase } from 'lucide-react';

const ResourcePlannerMatch = () => {
    const [view, setView] = useState('list'); 
    const [requests, setRequests] = useState([]);
    const [successAssignments, setSuccessAssignments] = useState([]);
    const [employees, setEmployees] = useState([]); // New state for Employee List
    const [selectedRequest, setSelectedRequest] = useState(null);
    const [candidates, setCandidates] = useState([]);
    const [loading, setLoading] = useState(false);
    const [message, setMessage] = useState({ text: '', type: '' });
    const [expandedInfo, setExpandedInfo] = useState(null); 
    const [lastSynced, setLastSynced] = useState(new Date().toLocaleTimeString());
    const [departmentName, setDepartmentName] = useState("Department");

    const userEmail = localStorage.getItem("email") || "eve@frauas.de"; 

    useEffect(() => {
        window.scrollTo({ top: 0, left: 0, behavior: 'smooth' });
    }, [view]);

    // Fetch Employee List (from the Swagger endpoint)
    const fetchEmployees = useCallback(async () => {
        setLoading(true);
        try {
            const response = await fetch(`http://localhost:8080/api/workforce-overview/all-employees?email=${userEmail}`);
            const data = await response.json();
            setEmployees(Array.isArray(data) ? data : []);
        } catch (err) {
            console.error("Failed to fetch employee list");
        } finally {
            setLoading(false);
        }
    }, [userEmail]);

    const fetchRequests = useCallback(async () => {
        setLoading(true);
        try {
            const response = await fetch(`http://localhost:8080/api/tasks/resource-planner?email=${userEmail}`);
            const data = await response.json();
            const list = Array.isArray(data) ? data : [];
            setRequests(list);

            if (list.length > 0 && list[0].department) {
                setDepartmentName(list[0].department.name);
            } else {
                switch (userEmail) {
                    case "eve@frauas.de": setDepartmentName("Information Technology"); break;
                    case "charlie@frauas.de": setDepartmentName("Research & Development"); break;
                    case "diana@frauas.de": setDepartmentName("Human Resources"); break;
                    default: setDepartmentName("Department");
                }
            }
            setLastSynced(new Date().toLocaleTimeString());
        } catch (err) {
            setMessage({ text: "Error connecting to Planner API.", type: 'error' });
        } finally {
            setLoading(false);
        }
    }, [userEmail]);

    const fetchSuccess = useCallback(async () => {
        setLoading(true);
        try {
            const response = await fetch(`http://localhost:8080/api/workforce-overview/success-notifications?email=${userEmail}`);
            const data = await response.json();
            setSuccessAssignments(Array.isArray(data) ? data : []);
        } catch (err) {
            console.error("Failed to fetch success notifications");
        } finally {
            setLoading(false);
        }
    }, [userEmail]);

    useEffect(() => {
        fetchRequests();
        fetchSuccess();
        fetchEmployees(); // Initialize employee list
    }, [fetchRequests, fetchSuccess, fetchEmployees]);

    const handleLoadMatches = async (req) => {
        setLoading(true);
        setSelectedRequest(req);
        try {
            const res = await fetch(`http://localhost:8080/api/resource-planner/staffing-requests/matches?requestId=${req.requestId}&topN=10`);
            const data = await res.json();
            
            if (data && data.matches && Array.isArray(data.matches)) {
                setCandidates(data.matches);
            } else if (Array.isArray(data)) {
                setCandidates(data);
            } else {
                setCandidates([]); 
            }
            
            setView('matching');
        } catch (err) {
            setCandidates([]);
            setMessage({ text: "Failed to load candidate matches.", type: 'error' });
        } finally {
            setLoading(false);
        }
    };

    const handleDecision = async (employeeDbId, accept) => {
        setLoading(true);
        try {
            const url = `http://localhost:8080/api/resource-planner/staffing-requests/reserve?requestId=${selectedRequest.requestId}&internalFound=${accept}`;
            const res = await fetch(url, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: accept ? JSON.stringify({ employeeDbId }) : JSON.stringify({})
            });

            if (res.ok) {
                setMessage({ 
                    text: accept ? "Employee Reserved successfully!" : "External recruitment process triggered.", 
                    type: 'success' 
                });
                setView('list');
                fetchRequests();
                fetchSuccess(); 
                fetchEmployees();
            } else {
                setMessage({ text: "Action failed. Please check API status.", type: 'error' });
            }
        } catch (err) {
            setMessage({ text: "Connection error. Action failed.", type: 'error' });
        } finally {
            setLoading(false);
            setTimeout(() => setMessage({ text: '', type: '' }), 3000);
        }
    };

    // --- NEW: RENDER EMPLOYEE LIST ---
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

    const renderSuccessAssignments = () => (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
            {successAssignments.length === 0 ? (
                <div style={styles.emptyState}>No successful assignments recorded yet.</div>
            ) : (
                successAssignments.map(item => (
                    <div key={item.requestId} style={styles.successRow}>
                        <div style={{ flex: '1' }}>
                            <div style={styles.headerRow}>
                                <h3 style={styles.projectTitleText}>{item.employeeName}</h3>
                                <span style={styles.performanceBadge}>
                                    ⭐ PERFORMANCE: {item.performanceRating}
                                </span>
                            </div>
                            <div style={styles.congratsBanner}>✨ {item.congratsMessage}</div>
                            <div style={styles.projectSubline}>
                                <span style={{ color: '#4f46e5' }}>{item.projectName}</span>
                                <span style={{ color: '#d1d5db' }}>|</span>
                                <span style={{ color: '#6b7280' }}>{item.jobTitle}</span>
                            </div>
                            <div style={styles.metaGridSuccess}>
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
                                        <span key={skill} style={styles.skillTagSmallSuccess}>{skill}</span>
                                    ))}
                                </div>
                            </div>
                        </div>
                    </div>
                ))
            )}
        </div>
    );

    return (
        <div style={styles.pageWrapper} onClick={() => setExpandedInfo(null)}>
            <main style={styles.container}>
                {message.text && (
                    <div style={{
                        ...styles.statusMessage, 
                        backgroundColor: message.type === 'error' ? '#fee2e2' : '#dcfce7',
                        color: message.type === 'error' ? '#991b1b' : '#166534',
                        border: `1px solid ${message.type === 'error' ? '#fecaca' : '#bbf7d0'}`
                    }}>
                        {message.text}
                    </div>
                )}

                <div style={styles.titleRow}>
                    <div style={styles.titleGroup}>
                        <h1 style={styles.mainTitle}>Staffing & Matchmaking</h1>
                        <p style={styles.subTitle}>Viewing <strong>{departmentName}</strong> operations</p>
                    </div>
                    <div style={styles.syncGroup}>
                        <span style={styles.syncText}>Last synced: {lastSynced}</span>
                        <button onClick={(e) => { e.stopPropagation(); fetchRequests(); fetchSuccess(); fetchEmployees(); }} style={styles.refreshBtn} disabled={loading}>
                            <RefreshCw size={16} className={loading ? "animate-spin" : ""} />
                        </button>
                    </div>
                </div>

                <div style={styles.tabBar}>
                    <button 
                        style={{...styles.tabItem, ...(view === 'list' ? styles.activeTab : {})}} 
                        onClick={() => setView('list')}
                    >
                        Pending Approvals ({requests.length})
                    </button>
                    <button 
                        style={{...styles.tabItem, ...(view === 'success' ? styles.activeTab : {})}} 
                        onClick={() => setView('success')}
                    >
                        Successful Assignments ({successAssignments.length})
                    </button>
                    <button 
                        style={{...styles.tabItem, ...(view === 'employees' ? styles.activeTab : {})}} 
                        onClick={() => setView('employees')}
                    >
                        Employee List ({employees.length})
                    </button>
                </div>

                {view === 'list' && (
                    <div style={styles.list}>
                        {requests.length === 0 ? (
                            <div style={styles.emptyState}>No approved requests awaiting matchmaking.</div>
                        ) : (
                            requests.map((item) => (
                                <div key={item.requestId} style={styles.cardlessRow}>
                                    <div style={styles.cardMain}>
                                        <div style={styles.cardHeader}>
                                            <div>
                                                <div style={styles.badge}>DEPT HEAD APPROVED</div>
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
                                                                    <h5 style={styles.panelTitle}>Manager Info</h5>
                                                                    <p style={styles.floatText}><strong>Name:</strong> {item.project?.managerUser?.employee?.firstName} {item.project?.managerUser?.employee?.lastName}</p>
                                                                    <p style={styles.floatText}><strong>Email:</strong> {item.project?.managerUser?.email}</p>
                                                                </div>
                                                                <div>
                                                                    <h5 style={styles.panelTitle}>Project Details</h5>
                                                                    <p style={styles.floatText}><strong>Status:</strong> {item.project?.status}</p>
                                                                    <p style={styles.floatText}><strong>Location:</strong> {item.project?.location}</p>
                                                                </div>
                                                                <div>
                                                                    <h5 style={styles.panelTitle}>Timeline</h5>
                                                                    <p style={styles.floatText}><strong>Start:</strong> {item.project?.startDate}</p>
                                                                    <p style={styles.floatText}><strong>End:</strong> {item.project?.endDate}</p>
                                                                </div>
                                                            </div>
                                                        </div>
                                                    )}
                                                </div>
                                            </div>
                                        </div>
                                        <div style={styles.metaGrid}>
                                            <div style={styles.metaCol}>
                                                <div style={styles.metaItem}><strong>Request ID:</strong> {item.requestId}</div>
                                                <div style={styles.metaItem}><strong>Project Location:</strong> {item.project?.location}</div>
                                                <div style={styles.metaItem}><strong>Work Mode:</strong> {item.workLocation}</div>
                                            </div>
                                            <div style={styles.metaCol}>
                                                <div style={styles.metaItem}><strong>Exp Required:</strong> {item.experienceYears} yr(s)</div>
                                                <div style={styles.metaItem}><strong>Req. Hours:</strong> {item.availabilityHoursPerWeek} hrs/wk</div>
                                            </div>
                                            <div style={styles.metaCol}>
                                                <div style={styles.metaItem}><strong>Start:</strong> {item.projectStartDate}</div>
                                                <div style={styles.metaItem}><strong>End:</strong> {item.projectEndDate}</div>
                                            </div>
                                        </div>
                                        <div style={styles.descriptionRow}>
                                            <div style={styles.descCol}>
                                                <span style={styles.smallLabel}>Request Description:</span>
                                                <p style={styles.descText}>{item.description}</p>
                                            </div>
                                            <div style={styles.descCol}>
                                                <span style={styles.smallLabel}>Project Context:</span>
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
                                        <button onClick={() => handleLoadMatches(item)} style={styles.btnMatchLarge}>
                                            Find Best Matches <ChevronRight size={20} />
                                        </button>
                                    </div>
                                </div>
                            ))
                        )}
                    </div>
                )}

                {view === 'success' && renderSuccessAssignments()}
                {view === 'employees' && renderEmployeeList()}

                {view === 'matching' && (
                    <div>
                        <button onClick={() => setView('list')} style={styles.backBtn}><ArrowLeft size={18}/> Back</button>
                        <h1 style={styles.mainTitle}>Best Matches for Request #{selectedRequest?.requestId}</h1>
                        {candidates.length === 0 ? (
                            <div style={styles.noMatchesFoundCard}>
                                <AlertCircle size={48} color="#f59e0b" />
                                <h3>No internal matches found</h3>
                                <p>Unable to get employees for this role in our organisation.</p>
                                <button onClick={() => handleDecision(null, false)} style={styles.btnExternalTriggerLarge}>
                                    Trigger External Recruitment
                                </button>
                            </div>
                        ) : (
                            candidates.map(emp => (
                                <div key={emp.employeeDbId} style={styles.matchProfileRow}>
                                    <div style={styles.profileContent}>
                                        <div style={styles.profileHeader}>
                                            <div>
                                                {emp.applied ? (
                                                    <span style={styles.appliedBadge}><UserCheck size={12}/> Self Applied</span>
                                                ) : (
                                                    <span style={styles.systemBadge}><Cpu size={12}/> AI System Match</span>
                                                )}
                                                <h2 style={styles.empName}>{emp.firstName} {emp.lastName} <span style={styles.empIdTag}>({emp.employeeId})</span></h2>
                                                <p style={styles.empSubText}>{emp.email} • {emp.location}</p>
                                            </div>
                                            <div style={styles.scoreCircle}>
                                                <span style={styles.scoreVal}>{emp.score}%</span>
                                                <span style={styles.scoreLab}>Match</span>
                                            </div>
                                        </div>
                                        <div style={styles.dataGrid}>
                                            <div style={styles.dataColProfile}>
                                                <h4 style={styles.sectionLabel}><Zap size={14}/> Technical Skills</h4>
                                                <div style={styles.tagWrap}>
                                                    {emp.skills?.map(s => <span key={s} style={styles.skillTag}>{s}</span>)}
                                                </div>
                                            </div>
                                            <div style={styles.dataColProfile}>
                                                <h4 style={styles.sectionLabel}><Globe size={14}/> Languages</h4>
                                                <div style={styles.langList}>
                                                    {emp.languages?.map(l => <span key={l.name} style={styles.langText}>{l.name} ({l.proficiency})</span>)}
                                                </div>
                                            </div>
                                            <div style={styles.dataColProfile}>
                                                <h4 style={styles.sectionLabel}><Award size={14}/> Professional Profile</h4>
                                                <p style={styles.detailText}><strong>Seniority:</strong> {emp.seniorityLevel}</p>
                                                <p style={styles.detailText}><strong>Experience:</strong> {emp.experienceYears} Years</p>
                                                <p style={styles.detailText}><strong>Perf. Grade:</strong> {emp.performanceGrade}</p>
                                            </div>
                                            <div style={styles.dataColProfile}>
                                                <h4 style={styles.sectionLabel}><Phone size={14}/> Administration</h4>
                                                <p style={styles.detailText}><strong>Wage:</strong> €{emp.wagePerHour}/hr</p>
                                                <p style={styles.detailText}><strong>Emergency:</strong> {emp.emergencyContact}</p>
                                                <p style={styles.detailText}><strong>Availability:</strong> {emp.availableHoursPerWeek}h/week</p>
                                            </div>
                                        </div>
                                    </div>
                                    <div style={styles.decisionStrip}>
                                        <button onClick={() => handleDecision(emp.employeeDbId, true)} style={styles.btnReserve}>Reserve Internal</button>
                                        <button onClick={() => handleDecision(null, false)} style={styles.btnExternalSmall}>Trigger External</button>
                                    </div>
                                </div>
                            ))
                        )}
                    </div>
                )}
            </main>
        </div>
    );
};

const styles = {
    pageWrapper: { background: '#f8fafc', minHeight: '100vh', fontFamily: 'Inter, sans-serif', position: 'relative' },
    statusMessage: { position: 'fixed', top: '20px', left: '50%', transform: 'translateX(-50%)', padding: '12px 24px', borderRadius: '8px', fontWeight: '600', zIndex: 1000, boxShadow: '0 4px 12px rgba(0,0,0,0.1)' },
    container: { maxWidth: '1200px', margin: '0 auto', padding: '60px 40px' },
    titleRow: { display: 'flex', justifyContent: 'space-between', alignItems: 'flex-end', marginBottom: '20px' },
    mainTitle: { fontSize: '38px', fontWeight: '800', color: '#1e293b', margin: 0 },
    subTitle: { color: '#64748b', fontSize: '16px', marginTop: '6px' },
    syncGroup: { display: 'flex', alignItems: 'center', gap: '12px' },
    syncText: { fontSize: '13px', color: '#94a3b8' },
    refreshBtn: { background: 'white', border: '1px solid #e2e8f0', width: '36px', height: '36px', borderRadius: '50%', cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center' },
    tabBar: { display: 'flex', gap: '20px', marginBottom: '40px', borderBottom: '1px solid #e5e7eb' },
    tabItem: { padding: '10px 20px', border: 'none', background: 'none', cursor: 'pointer', color: '#6b7280', fontWeight: '600', transition: '0.2s', fontSize: '15px' },
    activeTab: { color: '#4f46e5', borderBottom: '2px solid #4f46e5' },
    cardlessRow: { display: 'flex', justifyContent: 'space-between', gap: '50px', marginBottom: '60px', paddingBottom: '40px', borderBottom: '1px solid #f1f5f9' }, 
    cardMain: { flex: 1 },
    cardHeader: { display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '25px' },
    jobTitle: { fontSize: '30px', fontWeight: 'bold', color: '#1e293b', margin: 0 },
    badge: { background: '#dcfce7', color: '#166534', fontSize: '11px', fontWeight: '800', padding: '4px 10px', borderRadius: '4px', marginBottom: '12px', display: 'inline-block' },
    subHeader: { display: 'flex', gap: '10px', fontSize: '15px', marginTop: '6px', color: '#64748b' },
    projectLink: { color: '#6366f1', fontWeight: '700' },
    separator: { color: '#cbd5e1' },
    priceGroup: { display: 'flex', alignItems: 'center', gap: '18px' },
    wage: { color: '#059669', fontWeight: '800', fontSize: '30px' },
    infoCircle: { width: '28px', height: '28px', borderRadius: '50%', border: '1.5px solid #cbd5e1', background: 'none', color: '#1e293b', cursor: 'pointer', fontWeight: 'bold', fontSize: '14px' },
    floatingTab: { position: 'absolute', top: '40px', right: '0', width: '500px', background: 'white', boxShadow: '0 20px 25px -5px rgba(0,0,0,0.1)', borderRadius: '12px', border: '1px solid #e2e8f0', padding: '20px', zIndex: 100 },
    floatingGrid: { display: 'grid', gridTemplateColumns: '1.2fr 1fr 0.8fr', gap: '20px' },
    floatText: { fontSize: '12px', color: '#475569', margin: '4px 0' },
    panelTitle: { fontSize: '14px', fontWeight: 'bold', color: '#1e293b', marginBottom: '12px', borderBottom: '1px solid #f1f5f9', paddingBottom: '4px' },
    metaGrid: { display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: '12px', marginBottom: '25px', background: '#f8fafc', padding: '25px', borderRadius: '12px' },
    metaCol: { display: 'flex', flexDirection: 'column', gap: '8px' },
    metaItem: { fontSize: '13px', color: '#475569' },
    descriptionRow: { display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '40px', marginBottom: '25px' },
    descCol: { display: 'flex', flexDirection: 'column', gap: '6px' },
    smallLabel: { fontSize: '14px', fontWeight: '700', color: '#1e293b' },
    descText: { fontSize: '14px', color: '#64748b', lineHeight: '1.6', margin: 0 },
    skillRow: { display: 'flex', gap: '10px', flexWrap: 'wrap' },
    skillBadge: { background: '#e0e7ff', color: '#4338ca', padding: '6px 14px', borderRadius: '8px', fontSize: '13px', fontWeight: '700' },
    actionCol: { display: 'flex', alignItems: 'center', minWidth: '220px' },
    btnMatchLarge: { background: '#4f46e5', color: 'white', border: 'none', padding: '20px 30px', borderRadius: '12px', fontWeight: 'bold', cursor: 'pointer', fontSize: '16px', display: 'flex', alignItems: 'center', gap: '12px' },
    backBtn: { background: 'none', border: 'none', color: '#4f46e5', fontWeight: 'bold', cursor: 'pointer', display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '30px' },
    emptyState: { textAlign: 'center', padding: '100px 0', color: '#94a3b8', fontSize: '18px' },
    matchProfileRow: { display: 'flex', gap: '30px', background: '#ffffff', border: '1px solid #e2e8f0', borderRadius: '16px', padding: '30px', marginBottom: '30px', boxShadow: '0 4px 6px -1px rgba(0,0,0,0.05)' },
    profileContent: { flex: 1 },
    profileHeader: { display: 'flex', justifyContent: 'space-between', marginBottom: '25px' },
    empName: { fontSize: '22px', fontWeight: '800', margin: '8px 0 4px', color: '#1e293b' },
    empIdTag: { color: '#94a3b8', fontSize: '16px', fontWeight: '400' },
    empSubText: { color: '#64748b', fontSize: '14px' },
    appliedBadge: { background: '#dcfce7', color: '#166534', padding: '4px 10px', borderRadius: '20px', fontSize: '11px', fontWeight: '800', display: 'flex', alignItems: 'center', gap: '5px', width: 'fit-content' },
    systemBadge: { background: '#f1f5f9', color: '#475569', padding: '4px 10px', borderRadius: '20px', fontSize: '11px', fontWeight: '800', display: 'flex', alignItems: 'center', gap: '5px', width: 'fit-content' },
    scoreCircle: { border: '4px solid #10b981', width: '65px', height: '65px', borderRadius: '50%', display: 'flex', flexDirection: 'column', justifyContent: 'center', alignItems: 'center' },
    scoreVal: { fontSize: '16px', fontWeight: '900', color: '#065f46' },
    scoreLab: { fontSize: '9px', textTransform: 'uppercase', color: '#64748b' },
    dataGrid: { display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '25px' },
    dataColProfile: { display: 'flex', flexDirection: 'column', gap: '4px' },
    sectionLabel: { fontSize: '12px', textTransform: 'uppercase', color: '#94a3b8', letterSpacing: '0.5px', marginBottom: '10px', display: 'flex', alignItems: 'center', gap: '6px' },
    tagWrap: { display: 'flex', gap: '6px', flexWrap: 'wrap' },
    skillTag: { background: '#f8fafc', border: '1px solid #e2e8f0', padding: '4px 10px', borderRadius: '6px', fontSize: '12px', fontWeight: '600' },
    langList: { display: 'flex', flexDirection: 'column', gap: '4px' },
    langText: { fontSize: '13px', color: '#475569' },
    detailText: { fontSize: '13px', color: '#475569', margin: '4px 0' },
    decisionStrip: { display: 'flex', flexDirection: 'row', gap: '12px', alignItems: 'center', paddingLeft: '20px' },
    btnReserve: { background: '#10b981', color: 'white', border: 'none', padding: '10px 24px', borderRadius: '8px', fontWeight: '600', cursor: 'pointer', fontSize: '14px', transition: 'all 0.2s ease' },
    btnExternalSmall: { background: 'transparent', color: '#ef4444', border: '1px solid #fecaca', padding: '10px 24px', borderRadius: '8px', fontSize: '14px', fontWeight: '600', cursor: 'pointer', transition: 'all 0.2s ease' },
    noMatchesFoundCard: { display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', padding: '60px', background: '#fffbeb', border: '2px dashed #f59e0b', borderRadius: '16px', textAlign: 'center', marginTop: '20px' },
    btnExternalTriggerLarge: { background: '#ef4444', color: 'white', border: 'none', padding: '16px 32px', borderRadius: '12px', fontWeight: 'bold', fontSize: '16px', cursor: 'pointer', marginTop: '20px', boxShadow: '0 4px 6px -1px rgba(239, 68, 68, 0.4)' },
    successRow: { display: 'flex', flexDirection: 'row', alignItems: 'stretch', padding: '24px', borderLeft: '5px solid #10b981', backgroundColor: '#fff', borderRadius: '12px', boxShadow: '0 1px 3px rgba(0,0,0,0.1)', marginBottom: '16px' },
    headerRow: { display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '8px' },
    projectTitleText: { margin: 0, fontSize: '24px', color: '#1e293b', fontWeight: '700' },
    performanceBadge: { background: '#dcfce7', color: '#166534', padding: '4px 10px', borderRadius: '6px', fontSize: '11px', fontWeight: '800', display: 'flex', alignItems: 'center', gap: '4px' },

    // --- NEW EMPLOYEE LIST TABLE STYLES ---
    table: { width: '100%', borderCollapse: 'separate', borderSpacing: '0 10px', marginTop: '-10px' },
    tableHeader: { backgroundColor: 'transparent' },
    th: { textAlign: 'left', padding: '12px 16px', fontSize: '12px', fontWeight: '700', color: '#64748b', textTransform: 'uppercase', letterSpacing: '0.5px' },
    tableRow: { backgroundColor: '#ffffff', transition: 'all 0.2s', borderRadius: '8px', boxShadow: '0 1px 2px rgba(0,0,0,0.05)' },
    td: { padding: '16px', verticalAlign: 'top', borderTop: '1px solid #f1f5f9', borderBottom: '1px solid #f1f5f9' },
    
    // --- EMPLOYEE DATA STYLES ---
    empId: { fontSize: '11px', fontWeight: '700', color: '#6366f1', display: 'block' },
    empNameCell: { fontSize: '15px', fontWeight: '700', color: '#1e293b', display: 'block', marginTop: '2px' },
    empEmailCell: { fontSize: '12px', color: '#94a3b8', display: 'block' },
    skillCloud: { display: 'flex', flexWrap: 'wrap', gap: '6px', maxWidth: '400px' },
    smallSkillTag: { background: '#f1f5f9', color: '#475569', fontSize: '11px', fontWeight: '600', padding: '3px 8px', borderRadius: '4px', border: '1px solid #e2e8f0' },
    statusPill: { padding: '6px 12px', borderRadius: '20px', fontSize: '11px', fontWeight: '700', backgroundColor: '#ecfdf5', color: '#059669', display: 'inline-flex', alignItems: 'center', gap: '4px' },
    ratingStar: { color: '#f59e0b', fontWeight: '700', display: 'flex', alignItems: 'center', gap: '4px', fontSize: '14px' },
    expText: { fontSize: '12px', color: '#64748b', display: 'flex', alignItems: 'center', gap: '4px', marginTop: '4px' }
};
export default ResourcePlannerMatch;