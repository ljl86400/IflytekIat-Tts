package com.shawnlee.iflytekasr;

import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import com.iflytek.cloud.RecognizerResult;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Created by Shawn.lee on 2018/4/24.
 */

class Results {

    private HashMap<String, String> mIatResults ;        // 用HashMap存储听写结果

    public  Results(){
        this.mIatResults = new LinkedHashMap<>();    // 对存放结果HashMap字段进行实例化。
    }

    public HashMap<String,String> getmIatResults(){
        return mIatResults;
    }

    public void printTransResult (RecognizerResult results, EditText textEditorView) {
        String trans  = JsonParser.parseTransResult(results.getResultString(),"dst");
        String oris = JsonParser.parseTransResult(results.getResultString(),"src");
        if( TextUtils.isEmpty(trans)||TextUtils.isEmpty(oris) ){
            Log.e(MainActivity.class.getSimpleName(),"printTransResult: 解析结果失败，请确认是否已开通翻译功能。" );
        }else{
            textEditorView.setText( "原始语言:\n"+oris+"\n目标语言:\n"+trans );
        }
    }

    public void printResult(RecognizerResult results,EditText textEditorView) {
        String text = JsonParser.parseIatResult(results.getResultString());

        String sn = null;
        // 读取json结果中的sn字段
        try {
            JSONObject resultJson = new JSONObject(results.getResultString());
            sn = resultJson.optString("sn");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        this.getmIatResults().put(sn, text);

        StringBuffer resultBuffer = new StringBuffer();
        for (String key : this.getmIatResults().keySet()) {
            resultBuffer.append(this.getmIatResults().get(key));
        }

        textEditorView.setText(resultBuffer.toString());
        textEditorView.setSelection(textEditorView.length());
    }
}
