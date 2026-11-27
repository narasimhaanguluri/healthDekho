import { Link, useLocation } from "react-router";
import { useAuth } from "../auth/AuthContext.jsx";

export function BrandMark() {
  return (
    <span className="brand-mark" aria-hidden="true">
      <svg viewBox="0 0 28 28" fill="none" xmlns="http://www.w3.org/2000/svg">
        <path
          d="M14 2.5L23.5 6.3V13.4C23.5 19.2 19.45 24.35 14 25.8C8.55 24.35 4.5 19.2 4.5 13.4V6.3L14 2.5Z"
          fill="currentColor"
          fillOpacity="0.14"
          stroke="currentColor"
          strokeWidth="1.8"
          strokeLinejoin="round"
        />
        <path
          d="M14 9.2V17.2M10 13.2H18"
          stroke="currentColor"
          strokeWidth="2.1"
          strokeLinecap="round"
        />
      </svg>
    </span>
  );
}

export default function AppNavbar() {
  const { customer, signOut } = useAuth();
  const location = useLocation();
  const firstName = customer?.firstName?.trim() || "";

  const isDashboard = location.pathname.startsWith("/dashboard");
  const isRecommendations = location.pathname.startsWith("/recommendations");

  return (
    <header className="app-navbar">
      <div className="app-navbar-inner">
        <div className="navbar-left">
          <Link className="brand-lockup" to="/dashboard">
            <BrandMark />
            <span className="brand-text">
              <span className="brand">HealthDekho</span>
              <span className="brand-tagline">Care &amp; Coverage Portal</span>
            </span>
          </Link>
          <nav aria-label="Primary navigation" className="navbar-links">
            <Link className={`nav-link${isDashboard ? " active" : ""}`} to="/dashboard">
              Dashboard
            </Link>
            <Link className={`nav-link${isRecommendations ? " active" : ""}`} to="/recommendations">
              Recommendations
            </Link>
          </nav>
        </div>

        <div className="navbar-right">
          {firstName && (
            <span className="user-pill" title={firstName}>
              <span className="user-avatar" aria-hidden="true">
                {firstName.charAt(0).toUpperCase()}
              </span>
              <span className="user-pill-name">Hi, {firstName}</span>
            </span>
          )}
          <button className="secondary-button topbar-signout" onClick={signOut} type="button">
            Sign out
          </button>
        </div>
      </div>
    </header>
  );
}
