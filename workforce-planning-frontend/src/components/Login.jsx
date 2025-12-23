import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { LogIn, Shield, Users, Briefcase, UserCheck } from 'lucide-react';

const Login = () => {
  const navigate = useNavigate();
  const [selectedRole, setSelectedRole] = useState('');

  const roles = [
    { id: 'manager', label: 'Project Manager', icon: <Briefcase />, path: '/create-request' },
    { id: 'dept_head', label: 'Department Head', icon: <Shield />, path: '/approval' },
    { id: 'planner', label: 'Resource Planner', icon: <UserCheck />, path: '/planner' },
    { id: 'employee', label: 'Employee', icon: <Users />, path: '/employee' },
  ];

  const handleLogin = (e) => {
    e.preventDefault();
    if (selectedRole) {
      const rolePath = roles.find(r => r.id === selectedRole).path;
      navigate(rolePath);
    }
  };

  return (
    <div style={styles.container}>
      <div style={styles.loginCard}>
        <div style={styles.header}>
          <LogIn size={40} color="#4f46e5" />
          <h2>Workforce Planning Tool</h2>
          <p>Select your role to enter the dashboard</p>
        </div>

        <form onSubmit={handleLogin}>
          <div style={styles.roleGrid}>
            {roles.map((role) => (
              <div 
                key={role.id}
                onClick={() => setSelectedRole(role.id)}
                style={{
                  ...styles.roleCard,
                  borderColor: selectedRole === role.id ? '#4f46e5' : '#e2e8f0',
                  backgroundColor: selectedRole === role.id ? '#f5f3ff' : 'white'
                }}
              >
                {role.icon}
                <span>{role.label}</span>
              </div>
            ))}
          </div>

          <button 
            type="submit" 
            disabled={!selectedRole}
            style={{
              ...styles.loginBtn,
              backgroundColor: selectedRole ? '#4f46e5' : '#94a3b8'
            }}
          >
            Enter System
          </button>
        </form>
      </div>
    </div>
  );
};

const styles = {
  container: { height: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center', background: '#f8fafc' },
  loginCard: { background: 'white', padding: '40px', borderRadius: '20px', boxShadow: '0 20px 25px -5px rgba(0,0,0,0.1)', width: '450px' },
  header: { textAlign: 'center', marginBottom: '30px' },
  roleGrid: { display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '15px', marginBottom: '30px' },
  roleCard: { display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '10px', padding: '20px', border: '2px solid', borderRadius: '12px', cursor: 'pointer', transition: '0.2s' },
  loginBtn: { width: '100%', padding: '15px', color: 'white', border: 'none', borderRadius: '8px', fontWeight: 'bold', cursor: 'pointer' }
};

export default Login;