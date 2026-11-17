ALTER TABLE category ADD COLUMN IF NOT EXISTS deleted_flag integer NOT NULL DEFAULT 0;
ALTER TABLE payment_method ADD COLUMN IF NOT EXISTS deleted_flag integer NOT NULL DEFAULT 0;
ALTER TABLE shopping_label ADD COLUMN IF NOT EXISTS deleted_flag integer NOT NULL DEFAULT 0;

CREATE TABLE IF NOT EXISTS monthly_master_snapshot (
    group_id varchar NOT NULL,
    year integer NOT NULL,
    month integer NOT NULL,
    master_type varchar NOT NULL,
    master_id varchar NOT NULL,
    display_name varchar NOT NULL,
    display_order integer NOT NULL DEFAULT 0,
    status_at_close varchar NOT NULL DEFAULT 'ACTIVE',
    created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY(group_id,year,month,master_type,master_id),
    FOREIGN KEY(group_id) REFERENCES "group"(group_id) ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_monthly_master_snapshot_period
    ON monthly_master_snapshot(group_id,year,month,master_type,display_order);
