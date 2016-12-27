package com.an.app.netty;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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

    @ViewInject(R.id.anTvRight)
    private TextView anTvRight;
    @ViewInject(R.id.anIvRight)
    private ImageView anIvRight;

    private static TextView tvTime;
    private static TextView tvContent;
    private static TextView tvBtnMes;
    private static TextView tvContentTips;
    private static TextView tvCount;
    private static TextView tvSendCount;

    private String START_SERVICE = "com.an.app.netty.DataConnectService";
    private boolean sendding = false;//是否正在发送。
    private SharedPreferences.Editor editor;//保存是否点击服务开启的监听状态的editor
    private DataConnectBroadcastReceiver receiver;//连接服务的广播
    private DataAcceptBroadcastReceiver acceptReceiver;//连接服务的广播
    private boolean isConnectedToInternet = false;//是否鏈接到網絡
    private PopupWindow popupWindow;//选择的popupWindow;
    private String[] mIpArray = {"开发：117.78.48.143", "马佩：192.168.0.24"};//提供选择的ip地址。
    private String ipAddress = null;//ip地址。
    public String ACTION = "DataAcceptBroadcastReceiver";//接受数据的广播
    private static final int TIME = 1;//在没有数据时，仿真数据。
    private static final int SERVERDATA = 2;//真是服务器返回的数据。
    private static final int TIME_ACCOUNT = 3;//监听服务器返回的时间。。
    private static final int SERVERDATA_STATE = 4;//监听服务器是否异常的what值。
    private static boolean isRunnint = false;//监听服务器是否正常的boolean。
    private static int i = -1;//记录时间初始化，下面循环,-1减少时间统计误差
    private static int acceptAccount = 0;
    private static int sendAccount = -1;
    private static final int INIT = 5;
    //mHandler用于更新UI。
    public static final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case TIME:
                    /*long systime = System.currentTimeMillis();
                    CharSequence systimeStr = DateFormat.format("yyyy.MM.dd.  HH:mm:ss", systime);
                    tvContent.setMovementMethod(ScrollingMovementMethod.getInstance());
//                    tvContent.append(DataService.INSTANCE.getLongDateTime() + System.getProperty("line.separator"));
                    tvContent.append("--收到消息--" + systimeStr + System.getProperty("line.separator"));
                    int offset = tvContent.getLineCount() * tvContent.getLineHeight();
                    if (offset > tvContent.getHeight()) {
                        tvContent.scrollTo(0, offset - tvContent.getHeight());
                    }*/
                    tvSendCount.setText("已发送：" + sendAccount);
                    break;
                case SERVERDATA:
                    tvContent.setMovementMethod(ScrollingMovementMethod.getInstance());
                    String ret = (String) msg.obj;
                    tvContent.append("-- -- 收到消息 -- " + ret + "-- --" + System.getProperty("line.separator"));
                    int mOffset = tvContent.getLineCount() * tvContent.getLineHeight();
                    if (mOffset > tvContent.getHeight()) {
                        tvContent.scrollTo(0, mOffset - tvContent.getHeight());
                    }
                    tvCount.setText("已接收:" + msg.arg1);
                    break;
                case TIME_ACCOUNT:
