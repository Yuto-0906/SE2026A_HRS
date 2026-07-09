#!/bin/bash
# Copyright(C) 2007 National Institute of Informatics, All rights reserved.
# HSQLDB 管理GUI起動（Mac版 / runManager.bat 相当）
# 先に runServer.command でサーバを起動しておいてください。

# このスクリプト自身の場所へ移動（../lib を確実に解決するため）
cd "$(dirname "$0")" || exit 1

java -classpath ../lib/hsqldb.jar org.hsqldb.util.DatabaseManager -url jdbc:hsqldb:hsql://localhost
