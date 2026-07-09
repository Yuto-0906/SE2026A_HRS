#!/bin/bash
# =====================================================================
#  setup_database.command  ―  setup_database.ps1 の Mac版
#  ビルド後、setup.sql を実行してDBを初期化します（データはリセット）。
#  事前に DBサーバ（dev_program_DB/mydb/runServer.command）を起動しておいてください。
# =====================================================================
set -e
cd "$(dirname "$0")"
ROOT="$PWD"
HSQLDB="$ROOT/Waseda-SE/lib/hsqldb.jar"
BIN="$ROOT/Waseda-SE/bin"
SETUP_SQL="$ROOT/dev_program_DB/mydb/setup.sql"

# ビルド（build_mac.command を呼ぶ）
"$ROOT/build_mac.command"

# DBサーバが動いているか確認
if ! nc -z localhost 9001 2>/dev/null; then
	echo "DBサーバが起動していません。先に dev_program_DB/mydb/runServer.command を起動してください。"
	exit 1
fi

java -cp "$BIN:$HSQLDB" infrastructure.jdbc.DatabaseSetup "$SETUP_SQL"
