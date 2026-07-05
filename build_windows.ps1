$ErrorActionPreference = 'Stop'

$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$sourceRoot = Join-Path $root 'Waseda-SE\src'
$output = Join-Path $root 'Waseda-SE\bin'
$hsqldb = Join-Path $root 'Waseda-SE\lib\hsqldb.jar'

New-Item -ItemType Directory -Force -Path $output | Out-Null
$sources = Get-ChildItem -LiteralPath $sourceRoot -Recurse -Filter '*.java' |
    Select-Object -ExpandProperty FullName

& javac --release 17 --add-modules jdk.httpserver -encoding UTF-8 `
    -cp $hsqldb -d $output $sources

if ($LASTEXITCODE -ne 0) {
    throw "Java compilation failed with exit code $LASTEXITCODE"
}

Write-Output "Build completed: $output"
