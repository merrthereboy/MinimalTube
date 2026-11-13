package com.minimal.tube

import android.app.Application
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.localization.ContentCountry
import org.schabi.newpipe.extractor.localization.Localization

class MinimalTubeApp : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            NewPipe.init(
                DownloaderImpl.getInstance(),
                Localization("vi", "VN"),
                ContentCountry("VN")
            )
        } catch (e: Exception) {
            NewPipe.init(DownloaderImpl.getInstance())
        }
    }
}
