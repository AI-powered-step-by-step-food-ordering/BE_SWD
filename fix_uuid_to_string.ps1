# Script to replace UUID with String in controllers
$files = Get-ChildItem -Path "src\main\java\com\officefood\healthy_food_api\controller" -Filter "*.java" -Recurse

foreach ($file in $files) {
    $content = Get-Content $file.FullName -Raw -Encoding UTF8

    # Replace @PathVariable UUID with @PathVariable String
    $content = $content -replace '@PathVariable UUID', '@PathVariable String'

    # Remove import java.util.UUID if no other UUID usage
    if ($content -notmatch 'UUID[^;]' -or $content -notmatch '\bUUID\b' -or $content -match 'only PathVariable String') {
        # Keep it for now, we'll clean up manually if needed
    }

    Set-Content -Path $file.FullName -Value $content -Encoding UTF8 -NoNewline
    Write-Host "Processed: $($file.Name)"
}

Write-Host "`nDone! Processed $($files.Count) files."

