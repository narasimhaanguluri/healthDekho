import { useEffect, useRef, useState } from "react";
import { Link, useNavigate, useParams } from "react-router";
import { getCurrentCustomer, getProductDetails, registerPolicy } from "../api/auth.js";

function createIdempotencyKey() {
  if (typeof globalThis.crypto?.randomUUID === "function") {
    return globalThis.crypto.randomUUID();
  }
  return "xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx".replace(/[xy]/g, (char) => {
    const rand = Math.floor(Math.random() * 16);
    const value = char === "x" ? rand : (rand & 0x3) | 0x8;
    return value.toString(16);
  });
}

function formatTotalPremium(value) {
  const num = Number(value);
  return Number.isFinite(num) ? num.toFixed(2) : String(value);
}

function getTodayLocalDate() {
  const now = new Date();
  return new Date(now.getFullYear(), now.getMonth(), now.getDate());
}

function formatLocalIsoDate(date) {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
}

function addMonthsClamped(baseDate, monthsToAdd) {
  const targetYear = baseDate.getFullYear() + Math.floor((baseDate.getMonth() + monthsToAdd) / 12);
  const targetMonth = (baseDate.getMonth() + monthsToAdd) % 12;
  const lastDayOfTargetMonth = new Date(targetYear, targetMonth + 1, 0).getDate();
  const clampedDay = Math.min(baseDate.getDate(), lastDayOfTargetMonth);
  return new Date(targetYear, targetMonth, clampedDay);
}

function resolveIntervalMonths(frequency) {
  switch ((frequency || "").trim().toUpperCase()) {
    case "MONTHLY": return 1;
    case "QUARTERLY": return 3;
    case "HALF_YEARLY": return 6;
    case "YEARLY": return 12;
    default: return null;
  }
}

function calculateEstimatedTotalPremium(product) {
  const intervalMonths = resolveIntervalMonths(product?.premiumFrequency);
  const termMonths = Number(product?.termMonths);
  const installmentAmount = Number(product?.premiumAmount);
  if (!intervalMonths || !Number.isFinite(termMonths) || !Number.isFinite(installmentAmount)) {
    return null;
  }
  return ((termMonths / intervalMonths) * installmentAmount).toFixed(2);
}

