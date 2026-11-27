-- HealthDekho PostgreSQL Schema (7 Domain Tables)
-- Safe to re-run: only creates tables that do not already exist.

CREATE TABLE IF NOT EXISTS public.customer (
    id uuid NOT NULL,
    active boolean NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    date_of_birth date NOT NULL,
    email character varying(254) NOT NULL,
    first_name character varying(100) NOT NULL,
    last_name character varying(100) NOT NULL,
    mobile_number character varying(30) NOT NULL,
    normalized_email character varying(254) NOT NULL,
    password_hash character varying(100) NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    role character varying(30) DEFAULT 'CUSTOMER'::character varying NOT NULL,
    CONSTRAINT customer_pkey PRIMARY KEY (id),
    CONSTRAINT uk_customer_normalized_email UNIQUE (normalized_email),
    CONSTRAINT uk_customer_mobile_number UNIQUE (mobile_number)
);

CREATE TABLE IF NOT EXISTS public.insurance_product (
    id uuid NOT NULL,
    active boolean NOT NULL,
    max_age integer NOT NULL,
    min_age integer NOT NULL,
    name character varying(150) NOT NULL,
    premium_frequency character varying(30) NOT NULL,
    premium_per_installment numeric(19,2) NOT NULL,
    product_code character varying(50) NOT NULL,
    product_type character varying(50) NOT NULL,
    sum_insured numeric(19,2) NOT NULL,
    term_months integer NOT NULL,
    CONSTRAINT insurance_product_pkey PRIMARY KEY (id),
    CONSTRAINT uk_insurance_product_code UNIQUE (product_code)
);

CREATE TABLE IF NOT EXISTS public.customer_policy (
    id uuid NOT NULL,
    maturity_date date,
    policy_number character varying(64) NOT NULL,
    premium_frequency character varying(30) NOT NULL,
    premium_per_installment numeric(19,2) NOT NULL,
    product_name character varying(150) NOT NULL,
    product_type character varying(50) NOT NULL,
    registered_at timestamp(6) with time zone NOT NULL,
    registration_idempotency_key uuid NOT NULL,
    start_date date NOT NULL,
    status character varying(30) NOT NULL,
    sum_insured numeric(19,2) NOT NULL,
    term_months integer NOT NULL,
    total_premium numeric(19,2) NOT NULL,
    customer_id uuid NOT NULL,
    product_id uuid NOT NULL,
    CONSTRAINT customer_policy_pkey PRIMARY KEY (id),
    CONSTRAINT uk_customer_policy_number UNIQUE (policy_number),
    CONSTRAINT uq_customer_policy_product UNIQUE (customer_id, product_id),
    CONSTRAINT uq_customer_policy_registration_idempotency UNIQUE (customer_id, registration_idempotency_key),
    CONSTRAINT fk_customer_policy_customer FOREIGN KEY (customer_id) REFERENCES public.customer(id),
    CONSTRAINT fk_customer_policy_product FOREIGN KEY (product_id) REFERENCES public.insurance_product(id)
);

CREATE TABLE IF NOT EXISTS public.premium_installment (
    id uuid NOT NULL,
    due_date date NOT NULL,
    grace_date date,
    installment_number integer NOT NULL,
    paid_amount numeric(19,2) NOT NULL,
    scheduled_amount numeric(19,2) NOT NULL,
    status character varying(30) NOT NULL,
    policy_id uuid NOT NULL,
    CONSTRAINT premium_installment_pkey PRIMARY KEY (id),
    CONSTRAINT uq_installment_policy_number UNIQUE (policy_id, installment_number),
    CONSTRAINT fk_premium_installment_policy FOREIGN KEY (policy_id) REFERENCES public.customer_policy(id)
);

CREATE TABLE IF NOT EXISTS public.premium_payment (
    id uuid NOT NULL,
    amount numeric(19,2) NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    idempotency_key uuid NOT NULL,
    paid_at timestamp(6) with time zone NOT NULL,
    payment_method character varying(50) NOT NULL,
    penalty_amount numeric(19,2) NOT NULL,
    transaction_reference character varying(150) NOT NULL,
    installment_id uuid NOT NULL,
    CONSTRAINT premium_payment_pkey PRIMARY KEY (id),
    CONSTRAINT uk_premium_payment_idempotency UNIQUE (idempotency_key),
    CONSTRAINT uk_premium_payment_tx_ref UNIQUE (transaction_reference),
    CONSTRAINT fk_premium_payment_installment FOREIGN KEY (installment_id) REFERENCES public.premium_installment(id)
);

CREATE TABLE IF NOT EXISTS public.ai_recommendation_request (
    id uuid NOT NULL,
    calculated_age integer,
    candidate_snapshot jsonb,
    created_at timestamp(6) with time zone NOT NULL,
    goals character varying(2000),
    model_id character varying(200),
    preferred_product_type character varying(50),
    preferred_term_months integer,
    prompt_version character varying(100),
    status character varying(30) NOT NULL,
    customer_id uuid NOT NULL,
    CONSTRAINT ai_recommendation_request_pkey PRIMARY KEY (id),
    CONSTRAINT fk_ai_rec_req_customer FOREIGN KEY (customer_id) REFERENCES public.customer(id)
);

CREATE TABLE IF NOT EXISTS public.ai_recommendation_item (
    id uuid NOT NULL,
    rank integer NOT NULL,
    reason character varying(2000) NOT NULL,
    selected boolean NOT NULL,
    suitability character varying(100),
    product_id uuid NOT NULL,
    request_id uuid NOT NULL,
    CONSTRAINT ai_recommendation_item_pkey PRIMARY KEY (id),
    CONSTRAINT uq_recommendation_request_product UNIQUE (request_id, product_id),
    CONSTRAINT fk_ai_rec_item_product FOREIGN KEY (product_id) REFERENCES public.insurance_product(id),
    CONSTRAINT fk_ai_rec_item_request FOREIGN KEY (request_id) REFERENCES public.ai_recommendation_request(id)
);
