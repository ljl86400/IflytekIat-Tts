package com.shawnlee.iflytekasr;

import android.content.DialogInterface;
import android.net.Uri;

import java.net.URI;


/**
 * Created by Shawn.Lee on 2018/4/23.
 * 定义一个监听器类，用来监听提示对话框的点击动作
 */

class DialogButtonOnClick implements DialogInterface.OnClickListener {
    public int index; // 表示选项的索引
    public Uri uri ;

    public void setPath(String path) {
        this.path = path;
    }

    public String path ;
    private Integer position;

    public DialogButtonOnClick(int index)
    {
        this.index = index;
    }

    public void setUri(Uri uri){
        this.uri = uri;
    }

    public void setPosition(Integer position){
        this.position = position;
    }

    @Override
    public void onClick(DialogInterface dialog, int which)
    {
        /*switch (which){
            case 0:
            case 1:
            case 2:
                index = which;
                Log.d(TAG, "onClick:which " + which);
                Log.d(TAG, "onClick:index " + index);
                break;
            case DialogInterface.BUTTON_POSITIVE:
                switch (index){
                    case 0:
                        playSound(uri);
                        break;
                    case 1:
                        recognizeStream(position);
                        // showTip("识别功能待添加");
                        break;
                    case 2:
                        showTip("删除功能待添加");
                        break;
                    default:
                        break;
                }
                break;
            case DialogInterface.BUTTON_NEGATIVE:
                showTip("取消并退出");
                break;
            default:
                break;
        }*/
    }
}
