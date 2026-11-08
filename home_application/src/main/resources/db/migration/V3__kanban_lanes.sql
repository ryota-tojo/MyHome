CREATE TABLE IF NOT EXISTS kanban_lane (
    id varchar(20) PRIMARY KEY,
    group_id varchar NOT NULL REFERENCES "group"(group_id) ON UPDATE CASCADE ON DELETE CASCADE,
    lane_no integer NOT NULL,
    lane_name varchar(100) NOT NULL,
    disabled_flag integer NOT NULL DEFAULT 0,
    create_date timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_date timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_kanban_lane_name UNIQUE(group_id,lane_name)
);

INSERT INTO kanban_lane(id,group_id,lane_no,lane_name)
SELECT 'lane-'||substring(md5(g.group_id||':TODO'),1,12),g.group_id,1,'未着手' FROM "group" g
ON CONFLICT DO NOTHING;
INSERT INTO kanban_lane(id,group_id,lane_no,lane_name)
SELECT 'lane-'||substring(md5(g.group_id||':DOING'),1,12),g.group_id,2,'進行中' FROM "group" g
ON CONFLICT DO NOTHING;
INSERT INTO kanban_lane(id,group_id,lane_no,lane_name)
SELECT 'lane-'||substring(md5(g.group_id||':DONE'),1,12),g.group_id,3,'完了' FROM "group" g
ON CONFLICT DO NOTHING;

UPDATE kanban_card c SET status=l.id
FROM kanban_lane l
WHERE c.group_id=l.group_id
  AND ((c.status='TODO' AND l.lane_name='未着手')
    OR (c.status='DOING' AND l.lane_name='進行中')
    OR (c.status='DONE' AND l.lane_name='完了'));

ALTER TABLE kanban_card ALTER COLUMN status DROP DEFAULT;
DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname='fk_kanban_card_lane') THEN
        ALTER TABLE kanban_card ADD CONSTRAINT fk_kanban_card_lane
            FOREIGN KEY(status) REFERENCES kanban_lane(id) ON UPDATE CASCADE ON DELETE RESTRICT;
    END IF;
END $$;
CREATE INDEX IF NOT EXISTS idx_kanban_lane_group_order ON kanban_lane(group_id,lane_no);
