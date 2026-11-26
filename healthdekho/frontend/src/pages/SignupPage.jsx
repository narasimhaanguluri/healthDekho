import { useState } from "react";
import { Link, useNavigate } from "react-router";
import { registerCustomer } from "../api/auth.js";

export default function SignupPage() {
  const navigate = useNavigate();
  const [form, setForm] = useState({
    firstName: "",
    lastName: "",
    email: "",
    mobileNumber: "",
    dateOfBirth: "",
    password: "",
    confirmPassword: "",
  });
  const [error, setError] = useState("");
  const [submitting, setSubmitting] = useState(false);

  function updateField(event) {
    setForm((current) => ({ ...current, [event.target.name]: event.target.value }));
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setError("");
    if (form.password !== form.confirmPassword) {
      setError("Passwords do not match.");
      return;
    }
    setSubmitting(true);
    try {
      await registerCustomer(form);
      navigate("/login?registered=1", { replace: true });
    } catch (requestError) {
      if (requestError.response?.status === 409) {
        setError("An account with this email or mobile number already exists.");
      } else if (requestError.response?.status === 400) {
        setError("Please check the registration fields and try again.");
      } else {
        setError("Account creation is temporarily unavailable. Please try again.");
      }
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <main className="auth-card">
      <Link className="brand" to="/">HealthDekho</Link>
      <p className="eyebrow">CUSTOMER ACCESS</p>
      <h1>Create your account</h1>
      <p className="intro">Register as a customer, then sign in to access your dashboard.</p>
      <form onSubmit={handleSubmit}>
        <div className="name-fields">
          <div>
            <label htmlFor="first-name">First name</label>
            <input id="first-name" maxLength={100} name="firstName" onChange={updateField} required value={form.firstName} />
          </div>
          <div>
            <label htmlFor="last-name">Last name</label>
            <input id="last-name" maxLength={100} name="lastName" onChange={updateField} required value={form.lastName} />
          </div>
        </div>
        <label htmlFor="signup-email">Email address</label>
        <input autoComplete="email" id="signup-email" maxLength={254} name="email" onChange={updateField} required type="email" value={form.email} />
        <label htmlFor="mobile-number">Mobile number</label>
        <input autoComplete="tel" id="mobile-number" maxLength={30} name="mobileNumber" onChange={updateField} required type="tel" value={form.mobileNumber} />
        <label htmlFor="date-of-birth">Date of birth</label>
        <input
          id="date-of-birth"
          max={new Date().toISOString().slice(0, 10)}
          name="dateOfBirth"
          onChange={updateField}
          required
          type="date"
          value={form.dateOfBirth}
        />
        <label htmlFor="signup-password">Password</label>
        <input
          autoComplete="new-password"
          id="signup-password"
          maxLength={72}
          minLength={8}
          name="password"
          onChange={updateField}
          required
          type="password"
          value={form.password}
        />
        <label htmlFor="confirm-password">Confirm password</label>
        <input
          autoComplete="new-password"
          id="confirm-password"
          maxLength={72}
          minLength={8}
          name="confirmPassword"
          onChange={updateField}
          required
          type="password"
          value={form.confirmPassword}
        />
        <p className="help-text">Use 8–72 characters. Passwords are case-sensitive.</p>
        {error && <p className="error-message" role="alert">{error}</p>}
        <button disabled={submitting} type="submit">
          {submitting ? "Creating account…" : "Create account"}
        </button>
      </form>
      <p className="switch-link">Already have an account? <Link to="/login">Sign in</Link></p>
    </main>
  );
}
