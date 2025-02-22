package com.example.fixproblem.report.newsfeed

import androidx.paging.PagingData
import com.example.fixproblem.data.model.remote.Report

data class NewsFeedState(
    val isLoading: Boolean = false,
    val currentFilter: String = "Tất cả",
    val pagedReports: PagingData<Report> = PagingData.empty(),
    val error: String? = null
)
// Effects for NewsFeed
sealed interface NewsFeedEffect {
    data class ShowToast(val message: String) : NewsFeedEffect
    data class NavigateToDetail(val reportId: String) : NewsFeedEffect
    data class ShareReportLink(val reportUrl: String) : NewsFeedEffect
}
