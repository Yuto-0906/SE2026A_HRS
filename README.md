# SE2026A Hotel Reservation System

ソフトウェア工学Aのチーム開発で作成するホテル予約システムである。Java標準`HttpServer`，JDBC，HSQLDBを使用し，Webブラウザから予約と受付係業務を操作できる。

## 実装済みのユースケース

- 部屋を予約する
- 自分の予約一覧を確認する
- 予約をキャンセルする
- 受付係が全予約を確認する
- チェックインする
- チェックアウトする

## アーキテクチャ

講義資料に沿った多層アーキテクチャを採用している。

```text
Webブラウザ
    ↓
app.web              ユーザインタフェース層・boundary
    ↓
app.reservation
app.checkin
app.checkout         アプリケーション層・control
    ↓
domain               ドメイン層・entity
    ↓
JDBC DAO             データソース層
    ↓
HSQLDB
```

WebサーバにはJava標準の`com.sun.net.httpserver.HttpServer`を使用する。

基本設計の根拠，設計判断，UML図は[基本設計](基本設計/)に格納している。

## 必要な環境

- Java 17以上
- Windows PowerShell
- Webブラウザ

HSQLDBのJARはリポジトリ内に含まれている。

## 初回起動

### 1．DBを初期化する

別のPowerShellをリポジトリのルートで開き，次を実行する。

```powershell
.\setup_database.ps1
```

この操作は既存の予約データを削除するため，初回またはDBをリセットするときだけ実行する。
HSQLDBサーバーは，必要なときにスクリプトが自動で起動する。

### 2．Webシステムを起動する

```powershell
.\run_web.ps1
```

起動後，ブラウザで次を開く。

```text
http://127.0.0.1:8080
```

## ビルド

```powershell
.\build_windows.ps1
```

コンパイル結果は`Waseda-SE/bin`へ出力される。

## テスト

```powershell
.\test_windows.ps1
```

テストでは実運用DBを変更せず，メモリ上のHSQLDBを使用する。次を検証する。

- 予約状態の正しい遷移と不正遷移の拒否
- 5室予約後の満室判定
- キャンセル後の部屋再割当
- 予約所有者以外によるキャンセルの拒否
- チェックインとチェックアウト
- 宿泊料金6000円
- Webトップ画面，予約フォーム，予約一覧，受付係画面のHTTP応答

## 主なディレクトリ

```text
基本設計/                 アーキテクチャ設計，UML図，DB設計
要求分析/                 ユースケース記述，アクティビティ図
システム分析/             分析レベルクラス図，コミュニケーション図
Waseda-SE/src/app/web/    Web画面とHTTP処理
Waseda-SE/src/app/        ユースケース制御
Waseda-SE/src/domain/     ドメインとDAO
Waseda-SE/test/           自動テスト
dev_program_DB/mydb/      HSQLDBと初期化SQL
```

## 制約

本システムは授業内のローカル実行を対象とする。受付係認証，HTTPS，インターネット公開を前提としたセキュリティ対策は対象外である。
