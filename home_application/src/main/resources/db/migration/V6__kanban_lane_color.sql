ALTER TABLE kanban_lane
    ADD COLUMN IF NOT EXISTS lane_color varchar(7) NOT NULL DEFAULT '#f8f6f1';

ALTER TABLE kanban_lane
    DROP CONSTRAINT IF EXISTS ck_kanban_lane_color;

ALTER TABLE kanban_lane
    ADD CONSTRAINT ck_kanban_lane_color CHECK (lane_color ~ '^#[0-9A-Fa-f]{6}$');
