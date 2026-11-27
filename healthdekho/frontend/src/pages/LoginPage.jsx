import { useState } from "react";
import { Link, useNavigate, useSearchParams } from "react-router";
import { useAuth } from "../auth/AuthContext.jsx";
import AuthShowcase from "../components/AuthShowcase.jsx";

export default function LoginPage() {
  const { signIn } = useAuth();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
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
    <div className="auth-page">
      <div className="auth-split">
        <AuthShowcase
          description="Manage your active coverage, explore eligible protection plans, and get calm, plain-language guidance in one place."
          heading="Peace of mind for every stage of family health."
        />
        <main className="auth-card">
          <p className="eyebrow">CUSTOMER ACCESS</p>
          <h1>Welcome back</h1>
          <p className="intro">Sign in to continue to your coverage dashboard.</p>
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
              placeholder="name@example.com"
              required
              type="email"
              value={email}
            />
            <label htmlFor="login-password">Password</label>
            <div className="password-field">
              <input
                autoComplete="current-password"
                id="login-password"
                maxLength={72}
                name="password"
                onChange={(event) => setPassword(event.target.value)}
                required
                type={showPassword ? "text" : "password"}
                value={password}
              />
              <button
                aria-label={showPassword ? "Hide password" : "Show password"}
                className="password-toggle"
                onClick={() => setShowPassword((prev) => !prev)}
                type="button"
              >
                {showPassword ? "Hide" : "Show"}
              </button>
            </div>
            {error && <p className="error-message" role="alert">{error}</p>}
            <button disabled={submitting} type="submit">
              {submitting ? "Signing in…" : "Sign in"}
            </button>
          </form>
          <p className="switch-link">
            New to HealthDekho? <Link to="/signup">Create an account</Link>
          </p>
        </main>
      </div>
    </div>
  );
}
