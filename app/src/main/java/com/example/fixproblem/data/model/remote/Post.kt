package com.example.fixproblem.data.model.remote

data class Post(
    val id: Int,
    val imageUrl:Int,
    val caption:String,
    var like :Int = 0,
    val cmt :Int = 0,
    val share:Int = 0,
    //val imageUser:Int,
    val author:String,
    val timeAgo:String
)