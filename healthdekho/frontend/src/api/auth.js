import apiClient from "./client.js";

export async function registerCustomer(payload) {
  const response = await apiClient.post("/auth/register", payload);
  return response.data;
}

export async function loginCustomer(payload) {
  const response = await apiClient.post("/auth/login", payload);
  return response.data;
}

export async function getCurrentCustomer() {
  const response = await apiClient.get("/customers/me");
  return response.data;
}

export async function getDashboard() {
  const response = await apiClient.get("/dashboard");
  return response.data;
}

export async function getMyPolicies() {
  const response = await apiClient.get("/customers/me/policies");
  return response.data;
}

export async function getAvailableProducts({ page = 0, size = 20 } = {}) {
  const response = await apiClient.get("/products/available", { params: { page, size } });
  return response.data;
}

export async function getProductDetails(productCode) {
  const response = await apiClient.get(`/products/${encodeURIComponent(productCode)}`);
  return response.data;
}
