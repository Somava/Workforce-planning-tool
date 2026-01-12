import React from 'react';
import { useNavigate } from 'react-router-dom';

const ManagerHome = () => {
    const navigate = useNavigate();
    const firstName = localStorage.getItem("firstName");

    return (
        <div style={styles.container}>
            <div style={styles.welcomeBox}>
                <h1>Hello, {firstName}!</h1>
                <p>Welcome to your Management Dashboard. From here, you can initiate new staffing requests for your department.</p>
                
                <button 
                    onClick={() => navigate("/create-request")} 
                    style={styles.createBtn}
                >
                    + Create New Staffing Request
                </button>
            </div>
        </div>
    );
};

const styles = {
    container: { padding: '50px', display: 'flex', justifyContent: 'center' },
    welcomeBox: { background: 'white', padding: '40px', borderRadius: '12px', boxShadow: '0 4px 6px rgba(0,0,0,0.05)', maxWidth: '600px', textAlign: 'center' },
    createBtn: { marginTop: '20px', padding: '15px 30px', background: '#4f46e5', color: 'white', border: 'none', borderRadius: '8px', fontWeight: 'bold', cursor: 'pointer', fontSize: '16px' }
};

export default ManagerHome;