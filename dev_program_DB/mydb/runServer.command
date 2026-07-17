#!/bin/bash
# Copyright(C) 2007 National Institute of Informatics, All rights reserved.
# HSQLDB サーバ起動（Mac版 / runServer.bat 相当）
# Finderでダブルクリック、またはターミナルで ./runServer.command で起動できます。

# このスクリプト自身の場所へ移動（../lib を確実に解決するため）
cd "$(dirname "$0")" || exit 1

echo "HSQLDB サーバを起動します（停止するには Control-C）..."
java -classpath ../lib/hsqldb.jar org.hsqldb.Server -database mydb
