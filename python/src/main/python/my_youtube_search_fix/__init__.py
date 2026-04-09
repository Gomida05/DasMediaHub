from .search import Search, VideosSearch, ChannelsSearch, PlaylistsSearch, CustomSearch, ChannelSearch
from .extras import Video, Playlist, Suggestions, Hashtag, Comments, Transcript, Channel
from .streamurlfetcher import StreamURLFetcher
from .core.constants import *
from .core.utils import *

__title__ = 'my_youtube_search_fix'
__version__ = '1.6.2.post1'
__author__ = 'alexmercerind'
__license__ = 'MIT'

from .legacy import SearchVideos, SearchPlaylists
from .legacy import SearchVideos as searchYoutube