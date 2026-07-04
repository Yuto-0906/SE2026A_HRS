$ErrorActionPreference = 'Stop'

$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$mainOutput = Join-Path $root 'tmp\test-main-classes'
$testOutput = Join-Path $root 'tmp\test-classes'
$hsqldb = Join-Path $root 'Waseda-SE\lib\hsqldb.jar'
$setupSql = Join-Path $root 'dev_program_DB\mydb\setup.sql'

New-Item -ItemType Directory -Force -Path $mainOutput | Out-Null
New-Item -ItemType Directory -Force -Path $testOutput | Out-Null

$mainSources = Get-ChildItem -LiteralPath (Join-Path $root 'Waseda-SE\src') `
    -Recurse -Filter '*.java' | Select-Object -ExpandProperty FullName
& javac --release 17 --add-modules jdk.httpserver -encoding UTF-8 `
    -cp $hsqldb -d $mainOutput $mainSources
if ($LASTEXITCODE -ne 0) {
    throw "Main source compilation failed with exit code $LASTEXITCODE"
}

$testSources = Get-ChildItem -LiteralPath (Join-Path $root 'Waseda-SE\test') `
    -Recurse -Filter '*.java' | Select-Object -ExpandProperty FullName
$testCompileClasspath = "$mainOutput;$hsqldb"
& javac --release 17 --add-modules jdk.httpserver -encoding UTF-8 `
    -cp $testCompileClasspath -d $testOutput $testSources
if ($LASTEXITCODE -ne 0) {
    throw "Test source compilation failed with exit code $LASTEXITCODE"
}

$testRuntimeClasspath = "$mainOutput;$testOutput;$hsqldb"
& java --add-modules jdk.httpserver -cp $testRuntimeClasspath `
    domain.reservation.ReservationStateTest
if ($LASTEXITCODE -ne 0) {
    throw "ReservationStateTest failed with exit code $LASTEXITCODE"
}

& java --add-modules jdk.httpserver -cp $testRuntimeClasspath `
    integration.HotelReservationIntegrationTest $setupSql
if ($LASTEXITCODE -ne 0) {
    throw "HotelReservationIntegrationTest failed with exit code $LASTEXITCODE"
}

& java --add-modules jdk.httpserver -cp $testRuntimeClasspath `
    integration.WebHttpSmokeTest $setupSql
if ($LASTEXITCODE -ne 0) {
    throw "WebHttpSmokeTest failed with exit code $LASTEXITCODE"
}

Write-Output 'All tests passed.'
