# MinimalTube

<p align="center">
  <strong>A minimalist, ad-free YouTube client for Android engineered with pure typography.</strong>
</p>

<p align="center">
  <a href="https://github.com/merrthereboy/MinimalTube/releases/latest">
    <img src="https://img.shields.io/github/v/release/merrthereboy/MinimalTube?style=flat-square&color=white&label=Release" alt="Latest Release">
  </a>
  <a href="https://github.com/merrthereboy/MinimalTube/actions/workflows/build-apk.yml">
    <img src="https://img.shields.io/github/actions/workflow/status/merrthereboy/MinimalTube/build-apk.yml?style=flat-square&label=Build" alt="Build Status">
  </a>
  <img src="https://img.shields.io/badge/Platform-Android%207.0%2B-black?style=flat-square" alt="Android Version">
  <img src="https://img.shields.io/badge/License-GPL--3.0-gray?style=flat-square" alt="License">
</p>

---

## Overview

**MinimalTube** is an open-source, privacy-first Android application designed for users who seek a distraction-free video viewing experience. Unlike conventional media clients that clutter the screen with animations, cartoon icons, and algorithmic distractions, MinimalTube strips away all decorative visuals in favor of a clean, high-contrast typography interface.

The application leverages [NewPipeExtractor](https://github.com/TeamNewPipe/NewPipeExtractor) to parse media streams directly from YouTube servers without proprietary Google Play Services or user tracking.

---

## Key Features

- **Strictly Ad-Free Playback:** Bypasses video advertisements, sponsored banners, and tracking telemetry by streaming direct media links.
- **Pure Typography Design (Zero Emojis, Zero Icons):** Every navigation element, status indicator, and interaction button is driven purely by clean text and minimalist geometric borders.
- **AMOLED Deep Black Theme:** Fully native `#000000` canvas engineered to reduce eye fatigue and minimize battery consumption on OLED displays.
- **Hardware-Accelerated Engine:** Integrates Google's `androidx.media3:media3-exoplayer` for low-latency streaming up to 1080p resolution.
- **Privacy & Lightweight:** No Google login required, zero telemetry, and an ultra-compact APK size under 11 MB.

---

## Interface Concept

```
[ TRANG CHỦ ]    [ XU HƯỚNG ]    [ TÌM KIẾM ]
────────────────────────────────────────────────────────────

┌──────────────────────────────────────────────────────────┐
│                                                          │
│                   VIDEO THUMBNAIL (16:9)                 │
│                                                          │
└──────────────────────────────────────────────────────────┘
TIÊU ĐỀ VIDEO HIỂN THỊ RÕ RÀNG KHÔNG BIỂU TƯỢNG
Kênh Đăng Tải • 1.2M Lượt xem • 2 ngày trước

[ PHÁT ]    [ TẢI VỀ ]    [ LƯU ]
────────────────────────────────────────────────────────────
```

---

## Download & Installation

The latest pre-compiled APK binary is available directly on the GitHub Releases page:

👉 **[Download Latest APK (MinimalTube-v1.0.1.apk)](https://github.com/merrthereboy/MinimalTube/releases/latest)**

### Installation Steps:
1. Download the `.apk` file to your Android device (Android 7.0 / API 24 or newer).
2. Open the downloaded file and grant permission for **"Install from unknown sources"** if prompted by your browser or file manager.
3. Complete installation and launch MinimalTube.

---

## Tech Stack & Architecture

- **Language:** Kotlin 1.9
- **Media Engine:** AndroidX Media3 ExoPlayer (`1.2.1`)
- **Extraction Core:** [NewPipeExtractor](https://github.com/TeamNewPipe/NewPipeExtractor) (`v0.24.5`)
- **HTTP Client:** Square OkHttp (`4.12.0`)
- **Image Pipeline:** Coil (`2.5.0`)
- **CI/CD Automation:** GitHub Actions (automated compilation & release pipeline)

---

## Building from Source

### Prerequisites
- JDK 17 or higher
- Android SDK (API 34)

### Build via Command Line
```bash
# Clone the repository
git clone https://github.com/merrthereboy/MinimalTube.git
cd MinimalTube

# Compile Debug APK
./gradlew assembleDebug
```
The compiled output will be generated at:
`app/build/outputs/apk/debug/app-debug.apk`

---

## Disclaimer

MinimalTube is an independent open-source client developed under Fair Use principles and the GNU General Public License v3.0. This project is not affiliated with, maintained by, or endorsed by Google LLC or YouTube. All trademarks belong to their respective owners.
