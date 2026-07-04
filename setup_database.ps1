$ErrorActionPreference = 'Stop'

$root = Split-Path -Parent $MyInvocation.MyCommand.Path
& (Join-Path $root 'build_windows.ps1')

$classes = Join-Path $root 'Waseda-SE\bin'
$hsqldb = Join-Path $root 'Waseda-SE\lib\hsqldb.jar'
$setupSql = Join-Path $root 'dev_program_DB\mydb\setup.sql'
$classpath = "$classes;$hsqldb"

& java -cp $classpath infrastructure.jdbc.DatabaseSetup $setupSql

if ($LASTEXITCODE -ne 0) {
    throw "Database setup failed with exit code $LASTEXITCODE"
}
