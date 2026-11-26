import { useState } from "react";
import { Link, useNavigate, useSearchParams } from "react-router";
import { useAuth } from "../auth/AuthContext.jsx";

export default function LoginPage() {
  const { signIn } = useAuth();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit(event) {
    event.preventDefault();
    setError("");
    setSubmitting(true);
    try {
      await signIn({ email, password });
      navigate("/dashboard", { replace: true });
    } catch (requestError) {
      setError(requestError.response?.status === 401
        ? "Email or password is incorrect."
        : "Sign-in is temporarily unavailable. Please try again.");
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <main className="auth-card">
      <a className="brand" href="/" aria-label="HealthDekho home">HealthDekho</a>
      <p className="eyebrow">CUSTOMER ACCESS</p>
      <h1>Welcome back</h1>
      <p className="intro">Sign in to continue to your account.</p>
      {searchParams.get("registered") === "1" && (
        <p className="notice" role="status">Account created. Sign in to continue.</p>
      )}
      <form onSubmit={handleSubmit}>
        <label htmlFor="login-email">Email address</label>
        <input
          autoComplete="email"
          id="login-email"
          name="email"
          onChange={(event) => setEmail(event.target.value)}
          required
          type="email"
          value={email}
        />
        <label htmlFor="login-password">Password</label>
        <input
          autoComplete="current-password"
          id="login-password"
          maxLength={72}
          name="password"
          onChange={(event) => setPassword(event.target.value)}
          required
          type="password"
          value={password}
        />
        {error && <p className="error-message" role="alert">{error}</p>}
        <button disabled={submitting} type="submit">
          {submitting ? "Signing in…" : "Sign in"}
        </button>
      </form>
      <p className="switch-link">New to HealthDekho? <Link to="/signup">Create an account</Link></p>
    </main>
  );
}
