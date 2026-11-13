# MinimalTube — Ứng dụng xem YouTube Tối Giản Không Quảng Cáo

Dự án ứng dụng Android xem YouTube thuần văn bản (Brutalist / Minimalist Monochrome), không chứa bất kỳ icon đồ họa hay emoji nào, tích hợp trực tiếp thư viện `NewPipeExtractor` để bóc tách luồng phát trực tiếp sạch quảng cáo từ YouTube.

---

## 1. Đặc điểm nổi bật
- **100% Tiếng Việt:** Toàn bộ chuỗi hiển thị, nhãn điều hướng, thông báo đều bằng tiếng Việt chuẩn hóa.
- **Phong cách Tối giản Tuyệt đối (Text-Only / Flat Monochrome):**
  - Không có bất kỳ icon, sticker hay emoji nào.
  - Sử dụng các tab văn bản: `TRANG CHỦ`, `XU HƯỚNG`, `TÌM KIẾM`.
  - Các nút hành động bằng chữ trong khung viền dứt khoát: `[ PHÁT ]`, `[ TẢI VỀ ]`, `[ LƯU ]`.
  - Nền đen sâu `#000000` AMOLED tiết kiệm pin tối đa.
- **Tích hợp NewPipeExtractor:** Giải mã luồng video trực tiếp từ YouTube, không dùng API của Google, không phụ thuộc Google Play Services.
- **Trình phát ExoPlayer:** Phát video mượt mà 1080p/720p hoàn toàn không có quảng cáo đứt quãng.

---

## 2. Cấu trúc Dự án
```
MinimalTube/
├── app/
│   ├── build.gradle                 # Cấu hình dependency NewPipeExtractor & ExoPlayer
│   └── src/main/
│       ├── AndroidManifest.xml      # Khai báo quyền Internet và Activity
│       ├── java/com/minimal/tube/
│       │   ├── MinimalTubeApp.kt    # Khởi tạo NewPipe với Downloader
│       │   ├── DownloaderImpl.kt    # Bộ tải OkHttp cho NewPipeExtractor
│       │   ├── MainActivity.kt      # Điều hướng Tabs, Tìm kiếm, Phát video
│       │   ├── VideoAdapter.kt      # Hiển thị danh sách video theo thẻ tối giản
│       │   └── VideoItem.kt         # Data model video
│       └── res/
│           ├── layout/
│           │   ├── activity_main.xml # Giao diện chính với thanh tab chữ
│           │   └── item_video.xml    # Thẻ video tối giản không icon
│           └── values/
│               ├── strings.xml       # Chuẩn hóa tiếng Việt, không emoji
│               ├── colors.xml        # Bảng màu đen/trắng tương phản cao
│               └── themes.xml        # Theme Material AMOLED NoActionBar
└── .github/workflows/
    └── build-apk.yml                # CI/CD tự động xuất file APK trên GitHub Actions
```

---

## 3. Hướng dẫn Xuất file APK để cài lên điện thoại

### Cách 1: Tận dụng GitHub Actions (Khuyên dùng - Nhanh nhất, không cần cài Android Studio)
Nhờ gói **GitHub Pro** bạn vừa được duyệt, bạn có 3.000 phút máy ảo build code miễn phí mỗi tháng:
1. Tạo 1 repository mới trên GitHub cá nhân (đặt tên là `MinimalTube`).
2. Đẩy toàn bộ mã nguồn trong thư mục này lên GitHub:
   ```bash
   git init
   git add .
   git commit -m "Khoi tao MinimalTube thuan text khong icon"
   git branch -M main
   git remote add origin https://github.com/TÊN_CỦA_BẠN/MinimalTube.git
   git push -u origin main
   ```
3. Truy cập tab **Actions** trên repo GitHub của bạn $\rightarrow$ Hệ thống sẽ tự động biên dịch toàn bộ mã nguồn trong ~2 phút.
4. Bấm vào bản build thành công $\rightarrow$ Tải file **`MinimalTube-Debug-APK`** về điện thoại và cài đặt!

---

### Cách 2: Mở và Build bằng Android Studio
1. Mở **Android Studio** $\rightarrow$ Chọn **Open** $\rightarrow$ Trỏ tới thư mục `MinimalTube`.
2. Đợi Gradle đồng bộ (Sync) xong.
3. Chọn menu **Build** $\rightarrow$ **Build Bundle(s) / APK(s)** $\rightarrow$ **Build APK(s)**.
4. File APK sẽ nằm tại: `app/build/outputs/apk/debug/app-debug.apk`.
