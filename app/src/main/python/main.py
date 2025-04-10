import http.client, json, traceback
from pytubefix import YouTube, exceptions
from youtubesearchpython import Video, VideosSearch


def get_video_url(video_url: str):
    try:
        yt = YouTube(video_url)
        stream = yt.streams.get_highest_resolution()
        return str(stream.url)
    except Exception as e:
        print(f"error in url {e}")
        return False
    

# get_video_url("https://www.youtube.com/watch?v=buC8fFeYjXw&list=WL&index=4")
# get_video_url("https://www.youtube.com/watch?v=I6Veu_3O3UE")

def get_audio_url(media_url):
    
    try:
        # data = []
        # for i in media_url:
        yt = YouTube(media_url)
        stream = yt.streams.get_audio_only()
        
        return stream.url
    except Exception as e:
        print(f"error in url {e}")
        return False

# print(get_audio_url("I6Veu_3O3UE"))
def Searcher(inputer:str):

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
# b=Searcher("ethiopian music hagerie")
# print(b)
    
def SearchWithLink(inputer: str):
    try:

        video=Video.getInfo(inputer)
        
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
        print(f"trackback error {traceback.print_exc()}")
        return e
# b=SearchWithLink("https://www.youtube.com/watch?v=G4JsH6onYdY")
# print(b)

# print(b)
# SearchWithLink("eritrean")
def DownloadVideo(link:str, getPath: str)->(str| None| Exception):
    links= f"https://www.youtube.com/watch?v={link}"
    path = getPath
    try:
        stream = YouTube(links, client="ANDROID").streams.get_highest_resolution()
        sanitized_title = "".join(c for c in stream.title if c.isalnum() or c in (' ', '-', '_')).rstrip()
        filer= stream.download(output_path=path, filename=sanitized_title, timeout=20, max_retries=10)
        print(f"filler is here \n{filer}")
        return filer
    except exceptions.RegexMatchError as b:
        print(b)
        return None
    except http.client.IncompleteRead as v:
        DownloadVideo(links, path)
        print("we trying")
    except Exception as e:
        print(e)
        return False


def DownloadMusic(link: str, getPath: str):
    links = f"https://www.youtube.com/watch?v={link}"
    path = getPath
    try:
        stream = YouTube(links, client="ANDROID").streams.get_audio_only()

        sanitized_title = "".join(c for c in stream.title if c.isalnum() or c in (' ', '-', '_')).rstrip()
        
        filer=stream.download(output_path=path, filename=f"{sanitized_title}.mp3", timeout=9000, max_retries=35)
        return str(filer)
    except http.client.IncompleteRead as v:
        DownloadMusic(links, path)
        return "we trying"
    except exceptions.RegexMatchError as b:
        print(b)
        return None
    except Exception as e:
        print(f"downloading error {e}")
        return f"False!!!3{e}"
        # return e
# DownloadMusic("https://www.youtube.com/watch?v=buC8fFeYjXw&list=WL&index=4", "C:\\Users\\esrom\\AndroidStudioProjects\\ForUI\\app\\src\\main\\python")