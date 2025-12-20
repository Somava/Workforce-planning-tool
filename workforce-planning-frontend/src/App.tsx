import React from 'react';
import { BrowserRouter as Router, Routes, Route, Link, Navigate } from 'react-router-dom';
import StaffingRequest from './components/StaffingRequest';
import DeptHeadApproval from './components/DeptHeadApproval';
import EmployeeDashboard from './components/EmployeeDashboard'; // 1. Import the new dashboard

// --- Simple Mock Page Components ---
const DashboardPage = () => (
  <div style={styles.page}>
    <h1>🏠 Workforce Dashboard</h1>
    <p>Welcome to the central hub. All modules are currently unlocked for development.</p>
  </div>
);

const ReportsPage = () => (
  <div style={styles.page}>
    <h1>📊 Reports & Analytics</h1>
    <p>Visualizing resource utilization and gap analysis.</p>
  </div>
);

const ManagerPage = () => (
  <div style={styles.page}>
    <h1>💼 Managerial Oversight</h1>
    <p>Review team capacity and pending requests.</p>
  </div>
);

const PlannerPage = () => (
  <div style={styles.page}>
    <h1>🗓️ Resource Planning</h1>
    <p>Match employees to project requests based on skills.</p>
  </div>
);

// --- Navigation Component ---
const Navigation = () => {
  return (
    <nav style={styles.nav}>
      <div style={styles.navLinks}>
        <Link to="/dashboard" style={styles.link}>Home</Link>
        <Link to="/manager" style={styles.link}>Manager</Link>
        <Link to="/planner" style={styles.link}>Planner</Link>
        <Link to="/approval" style={styles.link}>Approval Inbox</Link>
        
        {/* 2. Added link for the Employee Dashboard */}
        <Link to="/employee" style={styles.link}>My Career</Link>
        
        <Link to="/reports" style={styles.link}>Reports</Link>
        <Link to="/create-request" style={styles.specialLink}>➕ New Request</Link>
      </div>
      <div style={styles.userBadge}>
        <span>👤 Dev Mode: <strong>Super User</strong></span>
      </div>
    </nav>
  );
};

// --- Main App Component ---
const App = () => {
  return (
    <Router>
      <div style={{ fontFamily: '"Segoe UI", Tahoma, Geneva, Verdana, sans-serif' }}>
        <Navigation />
        
        <div style={styles.content}>
          <Routes>
            <Route path="/" element={<Navigate to="/dashboard" replace />} />
            
            <Route path="/dashboard" element={<DashboardPage />} />
            <Route path="/manager" element={<ManagerPage />} />
            <Route path="/planner" element={<PlannerPage />} />
            <Route path="/approval" element={<DeptHeadApproval />} />
            
            {/* 3. Added Route for Employee Search & Apply */}
            <Route path="/employee" element={<EmployeeDashboard />} />
            
            <Route path="/create-request" element={<StaffingRequest />} />
            <Route path="/reports" element={<ReportsPage />} />
            <Route path="*" element={<h1>404 - Page Not Found</h1>} />
          </Routes>
        </div>
      </div>
    </Router>
  );
};

// --- Styles (Same as before) ---
const styles = {
  nav: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    padding: '0 2rem',
    height: '70px',
    background: '#1e293b',
    color: 'white',
    boxShadow: '0 2px 10px rgba(0,0,0,0.1)'
  },
  navLinks: { display: 'flex', gap: '25px', alignItems: 'center' },
  link: { color: '#cbd5e1', textDecoration: 'none', fontSize: '15px', fontWeight: '500' },
  specialLink: { 
    background: '#4f46e5', 
    padding: '8px 16px', 
    borderRadius: '8px', 
    color: 'white', 
    textDecoration: 'none',
    fontWeight: 'bold'
  },
  userBadge: { fontSize: '14px', color: '#94a3b8' },
  content: { padding: '40px', maxWidth: '1200px', margin: '0 auto' },
  page: { background: 'white', padding: '30px', borderRadius: '12px', boxShadow: '0 4px 6px -1px rgba(0,0,0,0.1)' }
};

export default App;