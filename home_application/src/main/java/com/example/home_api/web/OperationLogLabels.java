package com.example.home_api.web;

import java.util.List;
import java.util.Map;

final class OperationLogLabels {
    private OperationLogLabels(){}
    static String screen(String path){
        if(path==null)return "不明な画面";
        if(path.equals("/admin")||path.equals("/admin/"))return "管理者ダッシュボード";
        if(path.matches("/admin/users/[^/]+/reset-password"))return "ユーザー詳細／パスワード再設定";
        if(path.matches("/admin/users/[^/]+.*"))return "ユーザー詳細";
        if(path.equals("/admin/users/new"))return "ユーザー新規登録";
        if(path.startsWith("/admin/users"))return "ユーザー一覧";
        if(path.matches("/admin/groups/[^/]+/members/.*")||path.matches("/admin/groups/[^/]+.*"))return "所属グループ詳細";
        if(path.equals("/admin/groups/new"))return "所属グループ新規登録";
        if(path.startsWith("/admin/groups"))return "所属グループ一覧";
        if(path.startsWith("/admin/settings/display"))return "表示設定";
        if(path.startsWith("/admin/settings/system")||path.equals("/admin/settings"))return "システム設定";
        if(path.startsWith("/admin/maintenance"))return "メンテナンス状態切替";
        if(path.startsWith("/admin/versions"))return "バージョン管理";
        if(path.startsWith("/admin/logs"))return "操作ログ";
        if(path.startsWith("/admin/data/backup")||path.startsWith("/admin/export"))return "データバックアップ";
        if(path.startsWith("/admin/data/import")||path.startsWith("/admin/import"))return "データ入れ替え";
        if(path.startsWith("/admin/data/initialize"))return "データ初期化";
        if(path.startsWith("/account/profile")||path.startsWith("/account/auto-login-url"))return "ユーザー情報";
        if(path.startsWith("/shopping"))return "購入データ";
        if(path.startsWith("/budgets"))return "予算";
        if(path.startsWith("/communication"))return "お付き合い帳";
        if(path.startsWith("/groups/select"))return "所属グループ選択";
        if(path.startsWith("/groups/setup"))return "所属グループ作成";
        if(path.startsWith("/groups/join"))return "所属グループ加入申請";
        if(path.startsWith("/group/settings"))return "所属グループ設定";
        if(path.equals("/group/members/order"))return "メンバー管理";
        if(path.matches("/group/members/[^/]+"))return "所属メンバー詳細";
        if(path.startsWith("/group/members"))return "メンバー管理";
        if(path.startsWith("/group/masters/categories")||path.startsWith("/group/feature/categories"))return "カテゴリー管理";
        if(path.startsWith("/group/masters/payments")||path.startsWith("/group/feature/payments"))return "支払い方法管理";
        if(path.startsWith("/group/masters/labels")||path.startsWith("/group/feature/labels"))return "ラベル管理";
        if(path.startsWith("/group/masters/kanban-lanes"))return "カンバン レーン管理";
        if(path.startsWith("/group/csv"))return "CSVエクスポート・インポート";
        if(path.startsWith("/group/kanban")||path.startsWith("/group/feature/kanban"))return "カンバン";
        if(path.startsWith("/budgets/yearly")||path.startsWith("/group/feature/budget-year"))return "予算一覧（年間）";
        if(path.startsWith("/budgets"))return "予算入力";
        if(path.startsWith("/shopping/templates"))return "購入データテンプレート入力";
        if(path.startsWith("/shopping"))return "購入データ入力";
        if(path.startsWith("/group/purchases/csv"))return "エクスポート・インポート";
        if(path.startsWith("/group/purchases"))return "購入データ管理";
        if(path.startsWith("/group/monthly"))return "月次処理";
        if(path.startsWith("/group/analysis/budget-actual"))return "予実対比";
        if(path.startsWith("/group/analysis/year-comparison"))return "前年対比";
        if(path.equals("/login"))return "ログイン";
        if(path.equals("/logout"))return "ログアウト";
        return "その他（"+path+"）";
    }
    static String action(String code,String method,String path){
        String c=code==null?"":code.toUpperCase();
        if(c.startsWith("SQL"))return "SQL処理";if(c.startsWith("API"))return "API処理";
        if(c.contains("DELETE")||c.contains("REMOVE")||"DELETE".equalsIgnoreCase(method))return "削除処理";
        if(c.contains("CREATE")||c.contains("ADD")||(path!=null&&path.endsWith("/new")&&"POST".equalsIgnoreCase(method)))return "新規処理";
        if("GET".equalsIgnoreCase(method))return "参照処理";
        return "更新処理";
    }
    static String result(Object status){try{int s=Integer.parseInt(String.valueOf(status));return s>=200&&s<400?"成功（"+s+"）":"失敗（"+s+"）";}catch(Exception e){return String.valueOf(status);}}
    static String target(String action,Object target){if(target==null||String.valueOf(target).isBlank())return "-";String prefix=action!=null&&action.contains("GROUP")||action!=null&&action.contains("MEMBER")||action!=null&&action.contains("LEADER")?"所属グループ・メンバー":"対象";return prefix+"："+target;}
    static List<Map<String,Object>> enrich(List<Map<String,Object>> rows){for(Map<String,Object> row:rows){String path=String.valueOf(row.getOrDefault("path","")),code=row.get("action")==null?null:String.valueOf(row.get("action")),method=String.valueOf(row.getOrDefault("method",""));row.put("screen_name",screen(path));row.put("action_name",action(code,method,path));row.put("result_name",result(row.get("status")));row.put("target_name",target(code,row.get("target_id")));if(row.get("detail")==null||String.valueOf(row.get("detail")).isBlank())row.put("detail_display",method+" "+path+" を実行");else row.put("detail_display",row.get("detail"));}return rows;}
}
