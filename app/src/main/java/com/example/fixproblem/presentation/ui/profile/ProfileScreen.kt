package com.example.fixproblem.presentation.ui.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintSet
import coil.compose.AsyncImage
import com.example.fixproblem.presentation.theme.CustomColor

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ProfileScreenPreview() {
    DetailProfileScreen()
}
@Composable
fun ProfileScreen() {
  //  val selectedCategory by remember { mutableStateOf(posts) }
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                DetailProfileScreen()
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = { /*TODO*/ },
                    modifier = Modifier
                        .wrapContentSize()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = CustomColor,
                        containerColor = Color.White
                    )  ,
                    border = BorderStroke(1.dp, CustomColor)
                ) {
                    Text("Bài viết của tôi", fontWeight = FontWeight.Bold, color = CustomColor)
                }
            }
            items(6) { index ->
                //NewsFeedCard(post = selectedCategory[index])
                Spacer(Modifier.height(10.dp))
            }
        }
    }
@Composable
fun DetailProfileScreen(modifier:Modifier =Modifier){
    val constrains = ConstraintSet {
        val (coverImg
                ,avatar
                ,name,
            location,
            bio,
            statsRow,
            actionsRow
        )= createRefsFor("coverImg","avatar","name","location","bio","statsRow","actionsRow")
        constrain(coverImg) {
            top.linkTo(parent.top)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
        }
        constrain(avatar) {
            top.linkTo(parent.top, margin = 100.dp)
            start.linkTo(parent.start, margin = 16.dp)
            end.linkTo(parent.end, margin = 16.dp)
        }
        constrain(name) {
            top.linkTo(avatar.bottom)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
        }
        constrain(location) {
            top.linkTo(name.bottom, margin = 4.dp)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
        }
        constrain(bio) {
            top.linkTo(location.bottom, margin = 4.dp)
            start.linkTo(parent.start, margin = 16.dp)
            end.linkTo(parent.end, margin = 16.dp)
        }
        constrain(statsRow) {
            top.linkTo(actionsRow.bottom, margin = 16.dp)
        }
        constrain(actionsRow) {
            top.linkTo(bio.bottom, margin = 16.dp)
        }
    }
    val link ="https://timelinecovers.pro/facebook-cover/download/beautiful_sunset-facebook-cover.jpg"
    val linkAvatar ="https://scontent.fhan15-2.fna.fbcdn.net/v/t39.30808-6/473188605_1155742485916434_1345283418346979923_n.jpg?_nc_cat=1&ccb=1-7&_nc_sid=127cfc&_nc_eui2=AeHQlsGTZy8iQr1hd6O4fM0k6owjMnVcKljqjCMydVwqWGyElXF3yeudhzTY7hksjcpjNHEZhu8bzqru5KtLGOuz&_nc_ohc=eKcD99rVSsMQ7kNvgFd6MPF&_nc_zt=23&_nc_ht=scontent.fhan15-2.fna&_nc_gid=ADiMJsOreNjDWiIg4_ufNkb&oh=00_AYCaVI_fQihyMKjzF3TxWyaE2wMtAWq45gOTMIXJwpwAlQ&oe=6787E341"
    ConstraintLayout(
        modifier = modifier.fillMaxSize(),
        constraintSet = constrains
    ){
        AsyncImage(
            model = link,
            modifier = modifier.fillMaxWidth().layoutId("coverImg"),
            contentScale = ContentScale.FillWidth,
            contentDescription = "Cover",
            filterQuality = FilterQuality.High
        )
        Box(
            modifier = Modifier
                .layoutId("avatar")
                .size(100.dp) // Kích thước Box phải bằng kích thước ảnh
                .clip(CircleShape) // Định hình Box thành hình tròn
                .border(2.dp, CustomColor, CircleShape) // Thêm viền
        ) {
            AsyncImage(
                model = linkAvatar,
                modifier = Modifier.fillMaxSize(), // Để ảnh lấp đầy toàn bộ Box
                contentScale = ContentScale.Crop,
                contentDescription = "Avatar",
                filterQuality = FilterQuality.High
            )
        }
        Text(
            text = "Bùi Văn Tiến",
            fontSize = 20.sp,
            modifier = modifier.layoutId("name")
        )
        Text(
            text = "Bắc Từ Liêm,Hà Nội",
            fontSize = 16.sp,
            modifier = modifier.layoutId("location"),
            color = Color.LightGray
        )
        Text(
            text = "Cristiano Ronaldo",
            fontSize = 16.sp,
            modifier = modifier.layoutId("bio"),
        )
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = modifier.fillMaxWidth()
                .height(IntrinsicSize.Min)
                .padding(16.dp)
                .layoutId("statsRow"),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatItem(label = "Bài Viết", value = "10")
            Spacer(modifier.width(10.dp))
            VerticalDivider(color = CustomColor)
            StatItem(label = "Số lượt thích", value = "100")
            VerticalDivider(color = CustomColor)
            StatItem(label = "Tiền ủng hộ", value = "10000")
        }
        Row (
            horizontalArrangement = Arrangement.SpaceAround,
            modifier = modifier.fillMaxWidth().layoutId("actionsRow").padding(10.dp)
        ){
            OutlinedButton(
                onClick = {},
                shape = CircleShape,
                border =BorderStroke(1.dp, CustomColor)
            ) {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = "Email",
                    tint = CustomColor
                )
            }
            Button(
                onClick = { /*TODO*/ },
                modifier = modifier.fillMaxWidth().weight(1f).padding(start= 10.dp,end =10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CustomColor)
            ) {
                Text(text = "Theo dõi", color = Color.White)
            }
            OutlinedButton(
                onClick = {},
                shape = CircleShape,
                border = BorderStroke(1.dp, CustomColor)
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share",
                    tint = CustomColor
                )
            }
        }
    }
}
@Composable
fun StatItem(
    label:String,
    value:String
){
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = label,color = CustomColor, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value,fontSize = 14.sp, color = Color.Gray)
    }
}



