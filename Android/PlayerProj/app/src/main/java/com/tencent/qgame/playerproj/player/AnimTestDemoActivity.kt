package com.tencent.qgame.playerproj.player

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.tencent.qgame.animplayer.AnimConfig
import com.tencent.qgame.animplayer.AnimView
import com.tencent.qgame.animplayer.inter.IAnimListener
import com.tencent.qgame.animplayer.util.ALog
import com.tencent.qgame.animplayer.util.IALog
import com.tencent.qgame.animplayer.util.ScaleType
import com.tencent.qgame.playerproj.databinding.ActivityTestDemoBinding
import java.io.File

class AnimTestDemoActivity : Activity(), IAnimListener {

    companion object {
        private const val TAG = "AnimSimpleDemoActivity"
    }

    private val dir by lazy {
        // 存放在sdcard应用缓存文件中
        getExternalFilesDir(null)?.absolutePath ?: Environment.getExternalStorageDirectory().path
    }

    // 视频信息
    data class VideoInfo(val fileName: String, val md5: String)

    // ps：每次修改mp4文件，但文件名不变，记得先卸载app，因为assets同名文件不会进行替换
    private val videoInfo = VideoInfo("video_sample.mp4", "202cf864283aa48a79b4bf8fee526870")

    // 动画View
    private lateinit var animView: AnimView

    private val uiHandler by lazy {
        Handler(Looper.getMainLooper())
    }

    private lateinit var binding: ActivityTestDemoBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTestDemoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // 文件加载完成后会调用init方法
        loadFile()
    }

    private fun init() {
        // 初始化日志
        initLog()
        // 获取动画view
        animView = binding.player
        // 居中（根据父布局按比例居中并全部显示，默认fitXY）
        animView.setScaleType(ScaleType.CENTER_CROP)
        // 注册动画监听
        animView.setAnimListener(this)
        animView.setLoop(100)
        /**
         * 开始播放主流程
         * ps: 主要流程都是对AnimView的操作，其它比如队列，或改变窗口大小等操作都不是必须的
         */
        play(videoInfo)
    }


    private fun play(videoInfo: VideoInfo) {
        // 播放前强烈建议检查文件的md5是否有改变
        // 因为下载或文件存储过程中会出现文件损坏，导致无法播放
        Thread {
            val file = File(dir + "/" + videoInfo.fileName)
            val md5 = FileUtil.getFileMD5(file)
            if (videoInfo.md5 == md5) {
                // 开始播放动画文件
                animView.startPlay(file)
            } else {
                Log.e(TAG, "md5 is not match, error md5=$md5")
            }
        }.start()
    }


    /**
     * 视频信息准备好后的回调，用于检查视频准备好后是否继续播放
     * @return true 继续播放 false 停止播放
     */
    override fun onVideoConfigReady(config: AnimConfig): Boolean {
        return true
    }

    /**
     * 视频开始回调
     */
    override fun onVideoStart() {
        Log.i(TAG, "onVideoStart")
    }

    /**
     * 视频渲染每一帧时的回调
     * @param frameIndex 帧索引
     */
    override fun onVideoRender(frameIndex: Int, config: AnimConfig?) {
    }

    /**
     * 视频播放结束(失败也会回调onComplete)
     */
    override fun onVideoComplete() {
        Log.i(TAG, "onVideoComplete")
    }

    /**
     * 播放器被销毁情况下会调用onVideoDestroy
     */
    override fun onVideoDestroy() {
        Log.i(TAG, "onVideoDestroy")
    }

    /**
     * 失败回调
     * 一次播放时可能会调用多次，建议onFailed只做错误上报
     * @param errorType 错误类型
     * @param errorMsg 错误消息
     */
    override fun onFailed(errorType: Int, errorMsg: String?) {
        Log.i(TAG, "onFailed errorType=$errorType errorMsg=$errorMsg")
    }


    override fun onPause() {
        super.onPause()
        // 页面切换是停止播放
        animView.stopPlay()
    }


    private fun initLog() {
        ALog.isDebug = true
        ALog.log = object : IALog {
            override fun i(tag: String, msg: String) {
                Log.i(tag, msg)
            }

            override fun d(tag: String, msg: String) {
                Log.d(tag, msg)
            }

            override fun e(tag: String, msg: String) {
                Log.e(tag, msg)
            }

            override fun e(tag: String, msg: String, tr: Throwable) {
                Log.e(tag, msg, tr)
            }
        }
    }


    private fun loadFile() {
        val files = Array(1) {
            videoInfo.fileName
        }
        FileUtil.copyAssetsToStorage(this, dir, files) {
            uiHandler.post {
                init()
            }
        }
    }


    private fun dp2px(context: Context, dp: Float): Float {
        val scale = context.resources.displayMetrics.density
        return dp * scale + 0.5f
    }
}

