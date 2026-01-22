import React, { useEffect, useState, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';

// --- Comprehensive Visual Mapping for all RequestStatus Enums ---
const STATUS_CONFIG = {
    'DRAFT': { color: '#f3f4f6', textColor: '#374151', label: 'Draft' },
    'SUBMITTED': { color: '#e0f2fe', textColor: '#0369a1', label: 'Correction Required' },
    'PM_RESUBMITTED': { color: '#e0e7ff', textColor: '#4338ca', label: 'PM Resubmitted' },
    'PM_CANCELLED': { color: '#111827', textColor: '#ffffff', label: 'PM Cancelled' },
    'PENDING_APPROVAL': { color: '#fef3c7', textColor: '#92400e', label: 'Pending Approval' },
    'REQUEST_REJECTED': { color: '#fee2e2', textColor: '#991b1b', label: 'Dept Head Rejected' },
    'APPROVED': { color: '#d1fae5', textColor: '#065f46', label: 'Approved/Open' },
    'REJECTED': { color: '#fee2e2', textColor: '#b91c1c', label: 'Rejected' },
    'EMPLOYEE_RESERVED': { color: '#fae8ff', textColor: '#86198f', label: 'Employee Reserved' },
    'INT_EMPLOYEE_APPROVED_BY_DH': { color: '#ecfdf5', textColor: '#047857', label: 'Int. Approved' },
    'INT_EMPLOYEE_REJECTED_BY_DH': { color: '#fff1f2', textColor: '#be123c', label: 'Int. DH Rejected' },
    'INT_EMPLOYEE_REJECTED_BY_EMP': { color: '#fff1f2', textColor: '#be123c', label: 'Emp. Declined' },
    'INT_EMPLOYEE_ASSIGNED': { color: '#dcfce7', textColor: '#166534', label: 'Int. Assigned' },
    'EXTERNAL_SEARCH_TRIGGERED': { color: '#eff6ff', textColor: '#1d4ed8', label: 'External Search' },
    'EXTERNAL_RESPONSE_RECEIVED': { color: '#f0f9ff', textColor: '#075985', label: 'Ext. Response' },
    'EXT_EMPLOYEE_REJECTED_BY_DH': { color: '#fef2f2', textColor: '#991b1b', label: 'Ext. DH Rejected' },
    'NO_EXT_EMPLOYEE_FOUND': { color: '#f9fafb', textColor: '#4b5563', label: 'No Ext. Found' },
    'ASSIGNED': { color: '#f3e8ff', textColor: '#6b21a8', label: 'Staff Assigned' },
    'CANCELLED': { color: '#111827', textColor: '#ffffff', label: 'Cancelled' },
    'OPEN': { color: '#ecfdf5', textColor: '#047857', label: 'Active' },
    'CLOSED': { color: '#6b7280', textColor: '#ffffff', label: 'Closed' }
};

const ManagerHome = () => {
    const navigate = useNavigate();
    const firstName = localStorage.getItem("firstName");
    const userEmail = localStorage.getItem("email");

    const [activeTab, setActiveTab] = useState('recent'); // 'recent' or 'rejected'
    const [requests, setRequests] = useState([]);
    const [rejectedRequests, setRejectedRequests] = useState([]);
    const [selectedRequest, setSelectedRequest] = useState(null); 
    const [isRefreshing, setIsRefreshing] = useState(false);
    const [lastSynced, setLastSynced] = useState(new Date().toLocaleTimeString());

    // Resubmit Modal States
    const [resubmitModal, setResubmitModal] = useState(null);
    const [editData, setEditData] = useState({});
    const [error, setError] = useState("");

    const fetchRequests = useCallback(async () => {
        if (!userEmail) return;
        setIsRefreshing(true);
        try {
            const [recentRes, rejectedRes] = await Promise.all([
                axios.get(`http://localhost:8080/api/requests/manager-requests?email=${userEmail}`),
                axios.get(`http://localhost:8080/api/manager/manager/rejected-requests?email=${userEmail}`)
            ]);
            setRequests(recentRes.data);
            setRejectedRequests(rejectedRes.data);
            setLastSynced(new Date().toLocaleTimeString());
        } catch (err) {
            console.error("Fetch failed", err);
        } finally {
            setIsRefreshing(false);
        }
    }, [userEmail]);

    useEffect(() => {
        fetchRequests();
        const interval = setInterval(fetchRequests, 30000);
        return () => clearInterval(interval);
    }, [fetchRequests]);

    const validateResubmission = (data, originalReq) => {
        const wage = parseFloat(data.wagePerHour);
        const experience = parseInt(data.experienceYears);
        
        let diffInDays = 100; 
        if(originalReq.projectStartDate && originalReq.projectEndDate) {
            const start = new Date(originalReq.projectStartDate);
            const end = new Date(originalReq.projectEndDate);
            const diffInMs = end - start;
            diffInDays = diffInMs / (1000 * 60 * 60 * 24);
        }

        if (!data.title || !data.description) return "Position title and description are required.";
        if (diffInDays < 60) return "Project duration must be at least 2 months (60 days).";
        if (isNaN(experience) || experience < 1 || experience > 25) return "Experience must be between 1 and 25 years.";
        if (isNaN(wage) || wage < 1 || wage > 40) return "Wage must be between 1.00 and 40.00 €.";
        if (!data.workLocation) return "Please select a work location.";
        
        return null;
    };

    const handleDecision = async (id, isResubmit, originalReq = null) => {
        setError("");
        let payload = {};

        if (isResubmit) {
            const validationError = validateResubmission(editData, originalReq);
            if (validationError) {
                setError(validationError);
                return;
            }

            payload = {
                title: editData.title,
                description: editData.description,
                requiredSkills: typeof editData.requiredSkills === 'string' 
                    ? editData.requiredSkills.split(',').map(s => s.trim()).filter(s => s !== "")
                    : editData.requiredSkills,
                experienceYears: parseInt(editData.experienceYears),
                wagePerHour: parseFloat(editData.wagePerHour),
                workLocation: editData.workLocation,
                availabilityHoursPerWeek: parseInt(editData.availabilityHoursPerWeek)
            };
        }

        try {
            const url = `http://localhost:8080/api/manager/staffing-request/review-decision?requestId=${id}&email=${userEmail}&isResubmit=${isResubmit}`;
            await axios.post(url, payload);
            setResubmitModal(null);
            fetchRequests();
        } catch (err) {
            setError("Communication with server failed. Please try again.");
        }
    };

    const openResubmitModal = (req) => {
        setError("");
        setEditData({
            title: req.title,
            description: req.description,
            requiredSkills: req.requiredSkills?.join(', ') || "",
            experienceYears: req.experienceYears,
            wagePerHour: req.wagePerHour,
            workLocation: req.workLocation || "Remote",
            availabilityHoursPerWeek: req.availabilityHoursPerWeek || "40"
        });
        setResubmitModal(req);
    };

    return (
        <div style={styles.container}>
            <div style={styles.contentWrapper}>
                
                <div style={styles.welcomeBox}>
                    <h1 style={{ margin: 0 }}>Hello, {firstName}! 👋</h1>
                    <p style={{ color: '#6b7280', margin: '10px 0 25px 0' }}>Manage your department staffing and track requests in real-time.</p>
                    <button onClick={() => navigate("/create-request")} style={styles.createBtn}>
                         New Staffing Request
                    </button>
                </div>

                <div style={styles.tabBar}>
                    <button 
                        style={{...styles.tabItem, ...(activeTab === 'recent' ? styles.activeTab : {})}}
                        onClick={() => setActiveTab('recent')}
                    >
                        All Requests ({requests.length})
                    </button>
                    <button 
                        style={{...styles.tabItem, ...(activeTab === 'rejected' ? styles.activeTab : {})}}
                        onClick={() => setActiveTab('rejected')}
                    >
                        Rejected Requests ({rejectedRequests.length})
                    </button>
                </div>

                <div style={styles.listSection}>
                    <div style={styles.listHeader}>
                        <h2 style={{ margin: 0, fontSize: '18px' }}>
                            {activeTab === 'recent' ? 'Recent Activity' : 'Action Required '}
                        </h2>
                        <div style={styles.syncContainer}>
                            <span style={styles.syncText}>Last synced: {lastSynced}</span>
                            <button 
                                onClick={fetchRequests} 
                                style={isRefreshing ? styles.refreshBtnSpin : styles.refreshBtn}
                                disabled={isRefreshing}
                            >↻</button>
                        </div>
                    </div>

                    <table style={styles.table}>
                        <thead>
                            <tr style={styles.tableHeader}>
                                <th style={styles.th}>Position Title</th>
                                <th style={styles.th}>Project</th>
                                <th style={styles.th}>Status</th>
                                <th style={styles.th}>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            {(activeTab === 'recent' ? requests : rejectedRequests).map((req) => (
                                <tr key={req.requestId} style={styles.tableRow}>
                                    <td style={styles.td}>
                                        <strong>{req.title}</strong>
                                        {activeTab === 'rejected' && <div style={styles.reasonText}>Reason: {req.rejectionReason}</div>}
                                    </td>
                                    <td style={styles.td}>{req.project?.name || req.projectName || 'N/A'}</td>
                                    <td style={styles.td}>
                                        <StatusBadge status={req.status} />
                                    </td>
                                    <td style={styles.td}>
                                        <div style={{display: 'flex', gap: '8px'}}>
                                            <button onClick={() => setSelectedRequest(req)} style={styles.infoBtn}>ⓘ</button>
                                            {activeTab === 'rejected' && (
                                                <>
                                                    <button onClick={() => openResubmitModal(req)} style={styles.resubmitBtn}>Resubmit</button>
                                                    <button onClick={() => handleDecision(req.requestId, false)} style={styles.cancelBtn}>Cancel</button>
                                                </>
                                            )}
                                        </div>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            </div>

            {selectedRequest && (
                <div style={styles.modalOverlay} onClick={() => setSelectedRequest(null)}>
                    <div style={{...styles.modalContent, width: '450px'}} onClick={e => e.stopPropagation()}>
                        <div style={styles.modalHeader}>
                            <h3 style={{ margin: 0 }}>Request Overview</h3>
                            <button onClick={() => setSelectedRequest(null)} style={styles.closeBtn}>×</button>
                        </div>
                        <div style={styles.modalBody}>
                            <DetailRow label="Request ID" value={`${selectedRequest.requestId}`} />
                            <DetailRow label="Title" value={selectedRequest.title} />
                            <DetailRow label="Project" value={selectedRequest.project?.name || selectedRequest.projectName} />
                            <DetailRow label="Status" value={selectedRequest.status} />
                            <DetailRow label="Location" value={selectedRequest.workLocation} />
                            <DetailRow label="Experience" value={`${selectedRequest.experienceYears} Years`} />
                            <DetailRow label="Hours/Week" value={selectedRequest.availabilityHoursPerWeek} />
                            <DetailRow label="Wage Budget" value={selectedRequest.wagePerHour ? `${Number(selectedRequest.wagePerHour).toFixed(2)} €/h` : 'N/A'} />

                            <hr style={styles.hr} />
                            <div style={{fontSize: '12px', color: '#6b7280', marginBottom: '4px'}}>Required Skills:</div>
                            <div style={{fontSize: '13px', fontWeight: '600'}}>{selectedRequest.requiredSkills?.join(", ")}</div>
                        </div>
                    </div>
                </div>
            )}

            {resubmitModal && (
                <div style={styles.modalOverlay}>
                    <div style={{...styles.modalContent, width: '550px'}}>
                        <div style={styles.modalHeader}>
                            <h3 style={{ margin: 0 }}>Edit & Resubmit Request</h3>
                            <button onClick={() => setResubmitModal(null)} style={styles.closeBtn}>×</button>
                        </div>
                        {error && <div style={styles.errorBanner}>{error}</div>}
                        <div style={{...styles.modalBody, display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '15px'}}>
                            <div style={{gridColumn: 'span 2', ...styles.inputGroup}}>
                                <label style={styles.label}>Position Title</label>
                                <input style={styles.input} value={editData.title} onChange={(e) => setEditData({...editData, title: e.target.value})} />
                            </div>
                            <div style={{gridColumn: 'span 2', ...styles.inputGroup}}>
                                <label style={styles.label}>Job Description</label>
                                <textarea style={{...styles.input, height: '70px'}} value={editData.description} onChange={(e) => setEditData({...editData, description: e.target.value})} />
                            </div>
                            <div style={styles.inputGroup}>
                                <label style={styles.label}>Exp. Years (1-25)</label>
                                <input 
                                    type="number" 
                                    min="1" 
                                    max="25" 
                                    style={styles.input} 
                                    value={editData.experienceYears} 
                                    onChange={(e) => setEditData({...editData, experienceYears: e.target.value})} 
                                />
                            </div>
                            <div style={styles.inputGroup}>
                                <label style={styles.label}>Wage (€/h, 1-40)</label>
                                <input 
                                    type="number" 
                                    min="1" 
                                    max="40" 
                                    step="0.01" 
                                    style={styles.input} 
                                    value={editData.wagePerHour} 
                                    onChange={(e) => setEditData({...editData, wagePerHour: e.target.value})} 
                                />
                            </div>
                            <div style={styles.inputGroup}>
                                <label style={styles.label}>Work Location</label>
                                <select style={styles.input} value={editData.workLocation} onChange={(e) => setEditData({...editData, workLocation: e.target.value})}>
                                    <option value="Remote">Remote</option>
                                    <option value="onsite">Onsite</option>
                                </select>
                            </div>
                            <div style={styles.inputGroup}>
                                <label style={styles.label}>Hrs/Week</label>
                                <select style={styles.input} value={editData.availabilityHoursPerWeek} onChange={(e) => setEditData({...editData, availabilityHoursPerWeek: e.target.value})}>
                                    <option value="40">40 (Full Time)</option>
                                    <option value="20">20 (Part Time)</option>
                                </select>
                            </div>
                            <div style={{gridColumn: 'span 2', ...styles.inputGroup}}>
                                <label style={styles.label}>Skills (Comma Separated)</label>
                                <input style={styles.input} value={editData.requiredSkills} onChange={(e) => setEditData({...editData, requiredSkills: e.target.value})} />
                            </div>
                        </div>
                        <div style={{display: 'flex', gap: '10px', marginTop: '20px'}}>
                            <button onClick={() => handleDecision(resubmitModal.requestId, true, resubmitModal)} style={{...styles.createBtn, flex: 1}}>Confirm Resubmission</button>
                            <button onClick={() => setResubmitModal(null)} style={{...styles.cancelBtn, flex: 1, border: '1px solid #e5e7eb', background: '#fff', color: '#6b7280'}}>Dismiss</button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

const StatusBadge = ({ status }) => {
    const config = STATUS_CONFIG[status] || { color: '#f3f4f6', textColor: '#1f2937', label: status?.replace(/_/g, ' ') };
    return <span style={{ ...styles.badge, backgroundColor: config.color, color: config.textColor }}>{config.label}</span>;
};

const DetailRow = ({ label, value }) => (
    <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '8px' }}>
        <span style={{ color: '#6b7280', fontSize: '13px' }}>{label}:</span>
        <span style={{ color: '#111827', fontSize: '13px', fontWeight: '600' }}>{value || 'N/A'}</span>
    </div>
);

const styles = {
    container: { padding: '50px 20px', backgroundColor: '#f9fafb', minHeight: '100vh', display: 'flex', justifyContent: 'center' },
    contentWrapper: { maxWidth: '1000px', width: '100%' },
    welcomeBox: { background: 'white', padding: '35px', borderRadius: '16px', boxShadow: '0 4px 6px rgba(0,0,0,0.03)', textAlign: 'center', marginBottom: '30px' },
    createBtn: { background: '#4f46e5', color: 'white', border: 'none', padding: '12px 24px', borderRadius: '8px', fontWeight: 'bold', cursor: 'pointer' },
    tabBar: { display: 'flex', gap: '20px', marginBottom: '20px', borderBottom: '1px solid #e5e7eb' },
    tabItem: { padding: '10px 20px', border: 'none', background: 'none', cursor: 'pointer', color: '#6b7280', fontWeight: '600', transition: '0.2s' },
    activeTab: { color: '#4f46e5', borderBottom: '2px solid #4f46e5' },
    listSection: { background: 'white', padding: '25px', borderRadius: '16px', boxShadow: '0 4px 6px rgba(0,0,0,0.03)' },
    listHeader: { display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' },
    syncContainer: { display: 'flex', alignItems: 'center', gap: '10px' },
    syncText: { fontSize: '12px', color: '#9ca3af' },
    refreshBtn: { background: 'none', border: 'none', color: '#4f46e5', fontSize: '20px', cursor: 'pointer' },
    refreshBtnSpin: { background: 'none', border: 'none', color: '#9ca3af', fontSize: '20px', animation: 'spin 1s linear infinite' },
    table: { width: '100%', borderCollapse: 'collapse' },
    tableHeader: { borderBottom: '1px solid #f3f4f6', textAlign: 'left' },
    th: { padding: '12px', color: '#9ca3af', fontSize: '12px', textTransform: 'uppercase', fontWeight: '600' },
    td: { padding: '16px 12px', fontSize: '14px', borderBottom: '1px solid #f9fafb' },
    badge: { padding: '4px 10px', borderRadius: '12px', fontSize: '10px', fontWeight: 'bold', textTransform: 'uppercase', whiteSpace: 'nowrap' },
    infoBtn: { color: '#9ca3af', border: '1px solid #e5e7eb', background: 'none', width: '28px', height: '28px', borderRadius: '50%', cursor: 'pointer' },
    resubmitBtn: { background: '#ecfdf5', color: '#059669', border: '1px solid #bbf7d0', padding: '4px 12px', borderRadius: '6px', fontSize: '12px', fontWeight: 'bold', cursor: 'pointer' },
    cancelBtn: { background: '#fff1f2', color: '#e11d48', border: '1px solid #fecaca', padding: '4px 12px', borderRadius: '6px', fontSize: '12px', fontWeight: 'bold', cursor: 'pointer' },
    reasonText: { fontSize: '11px', color: '#ef4444', marginTop: '4px', fontStyle: 'italic' },
    modalOverlay: { position: 'fixed', top: 0, left: 0, width: '100%', height: '100%', backgroundColor: 'rgba(0,0,0,0.4)', display: 'flex', justifyContent: 'center', alignItems: 'center', zIndex: 1000 },
    modalContent: { background: 'white', borderRadius: '16px', padding: '25px', boxShadow: '0 20px 25px rgba(0,0,0,0.1)' },
    modalHeader: { display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px', borderBottom: '1px solid #f3f4f6', paddingBottom: '10px' },
    closeBtn: { background: 'none', border: 'none', fontSize: '20px', cursor: 'pointer', color: '#9ca3af' },
    hr: { border: '0', borderTop: '1px solid #f3f4f6', margin: '15px 0' },
    inputGroup: { display: 'flex', flexDirection: 'column', gap: '4px' },
    label: { fontSize: '12px', fontWeight: 'bold', color: '#4b5563' },
    input: { padding: '10px', borderRadius: '8px', border: '1px solid #d1d5db', fontSize: '14px', outline: 'none' },
    errorBanner: { background: '#fee2e2', color: '#b91c1c', padding: '10px', borderRadius: '8px', fontSize: '12px', marginBottom: '15px', border: '1px solid #fecaca' }
};

export default ManagerHome;