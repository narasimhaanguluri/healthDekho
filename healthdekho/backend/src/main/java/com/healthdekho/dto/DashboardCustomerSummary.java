package com.healthdekho.dto;

import java.util.UUID;

public record DashboardCustomerSummary(UUID id, String firstName, String lastName, String email) {
}
