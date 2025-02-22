package com.example.fixproblem.report.newsfeed.comment

import com.example.fixproblem.data.model.remote.Comment

data class CommentUiState(
    val comments: List<Comment> = emptyList(),
    val isSubmitting : Boolean = false,
    val isRefreshing : Boolean = false,
    val error:String =""
)
sealed interface CommentEffect{
    data class ShowToast(val toast:String):CommentEffect
    data class Error(val error:String):CommentEffect
    data class Reply(val idComment:String):CommentEffect
}