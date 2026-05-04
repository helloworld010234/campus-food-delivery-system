param(
    [string]$BaseUrl = "http://localhost:8081/api",
    [string]$Username = "admin",
    [string]$Password = "123456",
    [int]$PageSize = 100,
    [string]$MappingCsvPath = ".\scripts\dish-image-mapping.csv",
    [string]$FallbackImagePath = "",
    [string]$ReportCsvPath = ".\scripts\dish-image-migration-report.csv",
    [string]$UnresolvedCsvPath = ".\scripts\dish-image-unresolved.csv",
    [int]$TimeoutSec = 10,
    [switch]$DryRun
)

$ErrorActionPreference = "Stop"

function Ensure-Dir {
    param([string]$FilePath)
    $parent = Split-Path -Parent $FilePath
    if (-not [string]::IsNullOrWhiteSpace($parent) -and -not (Test-Path -LiteralPath $parent)) {
        New-Item -ItemType Directory -Path $parent -Force | Out-Null
    }
}

function Get-Origin {
    param([string]$Url)
    $u = [System.Uri]$Url
    return "$($u.Scheme)://$($u.Authority)"
}

function New-AuthHeaders {
    param([string]$Token)
    return @{
        token = $Token
    }
}

function Invoke-Api {
    param(
        [string]$Method,
        [string]$Path,
        [object]$Body,
        [hashtable]$Headers
    )

    $uri = if ($Path.StartsWith("http://") -or $Path.StartsWith("https://")) {
        $Path
    } else {
        "$BaseUrl$Path"
    }

    if ($Method -eq "GET" -or $Method -eq "DELETE") {
        return Invoke-RestMethod -Method $Method -Uri $uri -Headers $Headers -TimeoutSec $TimeoutSec
    }

    $json = $null
    if ($Body -ne $null) {
        $json = $Body | ConvertTo-Json -Depth 30
    }
    return Invoke-RestMethod -Method $Method -Uri $uri -Headers $Headers -ContentType "application/json" -Body $json -TimeoutSec $TimeoutSec
}

function Ensure-SuccessResult {
    param(
        [object]$Response,
        [string]$Action
    )
    if ($null -eq $Response) {
        throw "$Action failed: empty response"
    }
    if ($null -ne $Response.code -and [int]$Response.code -ne 1) {
        $msg = if ($Response.msg) { $Response.msg } elseif ($Response.message) { $Response.message } else { "unknown error" }
        throw "$Action failed: $msg"
    }
}

function Resolve-ImageUrlForCheck {
    param(
        [string]$ImageUrl,
        [string]$Origin,
        [string]$BasePath
    )
    if ([string]::IsNullOrWhiteSpace($ImageUrl)) {
        return $null
    }
    if ($ImageUrl.StartsWith("http://") -or $ImageUrl.StartsWith("https://")) {
        return $ImageUrl
    }
    if ($ImageUrl.StartsWith("/")) {
        if ($ImageUrl.StartsWith("/api/") -and $BasePath.StartsWith("/admin")) {
            $ImageUrl = $ImageUrl -replace "^/api/", "/admin/"
        }
        return "$Origin$ImageUrl"
    }
    return $ImageUrl
}

function Get-HttpStatus {
    param([string]$Url)
    if ([string]::IsNullOrWhiteSpace($Url)) {
        return -1
    }
    try {
        $r = Invoke-WebRequest -Uri $Url -Method Head -UseBasicParsing -TimeoutSec $TimeoutSec
        return [int]$r.StatusCode
    } catch {
        if ($_.Exception.Response) {
            return [int]$_.Exception.Response.StatusCode.value__
        }
        return -1
    }
}

function Upload-LocalImage {
    param(
        [string]$Token,
        [string]$LocalImagePath
    )
    if (-not (Test-Path -LiteralPath $LocalImagePath)) {
        throw "local image not found: $LocalImagePath"
    }

    $uploadUrl = "$BaseUrl/common/upload"
    $curlOutput = & curl.exe -sS -X POST $uploadUrl -H ("token: " + $Token) -F ("file=@" + $LocalImagePath)
    if ($LASTEXITCODE -ne 0) {
        throw "upload failed by curl, exit code: $LASTEXITCODE"
    }

    $resp = $curlOutput | ConvertFrom-Json
    Ensure-SuccessResult -Response $resp -Action ("upload image " + $LocalImagePath)
    return [string]$resp.data
}

function Get-AllDishes {
    param([hashtable]$Headers)
    $all = @()
    $page = 1
    while ($true) {
        $resp = Invoke-Api -Method "GET" -Path ("/dish/page?page={0}&pageSize={1}" -f $page, $PageSize) -Headers $Headers
        Ensure-SuccessResult -Response $resp -Action ("list dish page " + $page)
        $records = @()
        if ($resp.data -and $resp.data.records) {
            $records = @($resp.data.records)
        }
        if ($records.Count -eq 0) {
            break
        }
        $all += $records
        if ($records.Count -lt $PageSize) {
            break
        }
        $page++
    }
    return $all
}

function Build-UpdateBody {
    param(
        [object]$DishDetail,
        [string]$NewImageUrl
    )
    return @{
        id = $DishDetail.id
        name = $DishDetail.name
        categoryId = $DishDetail.categoryId
        price = $DishDetail.price
        image = $NewImageUrl
        description = $DishDetail.description
        status = $DishDetail.status
        flavors = $(if ($DishDetail.flavors) { $DishDetail.flavors } else { @() })
    }
}

$origin = Get-Origin -Url $BaseUrl
$basePath = ([System.Uri]$BaseUrl).AbsolutePath

