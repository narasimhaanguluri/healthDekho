import { useCallback, useEffect, useRef, useState } from "react";
import { Link, useNavigate } from "react-router";
import { generateRecommendations, getRecommendationHistory } from "../api/auth.js";
import { useAuth } from "../auth/AuthContext.jsx";
import AppNavbar from "../components/AppNavbar.jsx";
import { statusBadgeClass, suitabilityBadgeClass, typeBadgeClass } from "../utils/policyUi.js";

function formatMoney(value) {
  const num = Number(value);
  return Number.isFinite(num) ? num.toFixed(2) : String(value);
}

function getTodayIsoDate() {
  const now = new Date();
  const year = now.getFullYear();
  const month = String(now.getMonth() + 1).padStart(2, "0");
  const day = String(now.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
}

function formatTimestamp(isoString) {
  if (!isoString) return "";
  const date = new Date(isoString);
  return Number.isNaN(date.getTime()) ? isoString : date.toLocaleString();
}

export default function RecommendationsPage() {
  const navigate = useNavigate();
  const { customer } = useAuth();
  const dobTouchedRef = useRef(false);
  const todayIso = getTodayIsoDate();

  const [form, setForm] = useState({
    dateOfBirth: customer?.dateOfBirth || "",
    insuranceGoals: "",
    preferredPolicyTermMonths: "",
    insuranceType: "",
  });
  const [submitting, setSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState("");
  const [result, setResult] = useState(null);

  const [historyState, setHistoryState] = useState({
    loading: true,
    error: "",
    items: [],
  });

  useEffect(() => {
    if (!dobTouchedRef.current && customer?.dateOfBirth) {
      setForm((current) =>
        current.dateOfBirth ? current : { ...current, dateOfBirth: customer.dateOfBirth },
      );
    }
  }, [customer]);

  const loadHistory = useCallback(async () => {
    setHistoryState((current) => ({ ...current, loading: true, error: "" }));
    try {
      const data = await getRecommendationHistory({ page: 0, size: 20 });
      setHistoryState({ loading: false, error: "", items: data.items || [] });
    } catch {
      setHistoryState((current) => ({
        ...current,
        loading: false,
        error: "Unable to load recommendation history.",
      }));
    }
  }, []);

  useEffect(() => {
    loadHistory();
  }, [loadHistory]);

  function updateField(event) {
    if (event.target.name === "dateOfBirth") {
      dobTouchedRef.current = true;
    }
    setForm((current) => ({ ...current, [event.target.name]: event.target.value }));
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setSubmitError("");

    const dateOfBirth = form.dateOfBirth.trim();
    const goals = form.insuranceGoals.trim();
    const termValue = form.preferredPolicyTermMonths.trim();
    const type = form.insuranceType.trim();

    if (!dateOfBirth && !goals && !termValue && !type) {
      setSubmitError("Enter at least one preference (date of birth, goals, policy term, or insurance type).");
      return;
    }

    const payload = {
      dateOfBirth: dateOfBirth || null,
      insuranceGoals: goals || null,
      preferredPolicyTermMonths: termValue ? Number(termValue) : null,
      insuranceType: type || null,
    };

    setSubmitting(true);
    try {
      const response = await generateRecommendations(payload);
      setResult(response);
      await loadHistory();
    } catch (error) {
      const status = error.response?.status;
      if (status === 400) {
        setSubmitError("Please provide valid recommendation preferences.");
      } else {
        setSubmitError("AI recommendations are currently unavailable. Please try again later.");
      }
      await loadHistory();
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="page-wrapper">
      <AppNavbar />
      <main className="dashboard-shell">
        <section className="page-hero">
          <div className="page-hero-copy">
            <p className="eyebrow">AI COVERAGE ADVISOR</p>
            <h1>AI Insurance Recommendations</h1>
            <p className="intro">
              Share your priorities to receive clear, explainable matches filtered strictly against active eligible plans.
            </p>
          </div>
          <Link className="secondary-link-button" to="/dashboard">
            Back To Dashboard
          </Link>
        </section>

        <section aria-labelledby="rec-form-heading" className="dashboard-section">
          <h2 id="rec-form-heading">Your Insurance Preferences</h2>
          <p className="help-text">
            Recommendations are advisory only and exclude products you already hold or fall outside age criteria.
          </p>
          <form onSubmit={handleSubmit}>
            <div className="advisor-form-grid">
              <div>
                <label htmlFor="insurance-goals">Insurance Goals</label>
                <textarea
                  id="insurance-goals"
                  maxLength={2000}
                  name="insuranceGoals"
                  onChange={updateField}
                  placeholder="e.g., Family health protection, hospital cashless support, and long-term security"
                  rows={5}
                  value={form.insuranceGoals}
                />
              </div>
              <div className="advisor-controls">
                <div>
                  <label htmlFor="rec-dob">Date of Birth</label>
                  <input
                    id="rec-dob"
                    max={todayIso}
                    name="dateOfBirth"
                    onChange={updateField}
                    type="date"
                    value={form.dateOfBirth}
                  />
                </div>
                <div className="name-fields">
                  <div>
                    <label htmlFor="preferred-term">Preferred Policy Term (Months)</label>
                    <input
                      id="preferred-term"
                      min={1}
                      name="preferredPolicyTermMonths"
                      onChange={updateField}
                      placeholder="e.g., 120"
                      type="number"
                      value={form.preferredPolicyTermMonths}
                    />
                  </div>
                  <div>
                    <label htmlFor="insurance-type">Insurance Type</label>
                    <select id="insurance-type" name="insuranceType" onChange={updateField} value={form.insuranceType}>
                      <option value="">Any type</option>
                      <option value="HEALTH">HEALTH</option>
                      <option value="LIFE">LIFE</option>
                      <option value="RETIREMENT">RETIREMENT</option>
                      <option value="EDUCATION">EDUCATION</option>
                    </select>
                  </div>
                </div>
              </div>
            </div>
            {submitError && <p className="error-message" role="alert">{submitError}</p>}
            <div className="advisor-submit-row">
              <button disabled={submitting} type="submit">
                {submitting ? "Generating Recommendations…" : "Get AI Recommendations"}
              </button>
            </div>
          </form>
        </section>

        <section aria-labelledby="rec-results-heading" className="dashboard-section">
          <div className="section-header">
            <h2 id="rec-results-heading">Recommendations</h2>
            {!submitting && result && (
              <span className="count-pill">{result.recommendations.length}</span>
            )}
          </div>
          {submitting && <p className="status-message" role="status">Analyzing eligible products…</p>}
          {!submitting && !result && (
            <p className="empty-state">Submit your preferences above to receive tailored recommendations.</p>
          )}
          {!submitting && result && result.recommendations.length === 0 && (
            <p className="empty-state">
              No eligible active products remain for your profile and current policies.
            </p>
          )}
          {!submitting && result && result.recommendations.length > 0 && (
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
                    <th>Recommendation Reason</th>
                    <th>Suitability</th>
                    <th>Action</th>
                  </tr>
                </thead>
                <tbody>
                  {result.recommendations.map((item) => (
                    <tr key={item.productCode}>
                      <td className="mono">{item.productCode}</td>
                      <td>{item.policyName}</td>
                      <td><span className={typeBadgeClass(item.policyType)}>{item.policyType}</span></td>
                      <td className="num">{item.policyTermMonths} months</td>
                      <td className="num">INR {formatMoney(item.sumInsured)}</td>
                      <td>{item.premiumFrequency || "—"}</td>
                      <td className="num">INR {formatMoney(item.premiumAmount)}</td>
                      <td className="reason-cell">{item.recommendationReason}</td>
                      <td><span className={suitabilityBadgeClass(item.suitability)}>{item.suitability}</span></td>
                      <td>
                        <button
                          className="table-action-button"
                          onClick={() => navigate(`/policies/register/${encodeURIComponent(item.productCode)}`)}
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
          )}
        </section>

        <section aria-labelledby="rec-history-heading" className="dashboard-section">
          <div className="section-header">
            <h2 id="rec-history-heading">Recommendation History</h2>
            {!historyState.loading && !historyState.error && (
              <span className="count-pill">{historyState.items.length}</span>
            )}
          </div>
          {historyState.loading && <p className="status-message" role="status">Loading recommendation history…</p>}
          {!historyState.loading && historyState.error && (
            <p className="error-message" role="alert">{historyState.error}</p>
          )}
          {!historyState.loading && !historyState.error && historyState.items.length === 0 && (
            <p className="empty-state">No recommendation history yet.</p>
          )}
          {!historyState.loading && !historyState.error && historyState.items.length > 0 && (
            <div className="history-list">
              {historyState.items.map((entry) => (
                <article className="history-card" key={entry.requestId}>
                  <div className="section-header-between">
                    <span className="history-date">{formatTimestamp(entry.requestDate)}</span>
                    <span className={statusBadgeClass(entry.status)}>{entry.status}</span>
                  </div>
                  <p className="help-text">
                    Goals: {entry.insuranceGoals || "—"} | Term: {entry.preferredPolicyTermMonths ? `${entry.preferredPolicyTermMonths} mo` : "—"} | Type: {entry.insuranceType || "Any"}
                  </p>
                  {entry.recommendations && entry.recommendations.length > 0 && (
                    <ul className="history-items">
                      {entry.recommendations.map((rec) => (
                        <li key={rec.productCode}>
                          <div>
                            <strong className="mono">{rec.productCode}</strong> — {rec.policyName}{" "}
                            <span className={suitabilityBadgeClass(rec.suitability)}>{rec.suitability}</span>
                          </div>
                          <button
                            className="secondary-button compact-button"
                            onClick={() => navigate(`/policies/register/${encodeURIComponent(rec.productCode)}`)}
                            type="button"
                          >
                            Register
                          </button>
                        </li>
                      ))}
                    </ul>
                  )}
                </article>
              ))}
            </div>
          )}
        </section>
      </main>
    </div>
  );
}
