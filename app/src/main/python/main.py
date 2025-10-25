from pytubefix import YouTube, Playlist
from pytubefix.exceptions import (
    PytubeFixError,
    VideoUnavailable,
    RegexMatchError,
    ExtractError
)
from youtubesearchpython import Video, VideosSearch
from requests import RequestException
from typing import Any, Optional
import json, traceback, socket

def make_response(success: bool, error: Optional[str] = None, result: Any = None):
    return json.dumps(
        {
            "success": success,
            "error": error,
            "result": result
        }
    )


def get_video_url(video_url: str):
    try:
        yt = YouTube(video_url)
        stream = yt.streams.get_highest_resolution()
        return make_response(success = True, result = str(stream.url))
    except Exception as e:
        print(f"error in url {e}")
        return make_response(success = False, result = str(e))


def get_audio_url(media_url):
    try:
        yt = YouTube(media_url)
        stream = yt.streams.get_audio_only()
        
        return make_response(success = True, result = str(stream.url))
    except Exception as e:
        print(f"error in url {e}")
        return make_response(success = False, result = str(e))
    

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
        print(f"There is an error in searching that playlist {e}")
        return False

def Searcher(inputer: str):

    try:
        search = VideosSearch(inputer, limit=80)
        results = search.result()
        return make_response(success=True, error=None, result= results)

    except RequestException as e:
        err_msg = f"Network error while searching videos: {str(e)}"
    except socket.gaierror:
        err_msg = "DNS lookup failed — check your internet connection."
    except TimeoutError:
        err_msg = "Search request timed out."
    except PytubeFixError as e:
        err_msg = f"YouTube library error: {str(e)}"
    except Exception as e:
        err_msg = f"Unexpected error during search: {str(e)}"

    print(err_msg)
    traceback.print_exc()
    return make_response(success=False, error=err_msg)


def SearchWithLink(inputer: str):
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

    print(err_msg)
    traceback.print_exc()
    return make_response(success=False, error=err_msg)