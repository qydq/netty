package com.an.app.netty;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.an.base.utils.DataService;
import com.an.base.utils.NetBroadcastReceiverUtils;
import com.an.base.view.SuperActivity;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

@ContentView(R.layout.activity_main)
public class MainActivity extends SuperActivity implements View.OnClickListener {
    @ViewInject(R.id.btnSend)
    private TextView btnSend;
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
    @ViewInject(R.id.ivSelector)
    private ImageView ivSelector;
    @ViewInject(R.id.editTextIp)
    private EditText editTextIp;
    @ViewInject(R.id.tvTips)
    private TextView tvTips;
    @ViewInject(R.id.tvTpMsg)
    private TextView tvTpMsg;
    @ViewInject(R.id.llSend)
    private LinearLayout llSend;
    @ViewInject(R.id.anPb)
    private ProgressBar anPb;

    private String START_SERVICE = "com.an.app.netty.DataConnectService";
    private boolean sendding = false;//是否正在发送。
    private SharedPreferences.Editor editor;//保存是否点击服务开启的监听状态的editor
    private DataConnectBroadcastReceiver receiver;
    private boolean isConnectedToInternet = false;//是否鏈接到網絡
    private PopupWindow popupWindow;//选择的popupWindow;
    private String[] mIpArray = {"开发：117.78.48.143", "马佩：192.168.0.24"};//提供选择的ip地址。
    private String ipAddress = null;

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
        ipAddress = sp.getString("hostip", null);
        if (sendding) {
            anPb.setVisibility(View.VISIBLE);
            tvSendMsg.setText(R.string.StringSending);
            btnSend.setText(R.string.StringSendingBtn);
        }
        if (!TextUtils.isEmpty(ipAddress)) {
            editTextIp.setText(ipAddress);
            tvTips.setText("选择或输入ip地址(当前ip:" + ipAddress + ")");
        }
        llSend.setOnClickListener(this);
        ivSelector.setOnClickListener(this);
        editTextIp.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    tvTpMsg.setVisibility(View.INVISIBLE);
                }
            }
        });
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
            case R.id.llSend:
                if (isConnectedToInternet) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (DataService.INSTANCE.checkIp(editTextIp.getText().toString().trim())) {
                                sendDataToService();
                            } else {
                                tvTpMsg.setVisibility(View.VISIBLE);
                                showToast(getString(R.string.StringIpInfo));
                            }

                        }
                    });
                } else {
                    showToast(getString(R.string.StringNet));
                }
                break;
            case R.id.ivSelector:
                showPopupWindow(view);
                break;
            default:
                break;
        }
    }

    /**
     * @param view 控制显示的位置参数。
     */
    private void showPopupWindow(View view) {
        View contentView = LayoutInflater.from(mContext).inflate(R.layout.item_ipselector, null);
        ListView listView = (ListView) contentView.findViewById(R.id.listView);
        ArrayAdapter mArrAdapter = new ArrayAdapter<>(mContext, android.R.layout.simple_list_item_1, mIpArray);
        listView.setAdapter(mArrAdapter);
        popupWindow = new PopupWindow(contentView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setTouchable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setTouchInterceptor(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.d(TAG, "--qydq@@--showPopupWindow--onTouch--");
                return false;
                // 这里如果返回true的话，touch事件将被拦截
                // 拦截后 PopupWindow的onTouchEvent不被调用，这样点击外部区域无法dismiss
            }
        });
        //popupWind消失可以刷新数据 。
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
//                MyUtils.setBackgroundAlpha(1.0f, MainActivity.this);
            }
        });
        // 如果不设置PopupWindow的背景，无论是点击外部区域还是Back键都无法dismiss弹框
        // 我觉得这里是API的一个bug
        popupWindow.setBackgroundDrawable(ContextCompat.getDrawable(mContext, R.color.CommMainBg));
        popupWindow.showAsDropDown(view);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mIpArray.length != 0) {
                    popupWindow.dismiss();
                    tvTpMsg.setVisibility(View.INVISIBLE);
                    String ipAddress = mIpArray[position].substring(3).toString().trim();
                    editor.putString("hostip", ipAddress);
                    editor.commit();
                    editTextIp.setText(ipAddress);
                    tvTips.setText("选择或输入ip地址(当前ip:" + ipAddress + ")");
                }
            }
        });
        mArrAdapter.notifyDataSetChanged();
    }

    public void sendDataToService() {
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
            anPb.setVisibility(View.VISIBLE);
            tvSendMsg.setText(R.string.StringSending);
            btnSend.setText(R.string.StringSendingBtn);
            registerReceiver(receiver, filter);//注冊廣播
        } else {
            stopService(intent);
            sendding = false;
            anPb.setVisibility(View.INVISIBLE);
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
}