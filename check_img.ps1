Add-Type -AssemblyName System.Drawing
$folders = @('Skeleton','Orc_Warrior','Orc_Berserk','Orc_Shaman','Plent','Fire_Spirit')
foreach ($f in $folders) {
    $path = "f:/youxi/xiaoyouxi/assets/monster/$f/Idle.png"
    if (Test-Path $path) {
        $img = [System.Drawing.Bitmap]::new($path)
        Write-Host "$f Idle.png : $($img.Width)x$($img.Height)"
        $img.Dispose()
    }
    $path2 = "f:/youxi/xiaoyouxi/assets/monster/$f/Walk.png"
    if (Test-Path $path2) {
        $img2 = [System.Drawing.Bitmap]::new($path2)
        Write-Host "$f Walk.png : $($img2.Width)x$($img2.Height)"
        $img2.Dispose()
    }
    $path3 = "f:/youxi/xiaoyouxi/assets/monster/$f/Run.png"
    if (Test-Path $path3) {
        $img3 = [System.Drawing.Bitmap]::new($path3)
        Write-Host "$f Run.png : $($img3.Width)x$($img3.Height)"
        $img3.Dispose()
    }
    $path4 = "f:/youxi/xiaoyouxi/assets/monster/$f/Dead.png"
    if (Test-Path $path4) {
        $img4 = [System.Drawing.Bitmap]::new($path4)
        Write-Host "$f Dead.png : $($img4.Width)x$($img4.Height)"
        $img4.Dispose()
    }
}
