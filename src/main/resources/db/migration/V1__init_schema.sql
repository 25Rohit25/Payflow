--
-- PostgreSQL database dump
--

-- Dumped from database version 15.18
-- Dumped by pg_dump version 15.18

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: flyway_schema_history; Type: TABLE; Schema: public; Owner: payflow
--

CREATE TABLE public.flyway_schema_history (
    installed_rank integer NOT NULL,
    version character varying(50),
    description character varying(200) NOT NULL,
    type character varying(20) NOT NULL,
    script character varying(1000) NOT NULL,
    checksum integer,
    installed_by character varying(100) NOT NULL,
    installed_on timestamp without time zone DEFAULT now() NOT NULL,
    execution_time integer NOT NULL,
    success boolean NOT NULL
);


ALTER TABLE public.flyway_schema_history OWNER TO payflow;

--
-- Name: fraud_logs; Type: TABLE; Schema: public; Owner: payflow
--

CREATE TABLE public.fraud_logs (
    id uuid NOT NULL,
    created_at timestamp(6) without time zone,
    reason character varying(255) NOT NULL,
    transaction_id uuid NOT NULL,
    wallet_id uuid NOT NULL
);


ALTER TABLE public.fraud_logs OWNER TO payflow;

--
-- Name: idempotency_records; Type: TABLE; Schema: public; Owner: payflow
--

CREATE TABLE public.idempotency_records (
    id uuid NOT NULL,
    created_at timestamp(6) without time zone,
    idempotency_key character varying(255) NOT NULL,
    request_hash character varying(255) NOT NULL,
    request_path character varying(255) NOT NULL,
    response_payload oid,
    response_status integer,
    status character varying(255) NOT NULL,
    updated_at timestamp(6) without time zone,
    CONSTRAINT idempotency_records_status_check CHECK (((status)::text = ANY ((ARRAY['STARTED'::character varying, 'COMPLETED'::character varying, 'FAILED'::character varying])::text[])))
);


ALTER TABLE public.idempotency_records OWNER TO payflow;

--
-- Name: ledger_entries; Type: TABLE; Schema: public; Owner: payflow
--

CREATE TABLE public.ledger_entries (
    id uuid NOT NULL,
    amount numeric(19,4) NOT NULL,
    created_at timestamp(6) without time zone,
    direction character varying(255) NOT NULL,
    transaction_id uuid NOT NULL,
    wallet_id uuid,
    CONSTRAINT ledger_entries_direction_check CHECK (((direction)::text = ANY ((ARRAY['CREDIT'::character varying, 'DEBIT'::character varying])::text[])))
);


ALTER TABLE public.ledger_entries OWNER TO payflow;

--
-- Name: ledger_transactions; Type: TABLE; Schema: public; Owner: payflow
--

CREATE TABLE public.ledger_transactions (
    id uuid NOT NULL,
    created_at timestamp(6) without time zone,
    idempotency_key character varying(255) NOT NULL,
    status character varying(255) NOT NULL,
    type character varying(255) NOT NULL,
    CONSTRAINT ledger_transactions_status_check CHECK (((status)::text = ANY ((ARRAY['PENDING'::character varying, 'COMPLETED'::character varying, 'FAILED'::character varying])::text[]))),
    CONSTRAINT ledger_transactions_type_check CHECK (((type)::text = ANY ((ARRAY['DEPOSIT'::character varying, 'WITHDRAWAL'::character varying, 'TRANSFER'::character varying])::text[])))
);


ALTER TABLE public.ledger_transactions OWNER TO payflow;

--
-- Name: notifications; Type: TABLE; Schema: public; Owner: payflow
--

CREATE TABLE public.notifications (
    id uuid NOT NULL,
    created_at timestamp(6) without time zone,
    message character varying(255) NOT NULL,
    is_read boolean NOT NULL,
    wallet_id uuid NOT NULL
);


ALTER TABLE public.notifications OWNER TO payflow;

--
-- Name: outbox_events; Type: TABLE; Schema: public; Owner: payflow
--

