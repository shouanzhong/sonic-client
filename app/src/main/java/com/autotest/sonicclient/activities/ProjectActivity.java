package com.autotest.sonicclient.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.autotest.sonicclient.R;
import com.autotest.sonicclient.adapters.MyRecyclerViewAdapter;
import com.autotest.sonicclient.utils.Constant;
import com.autotest.sonicclient.utils.http.HttpUtil;
import com.autotest.sonicclient.utils.LogUtil;
import com.autotest.sonicclient.utils.ToastUtil;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;

public class ProjectActivity extends BaseActivity {
    private static final String TAG = "ProjectActivity";

    private RecyclerView recyclerView;
    private MyRecyclerViewAdapter adapter;
    private List<String> dataList;
    private HashMap<String, String> mapNameAndId= new HashMap();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project);
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setTitle("Projects");
        }

        recyclerView = findViewById(R.id.recyclerView);

        // 初始化数据
        initData();

        // 设置布局管理器
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 初始化适配器
        adapter = new MyRecyclerViewAdapter(dataList, position -> {
            // 点击事件处理
            String item = dataList.get(position);
            ToastUtil.showToast("进入项目: " + item);
            Intent intent = new Intent(this, SuitActivity.class);
            intent.putExtra(Constant.KEY_PROJECT_ID, mapNameAndId.get(item));
            this.startActivity(intent);
        });

        // 适配器
        recyclerView.setAdapter(adapter);

        // 分割线
        DividerItemDecoration divider = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(divider);
    }

    private void initData() {
        dataList = new ArrayList<>();
        HttpUtil.get(Constant.URL_SERVER_PROJECT_LIST, new HttpUtil.Callback<JSONArray>() {
            @Override
            public void onResponse(Call call, JSONArray item) throws IOException {
                LogUtil.i(TAG, "onResponse: " + item);
                for (Object o : item) {
                    JSONObject jsonObject = (JSONObject) o;
                    String id = jsonObject.getString("id");
                    String projectName = jsonObject.getString("projectName");
//                    String format = String.format("%s  %s", id, projectName);
                    dataList.add(projectName);
                    mapNameAndId.put(projectName, id);
                    LogUtil.i(TAG, String.format("onResponse: O = %s, type = %s", o, o.getClass()));
                }
                runOnUiThread(() -> {
                    adapter.notifyDataSetChanged();
                });
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                super.onFailure(call, e);
                LogUtil.i(TAG, "onFailure: Constant.URL_SERVER_PROJECT_LIST = " + Constant.URL_SERVER_PROJECT_LIST);
//                ToastUtil.showToast(ProjectActivity.this, "访问服务器失败");
            }
        });
    }

    class DataWrapper implements Serializable {
        private List<String> listGroup;
        private HashMap<String, List<String>> listItem;

        public DataWrapper(List<String> listGroup, HashMap<String, List<String>> listItem) {
            this.listGroup = listGroup;
            this.listItem = listItem;
        }

        public List<String> getListGroup() {
            return listGroup;
        }

        public HashMap<String, List<String>> getListItem() {
            return listItem;
        }
    }
}