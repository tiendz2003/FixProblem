package com.example.fixproblem.report.newsfeed

import com.example.fixproblem.data.model.remote.Report

sealed interface NewsFeedIntent {
    data object GetReport:NewsFeedIntent
    data class UpdateCategory(val category: String):NewsFeedIntent
    data class LikeReport(val reportId:String,val isLiked:Boolean):NewsFeedIntent
    data class CommentReport(val reportId:String):NewsFeedIntent
    data class ShareReport(val reportId:String):NewsFeedIntent
    data class PrefetchImages(val reports: List<Report>) : NewsFeedIntent

}