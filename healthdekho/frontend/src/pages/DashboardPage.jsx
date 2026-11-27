import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router";
import { getAvailableProducts, getCurrentCustomer, getMyPolicies } from "../api/auth.js";
import { useAuth } from "../auth/AuthContext.jsx";
import AppNavbar from "../components/AppNavbar.jsx";
import { statusBadgeClass, typeBadgeClass } from "../utils/policyUi.js";

function computeTotalSumInsured(policies) {
  const total = policies.reduce((sum, policy) => {
    const val = Number(policy.sumInsured);
    return Number.isFinite(val) ? sum + val : sum;
  }, 0);
  return total.toLocaleString("en-IN", { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

export default function DashboardPage() {
  const { customer: sessionCustomer } = useAuth();
  const navigate = useNavigate();

  const [profileState, setProfileState] = useState({ loading: true, error: "", data: sessionCustomer });
  const [policiesState, setPoliciesState] = useState({ loading: true, error: "", data: [] });
  const [productsState, setProductsState] = useState({
    loading: true,
    error: "",
    data: { items: [], page: 0, size: 20, totalElements: 0, totalPages: 0 },
  });
  const [page, setPage] = useState(0);

  useEffect(() => {
    let active = true;

    setProfileState((current) => ({ ...current, loading: true, error: "" }));
    getCurrentCustomer()
      .then((profile) => {
        if (active) setProfileState({ loading: false, error: "", data: profile });
      })
      .catch(() => {
        if (active) {
          setProfileState((current) => ({
            loading: false,
            error: "Unable to load customer profile.",
            data: current.data,
          }));
        }
      });

    setPoliciesState({ loading: true, error: "", data: [] });
    getMyPolicies()
      .then((policies) => {
        if (active) setPoliciesState({ loading: false, error: "", data: policies });
      })
      .catch(() => {
        if (active) setPoliciesState({ loading: false, error: "Unable to load your policies.", data: [] });
      });

    return () => {
      active = false;
    };
  }, []);

  useEffect(() => {
    let active = true;
    setProductsState((current) => ({ ...current, loading: true, error: "" }));
    getAvailableProducts({ page, size: 20 })
      .then((productsPage) => {
        if (active) setProductsState({ loading: false, error: "", data: productsPage });
      })
      .catch(() => {
        if (active) {
          setProductsState((current) => ({
            ...current,
            loading: false,
            error: "Unable to load available products.",
          }));
        }
      });
    return () => {
      active = false;
    };
  }, [page]);

  const profile = profileState.data;
  const fullName = profile
    ? `${profile.firstName || ""} ${profile.lastName || ""}`.trim() || profile.name
    : "";

  return (
    <div className="page-wrapper">
      <AppNavbar />
      <main className="dashboard-shell">
        <section className="page-hero">
          <div className="page-hero-copy">
            <p className="eyebrow">CUSTOMER DASHBOARD</p>
            <h1>Welcome to HealthDekho</h1>
            <p className="intro">
              Review your active coverage, check maturity timelines, and discover eligible protection plans.
            </p>
          </div>
          <div className="topbar-actions">
            <Link className="primary-link-button" to="/recommendations">
              AI Recommendations
            </Link>
          </div>
        </section>

        <div className="overview-grid">
          <section aria-labelledby="customer-info-heading" className="dashboard-section">
            <h2 id="customer-info-heading">Customer Information</h2>
            {profileState.loading && <p className="status-message" role="status">Loading profile…</p>}
            {!profileState.loading && profileState.error && (
              <p className="error-message" role="alert">{profileState.error}</p>
            )}
            {!profileState.loading && !profileState.error && profile && (
              <dl className="profile-grid">
                <div className="profile-tile"><dt>Name</dt><dd>{fullName}</dd></div>
                <div className="profile-tile"><dt>Email</dt><dd>{profile.email}</dd></div>
                {profile.mobileNumber && <div className="profile-tile"><dt>Mobile</dt><dd>{profile.mobileNumber}</dd></div>}
                {profile.dateOfBirth && <div className="profile-tile"><dt>Date of birth</dt><dd>{profile.dateOfBirth}</dd></div>}
              </dl>
            )}
          </section>

          <section aria-label="Coverage overview" className="kpi-grid">
            <div className="kpi-card">
              <span className="kpi-label">Active Policies</span>
              <span className="kpi-value">
                {policiesState.loading ? "—" : policiesState.data.length}
              </span>
              <span className="kpi-sub">Registered under your profile</span>
            </div>
            <div className="kpi-card">
              <span className="kpi-label">Total Sum Insured</span>
              <span className="kpi-value">
                {policiesState.loading ? "—" : `INR ${computeTotalSumInsured(policiesState.data)}`}
              </span>
              <span className="kpi-sub">Combined policy protection</span>
            </div>
            <div className="kpi-card">
              <span className="kpi-label">Available Plans</span>
              <span className="kpi-value">
                {productsState.loading ? "—" : productsState.data.totalElements}
              </span>
              <span className="kpi-sub">Ready for registration</span>
            </div>
          </section>
        </div>

        <section aria-labelledby="my-policies-heading" className="dashboard-section" id="my-policies">
          <div className="section-header">
            <h2 id="my-policies-heading">My Policies</h2>
            {!policiesState.loading && !policiesState.error && (
              <span className="count-pill" data-testid="policy-count">{policiesState.data.length}</span>
            )}
          </div>
          {policiesState.loading && <p className="status-message" role="status">Loading your policies…</p>}
          {!policiesState.loading && policiesState.error && (
            <p className="error-message" role="alert">{policiesState.error}</p>
          )}
          {!policiesState.loading && !policiesState.error && policiesState.data.length === 0 && (
            <p className="empty-state">No policies found</p>
          )}
          {!policiesState.loading && !policiesState.error && policiesState.data.length > 0 && (
            <div className="table-scroll">
              <table className="policy-table">
                <thead>
                  <tr>
                    <th>Policy Identifier</th>
                    <th>Policy Name</th>
                    <th>Policy Type</th>
                    <th>Policy Term</th>
                    <th>Policy Start Date</th>
                    <th>Policy Maturity Date</th>
                    <th>Premium Frequency</th>
                    <th>Premium Amount</th>
                    <th>Sum Insured</th>
                    <th>Policy Status</th>
                  </tr>
                </thead>
                <tbody>
                  {policiesState.data.map((policy) => (
                    <tr key={policy.policyNumber}>
                      <td className="mono">
                        <Link to={`/policies/${encodeURIComponent(policy.policyNumber)}`}>
                          {policy.policyNumber}
                        </Link>
                      </td>
                      <td>{policy.policyName}</td>
                      <td><span className={typeBadgeClass(policy.policyType)}>{policy.policyType}</span></td>
                      <td className="num">{policy.policyTerm} months</td>
                      <td className="num">{policy.startDate}</td>
                      <td className="num">{policy.maturityDate}</td>
                      <td>{policy.premiumFrequency}</td>
                      <td className="num">INR {policy.premiumAmount}</td>
                      <td className="num">INR {policy.sumInsured}</td>
                      <td><span className={statusBadgeClass(policy.policyStatus)}>{policy.policyStatus}</span></td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </section>

        <section aria-labelledby="available-products-heading" className="dashboard-section">
          <div className="section-header">
            <h2 id="available-products-heading">Available Products</h2>
            {!productsState.loading && !productsState.error && (
              <span className="count-pill">{productsState.data.totalElements}</span>
            )}
          </div>
          {productsState.loading && <p className="status-message" role="status">Loading available products…</p>}
          {!productsState.loading && productsState.error && (
            <p className="error-message" role="alert">{productsState.error}</p>
          )}
          {!productsState.loading && !productsState.error && productsState.data.items.length === 0 && (
            <p className="empty-state">No available products found.</p>
          )}
          {!productsState.loading && !productsState.error && productsState.data.items.length > 0 && (
            <>
              <div className="table-scroll">
                <table className="policy-table">
                  <thead>
                    <tr>
                      <th>Product Code</th>
                      <th>Policy Name</th>
                      <th>Policy Type</th>
                      <th>Policy Term</th>
                      <th>Sum Insured</th>
                      <th>Premium Frequency</th>
                      <th>Premium Amount</th>
                      <th>Action</th>
                    </tr>
                  </thead>
                  <tbody>
                    {productsState.data.items.map((product) => (
                      <tr key={product.productCode}>
                        <td className="mono">{product.productCode}</td>
                        <td>{product.policyName}</td>
                        <td><span className={typeBadgeClass(product.policyType)}>{product.policyType}</span></td>
                        <td className="num">{product.termMonths} months</td>
                        <td className="num">INR {product.sumInsured}</td>
                        <td>{product.premiumFrequency}</td>
                        <td className="num">INR {product.premiumAmount}</td>
                        <td>
                          <button
                            className="table-action-button"
                            onClick={() => navigate(`/policies/register/${encodeURIComponent(product.productCode)}`)}
                            type="button"
                          >
                            Register
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
              {productsState.data.totalPages > 1 && (
                <div className="pagination-bar">
                  <button
                    className="secondary-button"
                    disabled={page === 0}
                    onClick={() => setPage((p) => Math.max(0, p - 1))}
                    type="button"
                  >
                    Previous
                  </button>
                  <span>Page {productsState.data.page + 1} of {productsState.data.totalPages}</span>
                  <button
                    className="secondary-button"
                    disabled={page + 1 >= productsState.data.totalPages}
                    onClick={() => setPage((p) => p + 1)}
                    type="button"
                  >
                    Next
                  </button>
                </div>
              )}
            </>
          )}
        </section>
      </main>
    </div>
  );
}
