import React, { useEffect, useState, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';

// --- Visual Mapping for Statuses ---
const STATUS_CONFIG = {
    'DRAFT': { color: '#e5e7eb', textColor: '#374151', label: 'Draft' },
    'SUBMITTED': { color: '#fef3c7', textColor: '#92400e', label: 'Correction Required' },
    'PENDING_APPROVAL': { color: '#dbeafe', textColor: '#1e40af', label: 'Pending Approval' },
    'APPROVED': { color: '#d1fae5', textColor: '#065f46', label: 'Approved/Open' },
    'REJECTED': { color: '#fee2e2', textColor: '#991b1b', label: 'Rejected' },
    'ASSIGNED': { color: '#f3e8ff', textColor: '#6b21a8', label: 'Staff Assigned' },
    'CANCELLED': { color: '#111827', textColor: '#ffffff', label: 'Cancelled' },
    'OPEN': { color: '#ecfdf5', textColor: '#047857', label: 'Active' },
    'CLOSED': { color: '#6b7280', textColor: '#ffffff', label: 'Closed' }
};

const ManagerHome = () => {
    const navigate = useNavigate();
    const firstName = localStorage.getItem("firstName");
    const userEmail = localStorage.getItem("email");

    const [requests, setRequests] = useState([]);
    const [selectedRequest, setSelectedRequest] = useState(null); 
    const [isRefreshing, setIsRefreshing] = useState(false);
    const [lastSynced, setLastSynced] = useState(new Date().toLocaleTimeString());

    // --- Fetch Logic (Reflects Camunda/Backend updates) ---
    const fetchRequests = useCallback(async () => {
        if (!userEmail) return;
        setIsRefreshing(true);
        try {
            const res = await axios.get(`http://localhost:8080/api/requests/manager-requests?email=${userEmail}`);
            setRequests(res.data);
            setLastSynced(new Date().toLocaleTimeString());
        } catch (err) {
            console.error("Fetch failed", err);
        } finally {
            setIsRefreshing(false);
        }
    }, [userEmail]);

    useEffect(() => {
        fetchRequests();
        const interval = setInterval(fetchRequests, 30000); // Polling for real-time updates
        return () => clearInterval(interval);
    }, [fetchRequests]);

    return (
        <div style={styles.container}>
            <div style={styles.contentWrapper}>
                
                {/* Welcome Box */}
                <div style={styles.welcomeBox}>
                    <h1 style={{ margin: 0 }}>Hello, {firstName}! 👋</h1>
                    <p style={{ color: '#6b7280', margin: '10px 0 25px 0' }}>Manage your department staffing and track requests in real-time.</p>
                    <button onClick={() => navigate("/create-request")} style={styles.createBtn}>
                         New Staffing Request
                    </button>
                </div>

                {/* Table Section with the Icon-Refresh Style */}
                <div style={styles.listSection}>
                    <div style={styles.listHeader}>
                        <h2 style={{ margin: 0, fontSize: '18px' }}>Your Recent Requests</h2>
                        
                        {/* THE REFRESH UI: Sync time + Icon */}
                        <div style={styles.syncContainer}>
                            <span style={styles.syncText}>Last synced: {lastSynced}</span>
                            <button 
                                onClick={fetchRequests} 
                                style={isRefreshing ? styles.refreshBtnSpin : styles.refreshBtn}
                                disabled={isRefreshing}
                                title="Sync with Camunda"
                            >
                                ↻
                            </button>
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
                            {requests.map((req) => (
                                <tr key={req.id} style={styles.tableRow}>
                                    <td style={styles.td}><strong>{req.title}</strong></td>
                                    <td style={styles.td}>{req.project?.name || 'N/A'}</td>
                                    <td style={styles.td}>
                                        <StatusBadge status={req.status} />
                                    </td>
                                    <td style={styles.td}>
                                        {/* Logic: SUBMITTED shows Manage, Others show ⓘ */}
                                        {req.status === 'SUBMITTED' ? (
                                            <button 
                                                onClick={() => navigate(`/correct-request/${req.id}`)} 
                                                style={styles.manageBtn}
                                            >
                                                Manage
                                            </button>
                                        ) : (
                                            <button 
                                                onClick={() => setSelectedRequest(req)} 
                                                style={styles.infoBtn}
                                            >
                                                ⓘ
                                            </button>
                                        )}
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            </div>

            {/* --- Detail Modal (The Meaningful Data from API) --- */}
            {selectedRequest && (
                <div style={styles.modalOverlay} onClick={() => setSelectedRequest(null)}>
                    <div style={styles.modalContent} onClick={e => e.stopPropagation()}>
                        <div style={styles.modalHeader}>
                            <h3 style={{ margin: 0 }}>Request Overview</h3>
                            <button onClick={() => setSelectedRequest(null)} style={styles.closeBtn}>×</button>
                        </div>
                        <div style={styles.modalBody}>
                            <DetailRow label="ID" value={`#${selectedRequest.id}`} />
                            <DetailRow label="Title" value={selectedRequest.title} />
                            <DetailRow label="Project" value={selectedRequest.project?.name} />
                            <DetailRow label="Location" value={selectedRequest.workLocation} />
                            <hr style={styles.hr} />
                            <DetailRow label="Wage" value={`${selectedRequest.wagePerHour} €/h`} />
                            <DetailRow label="Experience" value={`${selectedRequest.experienceYears} Years`} />
                            <DetailRow label="Skills" value={selectedRequest.requiredSkills?.join(", ")} />
                            <hr style={styles.hr} />
                            <DetailRow label="Dept Head" value={selectedRequest.department?.departmentHead?.email} />
                            <DetailRow label="Process ID" value={selectedRequest.processInstanceKey} />
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

// --- Helpers ---
const StatusBadge = ({ status }) => {
    const config = STATUS_CONFIG[status] || { color: '#f3f4f6', textColor: '#1f2937', label: status };
    return <span style={{ ...styles.badge, backgroundColor: config.color, color: config.textColor }}>{config.label}</span>;
};

const DetailRow = ({ label, value }) => (
    <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '8px' }}>
        <span style={{ color: '#6b7280', fontSize: '13px' }}>{label}:</span>
        <span style={{ color: '#111827', fontSize: '13px', fontWeight: '600' }}>{value || 'N/A'}</span>
    </div>
);

// --- Styles ---
const styles = {
    container: { padding: '50px 20px', backgroundColor: '#f9fafb', minHeight: '100vh', display: 'flex', justifyContent: 'center' },
    contentWrapper: { maxWidth: '950px', width: '100%' },
    welcomeBox: { background: 'white', padding: '35px', borderRadius: '16px', boxShadow: '0 4px 6px rgba(0,0,0,0.03)', textAlign: 'center', marginBottom: '30px' },
    createBtn: { background: '#4f46e5', color: 'white', border: 'none', padding: '12px 24px', borderRadius: '8px', fontWeight: 'bold', cursor: 'pointer' },
    listSection: { background: 'white', padding: '25px', borderRadius: '16px', boxShadow: '0 4px 6px rgba(0,0,0,0.03)' },
    listHeader: { display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' },
    
    // Refresh UI
    syncContainer: { display: 'flex', alignItems: 'center', gap: '10px' },
    syncText: { fontSize: '12px', color: '#9ca3af' },
    refreshBtn: { background: 'none', border: 'none', color: '#4f46e5', fontSize: '20px', cursor: 'pointer', lineHeight: '1' },
    refreshBtnSpin: { background: 'none', border: 'none', color: '#9ca3af', fontSize: '20px', cursor: 'wait', lineHeight: '1', animation: 'spin 1s linear infinite' },

    table: { width: '100%', borderCollapse: 'collapse' },
    tableHeader: { borderBottom: '1px solid #f3f4f6', textAlign: 'left' },
    th: { padding: '12px', color: '#9ca3af', fontSize: '12px', textTransform: 'uppercase', fontWeight: '600' },
    td: { padding: '16px 12px', fontSize: '14px', borderBottom: '1px solid #f9fafb' },
    badge: { padding: '4px 10px', borderRadius: '12px', fontSize: '11px', fontWeight: 'bold', textTransform: 'uppercase' },
    manageBtn: { color: '#4f46e5', background: '#eef2ff', border: 'none', padding: '6px 12px', borderRadius: '6px', fontWeight: 'bold', cursor: 'pointer' },
    infoBtn: { color: '#9ca3af', border: '1px solid #e5e7eb', background: 'none', width: '28px', height: '28px', borderRadius: '50%', cursor: 'pointer' },

    // Modal
    modalOverlay: { position: 'fixed', top: 0, left: 0, width: '100%', height: '100%', backgroundColor: 'rgba(0,0,0,0.4)', display: 'flex', justifyContent: 'center', alignItems: 'center', zIndex: 1000 },
    modalContent: { background: 'white', width: '420px', borderRadius: '16px', padding: '25px', boxShadow: '0 20px 25px rgba(0,0,0,0.1)' },
    modalHeader: { display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px', borderBottom: '1px solid #f3f4f6', paddingBottom: '10px' },
    closeBtn: { background: 'none', border: 'none', fontSize: '20px', cursor: 'pointer', color: '#9ca3af' },
    hr: { border: '0', borderTop: '1px solid #f3f4f6', margin: '15px 0' }
};

export default ManagerHome;