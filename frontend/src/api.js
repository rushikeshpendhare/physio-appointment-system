import axios from "axios";

const api = axios.create({
  baseURL: "http://localhost:8080/api",
});

// Pratyek request madhe token automatic lavto
api.interceptors.request.use((config) => {
  const token = localStorage.getItem("token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export default api;