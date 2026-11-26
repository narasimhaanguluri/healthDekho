import { useCallback, useEffect, useState } from "react";
import { Link, useNavigate } from "react-router";
import { generateRecommendations, getRecommendationHistory } from "../api/auth.js";

function formatMoney(value) {
  const num = Number(value);
  return Number.isFinite(num) ? num.toFixed(2) : String(value);
}

function formatTimestamp(isoString) {
  if (!isoString) return "";
  const date = new Date(isoString);
  return Number.isNaN(date.getTime()) ? isoString : date.toLocaleString();
}

export default function RecommendationsPage() {
  const navigate = useNavigate();

  const [form, setForm] = useState({
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
    setForm((current) => ({ ...current, [event.target.name]: event.target.value }));
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setSubmitError("");

    const goals = form.insuranceGoals.trim();
    const termValue = form.preferredPolicyTermMonths.trim();
    const type = form.insuranceType.trim();

    if (!goals && !termValue && !type) {
      setSubmitError("Enter at least one preference (goals, policy term, or insurance type).");
      return;
    }

    const payload = {
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
    <main className="dashboard-shell">
      <header className="dashboard-topbar">
        <div>
          <Link className="brand" to="/dashboard">HealthDekho</Link>
          <p className="eyebrow">AI COVERAGE ADVISOR</p>
          <h1>AI Insurance Recommendations</h1>
        </div>
        <Link className="secondary-link-button" to="/dashboard">
          Back To Dashboard
        </Link>
      </header>

      <section aria-labelledby="rec-form-heading" className="dashboard-section">
        <h2 id="rec-form-heading">Your Insurance Preferences</h2>
        <p className="help-text">
          Recommendations are advisory only and filtered against active products matching your profile age and existing coverage.
        </p>
        <form onSubmit={handleSubmit}>
          <label htmlFor="insurance-goals">Insurance Goals</label>
          <textarea
            id="insurance-goals"
            maxLength={2000}
            name="insuranceGoals"
            onChange={updateField}
            placeholder="e.g., Family income protection and long-term health security"
            rows={3}
            value={form.insuranceGoals}
          />
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
          {submitError && <p className="error-message" role="alert">{submitError}</p>}
          <button disabled={submitting} type="submit">
            {submitting ? "Generating Recommendations…" : "Get AI Recommendations"}
          </button>
        </form>
      </section>

      <section aria-labelledby="rec-results-heading" className="dashboard-section">
        <h2 id="rec-results-heading">Recommendations</h2>
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
          <div className="product-grid">
            {result.recommendations.map((item) => (
              <article className="product-card" key={item.productCode}>
                <div className="product-card-top">
                  <span className="type-badge">{item.policyType}</span>
                  <span className="suitability-badge">{item.suitability}</span>
                </div>
                <p className="mono product-code">{item.productCode}</p>
                <h3>{item.policyName}</h3>
                <dl className="product-facts">
                  <div><dt>Policy Term</dt><dd>{item.policyTermMonths} months</dd></div>
                  <div><dt>Premium Amount</dt><dd>INR {formatMoney(item.premiumAmount)}</dd></div>
                  <div><dt>Sum Insured</dt><dd>INR {formatMoney(item.sumInsured)}</dd></div>
                </dl>
                <p className="recommendation-reason">{item.recommendationReason}</p>
                <button
                  className="register-button"
                  onClick={() => navigate(`/policies/register/${encodeURIComponent(item.productCode)}`)}
                  type="button"
                >
                  Register
                </button>
              </article>
            ))}
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
                  <span className={`status-badge status-${entry.status.toLowerCase()}`}>{entry.status}</span>
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
                          <span className="suitability-badge">{rec.suitability}</span>
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
  );
}
