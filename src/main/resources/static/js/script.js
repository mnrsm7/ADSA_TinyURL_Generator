async function shortenUrl() {
    const longUrl = document.getElementById("longUrl").value;
    const resultDiv = document.getElementById("result");

    if (!longUrl) {
        resultDiv.innerHTML = "<span style='color:red;'>Please enter a URL</span>";
        return;
    }

    try {
        const response = await fetch("http://localhost:8080/shorten", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                longUrl: longUrl
            })
        });

        if (!response.ok) {
            throw new Error("Failed to shorten URL");
        }

        const data = await response.json();

        resultDiv.innerHTML = `
            <p>Short URL:</p>
            <a href="${data.shortUrl}" target="_blank">
                ${data.shortUrl}
            </a>
        `;
    } catch (error) {
        resultDiv.innerHTML = `<span style="color:red;">Error: ${error.message}</span>`;
    }
}
