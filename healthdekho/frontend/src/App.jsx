import { BrowserRouter, Link, Navigate, Route, Routes } from "react-router";
import { AuthProvider, useAuth } from "./auth/AuthContext.jsx";
import ProtectedRoute from "./auth/ProtectedRoute.jsx";
import DashboardPage from "./pages/DashboardPage.jsx";
import LoginPage from "./pages/LoginPage.jsx";
import PolicyDetailsPage from "./pages/PolicyDetailsPage.jsx";
import PolicyRegistrationPage from "./pages/PolicyRegistrationPage.jsx";
import RecommendationsPage from "./pages/RecommendationsPage.jsx";
import SignupPage from "./pages/SignupPage.jsx";

function HomeRedirect() {
  const { status } = useAuth();
  if (status === "checking") return <p className="status-message" role="status">Checking your session…</p>;
  return <Navigate to={status === "authenticated" ? "/dashboard" : "/login"} replace />;
}

export function AppRoutes() {
  return (
    <Routes>
      <Route path="/" element={<HomeRedirect />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/signup" element={<SignupPage />} />
      <Route path="/register" element={<SignupPage />} />
      <Route path="/dashboard" element={<ProtectedRoute><DashboardPage /></ProtectedRoute>} />
      <Route path="/recommendations" element={<ProtectedRoute><RecommendationsPage /></ProtectedRoute>} />
      <Route
        path="/policies/register/:productCode"
        element={<ProtectedRoute><PolicyRegistrationPage /></ProtectedRoute>}
      />
      <Route
        path="/policies/:policyNumber"
        element={<ProtectedRoute><PolicyDetailsPage /></ProtectedRoute>}
      />
      <Route path="/account" element={<Navigate to="/dashboard" replace />} />
      <Route
        path="*"
        element={
          <div className="auth-page">
            <main className="auth-card auth-card-solo">
              <p className="eyebrow">NAVIGATION</p>
              <h1>Page not found</h1>
              <p className="intro">The page you requested could not be found.</p>
              <Link className="primary-link-button" to="/login">Return to sign in</Link>
            </main>
          </div>
        }
      />
    </Routes>
  );
}

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <AppRoutes />
      </BrowserRouter>
    </AuthProvider>
  );
}
