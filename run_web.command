#!/bin/bash
# =====================================================================
#  run_web.command  ―  run_web.ps1 の Mac版（ワンクリック全部入り）
#  ①ビルド → ②DBサーバ起動 → ③DB初期化 → ④Webサーバ起動
#  → ⑤ブラウザで http://127.0.0.1:8080 を開く
#  停止するには、このウィンドウで Control-C を押してください。
#
#  ※Windows版 run_web.ps1 は「ビルド→Web起動」のみですが、
#    こちらはデモ用にDB起動・初期化・ブラウザ表示までまとめています。
# =====================================================================
cd "$(dirname "$0")"
ROOT="$PWD"
HSQLDB="$ROOT/Waseda-SE/lib/hsqldb.jar"
BIN="$ROOT/Waseda-SE/bin"
DB_DIR="$ROOT/dev_program_DB/mydb"
CP="$BIN:$HSQLDB"

STARTED_DB=0
DBPID=""
WEBPID=""

cleanup() {
	echo ""
	echo "停止中..."
	# まず穏やかに終了要求（SIGTERM）
	[ -n "$WEBPID" ] && kill "$WEBPID" 2>/dev/null
	[ "$STARTED_DB" = "1" ] && [ -n "$DBPID" ] && kill "$DBPID" 2>/dev/null
	# 残っていれば強制終了（SIGKILL）
	sleep 1
	[ -n "$WEBPID" ] && kill -9 "$WEBPID" 2>/dev/null
	[ "$STARTED_DB" = "1" ] && [ -n "$DBPID" ] && kill -9 "$DBPID" 2>/dev/null
	echo "停止しました。"
	exit 0
}
trap cleanup INT TERM

# ① ビルド（build_mac.command を呼ぶ）
echo "① ビルド中..."
"$ROOT/build_mac.command" || { echo "ビルドに失敗しました。"; exit 1; }

# ② DBサーバ（HSQLDB, ポート9001）が未起動なら起動
if nc -z localhost 9001 2>/dev/null; then
	echo "② 既存のDBサーバに接続します（データは初期化しません）。"
else
	echo "② DBサーバを起動します..."
	# サブシェルを使わずjava自体を起動し、停止時に確実にkillできるようにする
	java -classpath "$DB_DIR/../lib/hsqldb.jar" org.hsqldb.Server -database "$DB_DIR/mydb" > /tmp/hrs_db.log 2>&1 &
	DBPID=$!
	STARTED_DB=1
	for _ in $(seq 1 20); do
		nc -z localhost 9001 2>/dev/null && break
		sleep 0.5
	done
	# ③ DB初期化（このスクリプトが新規起動したときだけ）
	echo "③ DBを初期化します..."
	java -cp "$CP" infrastructure.jdbc.DatabaseSetup "$DB_DIR/setup.sql"
fi

# ④ Webサーバ起動
echo "④ Webサーバを起動します..."
java --add-modules jdk.httpserver -cp "$CP" app.web.WebServer &
WEBPID=$!

# ⑤ ブラウザで開く
sleep 1
open "http://127.0.0.1:8080/" 2>/dev/null

echo ""
echo "----------------------------------------------------------"
echo "  ブラウザで http://127.0.0.1:8080/ を開きました。"
echo "  停止するには Control-C を押してください。"
echo "----------------------------------------------------------"

wait "$WEBPID"
