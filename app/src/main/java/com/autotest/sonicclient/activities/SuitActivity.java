package com.autotest.sonicclient.activities;

import androidx.annotation.NonNull;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ExpandableListView;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.autotest.sonicclient.R;
import com.autotest.sonicclient.adapters.SuitExpandableListAdapter;
import com.autotest.sonicclient.services.AdbService;
import com.autotest.sonicclient.services.RunServiceHelper;
import com.autotest.sonicclient.utils.Constant;
import com.autotest.sonicclient.utils.http.HttpUtil;
import com.autotest.sonicclient.utils.LogUtil;
import com.autotest.sonicclient.utils.ToastUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;

public class SuitActivity extends BaseActivity {
    private static final String TAG = "SuitActivity";

    private ExpandableListView expandableListView;
    private SuitExpandableListAdapter adapter;
    private List<String> listGroup;
    private HashMap<String, List<String>> listItem;
    private String projectID;
    private final HashMap<String, String> mapNameAndId = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suit);
        Intent intent = getIntent();
        projectID = intent.getStringExtra(Constant.KEY_PROJECT_ID);
        if (projectID == null) {
//            redirectLogin();
            projectID = "1";  // debug
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent();
                intent.setClass(SuitActivity.this, AdbService.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startService(intent);
            }
        });



        LogUtil.d(TAG, "onCreate: projectID = " + projectID);
        getSupportActionBar().setTitle(String.format("TestSuits-[%s]", Build.BOARD));
        //
        expandableListView = findViewById(R.id.expandableListView);
        listGroup = new ArrayList<>();
        listItem = new HashMap<>();
        adapter = new SuitExpandableListAdapter(this, listGroup, listItem);
        expandableListView.setAdapter(adapter);

        expandableListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int groupPosition) {
                // 处理分组项的展开
                String projectName = listGroup.get(groupPosition);
                if (listItem.get(projectName).size() > 0) {
                    return ;
                }
                String sid = mapNameAndId.get(projectName);
                ToastUtil.showToast(SuitActivity.this, "拉取远端数据");
                HttpUtil.get(String.format(Constant.URL_SERVER_TESTCASE_LIST, sid), new HttpUtil.Callback<JSONArray>() {
                    @Override
                    public void onResponse(Call call, JSONArray cases) throws IOException {
                        if (cases == null) {
                            redirectLogin();
                            return ;
                        }
                        ArrayList<String> names = new ArrayList<>();
                        for (JSONObject jsonObject : cases.toJavaList(JSONObject.class)) {
                            String name = jsonObject.getString(Constant.KEY_CASE_INFO_CASE_NAME);
                            names.add(name);
                        }
                        listItem.put(projectName, names);
                        runOnUiThread(() -> {
                            adapter.notifyDataSetChanged();
                        });
                    }

                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        super.onFailure(call, e);
                        ToastUtil.showToast(SuitActivity.this, e.getMessage());
                    }
                });
            }
        });

        initListData();

        // 运行
        Button btnShowChecked = findViewById(R.id.btnRun);
        btnShowChecked.setOnClickListener(v -> {
            if (!RunServiceHelper.waitServiceReady()) {
                ToastUtil.showToast(SuitActivity.this, "服务连接失败");
                return;
            }

            if (RunServiceHelper.isServiceRunning()) {
                ToastUtil.showToast(SuitActivity.this, "用例执行中, 请先停止");
                return;
            }
            List<String> checkedGroups = adapter.getCheckedGroups();
            ToastUtil.showToast(this, "Start: " + checkedGroups);
            for (String projectName : checkedGroups) {
                String sid = mapNameAndId.get(projectName);
                LogUtil.d(TAG, "onClick: suitId: " + sid);
                HttpUtil.get(String.format(Constant.URL_SERVER_TESTCASE_LIST, sid), new HttpUtil.Callback<JSONArray>() {
                    @Override
                    public void onResponse(Call call, JSONArray steps) throws IOException {
                        if (steps == null) {
                            redirectLogin();
                            return ;
                        }
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put(Constant.KEY_SUIT_INFO_SID, Integer.parseInt(sid));
                        jsonObject.put(Constant.KEY_SUIT_INFO_CASES, steps);

                        RunServiceHelper.startActionRunSuit(SuitActivity.this, jsonObject);
//                        finish();
                    }

                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        super.onFailure(call, e);
                        ToastUtil.showToast(SuitActivity.this, e.getMessage());
                    }
                });
            }
        });

        // 刷新
        SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            // 加载
            initListData();
            swipeRefreshLayout.setRefreshing(false);
        });

    }

    private void initListData() {

        HttpUtil.get(Constant.URL_SERVER_TESTSUITE_LIST,
                new HttpUtil.Callback<JSONArray>() {
            @Override
            public void onResponse(Call call, JSONArray suits) {
                LogUtil.d(TAG, "onResponse: " + suits);
                if (suits.size() == 0) {
                    ToastUtil.showToast(SuitActivity.this, String.format("%s 无用例", Build.MODEL), true);
                }
                mapNameAndId.clear();
                listGroup.clear();
                for (Object o : suits) {
                    JSONObject jsonObject = (JSONObject) o;
                    String id = jsonObject.getString("id");
                    String projectName = jsonObject.getString("name");
                    mapNameAndId.put(projectName, id);
                    listGroup.add(projectName);
                    listItem.put(projectName, new ArrayList<String>());
                    LogUtil.d(TAG, String.format("onResponse: type = %s, O = %s", o.getClass(), o));
                }
                LogUtil.d(TAG, listItem.toString());
                runOnUiThread(() -> {
                    adapter.notifyDataSetChanged();
                });
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                super.onFailure(call, e);
                ToastUtil.showToast(SuitActivity.this, e.getMessage(), true);
                redirectLogin();
            }
        });
    }
}