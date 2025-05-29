# API Testing Script for CanvaMedium
# -----------------------------------

# This script tests the main API endpoints of CanvaMedium
# Run from the project root directory

# 1. Variables
$baseUrl = "http://localhost:8080"
$token = ""
$refreshToken = ""
$templateId = ""
$articleId = ""

Write-Host "CanvaMedium API Testing Script" -ForegroundColor Green
Write-Host "===============================`n" -ForegroundColor Green

# 2. Test User Registration
Write-Host "1. Testing User Registration..." -ForegroundColor Yellow
$registrationBody = @{
    username = "testuser1"
    email = "test1@example.com"
    password = "password123"
    fullName = "Test User One"
} | ConvertTo-Json

try {
    $response = Invoke-WebRequest -Uri "$baseUrl/api/auth/register" -Method Post -ContentType "application/json" -Body $registrationBody
    Write-Host "   Registration successful! Status: $($response.StatusCode)" -ForegroundColor Green
    Write-Host "   Response: $($response.Content)" -ForegroundColor Gray
} catch {
    Write-Host "   Registration failed or user may already exist: $($_.Exception.Message)" -ForegroundColor Yellow
}

# 3. Test User Login
Write-Host "`n2. Testing User Login..." -ForegroundColor Yellow
$loginBody = @{
    username = "testuser1"
    password = "password123"
} | ConvertTo-Json

try {
    $response = Invoke-WebRequest -Uri "$baseUrl/api/auth/login" -Method Post -ContentType "application/json" -Body $loginBody
    $authResponse = $response.Content | ConvertFrom-Json
    $token = $authResponse.token
    $refreshToken = $authResponse.refreshToken
    
    Write-Host "   Login successful! Status: $($response.StatusCode)" -ForegroundColor Green
    Write-Host "   Token received and stored for subsequent requests" -ForegroundColor Green
} catch {
    Write-Host "   Login failed: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "   Cannot proceed with authenticated endpoints. Exiting." -ForegroundColor Red
    exit
}

# 4. Test Create Template
Write-Host "`n3. Testing Template Creation..." -ForegroundColor Yellow
$templateBody = @{
    name = "Test Template"
    layout = @{
        elements = @(
            @{
                type = "TEXT"
                x = 10
                y = 10
                width = 200
                height = 100
                content = "Sample text"
            }
        )
    }
} | ConvertTo-Json -Depth 5

try {
    $headers = @{
        "Authorization" = "Bearer $token"
    }
    $response = Invoke-WebRequest -Uri "$baseUrl/api/templates" -Method Post -ContentType "application/json" -Headers $headers -Body $templateBody
    $templateResponse = $response.Content | ConvertFrom-Json
    $templateId = $templateResponse.id
    
    Write-Host "   Template creation successful! Status: $($response.StatusCode)" -ForegroundColor Green
    Write-Host "   Template ID: $templateId" -ForegroundColor Green
} catch {
    Write-Host "   Template creation failed: $($_.Exception.Message)" -ForegroundColor Red
}

# 5. Test Get All Templates
Write-Host "`n4. Testing Get All Templates..." -ForegroundColor Yellow
try {
    $headers = @{
        "Authorization" = "Bearer $token"
    }
    $response = Invoke-WebRequest -Uri "$baseUrl/api/templates" -Method Get -Headers $headers
    
    Write-Host "   Retrieved templates successfully! Status: $($response.StatusCode)" -ForegroundColor Green
    Write-Host "   Response: $($response.Content)" -ForegroundColor Gray
} catch {
    Write-Host "   Failed to retrieve templates: $($_.Exception.Message)" -ForegroundColor Red
}

# 6. Test Create Article
Write-Host "`n5. Testing Article Creation..." -ForegroundColor Yellow
$articleBody = @{
    title = "Test Article"
    content = @{
        blocks = @(
            @{
                type = "paragraph"
                text = "This is a test article"
            }
        )
    }
    previewText = "Test article preview"
    templateId = $templateId
} | ConvertTo-Json -Depth 5

try {
    $headers = @{
        "Authorization" = "Bearer $token"
    }
    $response = Invoke-WebRequest -Uri "$baseUrl/api/articles" -Method Post -ContentType "application/json" -Headers $headers -Body $articleBody
    $articleResponse = $response.Content | ConvertFrom-Json
    $articleId = $articleResponse.id
    
    Write-Host "   Article creation successful! Status: $($response.StatusCode)" -ForegroundColor Green
    Write-Host "   Article ID: $articleId" -ForegroundColor Green
} catch {
    Write-Host "   Article creation failed: $($_.Exception.Message)" -ForegroundColor Red
}

# 7. Test Get All Articles
Write-Host "`n6. Testing Get All Articles..." -ForegroundColor Yellow
try {
    $headers = @{
        "Authorization" = "Bearer $token"
    }
    $response = Invoke-WebRequest -Uri "$baseUrl/api/articles" -Method Get -Headers $headers
    
    Write-Host "   Retrieved articles successfully! Status: $($response.StatusCode)" -ForegroundColor Green
    Write-Host "   Response: $($response.Content)" -ForegroundColor Gray
} catch {
    Write-Host "   Failed to retrieve articles: $($_.Exception.Message)" -ForegroundColor Red
}

# 8. Test Get Article by ID
if ($articleId) {
    Write-Host "`n7. Testing Get Article by ID..." -ForegroundColor Yellow
    try {
        $headers = @{
            "Authorization" = "Bearer $token"
        }
        $response = Invoke-WebRequest -Uri "$baseUrl/api/articles/$articleId" -Method Get -Headers $headers
        
        Write-Host "   Retrieved article successfully! Status: $($response.StatusCode)" -ForegroundColor Green
        Write-Host "   Response: $($response.Content)" -ForegroundColor Gray
    } catch {
        Write-Host "   Failed to retrieve article: $($_.Exception.Message)" -ForegroundColor Red
    }
}

# 9. Test Update Article
if ($articleId) {
    Write-Host "`n8. Testing Update Article..." -ForegroundColor Yellow
    $updateArticleBody = @{
        title = "Updated Test Article"
        content = @{
            blocks = @(
                @{
                    type = "paragraph"
                    text = "This is an updated test article"
                }
            )
        }
        previewText = "Updated test article preview"
        templateId = $templateId
    } | ConvertTo-Json -Depth 5

    try {
        $headers = @{
            "Authorization" = "Bearer $token"
        }
        $response = Invoke-WebRequest -Uri "$baseUrl/api/articles/$articleId" -Method Put -ContentType "application/json" -Headers $headers -Body $updateArticleBody
        
        Write-Host "   Article update successful! Status: $($response.StatusCode)" -ForegroundColor Green
        Write-Host "   Response: $($response.Content)" -ForegroundColor Gray
    } catch {
        Write-Host "   Article update failed: $($_.Exception.Message)" -ForegroundColor Red
    }
}

# 10. Summary
Write-Host "`nAPI Testing Complete" -ForegroundColor Green
Write-Host "===================" -ForegroundColor Green
Write-Host "You can now use Postman or another tool for more detailed testing." -ForegroundColor White 