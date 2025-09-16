CREATE TABLE IF NOT EXISTS inbound_events (
id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
source VARCHAR(50) NOT NULL,
event_id VARCHAR(100) NOT NULL,
payload JSONB NOT NULL,
signature_ok BOOLEAN NOT NULL DEFAULT FALSE,
received_at TIMESTAMPTZ NOT NULL DEFAULT now(),
UNIQUE(source, event_id)
);


CREATE TABLE IF NOT EXISTS dlq_events (
id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
source VARCHAR(50) NOT NULL,
event_id VARCHAR(100) NOT NULL,
payload JSONB NOT NULL,
error TEXT,
failed_at TIMESTAMPTZ NOT NULL DEFAULT now()
);