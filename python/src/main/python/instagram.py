from yt_dlp import YoutubeDL
from main import make_response, time, ssl, socket
from yt_dlp.utils import DownloadError, ExtractorError


def getInstagramVideo(url):
    last_error = None

    ydl_opts = {
        "quiet": True,
        "no_warnings": True,
        "format": "best",
        "noplaylist": True,
        # Uncomment if login is required
        # "cookiefile": "instagram_cookies.txt"
    }

    for attempt in range(1, 3 + 1):
        try:
            with YoutubeDL(ydl_opts) as ydl:
                info = ydl.extract_info(url, download=False)

                if not info:
                    raise ExtractorError("Failed to extract Instagram video info")

                formats = info.get("formats", [])

                best = None
                for f in formats:
                    if (
                        f.get("ext") == "mp4"
                        and f.get("vcodec") != "none"
                        and f.get("url")
                    ):
                        best = f
                        break

                # Fallback to direct URL if formats aren't available
                if not best and info.get("url"):
                    best = {
                        "url": info.get("url"),
                        "ext": "mp4",
                        "vcodec": "unknown",
                        "acodec": "unknown",
                    }

                if not best:
                    raise ExtractorError("No MP4 stream found")

                result = {
                    "id": info.get("id"),
                    "title": info.get("title"),
                    "duration": str(info.get("duration")),
                    "thumbnail": info.get("thumbnail"),
                    "uploader": info.get("uploader"),
                    "view_count": str(info.get("view_count")),
                    "like_count": str(info.get("like_count")),
                    "webpage_url": info.get("webpage_url"),
                    "stream_url": best
                }

                return make_response(
                    success=True,
                    error=None,
                    result=result
                )

        except DownloadError:
            last_error = "VIDEO_UNAVAILABLE"

        except ExtractorError:
            last_error = "INSTAGRAM_EXTRACTION_FAILED"

        except (ssl.SSLError, socket.error) as e:
            last_error = f"Network/TLS error: {e}"

        except Exception as e:
            last_error = f"Unexpected error: {e}"

    return make_response(
        success=False,
        error=last_error or "FAILED_AFTER_RETRIES",
        result=None
    )


# dsd = getInstagramVideo("https://www.instagram.com/reel/DXBB07DjAtr/?hl=en")
# print(dsd)