CREATE TABLE public.outbox_events (
    id uuid NOT NULL,
    aggregate_id character varying(255) NOT NULL,
    aggregate_type character varying(255) NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    payload text NOT NULL,
    type character varying(255) NOT NULL
);


ALTER TABLE public.outbox_events OWNER TO payflow;

--
-- Name: revinfo; Type: TABLE; Schema: public; Owner: payflow
--

CREATE TABLE public.revinfo (
    rev integer NOT NULL,
    revtstmp bigint
);


ALTER TABLE public.revinfo OWNER TO payflow;

--
-- Name: revinfo_seq; Type: SEQUENCE; Schema: public; Owner: payflow
--

CREATE SEQUENCE public.revinfo_seq
    START WITH 1
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.revinfo_seq OWNER TO payflow;

--
-- Name: users; Type: TABLE; Schema: public; Owner: payflow
--

CREATE TABLE public.users (
    id uuid NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    email character varying(255),
    first_name character varying(255),
    last_name character varying(255),
    password_hash character varying(255) NOT NULL,
    status character varying(255),
    updated_at timestamp(6) without time zone,
    version bigint
);


ALTER TABLE public.users OWNER TO payflow;

--
-- Name: users_aud; Type: TABLE; Schema: public; Owner: payflow
--

CREATE TABLE public.users_aud (
    id uuid NOT NULL,
    rev integer NOT NULL,
    revtype smallint,
    created_at timestamp(6) without time zone,
    email character varying(255),
    first_name character varying(255),
    last_name character varying(255),
    password_hash character varying(255),
    status character varying(255),
    updated_at timestamp(6) without time zone
);


ALTER TABLE public.users_aud OWNER TO payflow;

--
-- Name: wallets; Type: TABLE; Schema: public; Owner: payflow
--

CREATE TABLE public.wallets (
    id uuid NOT NULL,
    balance numeric(19,4) NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    currency character varying(3) NOT NULL,
    status character varying(255) NOT NULL,
    updated_at timestamp(6) without time zone,
    user_id uuid NOT NULL,
    version bigint,
    CONSTRAINT wallets_status_check CHECK (((status)::text = ANY ((ARRAY['ACTIVE'::character varying, 'BLOCKED'::character varying, 'CLOSED'::character varying])::text[])))
);


ALTER TABLE public.wallets OWNER TO payflow;

--
-- Name: wallets_aud; Type: TABLE; Schema: public; Owner: payflow
--

CREATE TABLE public.wallets_aud (
    id uuid NOT NULL,
    rev integer NOT NULL,
    revtype smallint,
    balance numeric(19,4),
    created_at timestamp(6) without time zone,
    currency character varying(3),
    status character varying(255),
    updated_at timestamp(6) without time zone,
    user_id uuid,
    CONSTRAINT wallets_aud_status_check CHECK (((status)::text = ANY ((ARRAY['ACTIVE'::character varying, 'BLOCKED'::character varying, 'CLOSED'::character varying])::text[])))
);


ALTER TABLE public.wallets_aud OWNER TO payflow;

--
-- Name: flyway_schema_history flyway_schema_history_pk; Type: CONSTRAINT; Schema: public; Owner: payflow
--

ALTER TABLE ONLY public.flyway_schema_history
    ADD CONSTRAINT flyway_schema_history_pk PRIMARY KEY (installed_rank);


--
-- Name: fraud_logs fraud_logs_pkey; Type: CONSTRAINT; Schema: public; Owner: payflow
--

ALTER TABLE ONLY public.fraud_logs
    ADD CONSTRAINT fraud_logs_pkey PRIMARY KEY (id);


--
-- Name: idempotency_records idempotency_records_pkey; Type: CONSTRAINT; Schema: public; Owner: payflow
--

ALTER TABLE ONLY public.idempotency_records
    ADD CONSTRAINT idempotency_records_pkey PRIMARY KEY (id);


--
-- Name: ledger_entries ledger_entries_pkey; Type: CONSTRAINT; Schema: public; Owner: payflow
--

