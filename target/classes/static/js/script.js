// Section switching
function showSection(sectionName) {
    // Hide all sections
    document.querySelectorAll('.section').forEach(section => {
        section.classList.remove('active');
    });
    
    // Remove active from all nav links
    document.querySelectorAll('.nav-link').forEach(link => {
        link.classList.remove('active');
    });
    
    // Show selected section
    document.getElementById('section-' + sectionName).classList.add('active');
    document.getElementById('nav-' + sectionName).classList.add('active');
}

// Toggle custom expiry input
function toggleCustomExpiry() {
    const expiration = document.getElementById('expiration').value;
    const customGroup = document.getElementById('customExpiryGroup');
    
    if (expiration === 'custom') {
        customGroup.style.display = 'block';
    } else {
        customGroup.style.display = 'none';
    }
}

// Shorten URL
async function shortenUrl() {
    const longUrl = document.getElementById("longUrl").value;
    const expiration = document.getElementById("expiration").value;
    const customTime = document.getElementById("customTime").value;
    const resultDiv = document.getElementById("result");

    if (!longUrl) {
        resultDiv.innerHTML = "<p style='color:red;'>❌ Please enter a URL</p>";
        return;
    }

    try {
        const body = { longUrl: longUrl };

        if (expiration === 'custom' && customTime) {
            body.expiresIn = customTime;
        } else if (expiration) {
            body.expiresIn = expiration;
        }

        const response = await fetch("http://localhost:8080/shorten", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(body)
        });

        if (!response.ok) {
            throw new Error("Failed to shorten URL");
        }

        const data = await response.json();
        let expiryInfo = '';
        
        if (data.expiresAt) {
            expiryInfo = `<p><strong>⏰ Expires At:</strong> ${new Date(data.expiresAt).toLocaleString()}</p>`;
        } else {
            expiryInfo = `<p><strong>⏰ Expiration:</strong> Never</p>`;
        }

        resultDiv.innerHTML = `
            <div class="result success">
                <p><strong>✅ URL Shortened Successfully!</strong></p>
                <hr>
                <p><strong>📝 Original URL:</strong><br><small>${data.originalUrl}</small></p>
                <p><strong>🔗 Short URL:</strong><br>
                    <a href="${data.shortUrl}" target="_blank" style="font-weight:bold;">${data.shortUrl}</a>
                    <button onclick="copyText('${data.shortUrl}')" style="padding:6px 12px; margin:5px 0;">📋 Copy</button>
                </p>
                <p><strong>Code:</strong> <span class="highlight">${data.shortCode}</span></p>
                <p><strong>📅 Created:</strong> ${new Date(data.createdAt).toLocaleString()}</p>
                ${expiryInfo}
            </div>
        `;
        
        document.getElementById("longUrl").value = '';
        document.getElementById("expiration").value = '';
        document.getElementById("customTime").value = '';
    } catch (error) {
        resultDiv.innerHTML = `<p style='color:red;'>❌ Error: ${error.message}</p>`;
    }
}

// Get URL Analytics
async function getUrlAnalytics() {
    const shortCode = document.getElementById("analyticsCode").value;
    const resultDiv = document.getElementById("analyticsResult");

    if (!shortCode) {
        resultDiv.innerHTML = "<p style='color:red;'>❌ Please enter a short code</p>";
        return;
    }

    resultDiv.innerHTML = "<p>⏳ Loading...</p>";

    try {
        const response = await fetch(`http://localhost:8080/analytics/${shortCode}`);

        if (!response.ok) {
            throw new Error("URL not found or expired");
        }

        const data = await response.json();
        let html = `
            <div>
                <p><strong>🔗 Short Code:</strong> <span class="highlight">${data.shortCode}</span></p>
                <p><strong>📝 Original URL:</strong><br><small>${data.originalUrl}</small></p>
                <p><strong>👆 Total Clicks:</strong> <span class="highlight">${data.totalClicks}</span></p>
                <p><strong>📅 Created:</strong> ${new Date(data.createdAt).toLocaleString()}</p>
                <p><strong>⏰ Last Accessed:</strong> ${data.lastAccessedAt ? new Date(data.lastAccessedAt).toLocaleString() : 'Never'}</p>
                <p><strong>Status:</strong> <span style="color: ${data.isExpired ? 'red' : 'green'}">${data.isExpired ? '❌ Expired' : '✅ Active'}</span></p>
        `;

        if (data.expiresAt) {
            html += `<p><strong>⏳ Expires At:</strong> ${new Date(data.expiresAt).toLocaleString()}</p>`;
        } else {
            html += `<p><strong>⏳ Expiration:</strong> Never</p>`;
        }

        if (data.accessLogs && data.accessLogs.length > 0) {
            html += `<p><strong>📊 Access Records:</strong> ${data.totalAccessRecords}</p><table><tr><th>Timestamp</th><th>IP Address</th></tr>`;
            data.accessLogs.slice(0, 5).forEach(log => {
                html += `<tr><td>${new Date(log.accessedAt).toLocaleString()}</td><td>${log.ipAddress || 'N/A'}</td></tr>`;
            });
            html += '</table>';
        }

        html += '</div>';
        resultDiv.innerHTML = html;
    } catch (error) {
        resultDiv.innerHTML = `<p style='color:red;'>❌ Error: ${error.message}</p>`;
    }
}

