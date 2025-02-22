package com.example.fixproblem.presentation.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage


@Composable
fun ProfileListPost(
    modifier: Modifier =Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(8.dp)) {
        items(10){index->
            PostItem()
            if(index <9){
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = Color.LightGray.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
fun PostItem(
    modifier: Modifier =Modifier
) {
   Column(
       modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
   ) {
       Row(
           modifier = modifier.fillMaxWidth(),
           verticalAlignment = Alignment.CenterVertically
       ) {
           // Post content
           Text(
               text = "Đây là nội dung bài viết số ${(1..1000).random()}",
               modifier = Modifier.padding(vertical = 8.dp)
           )

           // Post image (if any)
           AsyncImage(
               model = "https://picsum.photos/seed/${(1..1000).random()}/400/300",
               contentDescription = "Post image",
               modifier = Modifier
                   .fillMaxWidth()
                   .height(200.dp)
                   .clip(RoundedCornerShape(8.dp)),
               contentScale = ContentScale.Crop
           )

           // Interaction buttons
           Row(
               modifier = Modifier
                   .fillMaxWidth()
                   .padding(top = 8.dp),
               horizontalArrangement = Arrangement.SpaceBetween
           ) {
               InteractionButton(
                   icon = Icons.Default.FavoriteBorder,
                   text = "${(1..100).random()} Thích"
               )
               InteractionButton(
                   icon = Icons.Default.Email,
                   text = "${(1..50).random()} Bình luận"
               )
               InteractionButton(
                   icon = Icons.Default.Share,
                   text = "Chia sẻ"
               )
           }
       }
   }
}

@Composable
fun InteractionButton(icon: ImageVector, text: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = Color.Gray
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            color = Color.Gray,
            fontSize = 14.sp
        )
    }
}
