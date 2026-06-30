"""URL extraction helpers for video metadata and MP4 stream retrieval.

This module wraps ``yt_dlp`` to extract video metadata and a progressive MP4 stream URL
from a supported video page URL.

Example:
    from UrlExtractor import getInfoFromUrl

    response = getInfoFromUrl("https://www.youtube.com/watch?v=abc123")
    if response["success"]:
        print(response["result"]["stream_url"])
    else:
        print("Extraction failed:", response["error"])
"""

from yt_dlp import YoutubeDL
from main import make_response, time, ssl, socket
from yt_dlp.utils import DownloadError, ExtractorError

ydl_instance = YoutubeDL(
    {
        "quiet": True,
        "format": "bv*+ba/b",
        "no_warnings": True,
        "skip_download": True,
        "noplaylist": True,
        "extract_flat": False,
        "cachedir": False,
    }
)


def getInfoFromUrl(url: str):
    """Extract video info and a progressive MP4 stream URL.

    Parameters:
        url (str): The video page URL to extract metadata from.

    Returns:
        dict: A response dictionary from ``make_response`` with the following keys:
            - ``success`` (bool): True when extraction succeeds.
            - ``error`` (str|None): Error code or message when extraction fails.
            - ``result`` (dict|None): Video metadata and stream URL when successful.

    Example:
        response = getInfoFromUrl("https://www.youtube.com/watch?v=abc123")
        if response["success"]:
            print(response["result"]["title"])
            print(response["result"]["stream_url"])
        else:
            print("Error:", response["error"])
    """
    last_error = None

    try:
        info = ydl_instance.extract_info(url, download=False)

        if not info:
            raise ExtractorError("Failed to extract video info")

        formats = info.get("formats", [])

        best = next(
            (
                f for f in formats
                if f.get("ext") == "mp4"
                and f.get("vcodec") != "none"
                and f.get("acodec") != "none"
                and f.get("url")
            ),
            None,
        )

        if not best:
            raise ExtractorError("No progressive MP4 stream found")

        result = {
            "id": info.get("id"),
            "title": info.get("title"),
            "duration": str(info.get("duration")),
            "thumbnail": info.get("thumbnail"),
            "uploader": info.get("uploader"),
            "view_count": str(info.get("view_count")),
            "like_count": str(info.get("like_count")),
            "webpage_url": info.get("webpage_url"),
            "stream_url": best,
        }
        

        return make_response(
            success=True,
            error=None,
            result=result,
        )

    except DownloadError:
        last_error = "The content is unavailable or cannot be accessed at this time."
    except ExtractorError:
        last_error = "We couldn't find a supported video format or stream for this link."

    except (ssl.SSLError, socket.error):
        last_error = "Secure connection failed. Please check your network or device date and time."
    except Exception as e:
        last_error = str(e)

    return make_response(
        success=False,
        error=last_error or "An unexpected error occurred. Please try again.",
        result=None,
    )
