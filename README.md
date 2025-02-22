# FixProblem
Ứng dụng để chụp lại những vấn đề môi trường xung quanh ta
*Tính năng chính:
  +Chụp ảnh các vấn đề lên bảng tin,mọi người có thể like,comment và chia sẻ để bàn luận và chia sẻ các vấn đề môi trường xã hội
  +Các vấn đề được chụp sẽ hiện lên trên map,mọi người có thể ấn và xem.
  +Tíng năng chỉ đường khi người dùng muốn và người sửa chữa có thể đến và sửa,...
  +Người dùng có thể đổi sang chế độ tối cho dễ nhìn
  +Gửi thông báo cho người dùng khi có like và comment
*Techstack:
  +Jetpackcompose để xây dựng giao diện
  +MapboxSdk,Navigation SDK v3,SearchSDK dành cho các chức năng liên quan đến map
  +FirebaseFirestore,Storage,Function,Authencation dành cho các chức năng đăng nhập,lưu trữ bài viết,bình luận,hình ảnh,gửi thông báo khi có người like và cmt,...
  +CameraX để chụp ảnh và quay video
  +DaggerHilt để cung cấp các dependency
  +RoomDatabase để lưu trữ các bài viết khi người dùng offline cũng có thể xem lại
  +Paging3 để phân trang việc tải bài viết giúp tăng hiệu suất và trải nghiệm người dùng
