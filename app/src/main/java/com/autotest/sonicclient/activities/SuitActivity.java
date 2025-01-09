package com.autotest.sonicclient.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.autotest.sonicclient.R;
import com.autotest.sonicclient.adapters.SuitExpandableListAdapter;
import com.autotest.sonicclient.handler.CaseHandler;
import com.autotest.sonicclient.services.RunService;
import com.autotest.sonicclient.utils.Constant;
import com.autotest.sonicclient.utils.HttpUtil;
import com.autotest.sonicclient.utils.JsonParser;
import com.autotest.sonicclient.utils.LogUtil;
import com.autotest.sonicclient.utils.ToastUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.Response;

public class SuitActivity extends BaseActivity {
    private static final String TAG = "SuitActivity";

    private ExpandableListView expandableListView;
    private SuitExpandableListAdapter adapter;
    private List<String> listGroup;
    private HashMap<String, List<String>> listItem;
    private String projectID;
    private final HashMap<String, String> mapNameAndId = new HashMap<>();;

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
        LogUtil.d(TAG, "onCreate: projectID = " + projectID);
        getSupportActionBar().setTitle("TestSuits");
        //
        expandableListView = findViewById(R.id.expandableListView);
        listGroup = new ArrayList<>();
        listItem = new HashMap<>();
        adapter = new SuitExpandableListAdapter(this, listGroup, listItem);
        expandableListView.setAdapter(adapter);
        initListData();

        Button btnShowChecked = findViewById(R.id.btnShowChecked);
        btnShowChecked.setOnClickListener(v -> {
            List<String> checkedGroups = adapter.getCheckedGroups();
            ToastUtil.showToast(this, "Start: " + checkedGroups);
            for (String projectName : checkedGroups) {
                String sid = mapNameAndId.get(projectName);
                LogUtil.d(TAG, "onClick: suitId: " + sid);
                HttpUtil.get(String.format(Constant.URL_SERVER_TESTCASE_LIST, sid), new HttpUtil.Callback<JSONObject>() {
                    @Override
                    public void onResponse(Call call, JSONObject item) throws IOException {
                        item.put("sid", Integer.parseInt(sid));
                        RunService.startActionRunSuit(SuitActivity.this, item);
                    }
                });
            }
            
//            JSONObject jsonObject = JsonParser.readJsonFromAssets(this, "suitCaseTemp.json");

        });
    }

    private void initListData() {

        HttpUtil.get(String.format(Constant.URL_SERVER_TESTSUITE_LIST, projectID),
                new HttpUtil.Callback<JSONObject>() {
            @Override
            public void onResponse(Call call, JSONObject item) {
                LogUtil.d(TAG, "onResponse: " + item);
                JSONArray content = item.getJSONArray("content");
                for (Object o : content) {
                    JSONObject jsonObject = (JSONObject) o;
                    String id = jsonObject.getString("id");
                    String projectName = jsonObject.getString("name");
                    List<JSONObject> testCases = jsonObject.getJSONArray("testCases").toJavaList(JSONObject.class);
                    mapNameAndId.put(projectName, id);
                    listGroup.add(projectName);
                    ArrayList<String> caseNameList = new ArrayList<>();
                    for (JSONObject testCase : testCases) {
                        String tName = testCase.getString("name");
                        caseNameList.add(tName);
                    }
                    listItem.put(projectName, caseNameList);
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
                redirectLogin();
            }
        });
    }
}