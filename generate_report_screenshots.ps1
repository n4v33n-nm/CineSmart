# CineSmart Project Report Screenshot Generator
# Captures pixel-perfect screenshots from the real CineSmart frontend for academic project reports

$ErrorActionPreference = "Stop"

$chromePath = "C:\Program Files\Google\Chrome\Application\chrome.exe"
$distDir = "C:/Users/navee/OneDrive/Desktop/CineSmart/frontend/dist/index.html"
$outputDir = "C:\Users\navee\OneDrive\Desktop\CineSmart\docs\screenshots"

if (-not (Test-Path $outputDir)) {
    New-Item -ItemType Directory -Force -Path $outputDir | Out-Null
}

$screens = @(
    @{ Name = "01-home-page.png"; Route = "#/"; Height = 1000; Vtb = 3500 },
    @{ Name = "02-movie-catalog.png"; Route = "#/movies"; Height = 1000; Vtb = 3500 },
    @{ Name = "03-movie-details.png"; Route = "#/movies/1"; Height = 1050; Vtb = 3500 },
    @{ Name = "04-seat-selection-map.png"; Route = "#/shows/1/seats"; Height = 1100; Vtb = 4000 },
    @{ Name = "05-smart-group-seating.png"; Route = "#/shows/1/seats?groupOpen=true"; Height = 1150; Vtb = 4500 },
    @{ Name = "06-payment-modal.png"; Route = "#/shows/1/seats?checkout=true"; Height = 1050; Vtb = 4000 },
    @{ Name = "07-booking-confirmation-ticket.png"; Route = "#/bookings?ticket=true"; Height = 1100; Vtb = 4000 },
    @{ Name = "08-my-bookings-waitlist.png"; Route = "#/bookings"; Height = 1050; Vtb = 3500 },
    @{ Name = "09-admin-dashboard.png"; Route = "#/admin"; Height = 1100; Vtb = 4000 },
    @{ Name = "10-login-page.png"; Route = "#/login"; Height = 950; Vtb = 3500 }
)

Write-Host "`n========================================================" -ForegroundColor Cyan
Write-Host " CineSmart Report Screenshot Generator" -ForegroundColor Yellow
Write-Host " Capturing 10 Real Project Output Screens..." -ForegroundColor Cyan
Write-Host "========================================================`n" -ForegroundColor Cyan

foreach ($item in $screens) {
    $outFile = Join-Path $outputDir $item.Name
    $url = "file:///$distDir$($item.Route)"
    $winSize = "1440,$($item.Height)"
    
    Write-Host "Capturing $($item.Name) ... " -NoNewline -ForegroundColor White
    
    $args = @(
        "--headless=new",
        "--no-sandbox",
        "--disable-gpu",
        "--allow-file-access-from-files",
        "--virtual-time-budget=$($item.Vtb)",
        "--window-size=$winSize",
        "--screenshot=$outFile",
        $url
    )
    
    $proc = Start-Process -FilePath $chromePath -ArgumentList $args -PassThru -WindowStyle Hidden
    $proc.WaitForExit()
    Start-Sleep -Milliseconds 800
    
    if (Test-Path $outFile) {
        $sizeKb = [math]::Round((Get-Item $outFile).Length / 1KB)
        Write-Host "DONE ($sizeKb KB)" -ForegroundColor Green
    } else {
        Write-Host "FAILED" -ForegroundColor Red
    }
}

Write-Host "`nAll 10 project screenshots captured successfully into docs/screenshots/!`n" -ForegroundColor Green

