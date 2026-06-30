"""Core media helper functions for YouTube extraction and search.

This module provides JSON-friendly response wrappers and user-facing error
messages for the app's Python backend.

Example:
    from main import get_video_url

    response = get_video_url("https://www.youtube.com/watch?v=abc123")
    print(response)
"""

from pytubefix import YouTube, Playlist
from pytubefix.exceptions import (
    PytubeFixError,
    VideoUnavailable,
    RegexMatchError,
    ExtractError
)
from my_youtube_search_fix import Video, VideosSearch
from requests import RequestException
from typing import Any, Optional
import json, traceback, socket, ssl

ssl._create_default_https_context = ssl._create_unverified_context


def make_response(success: bool, error: Optional[str] = None, result: Any = None):
    """Return a JSON-serializable response payload.

    Parameters:
        success (bool): Whether the operation succeeded.
        error (Optional[str]): A user-facing error message.
        result (Any): The successful result payload.

    Returns:
        str: JSON string containing success, error, and result keys.
    """
    return json.dumps(
        {
            "success": success,
            "error": error,
            "result": result
        }
    )


def get_video_url(video_url, retries: int = 3, delay: float = 1.0):
    """Fetch the highest-resolution video stream URL for a YouTube link.

    Parameters:
        video_url (str): The YouTube video URL.
        retries (int): Number of retry attempts on transient failure.
        delay (float): Seconds to wait between retry attempts.

    Returns:
        str: JSON string with the video URL or an error message.

    Example:
        response = get_video_url("https://www.youtube.com/watch?v=abc123")
    """
    last_error = None

    try:
        yt = YouTube(video_url)
        stream = yt.streams.get_highest_resolution()

        if stream is None or not stream.url:
            raise ExtractError("No valid stream found")
    
        return make_response(
            success=True,
            result=str(stream.url)
        )
    
    except (VideoUnavailable, RegexMatchError) as e:
        return make_response(
            success=False,
            error="The content is unavailable or cannot be accessed at this time."
        )
    
    except (ExtractError, PytubeFixError) as e:
        last_error = "We couldn't find a supported video format or stream for this link."
    
    except (ssl.SSLError, socket.error, RequestException):
        last_error = "Secure connection failed. Please check your network or device date and time."
    
    except Exception as e:
        last_error = str(e)


    # --- Final failure ---
    return make_response(
        success=False,
        error=last_error or "An unexpected error occurred. Please try again."
    )


def get_audio_url(media_url, retries: int = 3):
    """Fetch an audio-only stream URL for a YouTube link.

    Parameters:
        media_url (str): The source media URL.
        retries (int): Number of retry attempts on transient failure.

    Returns:
        str: JSON string with the audio stream URL or a user-friendly error message.
    """
    last_error = None

    try:
        yt = YouTube(media_url)

        # Prefer audio-only (smallest, most reliable)
        stream = yt.streams.get_audio_only()

        # Fallback if audio-only fails
        if stream is None:
            stream = yt.streams.filter(only_audio=True).first()

        if stream is None:
            raise ExtractError("No audio stream available")

        return make_response(
            success=True,
            result=str(stream.url)
        )

    except (VideoUnavailable, RegexMatchError):
        # These won't succeed on retry
        return make_response(
            success=False,
            error="The content is unavailable or cannot be accessed at this time."
        )

    except (PytubeFixError, ExtractError, RequestException, ssl.SSLError, socket.error):
        last_error = "Something went wrong while processing your request. Please try again."

    except Exception as e:
        # Catch-all to protect Chaquopy
        last_error = str(e)

    return make_response(
        success=False,
        error=last_error or "An unexpected error occurred. Please try again."
    )
    

def getPlayListUrls(youtube_url):
    """Return playlist item metadata for a YouTube playlist URL.

    Parameters:
        youtube_url (str): The playlist page URL.

    Returns:
        str|bool: JSON string of playlist items, or False on failure.
    """
    try:
        play_list = Playlist(youtube_url)

        data = [
            {
                "url": video.streams.get_highest_resolution().url,
                "title": video.title,
                "views": video.views,
                "date": video.publish_date.year,
                "duration": video.length,
                "thumbnail": video.thumbnail_url
            }
            for video in play_list.videos
        ]

        return json.dumps(data)
    except Exception:
        return False

def Searcher(inputer):
    """Search for videos by user query.

    Parameters:
        inputer: The search query input.

    Returns:
        str: JSON string containing search results or an error message.
    """
    try:

        # Coerce to string + trim
        query = str(inputer)
        if not query:
            return make_response(success=False, error="Query is empty.", result=None)

        search = VideosSearch(query=query)
        results = search.result()
        return make_response(success=True, error=None, result=results)

    except RequestException:
        err_msg = "A network error occurred while searching. Please check your connection and try again."
    except socket.gaierror:
        err_msg = "No internet connection. Please check your network and try again."
    except TimeoutError:
        err_msg = "The request took too long. Please try again."
    except PytubeFixError:
        err_msg = "Something went wrong while processing your request. Please try again."
    except Exception:
        traceback.print_exc()
        err_msg = "Something went wrong while processing your request. Please try again."

    traceback.print_exc()
    return make_response(success=False, error=err_msg, result=None)

# vdv = Searcher("Eritrean")
# print(vdv)

def SearchWithLink(inputer):
    """Fetch video metadata from a link for direct link-based lookup.

    Parameters:
        inputer: The video URL or identifier.

    Returns:
        str: JSON string containing video metadata or a user-facing error message.
    """
    try:

        video = Video.getInfo(inputer)

        data = {
            'videoId': str(video["id"]),
            'title': str(video["title"]),
            'viewNumber': str(video["viewCount"]["text"]),
            "date": str(video["publishDate"]),
            'channelName': str(video["channel"]["name"]),
            'description': str(video["description"])
        }

        return make_response(True, None, data)

    except VideoUnavailable:
        err_msg = "The requested video is unavailable or private."
    except RegexMatchError:
        err_msg = "Invalid video link or unable to extract video data."
    except ExtractError:
        err_msg = "We couldn't find a supported video format or stream for this link."
    except PytubeFixError:
        err_msg = "Something went wrong while processing your request. Please try again."
    except RequestException:
        err_msg = "A network error occurred. Please check your internet connection and try again."
    except TimeoutError:
        err_msg = "The request took too long. Please try again."
    except Exception:
        err_msg = "Something went wrong while processing your request. Please try again."

    traceback.print_exc()
    return make_response(success=False, error=err_msg)
