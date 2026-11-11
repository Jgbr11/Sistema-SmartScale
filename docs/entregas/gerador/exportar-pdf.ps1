param([Parameter(Mandatory)][string]$Pasta)
$arquivos = Get-ChildItem -Path $Pasta -Filter *.docx | Where-Object { -not $_.Name.StartsWith('~$') }
if (-not $arquivos) { throw "Nenhum .docx em $Pasta" }
$word = New-Object -ComObject Word.Application
$word.Visible = $false
try {
    foreach ($arquivo in $arquivos) {
        $pdf = [System.IO.Path]::ChangeExtension($arquivo.FullName, '.pdf')
        $doc = $word.Documents.Open($arquivo.FullName, $false, $true)
        $doc.ExportAsFixedFormat($pdf, 17)
        $doc.Close($false)
        "PDF: $pdf"
    }
} finally {
    $word.Quit()
}
