# Python Extraction Engine (`:python`)

The `:python` module is a specialized Android Library that serves as the "intelligence" of DasMediaHub. It leverages **Chaquopy** to bridge the gap between Kotlin and the rich ecosystem of Python media extraction libraries.

## Overview

Media platforms like TikTok, Instagram, and YouTube frequently change their APIs and obfuscation methods. This module solves that challenge by running high-performance Python scripts (like `yt-dlp` and `pytubefix`) directly within the Android runtime, providing the app with highly accurate metadata and stream extraction capabilities.

## Technical Stack

- **Integration:** Chaquopy 17.0.0
- **Python Version:** 3.14
- **Core Libraries:**
    - `yt-dlp`: The industry standard for extracting video metadata and streams from thousands of sites.
    - `pytubefix`: A specialized, maintained fork for robust YouTube interaction.
    - `httpx` & `requests`: Modern networking for Python-layer scraping.

## Module Structure

### Kotlin Interface (`src/main/java`)
- **`PyRuntime`**: The core manager that initializes the Python environment and handles JSON-based communication between Kotlin and Python.
- **`SocialMediaClient`**: A high-level interface for extracting data from platforms like TikTok and Instagram.
- **`YouTuber`**: Specialized client for YouTube searches, playlist extraction, and stream retrieval.
- **`ErrorMapper`**: Maps raw Python tracebacks and exceptions into user-friendly localized strings.

### Python Logic (`src/main/python`)
- **`main.py`**: Handles YouTube-specific logic including keyword searches and stream resolution.
- **`UrlExtractor.py`**: A `yt-dlp` wrapper dedicated to generic URL extraction (Social Media).
- **`my_youtube_search_fix`**: Custom search logic to handle specific platform edge cases.

## Usage Example

### Extracting Social Media Info
```kotlin
val url = "https://www.tiktok.com/@user/video/..."
val response = SocialMediaClient.getUrlInfo(url)

if (response.success) {
    val streamUrl = response.result?.streamUrl
    val title = response.result?.title
    // Proceed to playback or download
} else {
    val userFriendlyError = response.error
    // Show error to user
}
```

## Setup & Configuration

This module requires a local Python installation for build-time dependency resolution. 

1. Ensure `local.properties` contains your Python path:
   ```properties
   PYTHON_PATH=/path/to/your/python3
   ```
2. The module automatically installs required `pip` packages during the first build via the `chaquopy` block in `build.gradle.kts`.

---
**Note:** This module is for educational purposes. Ensure you comply with the Terms of Service of the platforms you are interacting with.
