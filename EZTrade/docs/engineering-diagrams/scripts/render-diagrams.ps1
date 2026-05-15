$ErrorActionPreference = "Stop"

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$DocsRoot = (Resolve-Path (Join-Path $ScriptDir "..")).Path
$Image = if ($env:PLANTUML_DOCKER_IMAGE) { $env:PLANTUML_DOCKER_IMAGE } else { "plantuml/plantuml:latest" }

if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
    throw "Se requiere Docker para renderizar los diagramas."
}

function Get-RelativeUnixPath {
    param(
        [Parameter(Mandatory = $true)][string]$BasePath,
        [Parameter(Mandatory = $true)][string]$TargetPath
    )

    $resolvedBase = (Resolve-Path $BasePath).Path.TrimEnd("\", "/") + [System.IO.Path]::DirectorySeparatorChar
    $resolvedTarget = (Resolve-Path $TargetPath).Path
    $baseUri = New-Object System.Uri($resolvedBase)
    $targetUri = New-Object System.Uri($resolvedTarget)

    return [System.Uri]::UnescapeDataString($baseUri.MakeRelativeUri($targetUri).ToString())
}

function Invoke-DockerPlantUml {
    param(
        [Parameter(Mandatory = $true)][string[]]$Arguments
    )

    & docker @Arguments
    if ($LASTEXITCODE -ne 0) {
        throw "El comando Docker fallo con codigo de salida ${LASTEXITCODE}: docker $($Arguments -join ' ')"
    }
}

Write-Host "Usando imagen Docker de PlantUML: $Image"
Write-Host "Raiz de diagramas: $DocsRoot"

$files = Get-ChildItem -Path $DocsRoot -Recurse -Filter "*.puml" |
    Where-Object { $_.FullName -match "[\\/]plantuml[\\/]" } |
    Sort-Object FullName

$renderedCount = 0

foreach ($file in $files) {
    $plantumlDir = $file.Directory.FullName
    $categoryDir = Split-Path -Parent $plantumlDir
    $renderedDir = Join-Path $categoryDir "rendered"
    New-Item -ItemType Directory -Force -Path $renderedDir | Out-Null

    $relFile = Get-RelativeUnixPath -BasePath $DocsRoot -TargetPath $file.FullName
    $relRendered = Get-RelativeUnixPath -BasePath $DocsRoot -TargetPath $renderedDir

    Write-Host "Renderizando $relFile"

    Invoke-DockerPlantUml -Arguments @(
        "run", "--rm",
        "-v", "${DocsRoot}:/workspace",
        $Image,
        "-tpng",
        "-o", "/workspace/$relRendered",
        "/workspace/$relFile"
    )

    Invoke-DockerPlantUml -Arguments @(
        "run", "--rm",
        "-v", "${DocsRoot}:/workspace",
        $Image,
        "-tsvg",
        "-o", "/workspace/$relRendered",
        "/workspace/$relFile"
    )

    $renderedCount += 1
}

Write-Host "Renderizados $renderedCount diagrama(s) PlantUML a PNG y SVG."
