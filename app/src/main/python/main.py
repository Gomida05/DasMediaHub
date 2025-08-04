from pytubefix import YouTube, Playlist
from youtubesearchpython import Video, VideosSearch
import json, traceback


def get_video_url(video_url: str):
    try:
        yt = YouTube(video_url)
        stream = yt.streams.get_highest_resolution()
        return str(stream.url)
    except Exception as e:
        print(f"error in url {e}")
        return False


def get_audio_url(media_url):
    try:
        yt = YouTube(media_url)
        stream = yt.streams.get_audio_only()

        return str(stream.url)
    except Exception as e:
        print(f"error in url {e}")
        return False


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

        return data
    except Exception as e:
        print(f"There is an error in searching that playlist {e}")
        return False

def Searcher(inputer: str):

    try:
        search = VideosSearch(inputer, limit=30)
        results = search.result()
        data = [
            {
                'videoId': str(video['id']),
                'title': str(video['title']),
                'views': str(video["viewCount"]["short"]),
                'dateOfVideo': str(video["publishedTime"]),
                'duration': str(video['duration']),
                'channelName': str(video['channel']['name']),
                'channelThumbnailsUrl': str(video['channel']['thumbnails'][0]["url"])
            }
            for video in results['result']
        ]

        return json.dumps(data)
    except Exception as e:
        print(e)
        return False


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
        return val
    except Exception as e:
        print(f"fff{e}")
        print(f"traceback error {traceback.print_exc()}")
        return e