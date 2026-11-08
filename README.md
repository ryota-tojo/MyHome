# Home Ledger

家計簿に特化した Spring Boot + Thymeleaf + PostgreSQL アプリケーションです。
PHP は実行構成から除外されています。

## 起動

1. `.env` に `DB_NAME`、`DB_USER`、`DB_PASSWORD` を設定します。
2. `./docker-restart.sh` を実行します。
3. `http://localhost:8081/login` を開きます。

初期管理者は `admin` / `adminadmin` です。初回ログイン直後にパスワード変更が必須です。
一般ユーザーは登録画面から作成でき、個人用家計グループと標準カテゴリが自動作成されます。

## 機能

- BCrypt認証、CSRF保護、権限の基盤
- 月次ダッシュボード（収入・支出・収支、カテゴリ別集計）
- 購入データの登録・一覧・削除
- カテゴリ別月次予算
- お付き合い帳
- 既存Java API群

## データ

PostgreSQLデータはDockerの `home_postgres_data` ボリュームに保存されます。ホストから接続する場合は、既存環境との競合を避けるため `localhost:5433` を使用します。
既存ボリュームには初期化SQLの変更が自動適用されないため、既存データを維持する場合は
`budgets(group_id, year, month, category_id)` と `fixed(group_id, year, month)` の
UNIQUE制約を手動マイグレーションしてください。

## 開発

```bash
cd home_api
./gradlew test
./gradlew bootRun
```

DB接続は環境変数 `DB_HOST`、`DB_PORT`、`DB_NAME`、`DB_USER`、`DB_PASSWORD` から読み込みます。