Write-Host "Login: $BaseUrl/employee/login"
$loginResp = Invoke-Api -Method "POST" -Path "/employee/login" -Body @{ username = $Username; password = $Password }
Ensure-SuccessResult -Response $loginResp -Action "login"
$token = [string]$loginResp.data.token
if ([string]::IsNullOrWhiteSpace($token)) {
    throw "login failed: token is empty"
}
$headers = New-AuthHeaders -Token $token

if (-not [string]::IsNullOrWhiteSpace($FallbackImagePath) -and -not (Test-Path -LiteralPath $FallbackImagePath)) {
    throw "fallback image not found: $FallbackImagePath"
}

$mapping = @{}
if (Test-Path -LiteralPath $MappingCsvPath) {
    $rows = Import-Csv -LiteralPath $MappingCsvPath
    foreach ($row in $rows) {
        if ($row.id -and $row.local_image_path) {
            $mapping[[string]$row.id] = [string]$row.local_image_path
        }
    }
} else {
    Ensure-Dir -FilePath $MappingCsvPath
    "id,local_image_path" | Out-File -FilePath $MappingCsvPath -Encoding UTF8
}

Write-Host "Fetch dish list..."
$dishes = Get-AllDishes -Headers $headers
Write-Host ("Total dishes: " + $dishes.Count)

$uploadedCache = @{}
$report = New-Object System.Collections.Generic.List[Object]
$unresolved = New-Object System.Collections.Generic.List[Object]

$migrated = 0
$skipped = 0
$needFix = 0

foreach ($dish in $dishes) {
    $id = [string]$dish.id
    $name = [string]$dish.name
    $oldImage = [string]$dish.image
    $checkUrl = Resolve-ImageUrlForCheck -ImageUrl $oldImage -Origin $origin -BasePath $basePath
    $status = Get-HttpStatus -Url $checkUrl

    if ($status -eq 200) {
        $skipped++
        $report.Add([pscustomobject]@{
            id = $id
            name = $name
            old_image = $oldImage
            status = $status
            action = "skip"
            new_image = ""
            note = "reachable"
        }) | Out-Null
        continue
    }

    $needFix++
    $localImage = $null
    if ($mapping.ContainsKey($id)) {
        $localImage = $mapping[$id]
    } elseif (-not [string]::IsNullOrWhiteSpace($FallbackImagePath)) {
        $localImage = $FallbackImagePath
    }

    if ([string]::IsNullOrWhiteSpace($localImage)) {
        $unresolved.Add([pscustomobject]@{
            id = $id
            name = $name
            old_image = $oldImage
            status = $status
            local_image_path = ""
            note = "need mapping image file"
        }) | Out-Null
        $report.Add([pscustomobject]@{
            id = $id
            name = $name
            old_image = $oldImage
            status = $status
            action = "unresolved"
            new_image = ""
            note = "no mapping and no fallback image"
        }) | Out-Null
        continue
    }

    if (-not (Test-Path -LiteralPath $localImage)) {
        $unresolved.Add([pscustomobject]@{
            id = $id
            name = $name
            old_image = $oldImage
            status = $status
            local_image_path = $localImage
            note = "local image path not found"
        }) | Out-Null
        $report.Add([pscustomobject]@{
            id = $id
            name = $name
            old_image = $oldImage
            status = $status
            action = "unresolved"
            new_image = ""
            note = "local image file not found"
        }) | Out-Null
        continue
    }

    if ($DryRun) {
        $report.Add([pscustomobject]@{
            id = $id
            name = $name
            old_image = $oldImage
            status = $status
            action = "dry-run"
            new_image = ""
            note = ("would replace by local file: " + $localImage)
        }) | Out-Null
        continue
    }

    $newImageUrl = $null
    if ($uploadedCache.ContainsKey($localImage)) {
        $newImageUrl = $uploadedCache[$localImage]
    } else {
        $newImageUrl = Upload-LocalImage -Token $token -LocalImagePath $localImage
        $uploadedCache[$localImage] = $newImageUrl
    }

    $detailResp = Invoke-Api -Method "GET" -Path ("/dish/" + $id) -Headers $headers
    Ensure-SuccessResult -Response $detailResp -Action ("get dish " + $id)
    $detail = $detailResp.data
    if ($null -eq $detail) {
        throw "dish detail is null, id=$id"
    }

    $updateBody = Build-UpdateBody -DishDetail $detail -NewImageUrl $newImageUrl
    $updateResp = Invoke-Api -Method "PUT" -Path "/dish" -Body $updateBody -Headers $headers
    Ensure-SuccessResult -Response $updateResp -Action ("update dish " + $id)

    $migrated++
    $report.Add([pscustomobject]@{
        id = $id
        name = $name
        old_image = $oldImage
        status = $status
        action = "migrated"
        new_image = $newImageUrl
        note = $localImage
    }) | Out-Null
}

Ensure-Dir -FilePath $ReportCsvPath
$report | Export-Csv -Path $ReportCsvPath -NoTypeInformation -Encoding UTF8

Ensure-Dir -FilePath $UnresolvedCsvPath
$unresolved | Export-Csv -Path $UnresolvedCsvPath -NoTypeInformation -Encoding UTF8

Write-Host ""
Write-Host "Done."
Write-Host ("Need fix: " + $needFix)
Write-Host ("Migrated: " + $migrated)
Write-Host ("Skipped (already reachable): " + $skipped)
Write-Host ("Unresolved: " + $unresolved.Count)
Write-Host ("Report: " + (Resolve-Path -LiteralPath $ReportCsvPath))
Write-Host ("Unresolved list: " + (Resolve-Path -LiteralPath $UnresolvedCsvPath))
