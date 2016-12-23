package com.an.app.netty;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.an.base.utils.NetBroadcastReceiverUtils;
import com.an.base.view.SuperActivity;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

@ContentView(R.layout.activity_main)
public class MainActivity extends SuperActivity implements View.OnClickListener {
    @ViewInject(R.id.btnSend)
    private Button btnSend;
    @ViewInject(R.id.anLlBack)
    private LinearLayout anLlBack;
    @ViewInject(R.id.anTvTitle)
    private TextView anTvTitle;
    @ViewInject(R.id.anLlRight)
    private LinearLayout anLlRight;
    @ViewInject(R.id.tvSendMsg)
    private TextView tvSendMsg;
    @ViewInject(R.id.tvNet)
    private TextView tvNet;
    @ViewInject(R.id.btnIp1)
    private Button btnIp1;
    @ViewInject(R.id.btnIp2)
    private Button btnIp2;

    private String START_SERVICE = "com.an.app.netty.DataConnectService";
    private boolean sendding = false;//是否正在发送。
    private SharedPreferences.Editor editor;//保存是否点击服务开启的监听状态的editor
    private DataConnectBroadcastReceiver receiver;
    private boolean isConnectedToInternet = false;//是否鏈接到網絡

    @Override
    public void initView() {
        anLlBack.setVisibility(View.INVISIBLE);
        anLlRight.setVisibility(View.INVISIBLE);
        if (NetBroadcastReceiverUtils.isConnectedToInternet(mContext)) {
            isConnectedToInternet = true;
        } else {
            isConnectedToInternet = false;
            tvNet.setVisibility(View.VISIBLE);
        }
        anTvTitle.setText(R.string.app_name);
        editor = sp.edit();
        sendding = sp.getBoolean("sendding", false);
        if (sendding) {
            tvSendMsg.setText(R.string.StringSending);
            btnSend.setText(R.string.StringSendingBtn);
        }
        btnSend.setOnClickListener(this);
        btnIp1.setOnClickListener(this);
        btnIp2.setOnClickListener(this);
    }

    @Override
    public void onNetChange(int netModel) {
        super.onNetChange(netModel);
        if (netModel == 1) {
            showToast("连接无线网络");
            isConnectedToInternet = true;
            tvNet.setVisibility(View.INVISIBLE);
        } else if (netModel == 0) {
            showToast("连接移动网络");
            isConnectedToInternet = true;
            tvNet.setVisibility(View.INVISIBLE);
        } else if (netModel == -1) {
            isConnectedToInternet = false;
            tvNet.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnSend:
                if (isConnectedToInternet) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //服务初始化
                            Intent intent = new Intent(mContext, DataConnectService.class);
                            intent.setAction(START_SERVICE);
                            //广播初始化
                            IntentFilter filter = new IntentFilter(Intent.ACTION_TIME_TICK);
                            receiver = new DataConnectBroadcastReceiver();

                            //在打开一个广播监听服务的启动状态
                            //注册系统的ACTION_TIME_TIME广播，每秒监听服务是否开启。
                            registerReceiver(receiver, filter);//点击按钮即开始广播，如果点击取消监听，则停止服务和广播。（广播生命周期段，如果在取消监听停止广播时，广播没有注册则会报nullReceiver not registered）
                            //没有发送数据则开启服务，发送数据，打开广播监听服务状态。反之亦然。
                            if (!sendding) {
                                startService(intent);//立即開啓一個服務。
                                sendding = true;
                                tvSendMsg.setText(R.string.StringSending);
                                btnSend.setText(R.string.StringSendingBtn);
                                registerReceiver(receiver, filter);//注冊廣播
                            } else {
                                stopService(intent);
                                sendding = false;
                                tvSendMsg.setText(R.string.StringSendDataMsg);
                                btnSend.setText(R.string.StringSendData);
                                if (receiver != null) {
                                    unregisterReceiver(receiver);
                                    receiver = null;//這裏賦值為空。
                                }
//                                //終止發送則發出一個廣播保證真的終止(這裏由於系統權限的問題不能夠終止系統的廣播。但是可以發送其它的廣播)
                                  //http://blog.csdn.net/ACM_TH/article/details/50509755
//                                Intent stopIntent = new Intent(Intent.ACTION_TIME_TICK);
//                                stopIntent.putExtra("kill", 0);
//                                sendOrderedBroadcast(stopIntent, null);
                            }
                            editor.putBoolean("sendding", sendding);
                            editor.commit();
                        }
                    });
                } else {
                    showToast(getString(R.string.StringNet));
                }
                break;
            case R.id.btnIp1:
                showToast(getString(R.string.StringIp1));
                editor.putString("hostip", "192.168.0.24");
                editor.commit();
                break;
            case R.id.btnIp2:
                showToast(getString(R.string.StringIp2));
                editor.putString("hostip", "117.78.48.143");
                editor.commit();
                break;
            default:
                break;
        }
    }
}