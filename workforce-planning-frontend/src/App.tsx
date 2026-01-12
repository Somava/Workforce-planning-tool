import React from 'react';
import { BrowserRouter as Router, Routes, Route, Link, useLocation, Navigate } from 'react-router-dom';
import Login from './components/Login';
import DeptHeadApproval from './components/DeptHeadApproval';
import EmployeeDashboard from './components/EmployeeDashboard';
import ResourcePlannerMatch from './components/ResourcePlannerMatch';
import StaffingRequest from './components/StaffingRequest';
import ManagerHome from './components/ManagerHome';

const Navigation = () => {
    const location = useLocation();
    const role = localStorage.getItem("role") || "";
    const firstName = localStorage.getItem("firstName") || "";
    const lastName = localStorage.getItem("lastName") || "";
    const email = localStorage.getItem("email") || "";
    const hrid = localStorage.getItem("employeeHrid") || "undefined";

    if (["/", "/login"].includes(location.pathname)) return null;

    return (
        <nav style={styles.nav}>
            <div style={styles.navLinks}>
                {role === 'ROLE_RESOURCE_PLNR' && <Link to="/planning" style={styles.link}>Planning</Link>}
                {role === 'ROLE_DEPT_HEAD' && <Link to="/approval" style={styles.link}>Approvals</Link>}
                {role === 'ROLE_MANAGER' && <Link to="/manager-home" style={styles.link}>Manager Home</Link>}
                {role === 'ROLE_EMPLOYEE' && <Link to="/employee-dashboard" style={styles.link}>Dashboard</Link>}
            </div>
            
            <div style={styles.userSection}>
                <div style={styles.userInfo}>
                    <div style={styles.userMainRow}>
                        <span style={styles.userName}>{firstName} {lastName}</span>
                        <span style={styles.roleBadge}>{role.replace('ROLE_', '').replace('_', ' ')}</span>
                    </div>
                    <div style={styles.userSubRow}>
                        <span>{email}</span>
                        <span style={styles.divider}>|</span>
                        <span style={styles.idTag}>ID: {hrid}</span>
                    </div>
                </div>
                <button style={styles.logout} onClick={() => { localStorage.clear(); window.location.href="/"; }}>Logout</button>
            </div>
        </nav>
    );
};

const App = () => {
    return (
        <Router>
            <div style={{ minHeight: '100vh', background: '#f8fafc', margin: 0, padding: 0 }}>
                <Navigation />
                <Routes>
                    <Route path="/" element={<Login />} />
                    <Route path="/login" element={<Login />} />
                    
                    {/* Components from image_5e2ee7.png */}
                    <Route path="/planning" element={<ResourcePlannerMatch />} />
                    <Route path="/approval" element={<DeptHeadApproval />} />
                    <Route path="/employee-dashboard" element={<EmployeeDashboard />} />
                    
                    {/* Manager Logic */}
                    <Route path="/manager-home" element={<ManagerHome />} />
                    <Route path="/create-request" element={<StaffingRequest />} />

                    <Route path="*" element={<Navigate to="/" />} />
                </Routes>
            </div>
        </Router>
    );
};

const styles: { [key: string]: React.CSSProperties } = {
    nav: { display: 'flex', justifyContent: 'space-between', padding: '0 40px', height: '80px', background: '#0f172a', color: 'white', alignItems: 'center' },
    navLinks: { display: 'flex', gap: '30px' },
    link: { color: 'white', textDecoration: 'none', fontWeight: 'bold' },
    userSection: { display: 'flex', alignItems: 'center', gap: '20px' },
    userInfo: { textAlign: 'right' },
    userMainRow: { display: 'flex', gap: '10px', alignItems: 'center', justifyContent: 'flex-end' },
    userName: { fontWeight: 'bold', fontSize: '18px' },
    roleBadge: { background: '#1e293b', color: '#60a5fa', padding: '2px 10px', borderRadius: '15px', fontSize: '12px', border: '1px solid #334155', textTransform: 'capitalize' },
    userSubRow: { fontSize: '13px', color: '#94a3b8', marginTop: '4px' },
    divider: { margin: '0 8px', color: '#334155' },
    idTag: { color: 'white', fontWeight: 'bold' },
    logout: { background: '#ef4444', color: 'white', border: 'none', padding: '8px 15px', borderRadius: '6px', cursor: 'pointer', fontWeight: 'bold' }
};

export default App;
