package com.shawnlee.iflytekasr;

import android.content.Context;
import android.media.MediaRecorder;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * Created by TonnyZ on 2018/4/23.
 */

class VoiceRecorder {
    private static final String PATH = "/sdcard/MyVoiceForder/Record/";     // 录音存储路径
    private static final String LOG_TAG = "AudioRecordTest";        // log标记
    private MediaRecorder mRecorder = null;     // 用于完成录音
    private String mFileName;

     VoiceRecorder(){

    }

    static public String getPath(){
        return  "/sdcard/MyVoiceForder/Record/";     // 获取音频文件的存储路径
    }

    static public String getLogTag(){
        return "AudioRecordTest";           // 获取录音机实例的LOG_TAG
    }

    private void startVoice() {
        String mFileName = PATH + UUID.randomUUID().toString() + ".amr";       // 设置录音保存路径和文件名称
        String state = android.os.Environment.getExternalStorageState();        // 获取外部存储器的状态

        if (!state.equals(android.os.Environment.MEDIA_MOUNTED)) {
            Log.i(LOG_TAG, "SD Card is not mounted,It is  " + state + ".");
        }       // 如果外部存储器没有加载，在log中打印一个错误提示

        File directory = new File(mFileName).getParentFile();       // 以mFileName为文件名&路径新建一个文件，并获取父文件

        if (!directory.exists() && !directory.mkdirs()) {
            Log.i(LOG_TAG, "Path to file could not be created");
        }       // 如果文件路径不存在且无法被创建，在log中打印一个错误提示

        mRecorder = new MediaRecorder();        // 实例一个录音机对象
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);        // 设置录音机对象的音源，这里设置的是从MIC获取声音
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);      // 设置输出文档的格式，Default应该是arm格式
        mRecorder.setOutputFile(mFileName);             // 设置输出文档的文件名
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);      // 设置录音机录音的编码格式，Default应该是arm格式
        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }       // 录音机初始化，并判断是否有异常，捕捉异常

        mRecorder.start();      // 开始录音
    }

    private void stopVoice() {
        mRecorder.stop();       // 停止录音
        mRecorder.release();        // 释放录音机对象
        mRecorder = null;       // 清空录音机设置&内容
    }
}
