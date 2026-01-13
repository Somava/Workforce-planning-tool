import React, { useState, useEffect } from 'react';

const DeptHeadDashboard = () => {
    const [requests, setRequests] = useState([]);
    const [message, setMessage] = useState({ text: '', type: '' });
    
    const deptId = localStorage.getItem("deptId"); 
    const headName = localStorage.getItem("firstName") || "Head";

    useEffect(() => {
        fetchDeptRequests();
    }, []);

    const fetchDeptRequests = async () => {
        try {
            // Fetching requests filtered by this Head's Department
            const response = await fetch(`http://localhost:8080/api/staffing-requests/department/${deptId}`);
            const data = await response.json();
            // Filter out requests that are already finalized if you want to focus on "Pending"
            setRequests(data.filter(r => r.status === 'PENDING' || r.status === 'SUBMITTED'));
        } catch (err) {
            console.error("Error fetching requests:", err);
        }
    };

    const handleAction = async (requestId, newStatus) => {
        try {
            // API call to update the status of the request
            const response = await fetch(`http://localhost:8080/api/staffing-requests/${requestId}/status?status=${newStatus}`, {
                method: 'PUT'
            });

            if (response.ok) {
                setMessage({ 
                    text: `Request ${requestId} has been ${newStatus.toLowerCase()}.`, 
                    type: newStatus === 'APPROVED' ? 'success' : 'error' 
                });
                // Remove from the local pending list
                setRequests(requests.filter(r => r.requestId !== requestId));
            }
        } catch (err) {
            setMessage({ text: "Failed to update request status.", type: 'error' });
        }
    };

    return (
        <div style={styles.container}>
            <header style={styles.header}>
                <div>
                    <h1 style={styles.title}>Department Head Approval</h1>
                    <p style={styles.subtitle}>Reviewing staffing budget for Dept #{deptId}</p>
                </div>
                <div style={styles.badge}>Dept Head: {headName}</div>
            </header>

            {message.text && (
                <div style={{ ...styles.alert, backgroundColor: message.type === 'success' ? '#dcfce7' : '#fef2f2', color: message.type === 'success' ? '#166534' : '#991b1b' }}>
                    {message.text}
                </div>
            )}

            <div style={styles.tableCard}>
                <table style={styles.table}>
                    <thead>
                        <tr>
                            <th style={styles.th}>Req ID</th>
                            <th style={styles.th}>Role Title</th>
                            <th style={styles.th}>Project</th>
                            <th style={styles.th}>Experience</th>
                            <th style={styles.th}>Hourly Wage</th>
                            <th style={styles.th}>Weekly Load</th>
                            <th style={styles.th}>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        {requests.map(req => (
                            <tr key={req.requestId} style={styles.tr}>
                                <td style={styles.td}><strong>#{req.requestId}</strong></td>
                                <td style={styles.td}>{req.title}</td>
                                <td style={styles.td}>Project {req.projectId}</td>
                                <td style={styles.td}>{req.experienceYears}y</td>
                                <td style={styles.td}>€{req.wagePerHour}/hr</td>
                                <td style={styles.td}>{req.availabilityHoursPerWeek}h</td>
                                <td style={styles.td}>
                                    <div style={styles.actionGroup}>
                                        <button 
                                            onClick={() => handleAction(req.requestId, 'APPROVED')} 
                                            style={styles.approveBtn}
                                        >
                                            Approve
                                        </button>
                                        <button 
                                            onClick={() => handleAction(req.requestId, 'REJECTED')} 
                                            style={styles.rejectBtn}
                                        >
                                            Reject
                                        </button>
                                    </div>
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
                {requests.length === 0 && (
                    <div style={styles.empty}>No pending staffing requests for your department.</div>
                )}
            </div>
        </div>
    );
};

const styles = {
    container: { padding: '40px', maxWidth: '1200px', margin: '0 auto', fontFamily: 'Inter, sans-serif' },
    header: { display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '30px' },
    title: { fontSize: '28px', fontWeight: '850', color: '#0f172a', margin: 0 },
    subtitle: { color: '#64748b', fontSize: '16px' },
    badge: { padding: '8px 16px', background: '#f1f5f9', borderRadius: '20px', fontWeight: '600', color: '#475569' },
    
    tableCard: { background: 'white', borderRadius: '16px', border: '1px solid #e2e8f0', boxShadow: '0 4px 6px rgba(0,0,0,0.05)', overflow: 'hidden' },
    table: { width: '100%', borderCollapse: 'collapse' },
    th: { textAlign: 'left', padding: '16px', background: '#f8fafc', color: '#64748b', fontWeight: '700', fontSize: '13px', textTransform: 'uppercase' },
    td: { padding: '16px', borderBottom: '1px solid #f1f5f9', color: '#1e293b' },
    
    actionGroup: { display: 'flex', gap: '10px' },
    approveBtn: { padding: '8px 16px', background: '#10b981', color: 'white', border: 'none', borderRadius: '8px', fontWeight: '700', cursor: 'pointer' },
    rejectBtn: { padding: '8px 16px', background: '#ef4444', color: 'white', border: 'none', borderRadius: '8px', fontWeight: '700', cursor: 'pointer' },
    
    alert: { padding: '15px', borderRadius: '10px', marginBottom: '20px', textAlign: 'center', fontWeight: '600' },
    empty: { padding: '60px', textAlign: 'center', color: '#94a3b8', fontStyle: 'italic' }
};

export default DeptHeadDashboard;