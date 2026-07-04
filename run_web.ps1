$ErrorActionPreference = 'Stop'

$root = Split-Path -Parent $MyInvocation.MyCommand.Path
& (Join-Path $root 'build_windows.ps1')

$classes = Join-Path $root 'Waseda-SE\bin'
$hsqldb = Join-Path $root 'Waseda-SE\lib\hsqldb.jar'
$classpath = "$classes;$hsqldb"

& java --add-modules jdk.httpserver -cp $classpath app.web.WebServer

if ($LASTEXITCODE -ne 0) {
    throw "Web server failed with exit code $LASTEXITCODE"
}
