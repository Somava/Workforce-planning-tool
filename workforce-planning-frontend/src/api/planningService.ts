import axios from 'axios';

// This matches your Spring Boot backend port
const API_BASE_URL = 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

export const getPlans = async () => {
  try {
    const response = await api.get('/plans');
    return response.data;
  } catch (error) {
    console.error("Error fetching plans:", error);
    throw error;
  }
};