// Get Top URLs
async function getTopUrls() {
    const resultDiv = document.getElementById("topUrlsResult");
    resultDiv.innerHTML = "<p>⏳ Loading...</p>";

    try {
        const response = await fetch("http://localhost:8080/analytics/top/urls");

        if (!response.ok) {
            throw new Error("Failed to fetch top URLs");
        }

        const data = await response.json();

        if (data.count === 0) {
            resultDiv.innerHTML = "<p>📭 No URLs found</p>";
            return;
        }

        let html = `<p><strong>Found ${data.count} top URLs</strong></p><table><tr><th>#</th><th>Code</th><th>Clicks</th><th>Created</th></tr>`;

        data.urls.forEach((url, index) => {
            html += `<tr>
                <td>${index + 1}</td>
                <td><strong>${url.shortCode}</strong></td>
                <td><span class="highlight">${url.clickCount}</span></td>
                <td><small>${new Date(url.createdAt).toLocaleDateString()}</small></td>
            </tr>`;
        });

        html += '</table>';
        resultDiv.innerHTML = html;
    } catch (error) {
        resultDiv.innerHTML = `<p style='color:red;'>❌ Error: ${error.message}</p>`;
    }
}

// Get All Active URLs
async function getAllActiveUrls() {
    const resultDiv = document.getElementById("activeUrlsResult");
    resultDiv.innerHTML = "<p>⏳ Loading...</p>";

    try {
        const response = await fetch("http://localhost:8080/analytics/active/urls");

        if (!response.ok) {
            throw new Error("Failed to fetch active URLs");
        }

        const data = await response.json();

        if (data.count === 0) {
            resultDiv.innerHTML = "<p>📭 No active URLs found</p>";
            return;
        }

        let html = `<p><strong>Found ${data.count} active URLs</strong></p><table><tr><th>Code</th><th>Clicks</th><th>Created</th><th>Status</th></tr>`;

        data.urls.forEach(url => {
            const isExpired = url.expiresAt && new Date(url.expiresAt) < new Date();
            const status = isExpired ? '❌ Expired' : '✅ Active';
            
            html += `<tr>
                <td><strong>${url.shortCode}</strong></td>
                <td>${url.clickCount}</td>
                <td><small>${new Date(url.createdAt).toLocaleDateString()}</small></td>
                <td>${status}</td>
            </tr>`;
        });

        html += '</table>';
        resultDiv.innerHTML = html;
    } catch (error) {
        resultDiv.innerHTML = `<p style='color:red;'>❌ Error: ${error.message}</p>`;
    }
}

// Get URL History
async function getUrlHistory() {
    const resultDiv = document.getElementById("historyResult");
    resultDiv.innerHTML = "<p>⏳ Loading...</p>";

    try {
        const response = await fetch("http://localhost:8080/analytics/history");

        if (!response.ok) {
            throw new Error("Failed to fetch history");
        }

        const data = response.json();

        if (data.historySize === 0) {
            resultDiv.innerHTML = "<p>📭 No history available</p>";
            return;
        }

        let html = `
            <p><strong>📚 History Size:</strong> ${data.historySize} / ${data.maxSize}</p>
            <p><strong>Status:</strong> ${data.historySize > 0 ? '✅ Has entries' : '📭 Empty'}</p>
        `;

        if (data.history && data.history.length > 0) {
            html += '<table><tr><th>#</th><th>Code</th><th>Created</th></tr>';
            data.history.forEach((entry, index) => {
                html += `<tr>
                    <td>${index + 1}</td>
                    <td><strong>${entry.shortCode}</strong></td>
                    <td><small>${new Date(entry.timestamp).toLocaleString()}</small></td>
                </tr>`;
            });
            html += '</table>';
        }

        resultDiv.innerHTML = html;
    } catch (error) {
        resultDiv.innerHTML = `<p style='color:red;'>❌ Error: ${error.message}</p>`;
    }
}

// Copy to clipboard
function copyText(text) {
    navigator.clipboard.writeText(text).then(() => {
        alert('✅ Copied: ' + text);
    }).catch(() => {
        alert('Failed to copy');
    });
}
