package com.an.app.netty;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;


/**
 * Created by qydda on 2016/12/15.
 * START_STICKY：如果service进程被kill掉，保留service的状态为开始状态，但不保留递送的intent对象。随后系统会尝试重新创建service，由于服务状态为开始状态，所以创建服务后一定会调用onStartCommand(Intent,int,int)方法。如果在此期间没有任何启动命令被传递到service，那么参数Intent将为null。
 * START_NOT_STICKY：“非粘性的”。使用这个返回值时，如果在执行完onStartCommand后，服务被异常kill掉，系统将会把它置为started状态，系统不会自动重启该服务，直到startService(Intent intent)方法再次被调用;。
 * START_REDELIVER_INTENT：重传Intent。使用这个返回值时，如果在执行完onStartCommand后，服务被异常kill掉，系统会自动重启该服务，并将Intent的值传入。
 * START_STICKY_COMPATIBILITY：START_STICKY的兼容版本，但不保证服务被kill后一定能重启。
 */

public class DataConnectService extends Service {
    private final IBinder mBinder = new LocalBinder();
    private String TAG = "DataConnectService";
    Runnable mBackgroundRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //在服务里面发送数据，
            DataSendClient client = new DataSendClient(getApplicationContext());
            try {
                client.start();
//                            client.sendDataToService();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
    private Handler handler;
    private HandlerThread thread;

    //Class used for the client Binder.  Because we know this service always
    //runs in the same process as its clients, we don't need to deal with IPC.
    public class LocalBinder extends Binder {
        DataConnectService getService() {
            // Return this instance of LocalService so clients can call public methods
            return DataConnectService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //在服务里面发送数据，
        thread = new HandlerThread("HandlerThread");
        thread.start();
        handler = new Handler(thread.getLooper());
        handler.post(mBackgroundRunnable);
        //START_REDELIVER_INTENT;
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(mBackgroundRunnable);
        System.out.println(TAG + "--qydq--服务已经停止--");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
}
