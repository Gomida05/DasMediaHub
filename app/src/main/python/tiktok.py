from yt_dlp import YoutubeDL
from main import make_response, time, ssl, socket
from yt_dlp.utils import DownloadError, ExtractorError


def get_video_stream(url: str):
    last_error = None
    
    ydl_opts = {
        "quiet": True,
        "no_warnings": True,
        "format": "bv*+ba/b",
        "noplaylist": True
    }

    for attempt in range(1, 3 + 1):
        try:
            with YoutubeDL(ydl_opts) as ydl:
                info = ydl.extract_info(url, download=False)

                if not info:
                    raise ExtractorError("Failed to extract video info")
                
                formats = info.get("formats", [])
                
                best = None
                for f in formats:
                    if (
                        f.get("ext") == "mp4"
                        and f.get("vcodec") != "none"
                        and f.get("acodec") != "none"
                        and f.get("url")
                    ):
                        best = f
                        break
                
                if not best:
                    raise ExtractorError("No progressive MP4 stream found")

                result = {
                    "id": info.get("id"),
                    "title": info.get("title"),
                    "duration": info.get("duration"),
                    "thumbnail": info.get("thumbnail"),
                    "uploader": info.get("uploader"),
                    "view_count": info.get("view_count"),
                    "like_count": info.get("like_count"),
                    "webpage_url": info.get("webpage_url"),
                    "stream_url": best
                }

                return make_response (
                    success =  True,
                    error = None,
                    result= result
                )

        except DownloadError:
             last_error = "VIDEO_UNAVAILABLE"
        except ExtractorError:
            last_error = "TIKTOK_EXTRACTION_FAILED"
            
        except (ssl.SSLError, socket.error) as e:
            last_error = f"Network/TLS error: {e}"

        except Exception as e:
            last_error = f"Unexpected error: {e}"
    
    return make_response(
        success = False,
        error = last_error or "FAILED_AFTER_RETRIES",
        result = None
    )



def testVideo(url, out_dir: str = "."):

    ydl_format = {
        "format": "bv*+ba/b",
        "outtmpl": f"{out_dir}/%(upload)s_%(upload_date>%Y-%m-%f)s_%(id)s.%(ext)s",
        "merge_output_format": "mp4",
        "noprogress": False,
        "quiet": False
    }

    with YoutubeDL(ydl_format) as ydl:
        info = ydl.extract_info(url= url)
        print(f"here is the info \n {info.keys()}")
        return ydl.prepare_filename(info_dict= info)