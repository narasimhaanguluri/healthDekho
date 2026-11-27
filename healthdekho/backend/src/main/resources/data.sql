-- Seed the 8 active HealthDekho insurance products into PostgreSQL / AWS RDS.
-- Safe to re-run: skips any product_code that already exists.

INSERT INTO public.insurance_product (id, active, max_age, min_age, name, premium_frequency, premium_per_installment, product_code, product_type, sum_insured, term_months)
VALUES ('286d9d3f-db0a-443c-b0dc-7f360ba48c4f', true, 60, 18, 'Health Secure Plus', 'MONTHLY', 1500.00, 'HLT001', 'HEALTH', 500000.00, 12)
ON CONFLICT (product_code) DO NOTHING;

INSERT INTO public.insurance_product (id, active, max_age, min_age, name, premium_frequency, premium_per_installment, product_code, product_type, sum_insured, term_months)
VALUES ('3db3d99e-d23c-4356-9cbb-3b33e7be080b', true, 65, 21, 'Family Health Shield', 'QUARTERLY', 4200.00, 'HLT002', 'HEALTH', 1000000.00, 24)
ON CONFLICT (product_code) DO NOTHING;

INSERT INTO public.insurance_product (id, active, max_age, min_age, name, premium_frequency, premium_per_installment, product_code, product_type, sum_insured, term_months)
VALUES ('01eedcee-286b-4712-85ff-7406735c3d4c', true, 80, 60, 'Senior Citizen Care', 'HALF_YEARLY', 9500.00, 'HLT003', 'HEALTH', 500000.00, 12)
ON CONFLICT (product_code) DO NOTHING;

INSERT INTO public.insurance_product (id, active, max_age, min_age, name, premium_frequency, premium_per_installment, product_code, product_type, sum_insured, term_months)
VALUES ('6abfb3f2-f7ab-409a-9c5b-f8327576528b', true, 65, 18, 'Critical Illness Shield', 'YEARLY', 8500.00, 'HLT004', 'HEALTH', 1500000.00, 36)
ON CONFLICT (product_code) DO NOTHING;

INSERT INTO public.insurance_product (id, active, max_age, min_age, name, premium_frequency, premium_per_installment, product_code, product_type, sum_insured, term_months)
VALUES ('99c51fd7-0f55-4c19-ae65-a8cf323ed13c', true, 55, 18, 'Smart Life Protect', 'MONTHLY', 2500.00, 'LIF001', 'LIFE', 2500000.00, 120)
ON CONFLICT (product_code) DO NOTHING;

INSERT INTO public.insurance_product (id, active, max_age, min_age, name, premium_frequency, premium_per_installment, product_code, product_type, sum_insured, term_months)
VALUES ('0b700294-205c-4d1e-808d-93d230a6a9f7', true, 60, 21, 'Family Life Cover', 'YEARLY', 18000.00, 'LIF002', 'LIFE', 5000000.00, 240)
ON CONFLICT (product_code) DO NOTHING;

INSERT INTO public.insurance_product (id, active, max_age, min_age, name, premium_frequency, premium_per_installment, product_code, product_type, sum_insured, term_months)
VALUES ('9922ce2b-f60d-44ff-b556-bf5f6f85736a', true, 60, 25, 'Retirement Future Plan', 'HALF_YEARLY', 15000.00, 'RET001', 'RETIREMENT', 3000000.00, 180)
ON CONFLICT (product_code) DO NOTHING;

INSERT INTO public.insurance_product (id, active, max_age, min_age, name, premium_frequency, premium_per_installment, product_code, product_type, sum_insured, term_months)
VALUES ('af32ccd3-f755-4030-b8c3-43797d3191c7', true, 50, 21, 'Child Education Secure', 'QUARTERLY', 6000.00, 'EDU001', 'EDUCATION', 1200000.00, 120)
ON CONFLICT (product_code) DO NOTHING;
