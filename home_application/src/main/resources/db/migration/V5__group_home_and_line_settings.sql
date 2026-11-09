INSERT INTO group_setting_master(key,init_value,remarks,create_date,update_date)
VALUES
    ('home_image_file','','ホーム画像ファイル名',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),
    ('line_enabled','false','LINE通知送信フラグ',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),
    ('line_url','','LINE API URL',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),
    ('line_token','','LINEトークン',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),
    ('line_destination_id','','LINE送信先ID',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)
ON CONFLICT(key) DO NOTHING;
