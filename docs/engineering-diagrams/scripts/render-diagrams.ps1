$ErrorActionPreference = "Stop"

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$DocsRoot = (Resolve-Path (Join-Path $ScriptDir "..")).Path
$Image = if ($env:PLANTUML_DOCKER_IMAGE) { $env:PLANTUML_DOCKER_IMAGE } else { "plantuml/plantuml:latest" }
$DefaultJar = Join-Path $env:TEMP "plantuml.jar"
$Jar = if ($env:PLANTUML_JAR) {
    (Resolve-Path $env:PLANTUML_JAR).Path
} elseif (Test-Path $DefaultJar) {
    (Resolve-Path $DefaultJar).Path
} else {
    $null
}

$UseDocker = $false
if (Get-Command docker -ErrorAction SilentlyContinue) {
    try {
        & docker info *> $null
        $UseDocker = $LASTEXITCODE -eq 0
    } catch {
        $UseDocker = $false
        $Error.Clear()
    }
}

if (-not $UseDocker -and -not $Jar) {
    throw "Docker or a local PlantUML jar is required to render the diagrams."
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
        throw "Docker command failed with exit code ${LASTEXITCODE}: docker $($Arguments -join ' ')"
    }
}

Write-Host "Using PlantUML Docker image: $Image"
Write-Host "Diagram root: $DocsRoot"
if ($UseDocker) {
    Write-Host "Renderer: Docker"
} else {
    Write-Host "Renderer: local PlantUML jar ($Jar)"
}

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

    Write-Host "Rendering $relFile"

    if ($UseDocker) {
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
    } else {
        & java -jar $Jar -tpng -o "..\rendered" $file.FullName
        if ($LASTEXITCODE -ne 0) {
            throw "PlantUML PNG render failed with exit code ${LASTEXITCODE}: $relFile"
        }

        & java -jar $Jar -tsvg -o "..\rendered" $file.FullName
        if ($LASTEXITCODE -ne 0) {
            throw "PlantUML SVG render failed with exit code ${LASTEXITCODE}: $relFile"
        }
    }

    $renderedCount += 1
}

Write-Host "Rendered $renderedCount PlantUML diagram(s) to PNG and SVG."
