import { Link } from "react-router";
import { BrandMark } from "./AppNavbar.jsx";

export default function AuthShowcase({ heading, description }) {
  return (
    <aside className="auth-showcase">
      <Link className="brand-lockup" to="/">
        <BrandMark />
        <span className="brand-text">
          <span className="brand">HealthDekho</span>
          <span className="brand-tagline">Care &amp; Coverage Portal</span>
        </span>
      </Link>

      <div className="showcase-copy">
        <h2>{heading}</h2>
        <p>{description}</p>
      </div>

      <ul className="trust-list">
        <li>
          <span className="trust-dot" aria-hidden="true">1</span>
          <span>Personalized AI recommendations tailored to your family goals and age eligibility.</span>
        </li>
        <li>
          <span className="trust-dot" aria-hidden="true">2</span>
          <span>Transparent premiums, clear maturity dates, and full installment schedules upfront.</span>
        </li>
        <li>
          <span className="trust-dot" aria-hidden="true">3</span>
          <span>Protected customer sessions so your medical and policy records stay private.</span>
        </li>
      </ul>
    </aside>
  );
}
