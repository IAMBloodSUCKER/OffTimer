param(
    [string]$InputPng = "src/main/resources/icon.png",
    [string]$OutputIco = "src/main/resources/icon.ico"
)

Add-Type -AssemblyName System.Drawing

$background = [System.Drawing.Color]::FromArgb(26, 39, 68)
$sizes = @(16, 32, 48, 64, 128, 256)
$pngData = New-Object System.Collections.Generic.List[byte[]]

$root = Split-Path $PSScriptRoot -Parent
$sourcePath = Join-Path $root ($InputPng -replace '/', '\')
$outputPath = Join-Path $root ($OutputIco -replace '/', '\')

$source = [System.Drawing.Bitmap]::FromFile($sourcePath)

# Flatten transparency — Windows shortcuts show transparent pixels as black.
$flattened = New-Object System.Drawing.Bitmap($source.Width, $source.Height)
$flatGraphics = [System.Drawing.Graphics]::FromImage($flattened)
$flatGraphics.Clear($background)
$flatGraphics.DrawImage($source, 0, 0, $source.Width, $source.Height)
$flatGraphics.Dispose()
$source.Dispose()
$flattened.Save($sourcePath, [System.Drawing.Imaging.ImageFormat]::Png)
$source = $flattened

foreach ($size in $sizes) {
    $bitmap = New-Object System.Drawing.Bitmap($size, $size)
    $graphics = [System.Drawing.Graphics]::FromImage($bitmap)
    $graphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
    $graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::HighQuality
    $graphics.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality
    $graphics.CompositingQuality = [System.Drawing.Drawing2D.CompositingQuality]::HighQuality
    $graphics.Clear($background)
    $graphics.DrawImage($source, 0, 0, $size, $size)
    $graphics.Dispose()

    $stream = New-Object System.IO.MemoryStream
    $bitmap.Save($stream, [System.Drawing.Imaging.ImageFormat]::Png)
    $pngData.Add($stream.ToArray()) | Out-Null
    $stream.Dispose()
    $bitmap.Dispose()
}

$source.Dispose()

$count = $pngData.Count
$offset = 6 + ($count * 16)
$output = New-Object System.IO.MemoryStream
$writer = New-Object System.IO.BinaryWriter($output)

$writer.Write([uint16]0)
$writer.Write([uint16]1)
$writer.Write([uint16]$count)

for ($i = 0; $i -lt $count; $i++) {
    $size = $sizes[$i]
    $width = if ($size -ge 256) { [byte]0 } else { [byte]$size }
    $height = if ($size -ge 256) { [byte]0 } else { [byte]$size }
    $writer.Write($width)
    $writer.Write($height)
    $writer.Write([byte]0)
    $writer.Write([byte]0)
    $writer.Write([uint16]1)
    $writer.Write([uint16]32)
    $writer.Write([uint32]$pngData[$i].Length)
    $writer.Write([uint32]$offset)
    $offset += $pngData[$i].Length
}

foreach ($data in $pngData) {
    $writer.Write($data)
}

$writer.Close()
[System.IO.File]::WriteAllBytes($outputPath, $output.ToArray())
Write-Host "Created $outputPath with sizes: $($sizes -join ', ')"
