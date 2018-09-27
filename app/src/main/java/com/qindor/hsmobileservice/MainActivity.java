package com.qindor.hsmobileservice;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;

import com.qindor.hsmobileservice.Activity.LoginActivity;

public class MainActivity extends AppCompatActivity {
    private ImageView imageView;
    private TextView store,version;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        store = findViewById(R.id.logding_store);
        version = findViewById(R.id.logding_version);
        version.setText("版本号："+getLocalVersion(MainActivity.this));
        SharedPreferences sharedPreferences=getSharedPreferences("config",0);
        store.setText("分店号："+sharedPreferences.getString("store",""));
        imageView=(ImageView)findViewById(R.id.login_image);
        imageView.setImageResource(R.drawable.qmxlogo);
        AlphaAnimation animation = new AlphaAnimation(0, 1);
        animation.setDuration(5000);
        imageView.startAnimation(animation);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
               /* AlphaAnimation alphaAnimation = new AlphaAnimation(1, 0);
                alphaAnimation.setDuration(1500);
                imageView.startAnimation(animation);
                alphaAnimation.setFillAfter(true);
                alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });*/
                Intent intent=new Intent(MainActivity.this,LoginActivity.class);
                startActivity(intent);
                MainActivity.this.finish();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
    }
// * 获取本地软件版本号
//	 */
    public static int getLocalVersion(Context ctx) {
        int localVersion = 0;
        try {
            PackageInfo packageInfo = ctx.getApplicationContext()
                    .getPackageManager()
                    .getPackageInfo(ctx.getPackageName(), 0);
            localVersion = packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return localVersion;
    }

    /**
     * 获取本地软件版本号名称
     */
    public static String getLocalVersionName(Context ctx) {
        String localVersion = "";
        try {
            PackageInfo packageInfo = ctx.getApplicationContext()
                    .getPackageManager()
                    .getPackageInfo(ctx.getPackageName(), 0);
            localVersion = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return localVersion;
    }
}

