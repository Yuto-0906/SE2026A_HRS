#!/bin/bash
# =====================================================================
#  build_mac.command  ―  build_windows.ps1 の Mac版
#  Waseda-SE/src をコンパイルして Waseda-SE/bin に出力します。
# =====================================================================
set -e
cd "$(dirname "$0")"
ROOT="$PWD"
HSQLDB="$ROOT/Waseda-SE/lib/hsqldb.jar"
OUT="$ROOT/Waseda-SE/bin"

mkdir -p "$OUT"
# Waseda-SE 内へ移動して相対パスでコンパイル（パスに空白が含まれても安全なため）
( cd "$ROOT/Waseda-SE" \
	&& javac --release 17 --add-modules jdk.httpserver -encoding UTF-8 \
		-cp "lib/hsqldb.jar" -d "bin" $(find src -name '*.java') )

echo "Build completed: $OUT"
