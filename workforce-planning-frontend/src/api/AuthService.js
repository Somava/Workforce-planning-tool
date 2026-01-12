/**
 * AuthService.js
 * Handles the auto-login API as per Swagger documentation.
 */

const API_URL = "http://localhost:8080/api/auth";

const AuthService = {
    /**
     * Executes auto-login for 'Alice'.
     */
    autoLogin: async () => {
        const payload = {
            email: "alice@frauas.de",
            password: "password123"
        };

        try {
            const response = await fetch(`${API_URL}/login/auto`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(payload),
            });

            if (!response.ok) throw new Error("Auto-login failed");

            const data = await response.json();

            // Store keys exactly as they appear in the Swagger Response Body
            localStorage.setItem("token", data.token);
            localStorage.setItem("userId", data.userId);
            localStorage.setItem("employeeDbId", data.employeeDbId);
            localStorage.setItem("firstName", data.firstName);
            localStorage.setItem("lastName", data.lastName);
            localStorage.setItem("selectedRole", data.selectedRole);

            return data;
        } catch (error) {
            console.error("Auth Error:", error);
            throw error;
        }
    },

    getAuthHeader: () => {
        const token = localStorage.getItem("token");
        return token ? { "Authorization": `Bearer ${token}` } : {};
    },

    logout: () => {
        localStorage.clear();
        window.location.href = "/";
    }
};

export default AuthService;