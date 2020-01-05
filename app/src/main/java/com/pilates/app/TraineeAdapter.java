package com.pilates.app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.HashMap;
import java.util.Map;

public class TraineeAdapter extends BaseAdapter {
    private final Map<String, String> traineeNamesById;
    private final Map<Integer, String> idsByIndex;

    public TraineeAdapter(final Map<String, String> traineeNamesById) {
        this.traineeNamesById = traineeNamesById;
        this.idsByIndex = new HashMap<>();

        int index = 0;
        for (Map.Entry<String, String> entry : traineeNamesById.entrySet()) {
            idsByIndex.put(index, entry.getKey());
            index++;
        }
    }

    @Override
    public int getCount() {
        return idsByIndex.size();
    }

    @Override
    public String getItem(int position) {
        final String id = idsByIndex.get(position);
        return traineeNamesById.get(id);
    }

    @Override
    public long getItemId(int position) {
//        final String id = idsByIndex.get(position);

        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
           return LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_post_trainee_register, parent, false);
        }

        return convertView;
    }



    public void addAll() {

    }


}
