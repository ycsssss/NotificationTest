package com.ycs.servicetest;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.CollapsibleActionView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.dinuscxj.progressbar.CircleProgressBar;

import de.hdodenhof.circleimageview.CircleImageView;

public class DownLoadWindowService extends Service {
    private static RelativeLayout view;
    private RelativeLayout view2;
    private static ImageView civ;
    private TextView y;
    private int Y=0;
    private Handler handler=new Handler();
    private Boolean flag=true;
    public static CircleProgressBar ircleProgressBar;
    private static WindowManager windowManager;
    private static WindowManager.LayoutParams layoutParams;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("yyy", "1112" );
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            showFloatingWindow();
        }
        return super.onStartCommand(intent, flags, startId);
    }
    private void showFloatingWindow() {
        if (Settings.canDrawOverlays(this)) {
            // 获取WindowManager服务
            windowManager = (WindowManager) getApplicationContext().getSystemService(WINDOW_SERVICE);
            // 新建悬浮窗控件
            LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
            view = (RelativeLayout) inflater.inflate(R.layout.float_download, null);
            civ=view.findViewById(R.id.civ);
            ircleProgressBar=view.findViewById(R.id.progress);
            ircleProgressBar.setProgress(80);
            ircleProgressBar.setVisibility(View.GONE);
            view.setOnTouchListener(new FloatingOnTouchListener());
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendBroadcast(new Intent(DownLoadWindowService.this,DialogReceiver.class));
                }
            });
            // 设置LayoutParam
            layoutParams = new WindowManager.LayoutParams();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            } else {
                layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
            }
            layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            layoutParams.format = PixelFormat.RGBA_8888;
//            layoutParams.width = 300;
//            layoutParams.height = 200;
            //layoutParams.gravity = Gravity.RIGHT;
            layoutParams.x = windowManager.getDefaultDisplay().getWidth();
            layoutParams.y = -300;
            layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            windowManager.addView(view, layoutParams);
            setViewFade();
        }
    }
    private class FloatingOnTouchListener implements View.OnTouchListener {
        private int x;
        private int y;
        private int yy;
        @Override
        public boolean onTouch(View view, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    x = (int) event.getRawX();
                    yy= (int) event.getRawY();
                    y = (int) event.getRawY();
                    view.setAlpha(1);
                    handler.removeCallbacks(runnable);
                    break;
                case MotionEvent.ACTION_MOVE:
                    int nowX =(int) event.getRawX();
                    int nowY = (int) event.getRawY();
                    int movedX = nowX - windowManager.getDefaultDisplay().getWidth()/2;
                    int movedY = nowY - y;
                    x = nowX;
                    y = nowY;
                    layoutParams.x = movedX;
                    layoutParams.y = layoutParams.y + movedY;

                    // 更新悬浮窗控件布局
                    windowManager.updateViewLayout(view, layoutParams);
                    break;
                case MotionEvent.ACTION_UP:
                    int Y = (int) event.getRawY();
                    int mY = Y - y;
                    y = Y;
                    int X = (int) event.getRawX();
                    int mX = X - x;
                    x = X;
                    if(layoutParams.x + mX>0){
                        layoutParams.x = windowManager.getDefaultDisplay().getWidth();
                    }else{
                        layoutParams.x=-windowManager.getDefaultDisplay().getWidth();
                    }

                    layoutParams.y = layoutParams.y + mY;
                    setViewFade();
                    // 更新悬浮窗控件布局
                    windowManager.updateViewLayout(view, layoutParams);
                    break;

                default:
                    break;
            }
//            if(yy!=layoutParams.y){
//                //Toast.makeText(DownLoadWindowService.this, "变", Toast.LENGTH_SHORT).show();
//                return true;
//            }else {
                //Toast.makeText(DownLoadWindowService.this, "没变", Toast.LENGTH_SHORT).show();
                return false;
            //}

        }
    }
    private void setViewFade(){
        handler.removeCallbacks(runnable);
        handler.postDelayed(runnable,2000);
    }
    private Runnable runnable=new Runnable() {
        @Override
        public void run() {
            fade(view);
        }
    };
    public static void updateProgress(int progress){
        ircleProgressBar.setVisibility(View.VISIBLE);
        civ.setVisibility(View.GONE);
        ircleProgressBar.setProgress(progress);
        windowManager.updateViewLayout(view,layoutParams);
    }
    public static void recover(){
        ircleProgressBar.setVisibility(View.GONE);
        civ.setVisibility(View.VISIBLE);
    }
    private void fade(View view){
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "alpha",1,0.3f);
        animator.setDuration(1000);
        animator.start();
    }


}
