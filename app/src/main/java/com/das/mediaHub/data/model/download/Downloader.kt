package com.das.mediaHub.data.model.download

import com.das.mediaHub.data.model.AppUpdateInfo

interface Downloader {

    /**
     * Download a single video.
     * @param url the video URL
     * @param title display title for the download
     * @param notificationId unique ID for progress notification
     * @return the DownloadManager downloadId
     */
    fun downloadVideo(url: String, title: String, notificationId: Int): Long

    /**
     * Download a single music track.
     * @param url the music URL
     * @param title display title for the download
     * @param notificationId unique ID for progress notification
     * @return the DownloadManager downloadId
     */
    fun downloadMusic(url: String, title: String, notificationId: Int): Long

    /**
     * Download multiple music tracks as a playlist.
     * @param urls list of DownloadData objects
     * @param playlistName name of the playlist folder
     */
    fun downloadPlayListMusic(urls: List<DownloadData>, playlistName: String)

    /**
     * Download multiple videos as a playlist.
     * @param urls list of pairs containing URL and title
     * @param playlistName name of the playlist folder
     */
    fun downloadVideosPlayList(urls: List<Pair<String, String>>, playlistName: String)

    /**
     * Download a new version APK.
     */
    fun downloadNewVersionAPK(appInfo: AppUpdateInfo)
}