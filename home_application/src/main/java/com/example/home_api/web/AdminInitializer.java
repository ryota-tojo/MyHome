package com.example.home_api.web;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
public class AdminInitializer implements ApplicationRunner {
    private final JdbcTemplate jdbc;
    private final PasswordEncoder encoder;

    public AdminInitializer(JdbcTemplate jdbc, PasswordEncoder encoder) {
        this.jdbc = jdbc;
        this.encoder = encoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        jdbc.update("INSERT INTO group_setting_master(key,init_value,remarks,create_date,update_date) VALUES ('display_year',?,'家計簿の表示対象年',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP) ON CONFLICT(key) DO NOTHING", String.valueOf(java.time.LocalDate.now().getYear()));
        for (String groupId : jdbc.queryForList("SELECT g.group_id FROM \"group\" g WHERE g.status='ACTIVE' AND NOT EXISTS(SELECT 1 FROM payment_method p WHERE p.group_id=g.group_id)", String.class))
            for (int i = 0; i < GroupDefaults.PAYMENT_METHODS.size(); i++)
                jdbc.update("INSERT INTO payment_method(id,group_id,payment_no,payment_name,disabled_flag) VALUES (?,?,?,?,0)", "pay-" + java.util.UUID.randomUUID().toString().substring(0, 12), groupId, i + 1, GroupDefaults.PAYMENT_METHODS.get(i));
        jdbc.execute("INSERT INTO system_state(id) VALUES(1) ON CONFLICT(id) DO NOTHING");
        jdbc.update("UPDATE system_setting SET key='uploaded_font.legacy',description=value,updated_at=CURRENT_TIMESTAMP WHERE key='uploaded_font' AND NOT EXISTS(SELECT 1 FROM system_setting WHERE key='uploaded_font.legacy')");
        jdbc.update("DELETE FROM system_setting WHERE key='uploaded_font'");
        putDefault("page_size", "20", "一覧の表示件数");
        putDefault("log_page_size", "50", "ログの表示件数");
        putDefault("admin_header_color", "#EBEBEB", "管理者画面ヘッダ色");
        putDefault("admin_maintenance_header_color", "#f8d7da", "メンテナンス時ヘッダ色");
        putDefault("admin_body_color", "#e1fad7", "管理者画面ボディ色");
        putDefault("admin_font_family", "system-ui", "管理者画面フォント");
        putDefault("admin_font_size", "16", "管理者画面フォントサイズ(px)");
        putDefault("user_header_color", "#EBEBEB", "一般画面ヘッダ色");
        putDefault("user_maintenance_header_color", "#f8d7da", "一般画面メンテナンス時ヘッダ色");
        putDefault("user_body_color", "#FAEBD7", "一般画面ボディ色");
        putDefault("user_font_family", "system-ui", "一般画面フォント");
        putDefault("user_font_size", "16", "一般画面フォントサイズ(px)");
        putDefault("line_url", "", "LINE通知URL");
        putDefault("line_token", "", "LINE通知トークン");
        putDefault("line_destination_id", "", "LINE送信先ID");
        putDefault("line_enabled", "false", "LINE通知送信フラグ");
        jdbc.update("UPDATE system_setting SET description='LINE API URL' WHERE key='line_url'");
        jdbc.update("UPDATE system_setting SET description='LINE トークン' WHERE key='line_token'");
        jdbc.update("UPDATE system_setting SET description='LINE 送信先ID' WHERE key='line_destination_id'");
        jdbc.update("UPDATE system_setting SET description='LINE 通知送信フラグ' WHERE key='line_enabled'");
        putDefault("admin_debug_enabled", "false", "管理者デバッグログ出力");
        putDefault("admin_debug_retention_days", "30", "管理者デバッグログ保管日数");
        putDefault("user_debug_enabled", "false", "一般デバッグログ出力");
        putDefault("user_debug_retention_days", "30", "一般デバッグログ保管日数");
        putDefault("backup_enabled", "false", "自動バックアップ");
        putDefault("backup_interval_days", "1", "自動バックアップ間隔(日)");
        putDefault("backup_time", "03:00", "自動バックアップ実行時刻");
        putDefault("session_timeout_minutes", "30", "セッション有効時間");
        putDefault("login_failure_limit", "5", "ログイン失敗許容回数");
        putDefault("account_lock_minutes", "30", "アカウントロック時間");
        putDefault("password_min_length", "8", "パスワード最小文字数");
        putDefault("timezone", "Asia/Tokyo", "タイムゾーン");
        putDefault("datetime_format", "yyyy/MM/dd HH:mm:ss", "日時表示形式");
        putDefault("select_search_threshold", "8", "検索可能プルダウン切替件数");
        putDefault("flash_message_seconds", "5", "登録・更新メッセージ表示秒数");
        putDefault("ui_scale", "100", "画面全体の拡大率(%)");
        putDefault("font_size", "16", "共通フォントサイズ(px)");
        jdbc.update("UPDATE system_setting SET value='#EBEBEB',updated_at=CURRENT_TIMESTAMP WHERE key IN ('admin_header_color','user_header_color') AND value IN ('#d5f2ca','#e8e8e8','#f8f9fa')");
        jdbc.update("UPDATE system_setting SET value='#FAEBD7',updated_at=CURRENT_TIMESTAMP WHERE key='user_body_color' AND lower(value)='antiquewhite'");
        for (DefaultVersion version : DefaultVersion.values())
            jdbc.update("INSERT INTO version_info(version,published_at,content,enabled) VALUES (?,?,?,?) ON CONFLICT(version) DO NOTHING", version.version(), version.publishedAt(), version.content(), version.enabled());
        LocalDateTime now = LocalDateTime.now();
        var admin = jdbc.queryForList("SELECT user_id,user_password FROM \"user\" WHERE permission_flag=2 AND delete_flag=0 ORDER BY create_date LIMIT 1");
        if (admin.isEmpty()) {
            jdbc.update("INSERT INTO \"user\"(user_id,user_password,user_name,permission_flag,approve_flag,delete_flag,password_change_required,approve_date,create_date,update_date) VALUES ('admin',?,'管理者',2,1,0,1,?,?,?)", encoder.encode("adminadmin"), now, now, now);
        } else {
            String password = String.valueOf(admin.get(0).get("user_password"));
            if (!password.startsWith("$2")) {
                jdbc.update("UPDATE \"user\" SET user_password=?,permission_flag=2,approve_flag=1,delete_flag=0,password_change_required=1,update_date=? WHERE user_id=?", encoder.encode("adminadmin"), now, admin.get(0).get("user_id"));
            }
        }
        jdbc.update("DELETE FROM group_member WHERE user_id IN (SELECT user_id FROM \"user\" WHERE permission_flag=2)");
        jdbc.update("UPDATE \"user\" SET password_change_required=0 WHERE permission_flag<>2 AND password_change_required<>0");
        jdbc.update("UPDATE \"user\" SET approve_flag=1,approve_date=COALESCE(approve_date,CURRENT_TIMESTAMP),update_date=CURRENT_TIMESTAMP WHERE permission_flag<>2 AND delete_flag=0 AND approve_flag<>1");
        jdbc.update("UPDATE \"group\" g SET status='DELETED',update_date=CURRENT_TIMESTAMP WHERE NOT EXISTS(SELECT 1 FROM group_member gm JOIN \"user\" u ON u.user_id=gm.user_id WHERE gm.group_id=g.group_id AND gm.leader_flag=1 AND gm.approve_flag=1 AND u.delete_flag=0)");
    }

    private void putDefault(String key, String value, String description) {
        jdbc.update("INSERT INTO system_setting(key,value,description) VALUES(?,?,?) ON CONFLICT(key) DO NOTHING", key, value, description);
    }
}


