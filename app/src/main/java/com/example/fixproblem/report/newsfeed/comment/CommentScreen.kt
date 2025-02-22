package com.example.fixproblem.report.newsfeed.comment

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import com.example.fixproblem.R
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import com.example.fixproblem.data.model.remote.Comment
import com.example.fixproblem.data.model.remote.CommentReply
import com.example.fixproblem.extension.formatTimestamp
import com.example.fixproblem.presentation.theme.CustomColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentScreen(
    modifier: Modifier,
    isShowKeyboard:Boolean,
    reportId:String,
    onViewModelCreated: (CommentViewModel) -> Unit = {},
    viewModel: CommentViewModel = hiltViewModel()
){
    val comments = viewModel.comments.collectAsLazyPagingItems()
    val commentsWithReplies = viewModel.cmtsWithReplies.collectAsLazyPagingItems()
    val replyingTo by viewModel.replyingTo.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var commentText by remember { mutableStateOf("") }
    // truyền viewmodel
    LaunchedEffect(viewModel) {
        onViewModelCreated(viewModel)
    }
    LaunchedEffect(Unit) {
        viewModel.setReportId(reportId)
        viewModel.processIntent(CommentIntent.LoadComments)
    }
    Column(
        modifier = modifier
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally

    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Bình luận",
                    style = MaterialTheme.typography.titleLarge,
                    fontFamily = FontFamily(Font(R.font.medium, FontWeight.Bold)),
                    fontSize = 18.sp
                )
                    },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.White
            )
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            items(
                count = commentsWithReplies.itemCount,
                key = {index->
                    val comment =commentsWithReplies[index]
                    comment?.comment?.id ?:index
                }
            ){index->
                commentsWithReplies[index]?.let { commentsWithReplies->
                    CommentItemWithReplies(
                        commentWithReplies =commentsWithReplies,
                        onReply = {
                            viewModel.setReplyingTo(it)
                        },
                        onToggleReplies = {
                            viewModel.toggleReplies(it)
                        }
                    )
                }
            }
            when(commentsWithReplies.loadState.refresh){
                is LoadState.Loading->{
                    item {
                       CircularProgressIndicator()
                    }
                }
                is LoadState.Error->{
                    item {
                        Text(text = "Error")
                    }
                }
                else->{}
            }
        }
        CommentInputBar(
            commentText = commentText,
            onTextChange = {
                commentText = it
            },
            onSendClick = {
                if(replyingTo != null){
                    viewModel.addReply(commentText)
                }else{
                    viewModel.processIntent(CommentIntent.AddComment(commentText))
                }
                if(commentText.isNotBlank()){
                    commentText = ""
                }
            },
            onCancelReply = {viewModel.setReplyingTo(null)},
            replyingTo = replyingTo,
            showBottomSheet = isShowKeyboard
        )
    }
}
@Preview(showBackground = true)
@Composable
fun CommentScreenPreview() {
    val cmt = Comment(
        id = "Bùi Văn Tiến",
        userId = "Bùi Văn Tiến",
        content = "Ronaldo is good player",
        timestamp = 0
    )
   // CommentItem(cmt)
        //  CommentScreen(modifier = Modifier.fillMaxSize())
}
@Composable
fun CommentItemWithReplies(
    commentWithReplies: CommentReply,
    onReply: (Comment) -> Unit,
    onToggleReplies: (String) -> Unit
){
    Column{
        CommentItem(
            modifier = Modifier.padding(start = 5.dp),
            comment =  commentWithReplies.comment,
            onReply = { onReply(commentWithReplies.comment) }
        )
        //HIển thị reply
        if(commentWithReplies.comment.replyCount > 0){
            Text(
                modifier = Modifier.padding(start = 50.dp).clickable {
                    onToggleReplies(commentWithReplies.comment.id)
                },
                text = buildAnnotatedString {
                    withStyle(
                        SpanStyle(
                            fontFamily = FontFamily(Font(R.font.regular, FontWeight.Normal)),
                            fontSize = 15.sp
                        )
                    ){
                        append(if(commentWithReplies.isExpanded) "Ẩn" else "Hiển thị")
                        append(" ${commentWithReplies.comment.replyCount} ")
                        append("phản hồi")
                    }
                }
            )

        }
        //HIển thị reply
        if(commentWithReplies.isExpanded && commentWithReplies.replies.isNotEmpty()){
            commentWithReplies.replies.forEach { reply->
                CommentItem(
                    modifier = Modifier.padding(start = 46.dp),
                    comment = reply,
                    onReply = {
                        onReply(reply)
                    },
                    showReplyButton = false
                )
            }
        }
    }
}
@Composable
fun CommentItem(
    modifier: Modifier = Modifier,
    comment:Comment,
    onReply: () -> Unit,
    showReplyButton: Boolean = true
){
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        //Avatar
        AsyncImage(
            model = comment.userPhotoUrl,
            contentDescription = "Avatar",
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .border(1.dp, Color.LightGray, CircleShape),
            contentScale = ContentScale.Crop,
            placeholder = painterResource(id = R.drawable.ic_launcher_foreground),
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row (
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ){
                // Tên và thời gian
                Text(
                    text = comment.userName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    fontFamily =  FontFamily(Font(R.font.regular))
                )

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = comment.timestamp.formatTimestamp(),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray,
                    fontFamily =  FontFamily(Font(R.font.medium)),
                )
            }
            Text(text = comment.content, style = MaterialTheme.typography.bodyMedium, fontFamily = FontFamily(Font(R.font.medium)))

            // Reply button
            if (showReplyButton) {
                Text(
                    modifier = Modifier.clickable {
                        onReply()
                    },
                    text = "Trả lời",
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily(Font(R.font.regular))
                )
                /*TextButton(
                    onClick = onReply,
                ) {

                }*/
            }

        }
        IconButton(
            onClick = {}
        ) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = "More",
                tint = Color.Gray
            )
        }
        }
    }
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentInputBar(
    commentText: String,
    onTextChange: (String) -> Unit,
    onSendClick: () -> Unit,
    replyingTo :Comment? = null,
    onCancelReply: () -> Unit,
    showBottomSheet: Boolean
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(showBottomSheet) {
        if(showBottomSheet){
            focusRequester.requestFocus()
        }
    }
    Column {
        // Show reply indicator
        if (replyingTo != null) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Đang trả lời ${replyingTo.userName}",
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onCancelReply) {
                        Icon(Icons.Default.Close, "Hủy trả lời")
                    }
                }
            }
        }
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 8.dp,
            tonalElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = commentText,
                    onValueChange = onTextChange,
                    placeholder = {  Text(
                        if (replyingTo != null) "Viết phản hồi..."
                        else "Thêm bình luận..."
                    ) },
                    modifier = Modifier
                        .weight(1f)
                        .wrapContentHeight()
                        .focusRequester(focusRequester)
                    ,
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Color.LightGray.copy(alpha = 0.1f),
                        focusedContainerColor = Color.LightGray.copy(alpha = 0.1f),
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent
                    ),
                    maxLines = 5,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { onSendClick() })
                )
                IconButton(
                    onClick = {
                        onSendClick()
                    },
                    enabled = commentText.isNotEmpty()
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Gửi",
                        tint = if (commentText.isNotBlank())
                            CustomColor
                        else
                            Color.Gray
                    )
                }
            }
        }
    }

}