ALTER TABLE ONLY public.ledger_entries
    ADD CONSTRAINT ledger_entries_pkey PRIMARY KEY (id);


--
-- Name: ledger_transactions ledger_transactions_pkey; Type: CONSTRAINT; Schema: public; Owner: payflow
--

ALTER TABLE ONLY public.ledger_transactions
    ADD CONSTRAINT ledger_transactions_pkey PRIMARY KEY (id);


--
-- Name: notifications notifications_pkey; Type: CONSTRAINT; Schema: public; Owner: payflow
--

ALTER TABLE ONLY public.notifications
    ADD CONSTRAINT notifications_pkey PRIMARY KEY (id);


--
-- Name: outbox_events outbox_events_pkey; Type: CONSTRAINT; Schema: public; Owner: payflow
--

ALTER TABLE ONLY public.outbox_events
    ADD CONSTRAINT outbox_events_pkey PRIMARY KEY (id);


--
-- Name: revinfo revinfo_pkey; Type: CONSTRAINT; Schema: public; Owner: payflow
--

ALTER TABLE ONLY public.revinfo
    ADD CONSTRAINT revinfo_pkey PRIMARY KEY (rev);


--
-- Name: ledger_transactions uk_78vsrmtcnjtjj7isnp7rwk1s6; Type: CONSTRAINT; Schema: public; Owner: payflow
--

ALTER TABLE ONLY public.ledger_transactions
    ADD CONSTRAINT uk_78vsrmtcnjtjj7isnp7rwk1s6 UNIQUE (idempotency_key);


--
-- Name: idempotency_records uk_ol0gjg0uap11mq1y9ug506f1i; Type: CONSTRAINT; Schema: public; Owner: payflow
--

ALTER TABLE ONLY public.idempotency_records
    ADD CONSTRAINT uk_ol0gjg0uap11mq1y9ug506f1i UNIQUE (idempotency_key);


--
-- Name: wallets ukbkmwfsld7xrcansy6c7j1c50k; Type: CONSTRAINT; Schema: public; Owner: payflow
--

ALTER TABLE ONLY public.wallets
    ADD CONSTRAINT ukbkmwfsld7xrcansy6c7j1c50k UNIQUE (user_id, currency);


--
-- Name: users_aud users_aud_pkey; Type: CONSTRAINT; Schema: public; Owner: payflow
--

ALTER TABLE ONLY public.users_aud
    ADD CONSTRAINT users_aud_pkey PRIMARY KEY (rev, id);


--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: payflow
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


--
-- Name: wallets_aud wallets_aud_pkey; Type: CONSTRAINT; Schema: public; Owner: payflow
--

ALTER TABLE ONLY public.wallets_aud
    ADD CONSTRAINT wallets_aud_pkey PRIMARY KEY (rev, id);


--
-- Name: wallets wallets_pkey; Type: CONSTRAINT; Schema: public; Owner: payflow
--

ALTER TABLE ONLY public.wallets
    ADD CONSTRAINT wallets_pkey PRIMARY KEY (id);


--
-- Name: flyway_schema_history_s_idx; Type: INDEX; Schema: public; Owner: payflow
--

CREATE INDEX flyway_schema_history_s_idx ON public.flyway_schema_history USING btree (success);


--
-- Name: users_aud fkc4vk4tui2la36415jpgm9leoq; Type: FK CONSTRAINT; Schema: public; Owner: payflow
--

ALTER TABLE ONLY public.users_aud
    ADD CONSTRAINT fkc4vk4tui2la36415jpgm9leoq FOREIGN KEY (rev) REFERENCES public.revinfo(rev);


--
-- Name: wallets_aud fke5a4dfsg5v6u8jvt8410acxje; Type: FK CONSTRAINT; Schema: public; Owner: payflow
--

ALTER TABLE ONLY public.wallets_aud
    ADD CONSTRAINT fke5a4dfsg5v6u8jvt8410acxje FOREIGN KEY (rev) REFERENCES public.revinfo(rev);


--
-- PostgreSQL database dump complete
--
