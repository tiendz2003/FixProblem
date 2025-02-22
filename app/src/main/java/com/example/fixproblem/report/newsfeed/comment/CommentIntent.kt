package com.example.fixproblem.report.newsfeed.comment

sealed class CommentIntent{
    data object LoadComments : CommentIntent()
    data class AddComment(val text: String) : CommentIntent()
    data class LikeComment(val commentId: String) : CommentIntent()
    data class AddReply(val commentId: String, val text: String) : CommentIntent()

}