//                    tvTime.setText("数据监测记录(Time : " + msg.arg1 + " s)");
                    break;
                case SERVERDATA_STATE:
                    tvBtnMes.setText((String) msg.obj);
                    if (msg.obj.equals("服务器无响应")) {
                        tvBtnMes.setVisibility(View.VISIBLE);
                        tvContentTips.setText("-- --正在重新接受数据...");
                    } else {
                        tvBtnMes.setVisibility(View.INVISIBLE);
//                        tvContentTips.setText("-- -- 正在接受数据...");
                    }
                    break;
                case INIT:
                    try {
                        Thread.sleep(3000);
                        acceptAccount = 0;
                        sendAccount = 2;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    break;
            }

        }
    };
    private String startTime;

    @Override
    public void initView() {
        tvContent = (TextView) findViewById(R.id.tvContent);
        tvTime = (TextView) findViewById(R.id.tvTime);
        tvBtnMes = (TextView) findViewById(R.id.tvBtnMes);
        tvContentTips = (TextView) findViewById(R.id.tvContentTips);
        tvCount = (TextView) findViewById(R.id.tvCount);
        tvSendCount = (TextView) findViewById(R.id.tvSendCount);
        anLlBack.setVisibility(View.INVISIBLE);
//        anLlRight.setVisibility(View.INVISIBLE);
        anIvRight.setVisibility(View.INVISIBLE);
        anTvRight.setText("tips");
        anLlRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, TipsActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
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
        startTime = sp.getString("startTime", null);
        if (sendding) {
            anPb.setVisibility(View.VISIBLE);
            tvSendMsg.setText(R.string.StringSending);
            btnSend.setText(R.string.StringSendingBtn);
            if (!TextUtils.isEmpty(startTime)) {
                tvTime.setText("数据监测记录(Time:" + startTime + ")");
            }
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
                                tvTpMsg.setVisibility(View.INVISIBLE);
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
        //连接数据广播初始化
        IntentFilter filter = new IntentFilter(Intent.ACTION_TIME_TICK);
        receiver = new DataConnectBroadcastReceiver();

        //反馈广播初始化
        IntentFilter acceptFilter = new IntentFilter();
        acceptFilter.addAction("accept");
        acceptReceiver = new DataAcceptBroadcastReceiver(mHandler);

        registerReceiver(acceptReceiver, acceptFilter);

        //在打开一个广播监听服务的启动状态
        //注册系统的ACTION_TIME_TIME广播，每秒监听服务是否开启。
        registerReceiver(receiver, filter);//点击按钮即开始广播，如果点击取消监听，则停止服务和广播。（广播生命周期段，如果在取消监听停止广播时，广播没有注册则会报nullReceiver not registered）
        //没有发送数据则开启服务，发送数据，打开广播监听服务状态。反之亦然。
        if (!sendding) {
            startService(intent);//立即開啓一個服務。
            sendding = true;
            anPb.setVisibility(View.VISIBLE);
            tvTime.setText(R.string.StringData);
            tvContentTips.setText(R.string.StringConneting);
            tvSendMsg.setText(R.string.StringSending);
            btnSend.setText(R.string.StringSendingBtn);
            tvContent.setText("");//初始化tvContent内容
            registerReceiver(receiver, filter);//注冊廣播
            registerReceiver(acceptReceiver, acceptFilter);//注册广播接收服务器数据的。
            i = -1;//暂停是把i值服务-2；
            tvBtnMes.setText("");
            startTime = DataService.INSTANCE.getLongDateTime();
            editor.putString("startTime", startTime);
            editor.commit();
            tvTime.setText("数据监测记录(Time:" + startTime + ")");
            new TimeAcountThread().start();//统计时间的线程。
            new TimeThread().start();//监听本地时间变化。线程，非广播，广播见aw-an框架。
        } else {
            stopService(intent);
            sendding = false;
            mHandler.removeMessages(SERVERDATA_STATE);//移除SERVERDATA_STATE消息。
            mHandler.removeMessages(TIME_ACCOUNT);//移除SERVERDATA_STATE消息。
            anPb.setVisibility(View.INVISIBLE);
            tvContentTips.setText(R.string.StringPause);
            tvBtnMes.setText("");//停止监测中。
            tvSendMsg.setText(R.string.StringSendDataMsg);
            btnSend.setText(R.string.StringSendData);
            if (receiver != null) {
                unregisterReceiver(receiver);
                unregisterReceiver(acceptReceiver);
                receiver = null;//這裏賦值為空。
            }
            String getTime = tvTime.getText().toString().trim();
            tvTime.setText(getTime.subSequence(0, getTime.length() - 1) + "--" + DataService.INSTANCE.getLongDateTime() + ")");
//                                //終止發送則發出一個廣播保證真的終止(這裏由於系統權限的問題不能夠終止系統的廣播。但是可以發送其它的廣播)
            //http://blog.csdn.net/ACM_TH/article/details/50509755
//                                Intent stopIntent = new Intent(Intent.ACTION_TIME_TICK);
//                                stopIntent.putExtra("kill", 0);
//                                sendOrderedBroadcast(stopIntent, null);
        }
        editor.putBoolean("sendding", sendding);
        editor.commit();
//        mHandler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    Thread.sleep(2000);
//                    mHandler.sendEmptyMessage(INIT);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        }, 3000);
    }

    public static class DataAcceptBroadcastReceiver extends BroadcastReceiver {
        private final Handler handler;

        public DataAcceptBroadcastReceiver() {
            handler = mHandler;
        }

        public DataAcceptBroadcastReceiver(Handler handler) {
            this.handler = handler;
        }

        @Override
        public void onReceive(Context context, final Intent intent) {
            System.out.println("--qydq--MainActivity收到DataSendClientHandler消息--内容--" + intent.getStringExtra("ret"));
            isRunnint = true;
            acceptAccount++;
            Message msg = new Message();
            msg.obj = intent.getStringExtra("ret");
            msg.what = SERVERDATA;
            msg.arg1 = acceptAccount;
            handler.sendMessage(msg);
        }
    }

    public class TimeThread extends Thread {
        @Override
        public void run() {
            do {
                try {
                    Thread.sleep(2000);
                    sendAccount++;
                    Message msg = new Message();
                    msg.what = TIME;
                    mHandler.sendMessage(msg);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } while (sendding);
        }
    }

    public class TimeAcountThread extends Thread {
        @Override
        public void run() {
            do {
                try {
                    Thread.sleep(1000);
                    i++;
                    //msg已使用不能再用。This message is already in use.放在循环外面。
                    Message msg = new Message();
                    Message serverMsg = new Message();
                    msg.what = TIME_ACCOUNT;
                    msg.arg1 = i;
                    mHandler.sendMessage(msg);
                    //每十秒钟监听服务器是否连接成功。i不从0开始。
                    if (i > 0 && i % 10 == 0 && isRunnint) {
                        serverMsg.what = SERVERDATA_STATE;
                        serverMsg.obj = "监测正常";
                        mHandler.sendMessage(serverMsg);
                    }
                    if (i > 0 && i % 10 == 0 && !isRunnint) {
                        serverMsg.what = SERVERDATA_STATE;
                        serverMsg.obj = "服务器无响应";
                        mHandler.sendMessage(serverMsg);
                    }
                    //这行代码很重要。isRunnint = true时，变为初始状态false，收到广播的时候会变为true，先执行上面两个if语句。
//                    isRunnint = false;

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } while (sendding);
        }
    }
}