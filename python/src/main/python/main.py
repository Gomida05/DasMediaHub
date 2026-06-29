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
import json, traceback, socket, ssl, time

ssl._create_default_https_context = ssl._create_unverified_context
def make_response(success: bool, error: Optional[str] = None, result: Any = None):
    return json.dumps(
        {
            "success": success,
            "error": error,
            "result": result
        }
    )


def get_video_url(video_url, retries: int = 3, delay: float = 1.0):
    last_error = None

    for attempt in range(1, retries + 1):
        try:
            yt = YouTube(video_url)

            stream = yt.streams.get_highest_resolution()

            # if stream is None:
                # stream = yt.streams.get_audio_only()

            if stream is None or not stream.url:
                raise ExtractError("No valid stream found")

            return make_response(
                success=True,
                result=str(stream.url)
            )

        except (VideoUnavailable, RegexMatchError) as e:
            return make_response(
                success=False,
                error=str(e) or "Video unavailable or invalid URL"
            )

        except (ExtractError, PytubeFixError) as e:
            last_error = e

        except (ssl.SSLError, socket.error, RequestException) as e:
            last_error = f"Network/TLS error: {e}"

        except Exception as e:
            last_error = f"Unexpected error: {e}"


        time.sleep(delay)

    # --- Final failure ---
    return make_response(
        success=False,
        error=str(last_error) or "Failed to fetch video stream due to network issues"
    )


def get_audio_url(media_url, retries: int = 3):
    last_error = None

    for attempt in range(retries):
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

        except (VideoUnavailable, RegexMatchError) as e:
            # These won't succeed on retry
            return make_response(
                success=False,
                error=str(e)
            )

        except (PytubeFixError, ExtractError, RequestException, ssl.SSLError, socket.error) as e:
            last_error = e
            time.sleep(1.2)

        except Exception as e:
            # Catch-all to protect Chaquopy
            last_error = e
            time.sleep(1.2)

    return make_response(
        success=False,
        error=f"Failed to fetch audio stream after {retries} attempts: {last_error}"
    )
    

def getPlayListUrls(youtube_url):

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
    except Exception as e:
        return False

def Searcher(inputer):
    try:

        # Coerce to string + trim
        query = str(inputer)
        if not query:
            return make_response(success=False, error="Query is empty.", result=None)

        search = VideosSearch(query=query)
        results = search.result()
        return make_response(success=True, error=None, result=results)

    except RequestException as e:
        err_msg = f"Network error while searching videos: {e}"
    except socket.gaierror:
        err_msg = "DNS lookup failed — check your internet connection."
    except TimeoutError:
        err_msg = "Search request timed out."
    except PytubeFixError as e:
        err_msg = f"YouTube library error: {e}"
    except Exception as e:
        traceback.print_exc()
        err_msg = f"Unexpected error during search: {e}"

    traceback.print_exc()
    return make_response(success=False, error=err_msg, result=None)

# vdv = Searcher("Eritrean")
# print(vdv)

def SearchWithLink(inputer):
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

        val = json.dumps(data)
        return make_response(True, None, data)

    except VideoUnavailable:
        err_msg = "The requested video is unavailable or private."
    except RegexMatchError:
        err_msg = "Invalid YouTube URL or unable to extract video data."
    except ExtractError:
        err_msg = "Error extracting video information."
    except PytubeFixError as e:
        err_msg = f"YouTube processing error: {str(e)}"
    except RequestException as e:
        err_msg = f"Network request error: {str(e)}"
    except TimeoutError:
        err_msg = "Request to YouTube timed out."
    except Exception as e:
        err_msg = f"Unexpected error fetching video info: {str(e)}"

    traceback.print_exc()
    return make_response(success=False, error=err_msg)