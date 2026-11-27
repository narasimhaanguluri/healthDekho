import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { cleanup, fireEvent, render, screen, waitFor } from "@testing-library/react";
import { MemoryRouter } from "react-router";
import { AuthProvider } from "./AuthContext.jsx";
import { AppRoutes } from "../App.jsx";
import * as authApi from "../api/auth.js";

vi.mock("../api/auth.js", () => ({
  generateRecommendations: vi.fn(),
  getAvailableProducts: vi.fn(),
  getCurrentCustomer: vi.fn(),
  getMyPolicies: vi.fn(),
  getPolicyDetails: vi.fn(),
  getProductDetails: vi.fn(),
  getRecommendationHistory: vi.fn(),
  loginCustomer: vi.fn(),
  registerCustomer: vi.fn(),
  registerPolicy: vi.fn(),
}));

const customer = {
  id: "c84959ef-a649-4022-9e97-99a2e004cf08",
  firstName: "Asha",
  lastName: "Patel",
  email: "asha@example.test",
  mobileNumber: "+919876543210",
  dateOfBirth: "1993-02-14",
};

const hlt001Product = {
  productCode: "HLT001",
  policyName: "Health Secure Plus",
  policyType: "HEALTH",
  termMonths: 12,
  premiumFrequency: "MONTHLY",
  premiumAmount: "1500.00",
  sumInsured: "500000.00",
  minAge: 18,
  maxAge: 60,
};

const lif001Product = {
  productCode: "LIF001",
  policyName: "Smart Life Protect",
  policyType: "LIFE",
  termMonths: 120,
  premiumFrequency: "MONTHLY",
  premiumAmount: "2500.00",
  sumInsured: "2500000.00",
  minAge: 18,
  maxAge: 55,
};

