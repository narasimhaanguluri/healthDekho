import { useEffect, useState } from "react";
import { Link, useParams } from "react-router";
import { getPolicyDetails } from "../api/auth.js";
import AppNavbar from "../components/AppNavbar.jsx";
import { statusBadgeClass, typeBadgeClass } from "../utils/policyUi.js";

export default function PolicyDetailsPage() {
  const { policyNumber } = useParams();
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [policy, setPolicy] = useState(null);

  useEffect(() => {
    let active = true;
    setLoading(true);
    setError("");
    getPolicyDetails(policyNumber)
      .then((data) => {
        if (active) setPolicy(data);
      })
      .catch((err) => {
        if (!active) return;
        setError(
          err.response?.status === 404
            ? "Policy not found."
            : "Unable to load policy details right now.",
        );
      })
      .finally(() => {
        if (active) setLoading(false);
      });
    return () => {
      active = false;
    };
  }, [policyNumber]);

  return (
    <div className="page-wrapper">
      <AppNavbar />
      <main className="dashboard-shell narrow-shell">
        <section className="page-hero">
          <div className="page-hero-copy">
            <p className="eyebrow">POLICY DETAILS</p>
            <h1>{policy ? policy.policyName : "Policy Summary"}</h1>
            <p className="intro">
              Review coverage terms, maturity timeline, and your complete installment schedule.
            </p>
          </div>
          <Link className="secondary-link-button" to="/dashboard">Back To Dashboard</Link>
        </section>

        {loading && (
          <section className="dashboard-section">
            <p className="status-message" role="status">Loading policy details…</p>
          </section>
        )}
        {!loading && error && (
          <section className="dashboard-section">
            <p className="error-message" role="alert">{error}</p>
          </section>
        )}

        {!loading && !error && policy && (
          <>
            <section aria-label="Financial summary" className="kpi-grid">
              <div className="kpi-card">
                <span className="kpi-label">Sum Insured</span>
                <span className="kpi-value">INR {policy.sumInsured}</span>
                <span className="kpi-sub">Total coverage amount</span>
              </div>
              <div className="kpi-card">
                <span className="kpi-label">Installment Premium</span>
                <span className="kpi-value">INR {policy.premiumAmount}</span>
                <span className="kpi-sub">{policy.premiumFrequency}</span>
              </div>
              <div className="kpi-card">
                <span className="kpi-label">Total Premium</span>
                <span className="kpi-value">INR {policy.totalPremium}</span>
                <span className="kpi-sub">{policy.policyTerm} months term</span>
              </div>
            </section>

            <section className="dashboard-section">
              <div className="section-header-between">
                <h2>Policy Terms &amp; Dates</h2>
                <div className="topbar-actions">
                  <span className={typeBadgeClass(policy.policyType)}>{policy.policyType}</span>
                  <span className={statusBadgeClass(policy.policyStatus)}>{policy.policyStatus}</span>
                </div>
              </div>
              <dl className="profile-details">
                <div><dt>Policy Number</dt><dd className="mono">{policy.policyNumber}</dd></div>
                <div><dt>Policy Type</dt><dd>{policy.policyType}</dd></div>
                <div><dt>Status</dt><dd>{policy.policyStatus}</dd></div>
                <div><dt>Term</dt><dd className="num">{policy.policyTerm} months</dd></div>
                <div><dt>Start Date</dt><dd className="num">{policy.startDate}</dd></div>
                <div><dt>Maturity Date</dt><dd className="num">{policy.maturityDate}</dd></div>
                <div><dt>Premium</dt><dd className="num">INR {policy.premiumAmount} / {policy.premiumFrequency}</dd></div>
                <div><dt>Total Premium</dt><dd className="num">INR {policy.totalPremium}</dd></div>
                <div><dt>Sum Insured</dt><dd className="num">INR {policy.sumInsured}</dd></div>
              </dl>

              <h2 className="schedule-heading">Installment Schedule</h2>
              <div className="table-scroll">
                <table className="policy-table">
                  <thead>
                    <tr>
                      <th>#</th>
                      <th>Due Date</th>
                      <th>Grace Date</th>
                      <th>Amount</th>
                      <th>Paid Amount</th>
                      <th>Status</th>
                    </tr>
                  </thead>
                  <tbody>
                    {policy.installments.map((inst) => (
                      <tr key={inst.installmentNumber}>
                        <td className="num">{inst.installmentNumber}</td>
                        <td className="num">{inst.dueDate}</td>
                        <td className="num">{inst.graceDate}</td>
                        <td className="num">INR {inst.amount}</td>
                        <td className="num">INR {inst.paidAmount}</td>
                        <td><span className={statusBadgeClass(inst.status)}>{inst.status}</span></td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </section>
          </>
        )}
      </main>
    </div>
  );
}
