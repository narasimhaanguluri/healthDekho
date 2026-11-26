import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { cleanup, fireEvent, render, screen, waitFor } from "@testing-library/react";
import { MemoryRouter } from "react-router";
import { AuthProvider } from "./AuthContext.jsx";
import { AppRoutes } from "../App.jsx";
import * as authApi from "../api/auth.js";

vi.mock("../api/auth.js", () => ({
  getAvailableProducts: vi.fn(),
  getCurrentCustomer: vi.fn(),
  getMyPolicies: vi.fn(),
  loginCustomer: vi.fn(),
  registerCustomer: vi.fn(),
}));

const customer = {
  id: "c84959ef-a649-4022-9e97-99a2e004cf08",
  firstName: "Asha",
  lastName: "Patel",
  email: "asha@example.test",
  mobileNumber: "+919876543210",
  dateOfBirth: "1993-02-14",
};

const availableProductsPage = {
  items: [
    {
      productCode: "HLT001",
      policyName: "Health Secure Plus",
      policyType: "HEALTH",
      termMonths: 12,
      premiumFrequency: "MONTHLY",
      premiumAmount: "1500.00",
      sumInsured: "500000.00",
      minAge: 18,
      maxAge: 60,
    },
  ],
  page: 0,
  size: 20,
  totalElements: 1,
  totalPages: 1,
};

function renderAt(path) {
  return render(
    <AuthProvider>
      <MemoryRouter initialEntries={[path]}>
        <AppRoutes />
      </MemoryRouter>
    </AuthProvider>,
  );
}