export default function PolicyRegistrationPage() {
  const { productCode } = useParams();
  const navigate = useNavigate();
  const idempotencyKeyRef = useRef(createIdempotencyKey());

  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState("");
  const [product, setProduct] = useState(null);
  const [profile, setProfile] = useState(null);
  const [submitting, setSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState("");
  const [confirmation, setConfirmation] = useState(null);

  useEffect(() => {
    let active = true;
    idempotencyKeyRef.current = createIdempotencyKey();
    setLoading(true);
    setLoadError("");
    setSubmitError("");
    setConfirmation(null);

    Promise.all([getProductDetails(productCode), getCurrentCustomer()])
      .then(([productData, customerData]) => {
        if (!active) return;
        setProduct(productData);
        setProfile(customerData);
      })
      .catch((error) => {
        if (!active) return;
        setLoadError(
          error.response?.status === 404
            ? "The selected insurance product is unavailable."
            : "Unable to load policy registration details right now.",
        );
      })
      .finally(() => {
        if (active) setLoading(false);
      });

    return () => {
      active = false;
    };
  }, [productCode]);

  async function handleRegister() {
    if (submitting || !product) return;
    setSubmitError("");
    setSubmitting(true);
    try {
      const result = await registerPolicy(product.productCode, idempotencyKeyRef.current);
      setConfirmation(result);
    } catch (error) {
      const status = error.response?.status;
      const code = error.response?.data?.code;
      const detail = error.response?.data?.detail;
      if (status === 422 && code === "AGE_NOT_ELIGIBLE") {
        setSubmitError(detail || "Your age does not meet this product's eligibility criteria.");
      } else if (status === 409 && code === "DUPLICATE_POLICY") {
        setSubmitError("You have already registered this product.");
      } else if (status === 409 && code === "PRODUCT_UNAVAILABLE") {
        setSubmitError("This product is no longer active.");
      } else if (status === 404) {
        setSubmitError("The selected product could not be found.");
      } else {
        setSubmitError("Policy registration failed. Please try again.");
      }
    } finally {
      setSubmitting(false);
    }
  }

  if (confirmation) {
    return (
      <main className="dashboard-shell narrow-shell">
        <section className="dashboard-section">
          <Link className="brand" to="/dashboard">HealthDekho</Link>
          <p className="eyebrow">REGISTRATION CONFIRMED</p>
          <h1>{confirmation.message || "Policy registered successfully"}</h1>
          <p className="intro">
            Your insurance policy and installment schedule have been created.
          </p>
          <dl className="profile-details">
            <div><dt>Policy Number</dt><dd className="mono">{confirmation.policyNumber}</dd></div>
            <div><dt>Policy Name</dt><dd>{confirmation.policyName}</dd></div>
            <div><dt>Start Date</dt><dd>{confirmation.startDate}</dd></div>
            <div><dt>Maturity Date</dt><dd>{confirmation.maturityDate}</dd></div>
            <div><dt>Total Premium</dt><dd>INR {formatTotalPremium(confirmation.totalPremium)}</dd></div>
            <div><dt>Installment Count</dt><dd>{confirmation.installmentCount}</dd></div>
          </dl>
          <div className="actions-row">
            <button onClick={() => navigate("/dashboard")} type="button">
              Back To Dashboard
            </button>
            <button
              className="secondary-button"
              onClick={() => navigate("/dashboard#my-policies")}
              type="button"
            >
              View My Policies
            </button>
          </div>
        </section>
      </main>
    );
  }

  return (
    <main className="dashboard-shell narrow-shell">
      <section className="dashboard-section">
        <div className="section-header-between">
          <Link className="brand" to="/dashboard">HealthDekho</Link>
          <Link to="/dashboard">Back To Dashboard</Link>
        </div>
        <p className="eyebrow">POLICY REGISTRATION</p>
        <h1>Review and Register Policy</h1>

        {loading && <p className="status-message" role="status">Loading product details…</p>}
        {!loading && loadError && <p className="error-message" role="alert">{loadError}</p>}

        {!loading && !loadError && product && (() => {
          const startDateObj = getTodayLocalDate();
          const startDateText = formatLocalIsoDate(startDateObj);
          const maturityDateText = Number.isFinite(Number(product.termMonths))
            ? formatLocalIsoDate(addMonthsClamped(startDateObj, Number(product.termMonths)))
            : "—";
          const totalPremiumPayable = calculateEstimatedTotalPremium(product);

          return (
            <>
              {profile && (
                <div className="sub-card">
                  <h2>Applicant Information</h2>
                  <dl className="profile-details">
                    <div><dt>Customer Name</dt><dd>{`${profile.firstName} ${profile.lastName}`.trim()}</dd></div>
                    <div><dt>Date of Birth</dt><dd>{profile.dateOfBirth}</dd></div>
                    <div><dt>Email</dt><dd>{profile.email}</dd></div>
                    {profile.mobileNumber && <div><dt>Mobile Number</dt><dd>{profile.mobileNumber}</dd></div>}
                  </dl>
                </div>
              )}

              <div className="sub-card">
                <h2>Product Summary</h2>
                <dl className="profile-details">
                  <div><dt>Policy Type</dt><dd>{product.policyType}</dd></div>
                  <div><dt>Policy Name</dt><dd>{product.policyName}</dd></div>
                  <div><dt>Policy Term</dt><dd>{product.termMonths} months</dd></div>
                  <div><dt>Policy Start Date</dt><dd>{startDateText}</dd></div>
                  <div><dt>Policy Maturity Date</dt><dd>{maturityDateText}</dd></div>
                  <div><dt>Premium Payment Frequency</dt><dd>{product.premiumFrequency}</dd></div>
                  <div><dt>Premium Amount</dt><dd>INR {product.premiumAmount}</dd></div>
                  <div><dt>Sum Insured</dt><dd>INR {product.sumInsured}</dd></div>
                  <div>
                    <dt>Total Premium Payable</dt>
                    <dd>{totalPremiumPayable ? `INR ${totalPremiumPayable}` : "—"}</dd>
                  </div>
                  <div><dt>Age Eligibility</dt><dd>{product.minAge}–{product.maxAge} years</dd></div>
                </dl>
              </div>

              {submitError && <p className="error-message" role="alert">{submitError}</p>}

              <div className="actions-row">
                <button disabled={submitting} onClick={handleRegister} type="button">
                  {submitting ? "Registering…" : "Register"}
                </button>
                <button
                  className="secondary-button"
                  disabled={submitting}
                  onClick={() => navigate("/dashboard")}
                  type="button"
                >
                  Cancel
                </button>
              </div>
            </>
          );
        })()}
      </section>
    </main>
  );
}
