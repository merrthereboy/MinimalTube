package com.minimal.tube

import android.app.Application
import org.schabi.newpipe.extractor.NewPipe

class MinimalTubeApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Khởi tạo bộ bẻ khóa NewPipeExtractor
        NewPipe.init(DownloaderImpl.getInstance())
    }
}
