package com.bing.lan.rxbindclick;

import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.jakewharton.rxbinding.view.RxView;

import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;

import static android.R.attr.button;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "tag";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button button = (Button) findViewById(R.id.btn);
        final TextView label = (TextView) findViewById(R.id.tv);

        // RxView.clicks(button).subscribe(new Action1<Void>() {
        //     @Override
        //     public void call(Void aVoid) {
        //         button.setText("button被点击");
        //     }
        // });

        // http://nightfarmer.github.io/2016/08/31/RxJavaClick/


        Observable<Void> observable = RxView.clicks(button).share();
        observable
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        String msg = "#" + label.getText();
                        label.setText(msg);
                        Log.e(TAG, msg);
                    }
                })
                .buffer(observable.debounce(300, TimeUnit.MILLISECONDS))
                .map(new Func1<List<Void>, Integer>() {
                    @Override
                    public Integer call(List<Void> voids) {
                        int size = voids.size();
                        Log.e(TAG, size + "");
                        return size;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer integer) {
                        label.setText("");
                        String text = "" + integer + "连击";
                        button.setText(text);
                        Log.e(TAG, text);
                    }
                });
    }

    // Google实现算法
    // 看完上面实现，思路很简单，但是当要实现多次点击时，那上面的可能就代码量很大了，这里写下GoogleAPI提供的方法
    // Android监听连续点击次数代码实现
    long[] mHits = new long[2];
    // @Override
    public void onClick(View v) {
        //实现双击方法
        //src 拷贝的源数组
        //srcPos 从源数组的那个位置开始拷贝.
        //dst 目标数组
        //dstPos 从目标数组的那个位子开始写数据
        //length 拷贝的元素的个数
        System.arraycopy(mHits, 1, mHits, 0, mHits.length - 1);
        //实现左移，然后最后一个位置更新距离开机的时间，如果最后一个时间和最开始时间小于500，即双击
        mHits[mHits.length - 1] = SystemClock.uptimeMillis();
        if (mHits[0] >= (SystemClock.uptimeMillis() - 500)) {
            // 双击居中了。。。屏幕的一半和归属地的一半，更新窗口，保存lastX
            // params.x = wm.getDefaultDisplay().getWidth()/2-view.getWidth()/2;
            // wm.updateViewLayout(view, params);
            // Editor editor = sp.edit();
            // editor.putInt("lastx", params.x);
            // editor.commit();
        }
    }

    // 一般实现
    // 我们知道，一般实现双击事件，可以通过new OnClickListener()监听点击事件，然后记录前后两次点击距离开机的时间，由时间差在一定范围实现，代码如下：

    long firstClickTime = 0;
    public void onClick1(View view) {
        if(firstClickTime > 0){
            long secondClickTime = SystemClock.uptimeMillis();//距离上次开机时间
            long dtime = secondClickTime - firstClickTime;
            if(dtime > 500){
                Toast.makeText(getApplicationContext(), "实现双击事件监听", 0).show();
            } else{
                firstClickTime = 0;
            }
            return ;
        }
        firstClickTime = SystemClock.uptimeMillis();
    }


    // https://juejin.im/entry/57313cd6f38c840067d3f710

    public void test(Button button) {
        RxView.clicks(button)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Void>() {

                    @Override
                    public void call(Void aVoid) {

                    }
                });
    }

}
