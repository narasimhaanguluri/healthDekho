import { useEffect, useState } from "react";
import { Link } from "react-router";
import { getAvailableProducts, getCurrentCustomer, getMyPolicies } from "../api/auth.js";
import { useAuth } from "../auth/AuthContext.jsx";

export default function DashboardPage() {
  const { customer: sessionCustomer, signOut } = useAuth();

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
    <main className="dashboard-shell">
      <header className="dashboard-topbar">
        <div>
          <Link className="brand" to="/dashboard">HealthDekho</Link>
          <p className="eyebrow">CUSTOMER DASHBOARD</p>
          <h1>Welcome to HealthDekho</h1>
        </div>
        <button className="secondary-button topbar-signout" onClick={signOut} type="button">
          Sign out
        </button>
      </header>

      <section aria-labelledby="customer-info-heading" className="dashboard-section">
        <h2 id="customer-info-heading">Customer Information</h2>
        {profileState.loading && <p className="status-message" role="status">Loading profile…</p>}
        {!profileState.loading && profileState.error && (
          <p className="error-message" role="alert">{profileState.error}</p>
        )}
        {!profileState.loading && !profileState.error && profile && (
          <dl className="profile-details">
            <div><dt>Name</dt><dd>{fullName}</dd></div>
            <div><dt>Email</dt><dd>{profile.email}</dd></div>
            {profile.mobileNumber && <div><dt>Mobile</dt><dd>{profile.mobileNumber}</dd></div>}
            {profile.dateOfBirth && <div><dt>Date of birth</dt><dd>{profile.dateOfBirth}</dd></div>}
          </dl>
        )}
      </section>

      <section aria-labelledby="my-policies-heading" className="dashboard-section">
        <div className="section-header">
          <h2 id="my-policies-heading">My Policies</h2>
          {!policiesState.loading && !policiesState.error && (
            <span className="count-pill">{policiesState.data.length}</span>
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
                  <th>Policy Number</th>
                  <th>Policy Name</th>
                  <th>Policy Type</th>
                  <th>Status</th>
                  <th>Term</th>
                  <th>Premium</th>
                  <th>Sum Insured</th>
                </tr>
              </thead>
              <tbody>
                {policiesState.data.map((policy) => (
                  <tr key={policy.policyNumber}>
                    <td className="mono">{policy.policyNumber}</td>
                    <td>{policy.policyName}</td>
                    <td><span className="type-badge">{policy.policyType}</span></td>
                    <td><span className="status-badge">{policy.policyStatus}</span></td>
                    <td>{policy.policyTerm} months</td>
                    <td>INR {policy.premiumAmount} / {policy.premiumFrequency}</td>
                    <td>INR {policy.sumInsured}</td>
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
            <div className="product-grid">
              {productsState.data.items.map((product) => (
                <article className="product-card" key={product.productCode}>
                  <div className="product-card-top">
                    <span className="type-badge">{product.policyType}</span>
                    <span className="mono product-code">{product.productCode}</span>
                  </div>
                  <h3>{product.policyName}</h3>
                  <dl className="product-facts">
                    <div><dt>Premium Amount</dt><dd>INR {product.premiumAmount} ({product.premiumFrequency})</dd></div>
                    <div><dt>Sum Insured</dt><dd>INR {product.sumInsured}</dd></div>
                    <div><dt>Term</dt><dd>{product.termMonths} months</dd></div>
                    <div><dt>Entry Age</dt><dd>{product.minAge}–{product.maxAge} yrs</dd></div>
                  </dl>
                  <button
                    className="register-button"
                    disabled
                    title="Policy registration will be enabled in FR-3"
                    type="button"
                  >
                    Register
                  </button>
                </article>
              ))}
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
  );
}
