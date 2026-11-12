#Requires -Version 5.1

# --- CONFIG ---
$baseUrl     = "http://localhost:8080"
$verboseBody = $false

# --- COUNTERS ---
$script:pass = 0
$script:fail = 0

# --- HELPER ---
function Invoke-Api {
    param(
        [string]$Method = "GET",
        [string]$Path,
        $Body = $null,
        [int]$ExpectedStatus = 200
    )

    $uri = "$baseUrl$Path"
    $params = @{
        Uri             = $uri
        Method          = $Method
        UseBasicParsing = $true
        TimeoutSec      = 30
    }
    if ($null -ne $Body) {
        $params.Body        = ($Body | ConvertTo-Json -Depth 10 -Compress)
        $params.ContentType = "application/json"
    }

    $status  = 0
    $content = ""

    try {
        $resp    = Invoke-WebRequest @params
        $status  = [int]$resp.StatusCode
        $content = $resp.Content
    }
    catch {
        if ($_.Exception.Response) {
            $status = [int]$_.Exception.Response.StatusCode
            $stream = $_.Exception.Response.GetResponseStream()
            if ($stream) {
                $reader  = New-Object System.IO.StreamReader($stream)
                $content = $reader.ReadToEnd()
                $reader.Close()
            }
        }
        else {
            $status  = -1
            $content = $_.Exception.Message
        }
    }

    $parsed = $null
    if ($content -and $content.Trim().Length -gt 0) {
        try { $parsed = $content | ConvertFrom-Json } catch { $parsed = $null }
    }

    $ok    = ($status -eq $ExpectedStatus)
    $mark  = if ($ok) { "PASS" } else { "FAIL" }
    $color = if ($ok) { "Green" } else { "Red" }

    $line = "[{0}] {1,-6} {2} -> {3} (expected {4})" -f $mark, $Method, $Path, $status, $ExpectedStatus
    Write-Host $line -ForegroundColor $color

    if ($verboseBody -and $content) {
        Write-Host ("       body: {0}" -f $content) -ForegroundColor DarkGray
    }

    if ($ok) { $script:pass += 1 } else { $script:fail += 1 }

    return [PSCustomObject]@{
        Status  = $status
        Content = $content
        Parsed  = $parsed
    }
}

# --- START ---
Write-Host ""
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "  LMS REST API smoke-test" -ForegroundColor Cyan
Write-Host "  Base URL: $baseUrl" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

# 0. Connection
Write-Host "--- 0. Connection ---" -ForegroundColor Yellow
$ping = Invoke-Api -Method GET -Path "/api/courses"
if ($ping.Status -eq -1) {
    Write-Host ""
    Write-Host "ERROR: Application not available at $baseUrl" -ForegroundColor Red
    Write-Host "Start it with:" -ForegroundColor Red
    Write-Host "  mvn spring-boot:run -Dspring-boot.run.profiles=dev" -ForegroundColor Red
    exit 1
}

# 1. Reads
Write-Host ""
Write-Host "--- 1. Basic reads ---" -ForegroundColor Yellow
Invoke-Api -Method GET -Path "/api/courses"    | Out-Null
Invoke-Api -Method GET -Path "/api/courses/1"  | Out-Null
Invoke-Api -Method GET -Path "/api/categories" | Out-Null
Invoke-Api -Method GET -Path "/api/users"      | Out-Null

# 2. 404
Write-Host ""
Write-Host "--- 2. Not Found (404) ---" -ForegroundColor Yellow
Invoke-Api -Method GET -Path "/api/courses/999999" -ExpectedStatus 404 | Out-Null
Invoke-Api -Method GET -Path "/api/users/999999"   -ExpectedStatus 404 | Out-Null

# 3. Validation (400)
Write-Host ""
Write-Host "--- 3. Validation (400) ---" -ForegroundColor Yellow
$valBody = @{ name = ""; email = "not-an-email"; role = "STUDENT" }
$valResp = Invoke-Api -Method POST -Path "/api/users" -Body $valBody -ExpectedStatus 400

if ($valResp.Parsed -and $valResp.Parsed.fieldErrors) {
    $fields = @($valResp.Parsed.fieldErrors.PSObject.Properties.Name)
    Write-Host ("       fieldErrors keys: {0}" -f ($fields -join ", ")) -ForegroundColor DarkGray
    $hasName  = $fields -contains "name"
    $hasEmail = $fields -contains "email"
    if ($hasName -and $hasEmail) {
        Write-Host "[PASS] fieldErrors contains name and email" -ForegroundColor Green
        $script:pass += 1
    }
    else {
        Write-Host "[FAIL] fieldErrors missing expected keys" -ForegroundColor Red
        $script:fail += 1
    }
}

$createBadCourse = @{ title = "" }
Invoke-Api -Method POST -Path "/api/courses" -Body $createBadCourse -ExpectedStatus 400 | Out-Null

# 4. Create test user
Write-Host ""
Write-Host "--- 4. Create test user ---" -ForegroundColor Yellow
$suffix      = Get-Date -Format "yyyyMMddHHmmssfff"
$email       = "smoke-$suffix@example.com"
$newUserBody = @{ name = "Smoke Tester"; email = $email; role = "STUDENT" }
$newUserResp = Invoke-Api -Method POST -Path "/api/users" -Body $newUserBody -ExpectedStatus 201

