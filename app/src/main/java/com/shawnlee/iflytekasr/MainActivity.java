package com.shawnlee.iflytekasr;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;
import com.iflytek.sunflower.FlowerCollector;
import com.iflytek.cloud.SpeechUtility ;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

public class MainActivity extends Activity implements View.OnClickListener,View.OnLongClickListener{

    private HashMap<String, String> mIatResults = new LinkedHashMap<>();        // 用HashMap存储听写结果
    private EditText mResultTextEditorView;
    public SharedPreferences mSharedPreferences;
    public SpeechRecognizer mIatSpeechRecognizer;
    public RecognizerDialog mIatDialog;
    private MyListAdapter mAdapter;     // 语音列表适配器
    private static final String PATH = "/sdcard/MyVoiceForder/Record/";     // 录音存储路径
    private String mFileName = null;        // 语音文件保存路径
    private static final String LOG_TAG = "AudioRecordTest";        // log标记
    private MediaRecorder mRecorder = null;     // 用于完成录音
    private ListView mVoicesFilesListView;      // 用于显示语音列表的对象
    private List<String> mVoicesFilesList;      // 存放语音文件信息的语音列表对象；类似于目录性质的东西
    private Toast mToast;
    private boolean mTranslateEnable = false;
    public String TAG = MainActivity.class.getSimpleName();
    private String mEngineType = SpeechConstant.TYPE_CLOUD;     // 引擎类型
    private List<String> permissionList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);             // 继承基类onCreate方法
        requestWindowFeature(Window.FEATURE_NO_TITLE);  // 去掉窗口的title
        setContentView(R.layout.activity_main);         // 设置主界面
        initSpeech() ;
        requestPermission();

        mVoicesFilesListView = findViewById(R.id.voidList);    // 设置显示语音列表内容的界面
        mVoicesFilesList = new ArrayList<>();                // 将存放语音文件信息的列表实例化

        initButton();                                   // 将界面中的按钮集中初始化，降低代码阅读的难度

        /** 创建一个语音识别器，将已经初始化的语音监听器传给语音识别器的创建方法，
         *  这个识别器应该是在云端，将本地的数据进行打包处理并传到云上去应该是监听器干的事情
         */
        mIatSpeechRecognizer = SpeechRecognizer.createRecognizer(MainActivity.this, initSpeechListener);

        /** 将已经初始化的语音监听器当做构造参数，创建一个语音识别对话,这个对话应该是云上的服务，
         *  意思是在云端建立了这个识别对话，跟我们平时理解的本地识别对话好像还不太一样
         */
        mIatDialog = new RecognizerDialog(MainActivity.this, initSpeechListener);

        mSharedPreferences = getSharedPreferences(IatSettings.PREFER_NAME,
                Activity.MODE_PRIVATE);     // 使用SharedPreferences的方式对语音听写的设置进行存储
        mResultTextEditorView = findViewById(R.id.iat_text_edit_view);        // 设置文本消息编辑框的界面
        mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);

    }

    /**
     *  对界面中的按键进行初始化
     */
    private void initButton(){
        // 设置一个开始语音识别的按钮，并给按钮加上一个按键监听器
        Button startIatButton = findViewById(R.id.iat_button);
        Button startTtsButton = findViewById(R.id.tts_button);
        Button longTimeRecordAudioButton = findViewById(R.id.record_audio_button);
        Button startAudioStreamRecognizeButton = findViewById(R.id.iat_recognize_stream_button);
        startIatButton.setOnClickListener(this);
        startTtsButton.setOnClickListener(this);
        startAudioStreamRecognizeButton.setOnClickListener(this);

        longTimeRecordAudioButton.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startVoice();
                        break;
                    case MotionEvent.ACTION_UP:
                        stopVoice();
                        break;
                    default:
                        break;
                }
                return false;
            }
        });
    }

    /**
     *  设置APPID
     */
    private void initSpeech() {
        SpeechUtility. createUtility( this, SpeechConstant. APPID + "=5ac18d6c" );
    }

    /**
     * App所需权限进行申请
     */
    private void requestPermission(){
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED){
            permissionList.add( Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED){
            permissionList.add( Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED){
            permissionList.add( Manifest.permission.INTERNET);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED){
            permissionList.add( Manifest.permission.RECORD_AUDIO);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_NETWORK_STATE)
                != PackageManager.PERMISSION_GRANTED){
            permissionList.add( Manifest.permission.ACCESS_NETWORK_STATE);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_WIFI_STATE)
                != PackageManager.PERMISSION_GRANTED){
            permissionList.add( Manifest.permission.ACCESS_WIFI_STATE);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CHANGE_NETWORK_STATE)
                != PackageManager.PERMISSION_GRANTED){
            permissionList.add( Manifest.permission.CHANGE_NETWORK_STATE);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED){
            permissionList.add( Manifest.permission.READ_CONTACTS);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS)
                != PackageManager.PERMISSION_GRANTED){
            permissionList.add( Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_SETTINGS)
                != PackageManager.PERMISSION_GRANTED){
            permissionList.add( Manifest.permission.WRITE_SETTINGS);
        }
        if (!permissionList.isEmpty()){
            String[] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(MainActivity.this,permissions,1);
        }
        return;
    }

    /**
     * 初始化一个语音监听器。
     */
    private InitListener initSpeechListener = new InitListener() {
        @Override
        public void onInit(int code) {
            Log.d(TAG, "SpeechRecognizer init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                showTip("初始化失败，错误码：" + code);
            }
        }
    };

    /**
     * 对界面中的按键动作响应进行设置
     * @param view
     */
    int ret = 0; // 函数调用返回值
    @Override
    public void onClick(View view) {
        // 首先判断是否创建语音听写识别器成功，如果不成功是不能展开响应的语音功能的
        if( null == mIatSpeechRecognizer ){
            // 创建单例失败，与 21001 错误为同样原因，参考 http://bbs.xfyun.cn/forum.php?mod=viewthread&tid=9688
            // libmsc.so文件如果放在了libs文件夹里需要在gradle文件中声明，如果放在了jniLibs文件下就不要声明
            this.showTip( "创建对象失败，请确认 libmsc.so 放置正确，且有调用 createUtility 进行初始化" );
            return;
        }

        switch (view.getId()){
            // 响应“听写”按键，调用iatEventActive()方法，完成语音听写功能
            case R.id.iat_button:
                iatEventActive();
                break;
            // 响应“语音合成”按键，调用ttsEventActive()方法，完成语音合成功能
            case R.id.tts_button:
                ttsEventActive();
                break;
            case R.id.iat_recognize_stream_button:
                audioStreamRecognizeEventActive();
                this.showTip( "音频流识别按键已经响应" );
                break;
            default:
                break;
        }
    }

    // 对长时间的按键进行响应
    @Override
    public boolean onLongClick(View view) {
        switch (view.getId()){
            case R.id.record_audio_button:
                this.showTip( "录音键检测到了长按键动作" );
                break;
            default:
                break;
        }
        return false;
    }

    /** 开始录音 */
    private void startVoice() {
        mFileName = PATH + UUID.randomUUID().toString() + ".amr";       // 设置录音保存路径
        String state = android.os.Environment.getExternalStorageState();
        if (!state.equals(android.os.Environment.MEDIA_MOUNTED)) {
            Log.i(LOG_TAG, "SD Card is not mounted,It is  " + state + ".");
        }
        File directory = new File(mFileName).getParentFile();
        if (!directory.exists() && !directory.mkdirs()) {
            Log.i(LOG_TAG, "Path to file could not be created");
        }
        Toast.makeText(getApplicationContext(), "开始录音", Toast.LENGTH_SHORT).show();
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
        mRecorder.setOutputFile(mFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
        mRecorder.start();
    }

    /** 停止录音 */
    private void stopVoice() {
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
        mVoicesFilesList.add(mFileName);
        mAdapter = new MyListAdapter(MainActivity.this);
        mVoicesFilesListView.setAdapter(mAdapter);
        Toast.makeText(getApplicationContext(), "保存录音" + mFileName, Toast.LENGTH_SHORT).show();
    }

    private void showTip(final String str) {
        mToast.setText(str);
        mToast.show();
    }

    /**
     *对“开始”按键进行响应，完成语音听写的功能
     * 1】先将存放识别结果的对象清空
     * 2】设置参数（设置听写引擎（此处设置的是云识别）、设置返回结果格式（jason）、语言种类、
     * 	  语言区域、语音前端点、语音后端点、返回结果是否带有标点符号、语音保存路径、
     *    语音保存格式（wav、pcm）等）
     * 3】判断是否有名为 R.string.pref_key_iat_show 的首选项，如果有返回true，没有返回false，
     *	  默认返回true
     * 4】a：如果首选项返回为true，给语音听写对话框设置监听器，显示这个听写对话框，并提示用
     *    户开始讲话；b：如果首选项返回值为false，进行进一步判断能否启动监听器，如果不能启动
     *    监听器则提示错误，如果能启动监听器则提示客户开始讲话并开始监听用户讲话，
     * 如何判断一次听写结束：OnResult isLast=true 或者 onError
     */
    private void iatEventActive(){
        FlowerCollector.onEvent(MainActivity.this, "iat_recognize");
        mResultTextEditorView.setText(null);// 清空显示内容
        mIatResults.clear();
        // 设置参数
        setParam();
        // getString方法返回字符串iat_show,defValue是初始默认值，如果没有需要判断的对象就
        // 返回这个true值
        boolean isShowDialog = mSharedPreferences.getBoolean(
                getString(R.string.pref_key_iat_show), true);
        if (isShowDialog) {
            // 显示听写对话框
            mIatDialog.setListener(mRecognizerDialogListener);
            mIatDialog.show();
            showTip(getString(R.string.text_begin));
        } else {
            // 不显示听写对话框
            ret = mIatSpeechRecognizer.startListening(mRecognizerListener);
            if (ret != ErrorCode.SUCCESS) {
                showTip("听写失败,错误码：" + ret);
            } else {
                showTip(getString(R.string.text_begin));
            }
        }
        Toast.makeText(MainActivity.this,"听写开始",Toast.LENGTH_SHORT).show();
    }

    private void ttsEventActive(){
        if (mResultTextEditorView.getText().toString().equals("")){
            Toast.makeText(MainActivity.this,"没有需要合成的文本",Toast.LENGTH_SHORT).show();
            return;
        }
        // 实现tts的代码
        //1. 创建 SpeechSynthesizer 对象 , 第二个参数： 本地合成时传 InitListener
        SpeechSynthesizer mTts = SpeechSynthesizer.createSynthesizer( this, null);
        //2.合成参数设置，详见《 MSC Reference Manual》 SpeechSynthesizer 类
        //设置发音人（更多在线发音人，用户可参见 附录 13.2
        mTts.setParameter(SpeechConstant. VOICE_NAME, "vixyun" ); // 设置发音人
        mTts.setParameter(SpeechConstant. SPEED, "50" );// 设置语速
        mTts.setParameter(SpeechConstant. VOLUME, "80" );// 设置音量，范围 0~100
        mTts.setParameter(SpeechConstant. ENGINE_TYPE, SpeechConstant. TYPE_CLOUD); //设置云端
        //设置合成音频保存位置（可自定义保存位置），保存在 “./sdcard/iflytek.pcm”
        //保存在 SD 卡需要在 AndroidManifest.xml 添加写 SD 卡权限
        //仅支持保存为 pcm 和 wav 格式， 如果不需要保存合成音频，注释该行代码
        mTts.setParameter(SpeechConstant. TTS_AUDIO_PATH, "./sdcard/iflytek.pcm" );
        //3.开始合成
        mTts.startSpeaking( mResultTextEditorView.getText().toString(), new MySynthesizerListener()) ;
        Toast.makeText(MainActivity.this,"语音合成开始",Toast.LENGTH_SHORT).show();
    }

    private void audioStreamRecognizeEventActive(){
        mResultTextEditorView.setText(null);// 清空显示内容
        mIatResults.clear();
        // 设置参数
        setParam();
        // 设置音频来源为外部文件
        mIatSpeechRecognizer.setParameter(SpeechConstant.AUDIO_SOURCE, "-1");
        // 也可以像以下这样直接设置音频文件路径识别（要求设置文件在sdcard上的全路径）：
        // mIat.setParameter(SpeechConstant.AUDIO_SOURCE, "-2");
        // mIat.setParameter(SpeechConstant.ASR_SOURCE_PATH, "sdcard/XXX/XXX.pcm");
        ret = mIatSpeechRecognizer.startListening(mRecognizerListener);
        if (ret != ErrorCode.SUCCESS) {
            showTip("识别失败,错误码：" + ret);
        } else {
            byte[] audioData = FucUtil.readAudioFile(MainActivity.this, "iattest.wav");

            if (null != audioData) {
                showTip(getString(R.string.text_begin_recognizer));
                // 一次（也可以分多次）写入音频文件数据，数据格式必须是采样率为8KHz或16KHz（本地识别只支持16K采样率，云端都支持），
                // 位长16bit，单声道的wav或者pcm
                // 写入8KHz采样的音频时，必须先调用setParameter(SpeechConstant.SAMPLE_RATE, "8000")设置正确的采样率
                // 注：当音频过长，静音部分时长超过VAD_EOS将导致静音后面部分不能识别。
                // 音频切分方法：FucUtil.splitBuffer(byte[] buffer,int length,int spsize);
                mIatSpeechRecognizer.writeAudio(audioData, 0, audioData.length);
                mIatSpeechRecognizer.stopListening();
            } else {
                mIatSpeechRecognizer.cancel();
                showTip("读取音频流失败");
            }
        }
    }

    class MySynthesizerListener implements SynthesizerListener {

        @Override
        public void onSpeakBegin() {
            showTip(" 开始播放 ");
        }

        @Override
        public void onSpeakPaused() {
            showTip(" 暂停播放 ");
        }

        @Override
        public void onSpeakResumed() {
            showTip(" 继续播放 ");
        }

        @Override
        public void onBufferProgress(int percent, int beginPos, int endPos ,
                                     String info) {
            // 合成进度
        }

        @Override
        public void onSpeakProgress(int percent, int beginPos, int endPos) {
            // 播放进度
        }

        @Override
        public void onCompleted(SpeechError error) {
            if (error == null) {
                showTip("播放完成 ");
            } else if (error != null ) {
                showTip(error.getPlainDescription( true));
            }
        }

        @Override
        public void onEvent(int eventType, int arg1 , int arg2, Bundle obj) {
            // 以下代码用于获取与云端的会话 id，当业务出错时将会话 id提供给技术支持人员，可用于查询会话日志，定位出错原因
            // 若使用本地能力，会话 id为null
            //if (SpeechEvent.EVENT_SESSION_ID == eventType) {
            //     String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
            //     Log.d(TAG, "session id =" + sid);
            //}
        }
    }

    /**
     * 参数设置
     *
     * @return
     */
    public void setParam() {
        // 清空参数
        mIatSpeechRecognizer.setParameter(SpeechConstant.PARAMS, null);

        // 设置听写引擎
        mIatSpeechRecognizer.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
        // 设置返回结果格式
        mIatSpeechRecognizer.setParameter(SpeechConstant.RESULT_TYPE, "json");

        // 将false改成true后，出现脚本运行错误的提示
        this.mTranslateEnable = mSharedPreferences.getBoolean( this.getString(R.string.pref_key_translate), false );
        if( mTranslateEnable ){
            Log.i( TAG, "translate enable" );
            mIatSpeechRecognizer.setParameter( SpeechConstant.ASR_SCH, "1" );
            mIatSpeechRecognizer.setParameter( SpeechConstant.ADD_CAP, "translate" );
            mIatSpeechRecognizer.setParameter( SpeechConstant.TRS_SRC, "its" );
        }

        String lag = mSharedPreferences.getString("iat_language_preference",
                "mandarin");
        if (lag.equals("en_us")) {
            // 设置语言
            mIatSpeechRecognizer.setParameter(SpeechConstant.LANGUAGE, "en_us");
            mIatSpeechRecognizer.setParameter(SpeechConstant.ACCENT, null);

            if( mTranslateEnable ){
                mIatSpeechRecognizer.setParameter( SpeechConstant.ORI_LANG, "en" );
                mIatSpeechRecognizer.setParameter( SpeechConstant.TRANS_LANG, "cn" );
            }
        } else {
            // 设置语言
            mIatSpeechRecognizer.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
            // 设置语言区域
            mIatSpeechRecognizer.setParameter(SpeechConstant.ACCENT, lag);

            if( mTranslateEnable ){
                mIatSpeechRecognizer.setParameter( SpeechConstant.ORI_LANG, "cn" );
                mIatSpeechRecognizer.setParameter( SpeechConstant.TRANS_LANG, "en" );
            }
        }
        //此处用于设置dialog中不显示错误码信息
        //mIat.setParameter("view_tips_plain","false");

        // 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
        mIatSpeechRecognizer.setParameter(SpeechConstant.VAD_BOS, mSharedPreferences.getString("iat_vadbos_preference", "4000"));

        // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
        mIatSpeechRecognizer.setParameter(SpeechConstant.VAD_EOS, mSharedPreferences.getString("iat_vadeos_preference", "1000"));

        // 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
        mIatSpeechRecognizer.setParameter(SpeechConstant.ASR_PTT, mSharedPreferences.getString("iat_punc_preference", "1"));

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        // 注：AUDIO_FORMAT参数语记需要更新版本才能生效
        mIatSpeechRecognizer.setParameter(SpeechConstant.AUDIO_FORMAT,"wav");
        mIatSpeechRecognizer.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory()+"/msc/iat.wav");
    }

    /**
     * 听写UI监听器
     */
    private RecognizerDialogListener mRecognizerDialogListener = new RecognizerDialogListener() {
        public void onResult(RecognizerResult results, boolean isLast) {
            if( mTranslateEnable ){
                printTransResult( results );
            }else{
                printResult(results);
            }
        }

        /**
         * 识别回调错误.
         */
        public void onError(SpeechError error) {
            if(mTranslateEnable && error.getErrorCode() == 14002) {
                showTip( error.getPlainDescription(true)+"\n请确认是否已开通翻译功能" );
            } else {
                showTip(error.getPlainDescription(true));
            }
        }

    };

    /**
     * 听写监听器。
     */
    private RecognizerListener mRecognizerListener = new RecognizerListener() {

        @Override
        public void onBeginOfSpeech() {
            // 此回调表示：sdk内部录音机已经准备好了，用户可以开始语音输入
            showTip("开始说话");
        }

        @Override
        public void onError(SpeechError error) {
            // Tips：
            // 错误码：10118(您没有说话)，可能是录音机权限被禁，需要提示用户打开应用的录音权限。
            if(mTranslateEnable && error.getErrorCode() == 14002) {
                showTip( error.getPlainDescription(true)+"\n请确认是否已开通翻译功能" );
            } else {
                showTip(error.getPlainDescription(true));
            }
        }

        @Override
        public void onEndOfSpeech() {
            // 此回调表示：检测到了语音的尾端点，已经进入识别过程，不再接受语音输入
            showTip("结束说话");
        }

        @Override
        public void onResult(RecognizerResult results, boolean isLast) {
            Log.d(TAG, results.getResultString());
            if( mTranslateEnable ){
                // 在 mIatResult 的textView中显示翻译后的语音识别结果
                printTransResult( results );
            }else{
                // 在 mIatResult 的textView中显示语音识别结果
                printResult(results);
            }

            if (isLast) {
                // TODO 最后的结果
            }
        }

        @Override
        public void onVolumeChanged(int volume, byte[] data) {
            showTip("当前正在说话，音量大小：" + volume);
            Log.d(TAG, "返回音频数据："+data.length);
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
            // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
            // 若使用本地能力，会话id为null
            //	if (SpeechEvent.EVENT_SESSION_ID == eventType) {
            //		String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
            //		Log.d(TAG, "session id =" + sid);
            //	}
        }
    };

    private void printTransResult (RecognizerResult results) {
        String trans  = JsonParser.parseTransResult(results.getResultString(),"dst");
        String oris = JsonParser.parseTransResult(results.getResultString(),"src");
        if( TextUtils.isEmpty(trans)||TextUtils.isEmpty(oris) ){
            showTip( "解析结果失败，请确认是否已开通翻译功能。" );
        }else{
            mResultTextEditorView.setText( "原始语言:\n"+oris+"\n目标语言:\n"+trans );
        }

    }

    private void printResult(RecognizerResult results) {
        String text = JsonParser.parseIatResult(results.getResultString());

        String sn = null;
        // 读取json结果中的sn字段
        try {
            JSONObject resultJson = new JSONObject(results.getResultString());
            sn = resultJson.optString("sn");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mIatResults.put(sn, text);

        StringBuffer resultBuffer = new StringBuffer();
        for (String key : mIatResults.keySet()) {
            resultBuffer.append(mIatResults.get(key));
        }

        mResultTextEditorView.setText(resultBuffer.toString());
        mResultTextEditorView.setSelection(mResultTextEditorView.length());
    }

    /**
     * 语音列表适配器
     */
    private class MyListAdapter extends BaseAdapter {
        LayoutInflater mInflater;

        public MyListAdapter(Context context) {
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return mVoicesFilesList.size();
        }

        @Override
        public Object getItem(int position) {

            return position;
        }

        @Override
        public long getItemId(int position) {

            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = mInflater.inflate(R.layout.item_voicelist, null);
            TextView tv = convertView.findViewById(R.id.tv_armName);
            tv.setText(mVoicesFilesList.get(position));
            return convertView;
        }
    }
}