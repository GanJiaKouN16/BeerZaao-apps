package com.example.beerzaao.data.remote.dto

data class UpdateInfo(
    val version: String,
    val download_url: String,
    val release_date: String,
    val notes: String,
    val patchUrl: String? = null
) {
    val latestVersion: String get() = version
    val apkUrl: String get() = download_url
}
