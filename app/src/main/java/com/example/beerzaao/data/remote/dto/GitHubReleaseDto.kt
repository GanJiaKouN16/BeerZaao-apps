package com.example.beerzaao.data.remote.dto

import com.google.gson.annotations.SerializedName

data class GitHubRelease(
    @SerializedName("tag_name")
    val tagName: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("body")
    val body: String?,
    @SerializedName("published_at")
    val publishedAt: String?,
    @SerializedName("html_url")
    val htmlUrl: String?,
    @SerializedName("assets")
    val assets: List<GitHubAsset>?
)

data class GitHubAsset(
    @SerializedName("name")
    val name: String,
    @SerializedName("browser_download_url")
    val downloadUrl: String,
    @SerializedName("size")
    val size: Long
)

data class UpdateInfo(
    val version: String,
    val downloadUrl: String,
    val releaseDate: String?,
    val notes: String?,
    val releaseUrl: String?,
    val patchUrl: String? = null
) {
    val latestVersion: String get() = version
    val apkUrl: String get() = downloadUrl
}
