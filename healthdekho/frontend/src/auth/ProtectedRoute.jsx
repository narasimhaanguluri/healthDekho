import { Navigate } from "react-router";
import { useAuth } from "./AuthContext.jsx";

export default function ProtectedRoute({ children }) {
  const { status } = useAuth();
  if (status === "checking") {
    return <p className="status-message" role="status">Checking your session…</p>;
  }
  if (status !== "authenticated") {
    return <Navigate to="/login" replace />;
  }
  return children;
}
