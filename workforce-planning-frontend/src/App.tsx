import { useState, useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Link } from 'react-router-dom';

// Simple Page Components
const DashboardPage = () => <h1>Welcome to the Workforce Planning Dashboard!</h1>;
const ReportsPage = () => <h1>Reports and Analytics</h1>;

const PlannerPage = () => {
  const [plans, setPlans] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);

  useEffect(() => {
    // Calling the full backend URL directly to ensure connection
    fetch('http://localhost:8080/api/plans')
      .then((response) => {
        if (!response.ok) {
          throw new Error('Network response was not ok');
        }
        return response.json();
      })
      .then((data) => {
        setPlans(data);
        setLoading(false);
      })
      .catch((err) => {
        console.error("Fetch failed:", err);
        setError(true);
        setLoading(false);
      });
  }, []);

  if (loading) return <div>⏳ Loading workforce plans...</div>;
  if (error) return <div style={{ color: 'red' }}>⚠️ Error: Could not connect to the backend. Is it running on port 8080?</div>;

  return (
    <div>
      <h1>Staff Planning and Scheduling</h1>
      {plans.length === 0 ? (
        <p>No active plans found in the database.</p>
      ) : (
        <table border={1} style={{ width: '100%', borderCollapse: 'collapse', marginTop: '20px' }}>
          <thead>
            <tr style={{ backgroundColor: '#eee' }}>
              <th style={{ padding: '10px' }}>Plan Name</th>
              <th>Status</th>
              <th>Department</th>
            </tr>
          </thead>
          <tbody>
            {plans.map((plan: any) => (
              <tr key={plan.id}>
                <td style={{ padding: '10px' }}>{plan.name}</td>
                <td>{plan.status || 'Active'}</td>
                <td>{plan.department || 'General'}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
};

const App = () => {
  return (
    <Router>
      <nav style={{ padding: '10px', backgroundColor: '#f0f0f0', borderBottom: '1px solid #ccc' }}>
        <ul style={{ listStyle: 'none', padding: 0, margin: 0, display: 'flex' }}>
          <li style={{ marginRight: '20px' }}><Link to="/">Dashboard</Link></li>
          <li style={{ marginRight: '20px' }}><Link to="/planner">Planner</Link></li>
          <li><Link to="/reports">Reports</Link></li>
        </ul>
      </nav>

      <div style={{ padding: '20px', fontFamily: 'Arial, sans-serif' }}>
        <Routes>
          <Route path="/" element={<DashboardPage />} />
          <Route path="/planner" element={<PlannerPage />} />
          <Route path="/reports" element={<ReportsPage />} />
          <Route path="*" element={<h1>404 - Page Not Found</h1>} />
        </Routes>
      </div>
    </Router>
  );
};

export default App;