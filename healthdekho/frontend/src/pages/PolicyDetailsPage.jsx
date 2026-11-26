import { useEffect, useState } from "react";
import { Link, useParams } from "react-router";
import { getPolicyDetails } from "../api/auth.js";

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
    <main className="dashboard-shell">
      <section className="dashboard-section">
        <div className="section-header-between">
          <Link className="brand" to="/dashboard">HealthDekho</Link>
          <Link to="/dashboard">Back To Dashboard</Link>
        </div>
        <p className="eyebrow">POLICY DETAILS</p>
        {loading && <p className="status-message" role="status">Loading policy details…</p>}
        {!loading && error && <p className="error-message" role="alert">{error}</p>}
        {!loading && !error && policy && (
          <>
            <h1>{policy.policyName}</h1>
            <dl className="profile-details">
              <div><dt>Policy Number</dt><dd className="mono">{policy.policyNumber}</dd></div>
              <div><dt>Policy Type</dt><dd>{policy.policyType}</dd></div>
              <div><dt>Status</dt><dd>{policy.policyStatus}</dd></div>
              <div><dt>Term</dt><dd>{policy.policyTerm} months</dd></div>
              <div><dt>Start Date</dt><dd>{policy.startDate}</dd></div>
              <div><dt>Maturity Date</dt><dd>{policy.maturityDate}</dd></div>
              <div><dt>Premium</dt><dd>INR {policy.premiumAmount} / {policy.premiumFrequency}</dd></div>
              <div><dt>Total Premium</dt><dd>INR {policy.totalPremium}</dd></div>
              <div><dt>Sum Insured</dt><dd>INR {policy.sumInsured}</dd></div>
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
                      <td>{inst.installmentNumber}</td>
                      <td>{inst.dueDate}</td>
                      <td>{inst.graceDate}</td>
                      <td>INR {inst.amount}</td>
                      <td>INR {inst.paidAmount}</td>
                      <td><span className="status-badge">{inst.status}</span></td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </>
        )}
      </section>
    </main>
  );
}
