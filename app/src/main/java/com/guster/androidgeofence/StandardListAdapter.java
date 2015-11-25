/*******************************************************************************
 * Copyright 2014 Ingenious Lab

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 *******************************************************************************/

package com.guster.androidgeofence;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Gusterwoei on 4/10/14.
 *
 */
public class StandardListAdapter<T> extends ArrayAdapter<T> {
    private Context context;
    private int resId = 0;
    private List<T> data;
    private List<T> dataOri; // original full data list, used for filtering
    private ListAdapterListener listener;

    public StandardListAdapter(Context context, int listItemResId, List data, ListAdapterListener listener) {
        super(context, listItemResId, 0, data);
        this.context = context;
        this.resId = listItemResId;
        this.listener = listener;
        this.data = data;
        this.dataOri = new ArrayList<T>();
        for(T i : this.data) {
            dataOri.add(i);
        }
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public T getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if(view == null) {
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(resId, parent, false);
        }

        listener.getView(position, getItem(position), view, parent);

        return view;
    }

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> newData) {
        this.data = newData;
        notifyDataSetChanged();
    }

    public interface ListAdapterListener {
        View getView(int i, Object item, View view, ViewGroup parent);
        String getFilterCriteria(Object item);
    }

    @Override
    public Filter getFilter() {
        return new MyFilter();
    }

    private class MyFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence c) {

            FilterResults results = new FilterResults();
            List list = new ArrayList<T>();

            for(T t : dataOri) {
                String compareValue = listener.getFilterCriteria(t);
                compareValue = (compareValue==null)? "" : compareValue;
                if(compareValue.toLowerCase().matches(".*" + c.toString().toLowerCase() + ".*")) {
                    //Log.d("NISSAN", "filter criteria matched");
                    list.add(t);
                }
            }
            results.values = list;
            results.count = list.size();

            return results;
        }
        @Override
        protected void publishResults(CharSequence c, FilterResults filterResults) {
            if(filterResults.count > 0) {
                data = (List<T>)filterResults.values;

                // this will affect the original data source, use it with care
                /*clear();
                for(T i : data) {
                    add(i);
                }*/

                //notifyDataSetChanged();
            } else {
                data = new ArrayList<T>();
                //notifyDataSetInvalidated();
            }

            notifyDataSetChanged();
        }
    }
}
