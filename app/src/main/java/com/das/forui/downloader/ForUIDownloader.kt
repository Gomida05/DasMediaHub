package com.das.forui.downloader

import com.das.forui.data.model.AppUpdateInfo
import com.das.forui.data.model.DownloadData


interface ForUIDownloader {

    fun downloadVideo(url: String, title: String): Long

    fun downloadMusic(url: String, title: String): Long

    fun downloadPlayListMusic(urls: List<DownloadData>): Long

    fun downloadVideosPlayList(url: String, playListName: String, title: String): Long

    fun downloadNewVersionAPK(appInfo: AppUpdateInfo)
}