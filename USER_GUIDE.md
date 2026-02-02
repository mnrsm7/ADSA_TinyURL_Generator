# URL Shortener - User Guide

## Expiration Time Formats

You can set expiration times when creating shortened URLs. Here are all the supported formats:

### Preset Options (via UI dropdown):
- **No Expiration** - URL never expires
- **1 Hour** - `1h` - URL expires in 1 hour
- **24 Hours** - `24h` - URL expires in 1 day
- **7 Days** - `7d` - URL expires in 7 days
- **30 Days** - `30d` - URL expires in 30 days
- **Custom** - Enter your own format

### Custom Format Examples:
- `1h` - 1 hour
- `24h` - 24 hours
- `72h` - 72 hours (3 days)
- `1d` - 1 day
- `7d` - 7 days
- `14d` - 14 days
- `30d` - 30 days
- `1m` - 1 minute
- `120m` - 120 minutes (2 hours)

**Format Pattern:** `{number}{unit}`
- Units: `h` (hours), `d` (days), `m` (minutes)

---

## Using the Application

### Tab 1: Shorten URL

1. Enter a long URL (e.g., `https://www.example.com/very/long/url`)
2. Select an expiration time:
   - Choose from preset options
   - Or select "Custom" and enter a custom time
3. Click **"Shorten URL"** button
4. Copy the generated short URL
5. View analytics for this URL by clicking the **"📊 View Analytics"** button

**Output includes:**
- Original URL
- Short URL (clickable link with copy button)
- Short Code
- Creation timestamp
- Expiration time (if set)

---

### Tab 2: Analytics

#### Option A: View Analytics for a Specific URL
1. Enter the short code (e.g., `abc123`)
2. Click **"Get Analytics"** button
3. View detailed information:
   - **Original URL** - The full URL
   - **Total Clicks** - Number of times the URL was accessed
   - **Created At** - When the URL was shortened
   - **Last Accessed** - When it was last clicked
   - **Status** - Active ✅ or Expired ❌
   - **Expiration** - Expiry date/time or "No expiration"
   - **Access Records** - Count of detailed access logs
   - **Access Log Table** - Shows:
     - Timestamp of each access
     - IP address of the visitor
     - User Agent (browser info)
     - Referer (where they came from)

#### Option B: View Top 10 Most-Clicked URLs
1. Click **"Load Top 10 URLs"** button
2. See a ranking of your most popular shortened URLs
3. View metrics for each:
   - Rank position
   - Short Code
   - Original URL (preview)
   - Click count
   - Creation date
4. Click **"📊 Details"** to see full analytics for any URL

#### Option C: View All Active URLs
1. Click **"Load Active URLs"** button
2. See all non-expired URLs in your system
3. Each entry shows:
   - Short Code
   - Original URL (preview)
   - Total clicks
   - Created date
   - Status (Active/Expired)
4. Click **"📊 Details"** for full analytics

---

### Tab 3: History

This tab uses a **Stack data structure** to track URL creation history.

1. Click **"Load History"** button
2. View:
   - **History Size** - Number of URLs in the stack (max 100)
   - **Stack Status** - Indicates if history has entries
   - **URL Creation History** - List of recently shortened URLs (most recent first)
   - Shows: Rank, Short Code, Original URL, Creation timestamp

The stack maintains the last 100 URL shortening operations in LIFO (Last-In-First-Out) order.

---

## API Endpoints Reference

### 1. Create Short URL
```
POST /shorten
Body: {
    "longUrl": "https://example.com/long/url",
    "expiresIn": "7d"  // Optional
}
Response: {
    "shortUrl": "http://localhost:8080/r/abc123",
    "shortCode": "abc123",
    "originalUrl": "...",
    "expiresAt": "2026-02-04T...",
    "createdAt": "2026-01-28T..."
}
```

### 2. Redirect to Original URL
```
GET /r/{shortCode}
Example: /r/abc123
Returns: HTTP 302 redirect to original URL
```

### 3. Get Analytics for a URL
```
GET /analytics/{shortCode}
Example: /analytics/abc123
Returns: Detailed analytics including click count, access logs, etc.
```

### 4. Get Top 10 URLs
```
GET /analytics/top/urls
Returns: List of 10 most-clicked URLs
```

### 5. Get All Active URLs
```
GET /analytics/active/urls
Returns: List of all non-expired URLs
```

### 6. Get URL Creation History
```
GET /analytics/history
Returns: Stack of recent URL shortening operations
```

### 7. Get Most Recent History Entry
```
GET /analytics/history/recent
Returns: Most recent URL creation (peek from stack)
```

### 8. Clean Up Expired URLs
```
DELETE /admin/cleanup-expired
Returns: Success message
```

---

## Examples

### Example 1: Create a URL that expires in 7 days
```
POST /shorten
{
    "longUrl": "https://github.com/example/project",
    "expiresIn": "7d"
}
```

### Example 2: Create a URL with no expiration
```
POST /shorten
{
    "longUrl": "https://example.com"
}
```

### Example 3: Create a URL that expires in 3 hours
```
POST /shorten
{
    "longUrl": "https://example.com",
    "expiresIn": "3h"
}
```

---

## Features Summary

✅ **URL Shortening** - Convert long URLs to short codes using Base62 encoding

✅ **URL Expiration** - Set custom expiration times (hours, days, minutes)

✅ **Click Tracking** - Count every visit to a shortened URL

✅ **Access Logging** - Record IP, User-Agent, Referer for each access

✅ **Analytics Dashboard** - View detailed statistics and trends

✅ **Stack-based History** - Track recent URL shortenings using a Stack data structure

✅ **Top URLs Ranking** - See your most popular shortened URLs

✅ **Active URLs List** - View all non-expired URLs in your system

✅ **Responsive UI** - Works on desktop and mobile devices