describe("FR-1 authentication and dashboard flow", () => {
  beforeEach(() => {
    window.sessionStorage.clear();
    vi.clearAllMocks();
  });

  afterEach(() => {
    cleanup();
  });

  it("redirects from protected dashboard route without rendering private data", async () => {
    renderAt("/dashboard");

    expect(await screen.findByRole("heading", { name: "Welcome back" })).toBeInTheDocument();
    expect(screen.queryByText(customer.email)).not.toBeInTheDocument();
    expect(authApi.getMyPolicies).not.toHaveBeenCalled();
    expect(authApi.getAvailableProducts).not.toHaveBeenCalled();
  });

  it("logs in using token-only response, loads 3 dashboard sections, and clears session on logout", async () => {
    authApi.loginCustomer.mockResolvedValue({
      accessToken: "test-access-token",
      tokenType: "Bearer",
      expiresIn: 600,
    });
    authApi.getCurrentCustomer.mockResolvedValue(customer);
    authApi.getMyPolicies.mockResolvedValue([]);
    authApi.getAvailableProducts.mockResolvedValue(availableProductsPage);
    renderAt("/login");

    fireEvent.change(screen.getByLabelText("Email address"), { target: { value: customer.email } });
    fireEvent.change(screen.getByLabelText("Password"), { target: { value: "Password@123" } });
    fireEvent.click(screen.getByRole("button", { name: "Sign in" }));

    expect(await screen.findByRole("heading", { name: "Welcome to HealthDekho" })).toBeInTheDocument();
    expect(await screen.findByText("Asha Patel")).toBeInTheDocument();
    expect(screen.getByText(customer.email)).toBeInTheDocument();
    expect(await screen.findByText("No policies found")).toBeInTheDocument();
    expect(await screen.findByText("Health Secure Plus")).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "Register" })).toBeDisabled();
    expect(window.sessionStorage.getItem("healthdekho.accessToken")).toBe("test-access-token");

    fireEvent.click(screen.getByRole("button", { name: "Sign out" }));
    expect(await screen.findByRole("heading", { name: "Welcome back" })).toBeInTheDocument();
    expect(window.sessionStorage.getItem("healthdekho.accessToken")).toBeNull();
    expect(window.sessionStorage.getItem("healthdekho.customer")).toBeNull();
  });

  it("renders owned policies when present", async () => {
    window.sessionStorage.setItem("healthdekho.accessToken", "saved-token");
    window.sessionStorage.setItem("healthdekho.customer", JSON.stringify(customer));
    authApi.getCurrentCustomer.mockResolvedValue(customer);
    authApi.getMyPolicies.mockResolvedValue([
      {
        policyNumber: "HD-2026-0001",
        policyName: "Health Secure Plus",
        policyType: "HEALTH",
        policyTerm: 12,
        startDate: "2026-01-01",
        maturityDate: "2027-01-01",
        premiumFrequency: "MONTHLY",
        premiumAmount: "1500.00",
        sumInsured: "500000.00",
        policyStatus: "ACTIVE",
      },
    ]);
    authApi.getAvailableProducts.mockResolvedValue(availableProductsPage);

    renderAt("/dashboard");

    expect(await screen.findByText("HD-2026-0001")).toBeInTheDocument();
    expect(screen.getByText("ACTIVE")).toBeInTheDocument();
    expect(screen.queryByText("No policies found")).not.toBeInTheDocument();
  });

  it("creates an account with mobile number and matching passwords, then requires login", async () => {
    authApi.registerCustomer.mockResolvedValue({
      customerId: customer.id,
      message: "Customer registered successfully",
    });
    renderAt("/signup");

    fireEvent.change(screen.getByLabelText("First name"), { target: { value: "Asha" } });
    fireEvent.change(screen.getByLabelText("Last name"), { target: { value: "Patel" } });
    fireEvent.change(screen.getByLabelText("Email address"), { target: { value: customer.email } });
    fireEvent.change(screen.getByLabelText("Mobile number"), { target: { value: customer.mobileNumber } });
    fireEvent.change(screen.getByLabelText("Date of birth"), { target: { value: customer.dateOfBirth } });
    fireEvent.change(screen.getByLabelText("Password"), { target: { value: "Password@123" } });
    fireEvent.change(screen.getByLabelText("Confirm password"), { target: { value: "Mismatch@123" } });
    fireEvent.click(screen.getByRole("button", { name: "Create account" }));

    expect(await screen.findByRole("alert")).toHaveTextContent("Passwords do not match.");
    expect(authApi.registerCustomer).not.toHaveBeenCalled();

    fireEvent.change(screen.getByLabelText("Confirm password"), { target: { value: "Password@123" } });
    fireEvent.click(screen.getByRole("button", { name: "Create account" }));

    expect(await screen.findByRole("heading", { name: "Welcome back" })).toBeInTheDocument();
    expect(screen.getByRole("status")).toHaveTextContent("Account created. Sign in to continue.");
    expect(window.sessionStorage.getItem("healthdekho.accessToken")).toBeNull();
  });

  it("shows a generic credential failure without creating a session", async () => {
    authApi.loginCustomer.mockRejectedValue({ response: { status: 401 } });
    renderAt("/login");

    fireEvent.change(screen.getByLabelText("Email address"), { target: { value: customer.email } });
    fireEvent.change(screen.getByLabelText("Password"), { target: { value: "invalid password" } });
    fireEvent.click(screen.getByRole("button", { name: "Sign in" }));

    expect(await screen.findByRole("alert")).toHaveTextContent("Email or password is incorrect.");
    expect(window.sessionStorage.getItem("healthdekho.accessToken")).toBeNull();
  });

  it("clears an invalid saved session and redirects to login", async () => {
    window.sessionStorage.setItem("healthdekho.accessToken", "expired-token");
    window.sessionStorage.setItem("healthdekho.customer", JSON.stringify(customer));
    authApi.getCurrentCustomer.mockRejectedValue({ response: { status: 401 } });

    renderAt("/dashboard");
    await waitFor(() => {
      expect(window.sessionStorage.getItem("healthdekho.accessToken")).toBeNull();
    });
    expect(await screen.findByRole("heading", { name: "Welcome back" })).toBeInTheDocument();
  });
});
