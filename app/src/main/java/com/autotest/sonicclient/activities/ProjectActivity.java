package com.autotest.sonicclient.activities;

import androidx.appcompat.app.ActionBar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.autotest.sonicclient.R;
import com.autotest.sonicclient.adapters.MyAdapter;
import com.autotest.sonicclient.utils.Constant;
import com.autotest.sonicclient.utils.HttpUtil;
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
    private MyAdapter adapter;
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
        adapter = new MyAdapter(dataList, position -> {
            // 点击事件处理
            String item = dataList.get(position);
            ToastUtil.showToast("Clicked: " + item);
            Intent intent = new Intent(this, SuitActivity.class);
            intent.putExtra(Constant.KEY_PROJECT_ID, mapNameAndId.get(item));
            this.startActivity(intent);
        });

        // 绑定适配器
        recyclerView.setAdapter(adapter);
    }

    private void initData() {
        dataList = new ArrayList<>();
        HttpUtil.get(Constant.URL_SERVER_PROJECT_LIST, new HttpUtil.Callback<JSONArray>() {
            @Override
            public void onResponse(Call call, JSONArray item) throws IOException {
                Log.i(TAG, "onResponse: " + item);
                for (Object o : item) {
                    JSONObject jsonObject = (JSONObject) o;
                    String id = jsonObject.getString("id");
                    String projectName = jsonObject.getString("projectName");
//                    String format = String.format("%s  %s", id, projectName);
                    dataList.add(projectName);
                    mapNameAndId.put(projectName, id);
                    Log.i(TAG, String.format("onResponse: O = %s, type = %s", o, o.getClass()));
                }
                runOnUiThread(() -> {
                    adapter.notifyDataSetChanged();
                });
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