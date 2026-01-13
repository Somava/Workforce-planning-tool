import React, { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';

const Login = () => {
    const [creds, setCreds] = useState({ email: '', password: '' });
    const [showPassword, setShowPassword] = useState(false);
    const [error, setError] = useState('');
    const [isLoading, setIsLoading] = useState(false);
    const navigate = useNavigate();
    const inactivityTimer = useRef(null);

    useEffect(() => {
        document.body.style.margin = "0";
        document.body.style.padding = "0";
        document.body.style.overflow = "hidden";
        return () => { document.body.style.overflow = "auto"; };
    }, []);

    const resetInactivityTimer = () => {
        if (inactivityTimer.current) clearTimeout(inactivityTimer.current);
        inactivityTimer.current = setTimeout(() => {
            setCreds({ email: '', password: '' });
            if (creds.email || creds.password) setError('Form cleared due to 30s inactivity.');
        }, 30000); 
    };

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setCreds(prev => ({ ...prev, [name]: value }));
        resetInactivityTimer();
    };

    const handleLogin = async (e) => {
        e.preventDefault();
        if (inactivityTimer.current) clearTimeout(inactivityTimer.current);
        setError('');
        setIsLoading(true);

        try {
            const res = await fetch("http://localhost:8080/api/auth/login/auto", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(creds)
            });

            if (!res.ok) throw new Error("Invalid credentials or server error.");
            
            const data = await res.json();

            // Clear old session and store new verified data
            localStorage.clear();
            localStorage.setItem("token", data.token);
            localStorage.setItem("role", data.selectedRole);
            localStorage.setItem("firstName", data.firstName);
            localStorage.setItem("lastName", data.lastName);
            localStorage.setItem("employeeHrid", data.employeeHrId);
            
            // CRITICAL: Store the verified email for the Apply API
            localStorage.setItem("email", data.email || creds.email);

            // Role-Based Routing
            const role = data.selectedRole;
            if (role === 'ROLE_DEPT_HEAD') navigate("/approval");
            else if (role === 'ROLE_MANAGER') navigate("/manager-home");
            else if (role === 'ROLE_RESOURCE_PLNR') navigate("/planning");
            else navigate("/employee-dashboard");

        } catch (err) {
            setError(err.message);
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div style={styles.page}>
            <div style={styles.formSection}>
                <div style={styles.formContainer}>
                    <h2 style={styles.greet}>Welcome Back</h2>
                    <p style={styles.subGreet}>Enter your details to manage your profile.</p>
                    
                    {error && <div style={styles.errorBox}>{error}</div>}
                    
                    <form onSubmit={handleLogin} style={styles.form}>
                        <div style={styles.inputGroup}>
                            <label style={styles.label}>Email Address</label>
                            <input 
                                type="email" 
                                name="email" 
                                value={creds.email} 
                                placeholder="email@frauas.de" 
                                style={styles.input} 
                                required 
                                onChange={handleInputChange} 
                            />
                        </div>
                        <div style={styles.inputGroup}>
                            <label style={styles.label}>Password</label>
                            <div style={{ position: 'relative' }}>
                                <input 
                                    type={showPassword ? "text" : "password"} 
                                    name="password" 
                                    value={creds.password} 
                                    placeholder="••••••••" 
                                    style={styles.input} 
                                    required 
                                    onChange={handleInputChange} 
                                />
                                <button 
                                    type="button" 
                                    onClick={() => setShowPassword(!showPassword)} 
                                    style={styles.eyeBtn}
                                >
                                    {showPassword ? "🙈" : "👁️"}
                                </button>
                            </div>
                        </div>
                        <button 
                            type="submit" 
                            style={styles.submitBtn} 
                            disabled={isLoading}
                        >
                            {isLoading ? "Authenticating..." : "Login"}
                        </button>
                    </form>
                </div>
            </div>

            <div style={styles.heroSection}>
                <div style={styles.heroOverlay}>
                    <div style={styles.heroContent}>
                        <h1 style={styles.heroTitle}>Workforce Planning</h1>
                        <p style={styles.heroDesc}>
                            The central hub for Frankfurt UAS employees. Coordinate staffing requests, 
                            manage project assignments, and streamline department approvals.
                        </p>
                        
                        <div style={styles.checkList}>
                            <div style={styles.checkItem}>✅ Real-time Resource Allocation</div>
                            <div style={styles.checkItem}>✅ Automated Approval Workflows</div>
                            <div style={styles.checkItem}>✅ Skills-based Matching</div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

const styles = {
    page: { display: 'flex', height: '100vh', width: '100vw', position: 'fixed', top: 0, left: 0, background: '#fff', margin: 0, padding: 0, fontFamily: "'Inter', sans-serif" },
    formSection: { flex: '1', display: 'flex', alignItems: 'center', justifyContent: 'center', background: '#ffffff' },
    formContainer: { width: '100%', maxWidth: '380px', padding: '20px' },
    greet: { fontSize: '32px', fontWeight: '800', color: '#1e293b', margin: '0 0 8px 0' },
    subGreet: { color: '#64748b', marginBottom: '32px' },
    form: { display: 'flex', flexDirection: 'column', gap: '20px' },
    inputGroup: { display: 'flex', flexDirection: 'column', gap: '8px' },
    label: { fontSize: '14px', fontWeight: '600', color: '#1e293b' },
    input: { width: '100%', padding: '12px 16px', borderRadius: '10px', border: '1.5px solid #e2e8f0', boxSizing: 'border-box', fontSize: '16px' },
    eyeBtn: { position: 'absolute', right: '12px', top: '50%', transform: 'translateY(-50%)', background: 'none', border: 'none', cursor: 'pointer', fontSize: '18px' },
    submitBtn: { padding: '14px', background: '#4f46e5', color: 'white', border: 'none', borderRadius: '10px', fontWeight: 'bold', cursor: 'pointer', fontSize: '16px', transition: 'background 0.2s' },
    errorBox: { background: '#fef2f2', color: '#b91c1c', padding: '12px', borderRadius: '8px', marginBottom: '20px', fontSize: '14px', fontWeight: '500', border: '1px solid #fee2e2' },
    heroSection: { flex: '1.2', background: 'url("https://images.unsplash.com/photo-1522071820081-009f0129c71c?auto=format&fit=crop&w=1200&q=80")', backgroundSize: 'cover', backgroundPosition: 'center' },
    heroOverlay: { height: '100%', width: '100%', background: 'linear-gradient(135deg, rgba(79, 70, 229, 0.9) 0%, rgba(124, 58, 237, 0.9) 100%)', display: 'flex', alignItems: 'center', padding: '60px', boxSizing: 'border-box' },
    heroContent: { color: 'white', maxWidth: '80%' },
    heroTitle: { fontSize: '48px', fontWeight: '850', marginBottom: '24px', lineHeight: '1.1' },
    heroDesc: { fontSize: '18px', color: 'rgba(255,255,255,0.9)', lineHeight: '1.6', marginBottom: '30px' },
    checkList: { display: 'flex', flexDirection: 'column', gap: '12px' },
    checkItem: { fontSize: '16px', fontWeight: '500', display: 'flex', alignItems: 'center', gap: '10px' }
};

export default Login;