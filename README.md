# MinimalTube

<p align="center">
  <img src="docs/logo_banner.png" alt="MinimalTube Banner" width="600" />
</p>

<p align="center">
  <strong>A modern, distraction-free YouTube client for Android engineered with luxury minimalist dark mode, gesture controls, and zero ads.</strong>
</p>

<p align="center">
  <a href="https://github.com/merrthereboy/MinimalTube/releases/latest">
    <img src="https://img.shields.io/github/v/release/merrthereboy/MinimalTube?style=flat-square&color=00F2FE&label=Release" alt="Latest Release">
  </a>
  <a href="https://github.com/merrthereboy/MinimalTube/actions/workflows/build-apk.yml">
    <img src="https://img.shields.io/github/actions/workflow/status/merrthereboy/MinimalTube/build-apk.yml?style=flat-square&label=Build" alt="Build Status">
  </a>
  <img src="https://img.shields.io/badge/Platform-Android%207.0%2B-black?style=flat-square" alt="Android Version">
  <img src="https://img.shields.io/badge/Signature-Persistent%20Keystore-blue?style=flat-square" alt="Keystore Signature">
  <img src="https://img.shields.io/badge/License-GPL--3.0-gray?style=flat-square" alt="License">
</p>

---

## Overview

**MinimalTube** is an open-source, privacy-focused Android application crafted for users who demand a clean, elegant, and ad-free YouTube experience. Instead of noisy algorithms, cluttering popups, and intrusive banners, MinimalTube features a tailored **Luxury Dark Mode** interface adhering strictly to an 80/15/5 visual hierarchy:
- **80% Canvas:** Deep Obsidian Dark Background (`#0D0D11`).
- **15% Surfaces & Typography:** Secondary card surfaces (`#18181F`) with pure white headers (`#FFFFFF`) and muted slate subtitles (`#A1A1AA`).
- **5% Brand Gradient Accents:** Smooth spectrum transition from `#00F2FE` (Cyan) &rarr; `#2472FC` (Blue) &rarr; `#7928CA` (Violet) &rarr; `#FF2E93` (Magenta) &rarr; `#FFBE98` (Peach) for active indicators and primary interactions.

---

## Key Features (v1.2.0 Pro)

- **100% Ad-Free Video Playback:** Zero pre-roll ads, mid-rolls, or sponsored interruptions.
- **YouTube-Grade Player Gestures & HUD:**
  - Swipe up/down on the right side to adjust **Volume** with visual HUD.
  - Swipe up/down on the left side to adjust **Brightness** with visual HUD.
  - Double-tap right to **seek forward 10s** (`+10s`), double-tap left to **seek backward 10s** (`-10s`).
  - Speed selector (0.5x, 0.75x, 1.0x, 1.25x, 1.5x, 2.0x) and quality switcher (1080p, 720p, 480p, 360p, Auto).
- **Livestream Playback Support:** Native HLS (`m3u8`) streaming with ExoPlayer HttpDataSource integration.
- **Docked Mini-Player:** Minimize the player to continue listening while browsing other tabs or videos.
- **Background Playback (Audio Service):** Keep listening to music or podcasts with screen off via Android Foreground Service.
- **Real YouTube Comments Drawer:** View real-time YouTube comments, like counts, and author avatars.
- **Dedicated Channel Profile Pages:** Tap any channel name or avatar to view channel banner, subscribers, and full video catalog.
- **Deep Account Synchronization:**
  - Secure Google sign-in preserving session cookies locally.
  - Syncs your exact subscribed channels (with horizontal avatar carousel) and subscription feeds.
- **Smooth Vertical Shorts (ViewPager2):** Full-screen portrait Shorts player with seamless vertical swiping.
- **Persistent Signing Keystore:** Updates can be installed directly over existing versions without uninstallation.

---

## Download & Releases

Download the official pre-compiled APK binary directly from GitHub:

👉 **[Download MinimalTube v1.2.0 APK](https://github.com/merrthereboy/MinimalTube/releases/latest)**

---

## Building from Source

```bash
# Clone the repository
git clone https://github.com/merrthereboy/MinimalTube.git
cd MinimalTube

# Compile Release APK
./gradlew assembleRelease
```
The output APK will be generated at:
`app/build/outputs/apk/release/app-release.apk`

---

## Author & Credits

Developed with ❤️ by **Đoàn Tiến Đạt** ([@merrthereboy](https://github.com/merrthereboy)).
Built on the open-source foundation of the NewPipe project.