const availableProductsPage = {
  items: [hlt001Product],
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

describe("FR-1, FR-2 & FR-3 authentication, dashboard, recommendations, and policy registration flow", () => {
  beforeEach(() => {
    window.sessionStorage.clear();
    vi.clearAllMocks();
  });

  afterEach(() => {
    cleanup();
  });

  it("redirects from protected dashboard and recommendations routes without rendering private data", async () => {
    renderAt("/recommendations");

    expect(await screen.findByRole("heading", { name: "Welcome back" })).toBeInTheDocument();
    expect(screen.queryByText(customer.email)).not.toBeInTheDocument();
    expect(authApi.getRecommendationHistory).not.toHaveBeenCalled();
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
    expect(screen.getByRole("link", { name: "AI Recommendations" })).toHaveAttribute("href", "/recommendations");
    expect(screen.getByRole("button", { name: "Register" })).toBeEnabled();
    expect(window.sessionStorage.getItem("healthdekho.accessToken")).toBe("test-access-token");

    fireEvent.click(screen.getByRole("button", { name: "Sign out" }));
    expect(await screen.findByRole("heading", { name: "Welcome back" })).toBeInTheDocument();
    expect(window.sessionStorage.getItem("healthdekho.accessToken")).toBeNull();
    expect(window.sessionStorage.getItem("healthdekho.customer")).toBeNull();
  });

  it("submits preferences on RecommendationsPage, displays recommendations + history, and registers via FR-3 flow", async () => {
    window.sessionStorage.setItem("healthdekho.accessToken", "saved-token");
    window.sessionStorage.setItem("healthdekho.customer", JSON.stringify(customer));
    authApi.getCurrentCustomer.mockResolvedValue(customer);
    authApi.getRecommendationHistory
      .mockResolvedValueOnce({ items: [], page: 0, size: 20, totalElements: 0, totalPages: 0 })
      .mockResolvedValueOnce({
        items: [
          {
            requestId: "req-1",
            requestDate: "2026-11-27T01:00:00Z",
            status: "SUCCEEDED",
            insuranceGoals: "Family Protection",
            preferredPolicyTermMonths: 120,
            insuranceType: "LIFE",
            calculatedAge: 33,
            recommendations: [
              {
                productCode: "LIF001",
                policyName: "Smart Life Protect",
                policyType: "LIFE",
                policyTermMonths: 120,
                premiumFrequency: "MONTHLY",
                premiumAmount: 2500,
                sumInsured: 2500000,
                recommendationReason: "Strong match for family protection.",
                suitability: "HIGH",
                rank: 1,
              },
            ],
          },
        ],
        page: 0,
        size: 20,
        totalElements: 1,
        totalPages: 1,
      });
    authApi.generateRecommendations.mockResolvedValue({
      requestId: "req-1",
      status: "SUCCEEDED",
      recommendations: [
        {
          productCode: "LIF001",
          policyName: "Smart Life Protect",
          policyType: "LIFE",
          policyTermMonths: 120,
          premiumFrequency: "MONTHLY",
          premiumAmount: 2500,
          sumInsured: 2500000,
          recommendationReason: "Strong match for family protection.",
          suitability: "HIGH",
          rank: 1,
        },
      ],
    });
    authApi.getProductDetails.mockResolvedValue(lif001Product);

    renderAt("/recommendations");

    expect(await screen.findByRole("heading", { name: "AI Insurance Recommendations" })).toBeInTheDocument();
    expect(screen.getByLabelText("Date of Birth")).toHaveValue(customer.dateOfBirth);
    fireEvent.change(screen.getByLabelText("Insurance Goals"), { target: { value: "Family Protection" } });
    fireEvent.change(screen.getByLabelText("Preferred Policy Term (Months)"), { target: { value: "120" } });
    fireEvent.change(screen.getByLabelText("Insurance Type"), { target: { value: "LIFE" } });
    fireEvent.click(screen.getByRole("button", { name: "Get AI Recommendations" }));

    expect(await screen.findByText("Strong match for family protection.")).toBeInTheDocument();
    expect(screen.getByRole("columnheader", { name: "Premium Frequency" })).toBeInTheDocument();
    expect(screen.getByText("MONTHLY")).toBeInTheDocument();
    expect(screen.getByText("SUCCEEDED")).toBeInTheDocument();
    expect(authApi.generateRecommendations).toHaveBeenCalledWith({
      dateOfBirth: customer.dateOfBirth,
      insuranceGoals: "Family Protection",
      preferredPolicyTermMonths: 120,
      insuranceType: "LIFE",
    });

    const registerButtons = screen.getAllByRole("button", { name: "Register" });
    fireEvent.click(registerButtons[0]);

    expect(await screen.findByRole("heading", { name: "Review and Register Policy" })).toBeInTheDocument();
    expect(await screen.findByText("Smart Life Protect")).toBeInTheDocument();
    expect(authApi.getProductDetails).toHaveBeenCalledWith("LIF001");
  });

  it("navigates from dashboard Register to registration page, submits policy, shows confirmation, and updates dashboard", async () => {
    window.sessionStorage.setItem("healthdekho.accessToken", "saved-token");
    window.sessionStorage.setItem("healthdekho.customer", JSON.stringify(customer));
    authApi.getCurrentCustomer.mockResolvedValue(customer);
    authApi.getMyPolicies
      .mockResolvedValueOnce([])
      .mockResolvedValueOnce([
        {
          policyNumber: "HD-2026-000001",
          policyName: "Health Secure Plus",
          policyType: "HEALTH",
          policyTerm: 12,
          startDate: "2026-11-26",
          maturityDate: "2027-11-26",
          premiumFrequency: "MONTHLY",
          premiumAmount: "1500.00",
          sumInsured: "500000.00",
          policyStatus: "ACTIVE",
        },
      ]);
    authApi.getAvailableProducts
      .mockResolvedValueOnce(availableProductsPage)
      .mockResolvedValueOnce({ items: [], page: 0, size: 20, totalElements: 0, totalPages: 0 });
    authApi.getProductDetails.mockResolvedValue(hlt001Product);
    authApi.registerPolicy.mockResolvedValue({
      policyNumber: "HD-2026-000001",
      productCode: "HLT001",
      policyName: "Health Secure Plus",
      startDate: "2026-11-26",
      maturityDate: "2027-11-26",
      totalPremium: 18000,
      installmentCount: 12,
      message: "Policy registered successfully",
    });

    renderAt("/dashboard");

    const registerBtn = await screen.findByRole("button", { name: "Register" });
    fireEvent.click(registerBtn);

    expect(await screen.findByRole("heading", { name: "Review and Register Policy" })).toBeInTheDocument();
    expect(await screen.findByText("Health Secure Plus")).toBeInTheDocument();
    expect(screen.getByText(customer.mobileNumber)).toBeInTheDocument();
    expect(screen.getByText("Policy Start Date")).toBeInTheDocument();
    expect(screen.getByText("Policy Maturity Date")).toBeInTheDocument();
    expect(screen.getByText("Total Premium Payable")).toBeInTheDocument();
    expect(screen.getByText("INR 18000.00")).toBeInTheDocument();

    fireEvent.click(screen.getByRole("button", { name: "Register" }));

    expect(await screen.findByRole("heading", { name: "Policy registered successfully" })).toBeInTheDocument();
    expect(screen.getByText("HD-2026-000001")).toBeInTheDocument();

    fireEvent.click(screen.getByRole("button", { name: "Back To Dashboard" }));

    expect(await screen.findByText("HD-2026-000001")).toBeInTheDocument();
    expect(screen.getByText("2026-11-26")).toBeInTheDocument();
    expect(screen.getByText("2027-11-26")).toBeInTheDocument();
    expect(screen.getByTestId("policy-count")).toHaveTextContent("1");
    expect(await screen.findByText("No available products found.")).toBeInTheDocument();
  });

  it("displays age eligibility error on registration page when server rejects", async () => {
    window.sessionStorage.setItem("healthdekho.accessToken", "saved-token");
    window.sessionStorage.setItem("healthdekho.customer", JSON.stringify(customer));
    authApi.getCurrentCustomer.mockResolvedValue(customer);
    authApi.getProductDetails.mockResolvedValue({
      ...hlt001Product,
      productCode: "HLT003",
      policyName: "Senior Citizen Care",
      minAge: 60,
      maxAge: 80,
    });
    authApi.registerPolicy.mockRejectedValue({
      response: {
        status: 422,
        data: {
          code: "AGE_NOT_ELIGIBLE",
          detail: "Customer age 33 is outside the eligible range of 60 to 80 years.",
        },
      },
    });

    renderAt("/policies/register/HLT003");

    expect(await screen.findByText("Senior Citizen Care")).toBeInTheDocument();
    fireEvent.click(screen.getByRole("button", { name: "Register" }));

    expect(await screen.findByRole("alert")).toHaveTextContent(
      "Customer age 33 is outside the eligible range of 60 to 80 years.",
    );
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
