package com.shawnlee.iflytekasr;

import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.util.UUID;

/**
 * Created by TonnyZ on 2018/4/23.
 */

class PcmVoiceRecorder {
    private static final String PcmAudioPATH = "/sdcard/MyVoiceForder/Record/";     // 录音存储路径
    private static final String LOG_TAG = "PcmAudioRecordTest";        // log标记
    private String mPcmFileName;
    private ExtAudioRecorder recorder;      // 获取录制PCM语音文件的实例

    public String getFileName(){
        return mPcmFileName;
    }

    public String getPcmAudioPATH(){
        return PcmAudioPATH;
    }

    public String getLogTag(){
        return LOG_TAG;
    }

    public ExtAudioRecorder getRecorder(){
        return recorder;
    }

    // 尝试开始PCM格式录制音频
    public void startVoice() {
        // 实现录音的代码
        mPcmFileName = getPcmAudioPATH() + UUID.randomUUID().toString() + ".wav";       // 设置录音保存路径
        String state = android.os.Environment.getExternalStorageState();        // 获取外部存储器的状态

        if (!state.equals(android.os.Environment.MEDIA_MOUNTED)) {
            Log.i(getLogTag(), "SD Card is not mounted,It is  " + state + ".");
        }       // 如果外部存储器没有加载，在log中打印一个错误提示

        File directory = new File(mPcmFileName).getParentFile();       // 获取语音文件的存储路径

        if (!directory.exists() && !directory.mkdirs()) {
            Log.i(getLogTag(), "Path to file could not be created");
        }       // 如果文件路径不存在且无法被创建，在log中打印一个错误提示


        AuditRecorderConfiguration configuration = new AuditRecorderConfiguration.Builder()
                .recorderListener(listener)
                .uncompressed(true)
                .builder();
        recorder = new ExtAudioRecorder(configuration);
        // 设置输出文件
        recorder.setOutputFile(mPcmFileName);
        recorder.prepare();
        recorder.start();
    }

    // 尝试停止录制PCM格式声音
    public void stopVoice() {
        int time = recorder.stop();
        if (time > 0) {
            //成功的处理
            recorder.reset();
        } else {
            String st2 = new String("The_recording_time_is_too_short");
            // showTip(st2);
        }

    }

    /**
     * 录音失败的提示
     */
    ExtAudioRecorder.RecorderListener listener = new ExtAudioRecorder.RecorderListener() {
        @Override
        public void recordFailed(FailRecorder failRecorder) {
            if (failRecorder.getType() == FailRecorder.FailType.NO_PERMISSION) {
                // Toast.makeText(MainActivity.this, "录音失败，可能是没有给权限", Toast.LENGTH_SHORT).show();
            } else {
                // Toast.makeText(MainActivity.this, "发生了未知错误", Toast.LENGTH_SHORT).show();
            }
        }
    };
}
