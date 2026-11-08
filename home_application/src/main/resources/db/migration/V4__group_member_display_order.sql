ALTER TABLE group_member ADD COLUMN IF NOT EXISTS member_no integer NOT NULL DEFAULT 2147483647;

WITH ranked AS (
    SELECT group_id,user_id,(ROW_NUMBER() OVER(PARTITION BY group_id ORDER BY create_date,user_id))::integer AS no
    FROM group_member
)
UPDATE group_member gm SET member_no=ranked.no
FROM ranked
WHERE gm.group_id=ranked.group_id AND gm.user_id=ranked.user_id AND gm.member_no=2147483647;

CREATE INDEX IF NOT EXISTS idx_group_member_display_order ON group_member(group_id,member_no,create_date);
