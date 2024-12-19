package com.autotest.sonicclient.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.autotest.sonicclient.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SuitExpandableListAdapter extends BaseExpandableListAdapter {

    private Context context;
    private List<String> listGroup;
    private HashMap<String, List<String>> listItem;
    private HashMap<Integer, Boolean> groupCheckedState; // 保存父项勾选状态

    public SuitExpandableListAdapter(Context context, List<String> listGroup, HashMap<String, List<String>> listItem) {
        this.context = context;
        this.listGroup = listGroup;
        this.listItem = listItem;
        this.groupCheckedState = new HashMap<>();
        initCheckedState(); // 初始化父项状态
    }

    // 初始化父项的勾选状态
    private void initCheckedState() {
        for (int i = 0; i < listGroup.size(); i++) {
            groupCheckedState.put(i, false);
        }
    }

    @Override
    public int getGroupCount() {
        return listGroup.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return listItem.get(listGroup.get(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return listGroup.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return listItem.get(listGroup.get(groupPosition)).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        String groupTitle = (String) getGroup(groupPosition);
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_group, parent, false);
        }
        TextView textView = convertView.findViewById(R.id.textGroup);
        CheckBox checkBox = convertView.findViewById(R.id.checkBoxGroup);

        textView.setText(groupTitle);

        // 设置勾选状态，确保不返回 null
        boolean isChecked = Boolean.TRUE.equals(groupCheckedState.getOrDefault(groupPosition, false));
        checkBox.setOnCheckedChangeListener(null); // 清空之前的监听器
        checkBox.setChecked(isChecked); // 设置选中状态

        // 添加监听器
        checkBox.setOnCheckedChangeListener((buttonView, isChecked1) -> groupCheckedState.put(groupPosition, isChecked1));

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        String childText = (String) getChild(groupPosition, childPosition);
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(android.R.layout.simple_expandable_list_item_1, parent, false);
        }
        TextView textView = convertView.findViewById(android.R.id.text1);
        textView.setText(childText);
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    // 获取被勾选的父项
    public List<String> getCheckedGroups() {
        List<String> checkedGroups = new ArrayList<>();
        for (int i = 0; i < listGroup.size(); i++) {
            if (groupCheckedState.getOrDefault(i, false)) {
                checkedGroups.add(listGroup.get(i));
            }
        }
        return checkedGroups;
    }
}
