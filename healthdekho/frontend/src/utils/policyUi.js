export function typeBadgeClass(policyType) {
  const normalized = (policyType || "").trim().toLowerCase();
  if (normalized === "life" || normalized === "retirement" || normalized === "education") {
    return `type-badge type-${normalized}`;
  }
  return "type-badge";
}

export function suitabilityBadgeClass(suitability) {
  const normalized = (suitability || "").trim().toLowerCase();
  if (normalized === "high" || normalized === "medium" || normalized === "low") {
    return `suitability-badge suitability-${normalized}`;
  }
  return "suitability-badge";
}

export function statusBadgeClass(status) {
  const normalized = (status || "").trim().toLowerCase();
  if (normalized === "pending") return "status-badge status-pending";
  if (normalized === "failed" || normalized === "overdue" || normalized === "cancelled") {
    return "status-badge status-failed";
  }
  if (normalized === "no_results") return "status-badge status-no_results";
  return "status-badge";
}
