$ErrorActionPreference = 'Stop'

$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$databaseDirectory = Join-Path $root 'dev_program_DB\mydb'
$hsqldb = Join-Path $root 'Waseda-SE\lib\hsqldb.jar'
$port = 9001

function Test-DatabaseServer {
	$client = New-Object System.Net.Sockets.TcpClient
	try {
		$client.Connect('127.0.0.1', $port)
		return $true
	}
	catch {
		return $false
	}
	finally {
		$client.Dispose()
	}
}

if (Test-DatabaseServer) {
	Write-Output "Database server is already running on port $port."
	return
}

if (-not (Test-Path -LiteralPath $hsqldb)) {
	throw "HSQLDB JAR was not found: $hsqldb"
}

$process = Start-Process -FilePath 'java' `
	-ArgumentList "-cp `"$hsqldb`" org.hsqldb.Server -database mydb" `
	-WorkingDirectory $databaseDirectory `
	-WindowStyle Hidden `
	-PassThru

$deadline = (Get-Date).AddSeconds(10)
while ((Get-Date) -lt $deadline) {
	if (Test-DatabaseServer) {
		Write-Output "Database server started on port $port."
		return
	}
	if ($process.HasExited) {
		throw "Database server stopped during startup with exit code $($process.ExitCode)."
	}
	Start-Sleep -Milliseconds 200
}

throw "Database server did not start on port $port within 10 seconds."