$newUserId = $null
if ($newUserResp.Parsed -and $newUserResp.Parsed.id) {
    $newUserId = $newUserResp.Parsed.id
    Write-Host ("       student id = {0}, email = {1}" -f $newUserId, $email) -ForegroundColor DarkGray
}
else {
    Write-Host "ERROR: Cannot create test user, aborting" -ForegroundColor Red
    exit 1
}
$fakeEnroll = @{ studentId = $newUserId }
Invoke-Api -Method POST -Path "/api/courses/999999/enrollments" -Body $fakeEnroll -ExpectedStatus 404 | Out-Null


# 5. Enrollments
Write-Host ""
Write-Host "--- 5. Enrollments ---" -ForegroundColor Yellow
$enrollBody = @{ studentId = $newUserId }
Invoke-Api -Method POST -Path "/api/courses/1/enrollments" -Body $enrollBody -ExpectedStatus 201 | Out-Null
Invoke-Api -Method POST -Path "/api/courses/1/enrollments" -Body $enrollBody -ExpectedStatus 409 | Out-Null
Invoke-Api -Method GET  -Path "/api/courses/1/enrollments"                                   | Out-Null

# 6. Quiz
Write-Host ""
Write-Host "--- 6. Quiz ---" -ForegroundColor Yellow
$quizId        = 1
$quizQuestions = (Invoke-Api -Method GET -Path "/api/quizzes/$quizId/questions").Parsed
if ($quizQuestions -isnot [array]) { $quizQuestions = @($quizQuestions) }

if ($quizQuestions -and $quizQuestions.Count -gt 0) {
    $firstOpt   = $quizQuestions[0].options[0]
    $hasCorrect = $firstOpt.PSObject.Properties.Name -contains "correct"
    if ($hasCorrect) {
        Write-Host "[FAIL] 'correct' field leaked to student API" -ForegroundColor Red
        $script:fail += 1
    }
    else {
        Write-Host "[PASS] 'correct' hidden from student API" -ForegroundColor Green
        $script:pass += 1
    }

    $answers = @()
    foreach ($q in $quizQuestions) {
        $answers += [PSCustomObject]@{
            questionId        = $q.id
            selectedOptionIds = @($q.options[0].id)
        }
    }
    $takeBody = @{ answers = $answers }
    $takeUrl  = "/api/quizzes/$quizId/take?studentId=$newUserId"
    Invoke-Api -Method POST -Path $takeUrl                          -Body $takeBody -ExpectedStatus 200 | Out-Null
    Invoke-Api -Method POST -Path $takeUrl                          -Body $takeBody -ExpectedStatus 409 | Out-Null
    Invoke-Api -Method GET  -Path "/api/quizzes/$quizId/submissions"                                    | Out-Null
}

# 7. Assignment
Write-Host ""
Write-Host "--- 7. Assignment ---" -ForegroundColor Yellow
$assignCheck = Invoke-Api -Method GET -Path "/api/assignments/1"
if ($assignCheck.Status -eq 200) {
    $submitBody = @{ content = "smoke-test answer" }
    $submitUrl  = "/api/assignments/1/submit?studentId=$newUserId"
    $submitResp = Invoke-Api -Method POST -Path $submitUrl -Body $submitBody -ExpectedStatus 201

    $submissionId = $null
    if ($submitResp.Parsed -and $submitResp.Parsed.id) {
        $submissionId = $submitResp.Parsed.id
        Write-Host ("       submission id = {0}" -f $submissionId) -ForegroundColor DarkGray
    }

    $dupBody = @{ content = "again" }
    Invoke-Api -Method POST -Path $submitUrl -Body $dupBody -ExpectedStatus 409 | Out-Null

    if ($submissionId) {
        $gradeBody = @{ score = 75; feedback = "smoke grade" }
        $gradeUrl  = "/api/assignments/submissions/$submissionId/grade"
        Invoke-Api -Method PUT -Path $gradeUrl -Body $gradeBody -ExpectedStatus 200 | Out-Null
    }
}
else {
    Write-Host "       Assignment id=1 not found, skipping" -ForegroundColor Yellow
}

# 8. Reviews
Write-Host ""
Write-Host "--- 8. Reviews ---" -ForegroundColor Yellow
$reviewBody = @{ rating = 4; comment = "smoke review" }
$reviewUrl  = "/api/courses/1/reviews?studentId=$newUserId"
Invoke-Api -Method POST -Path $reviewUrl                    -Body $reviewBody -ExpectedStatus 201 | Out-Null
Invoke-Api -Method POST -Path $reviewUrl                    -Body $reviewBody -ExpectedStatus 409 | Out-Null
Invoke-Api -Method GET  -Path "/api/courses/1/reviews"                                            | Out-Null

# 9. Cleanup
Write-Host ""
Write-Host "--- 9. Cleanup ---" -ForegroundColor Yellow
$unenrollUrl = "/api/courses/1/enrollments/$newUserId"
Invoke-Api -Method DELETE -Path $unenrollUrl -ExpectedStatus 204 | Out-Null
Invoke-Api -Method DELETE -Path $unenrollUrl -ExpectedStatus 404 | Out-Null

# --- SUMMARY ---
$total     = $script:pass + $script:fail
$failColor = if ($script:fail -eq 0) { "Green" } else { "Red" }

Write-Host ""
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ("  Total:   {0}" -f $total)      -ForegroundColor Cyan
Write-Host ("  PASSED:  {0}" -f $script:pass) -ForegroundColor Green
Write-Host ("  FAILED:  {0}" -f $script:fail) -ForegroundColor $failColor
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

if ($script:fail -gt 0) { exit 1 }
exit 0