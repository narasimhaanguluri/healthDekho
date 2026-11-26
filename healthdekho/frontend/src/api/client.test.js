import { afterEach, beforeEach, describe, expect, it } from "vitest";
import apiClient from "./client.js";
import { getCurrentCustomer } from "./auth.js";

describe("API client authentication", () => {
  let originalAdapter;

  beforeEach(() => {
    window.sessionStorage.clear();
    originalAdapter = apiClient.defaults.adapter;
    apiClient.defaults.adapter = async (config) => {
      throw { config, response: { status: 401, config } };
    };
  });

  afterEach(() => {
    apiClient.defaults.adapter = originalAdapter;
    window.sessionStorage.clear();
  });

  it("clears the tab session after an API 401", async () => {
    window.sessionStorage.setItem("healthdekho.accessToken", "expired-token");
    window.sessionStorage.setItem("healthdekho.customer", JSON.stringify({ id: "customer-id" }));

    await expect(getCurrentCustomer()).rejects.toMatchObject({ response: { status: 401 } });
    expect(window.sessionStorage.getItem("healthdekho.accessToken")).toBeNull();
    expect(window.sessionStorage.getItem("healthdekho.customer")).toBeNull();
  });
});
