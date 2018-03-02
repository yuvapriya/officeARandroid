package com.example.yumanoha.officeARandroid;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import com.facebook.react.ReactInstanceManager;
import com.facebook.react.ReactInstanceManagerBuilder;
import com.facebook.react.ReactRootView;
import com.facebook.react.bridge.JSBundleLoader;
import com.facebook.react.bridge.JavaScriptExecutor;
import com.facebook.react.common.LifecycleState;
import com.facebook.react.modules.core.DefaultHardwareBackBtnHandler;
import com.facebook.react.shell.MainReactPackage;
import com.facebook.react.devsupport.*;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by yumanoha on 2/23/2018.
 */

public class ReactActivity extends Activity implements DefaultHardwareBackBtnHandler {
//    private ReactRootView mReactRootView;
//    private ReactInstanceManager mReactInstanceManager;

    private static final String TAG = "ReactActivity";
    public static final String JS_BUNDLE_REMOTE_URL = "https://raw.githubusercontent.com/fengjundev/React-Native-Remote-Update/master/remote/index.android.bundle";
//    public static final String JS_BUNDLE_LOCAL_FILE = "indexrn.android.bundle";
    public static final String JS_BUNDLE_LOCAL_FILE = "billboard.android.bundle.js";
    public static final String JS_BUNDLE_LOCAL_PATH = Environment.getExternalStorageDirectory().toString() + File.separator + JS_BUNDLE_LOCAL_FILE;
    private ReactInstanceManager mReactInstanceManager;
    private ReactRootView mReactRootView;
    private CompleteReceiver mDownloadCompleteReceiver;
    private long mDownloadId;

//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        mReactRootView = new ReactRootView(this);
//        mReactInstanceManager = ReactInstanceManager.builder()
//                .setApplication(getApplication())
////                .setJSBundleFile("https://raw.githubusercontent.com/hubcarl/smart-react-native-app/debug/app/src/main/assets/index.android.bundle")
//         //       .setBundleAssetName("helloworld.android.bundle")
//                .setBundleAssetName("ar.bundle")
////                .setJSMainModulePath("sample")
//                .addPackage(new MainReactPackage())
//                .setUseDeveloperSupport(BuildConfig.DEBUG)
//                .setInitialLifecycleState(LifecycleState.RESUMED)
//                .build();
//        mReactRootView.startReactApplication(mReactInstanceManager, "officeARandroid", null);
//
//        setContentView(mReactRootView);
//    }


//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        iniReactRootView();
//        setContentView(mReactRootView);
//        initDownloadManager();
//        updateBundle();
//    }
//
//    private void initDownloadManager() {
//        mDownloadCompleteReceiver = new CompleteReceiver();
//        registerReceiver(mDownloadCompleteReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
//    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        iniReactRootView();
        setContentView(mReactRootView);
//        initDownloadManager();
//        updateBundle();
    }

    private void initDownloadManager() {
        mDownloadCompleteReceiver = new CompleteReceiver();
        registerReceiver(mDownloadCompleteReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }
    private void iniReactRootView() {
        ReactInstanceManagerBuilder builder = ReactInstanceManager.builder()
                .setApplication(getApplication())
                .setBundleAssetName("index.android")
//                .setJSMainModuleName("index.android")
                .addPackage(new MainReactPackage())
                .setInitialLifecycleState(LifecycleState.RESUMED);

        File file = new File(JS_BUNDLE_LOCAL_PATH);
        if(file != null && file.exists()){
            builder.setJSBundleFile(JS_BUNDLE_LOCAL_PATH);
            Log.i(TAG, "load bundle from local cache");
        }else{
            builder.setBundleAssetName(JS_BUNDLE_LOCAL_FILE);
            Log.i(TAG, "load bundle from asset");
        }

        mReactRootView = new ReactRootView(this);
        mReactInstanceManager = builder.build();
        mReactRootView.startReactApplication(mReactInstanceManager, "billboard", null);

    }

    private void updateBundle() {

        // Should add version check here, if bundle file
        // is the newest , we do not need to update

        File file = new File(JS_BUNDLE_LOCAL_PATH);
        if(file != null && file.exists()){
            Log.i(TAG, "newest bundle exists !");
            return;
        }

        //Toast.makeText(BaseReactActivity.this, "Start downloading update", Toast.LENGTH_SHORT).show();

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(JS_BUNDLE_REMOTE_URL));
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
        request.setDestinationUri(Uri.parse("file://" + JS_BUNDLE_LOCAL_PATH));
        DownloadManager dm = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        mDownloadId = dm.enqueue(request);

        Log.i(TAG, "start download remote bundle");
    }

    private class CompleteReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            long completeDownloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            if(completeDownloadId == mDownloadId){
                onJSBundleLoadedFromServer();
            }
        }
    };

    private void onJSBundleLoadedFromServer() {
        File file = new File(JS_BUNDLE_LOCAL_PATH);
        if(file == null || !file.exists()){
            Log.i(TAG, "download error, check URL or network state");
            return;
        }

        Log.i(TAG, "download success, reload js bundle");

        Toast.makeText(ReactActivity.this, "Downloading complete", Toast.LENGTH_SHORT).show();
        try {
            Class<?> RIManagerClazz = mReactInstanceManager.getClass();
            Method method = RIManagerClazz.getDeclaredMethod("recreateReactContextInBackground",
                    JavaScriptExecutor.class, JSBundleLoader.class);
            method.setAccessible(true);
            method.invoke(mReactInstanceManager,
                    JavaScriptExecutor.class,
                    JSBundleLoader.createFileLoader(JS_BUNDLE_LOCAL_PATH));
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void invokeDefaultOnBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mReactInstanceManager != null) {
            mReactInstanceManager.onHostPause(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mReactInstanceManager != null) {
            mReactInstanceManager.onHostResume(this, this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mReactInstanceManager != null) {
            mReactInstanceManager.onHostDestroy(this);
        }
    }

    @Override
    public void onBackPressed() {
        if (mReactInstanceManager != null) {
            mReactInstanceManager.onBackPressed();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU && mReactInstanceManager != null) {
            mReactInstanceManager.showDevOptionsDialog();
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }
}
