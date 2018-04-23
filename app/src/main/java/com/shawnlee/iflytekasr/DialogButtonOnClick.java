package com.shawnlee.iflytekasr;

import android.content.DialogInterface;
import android.net.Uri;
import android.util.Log;
import static android.content.ContentValues.TAG;

/**
 * Created by Shawn.Lee on 2018/4/23.
 */

class DialogButtonOnClick implements DialogInterface.OnClickListener {
    public int index; // 表示选项的索引
    public Uri uri ;
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
