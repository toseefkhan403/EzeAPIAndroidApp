package com.android.ezepaymentsapp.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.LayoutInflater;
import android.widget.Toast;

import com.android.ezepaymentsapp.MainActivity;
import com.android.ezepaymentsapp.fragments.HomeFragment;
import com.android.ezepaymentsapp.R;
import com.android.ezepaymentsapp.fragments.SearchFragment;
import com.eze.api.EzeAPI;

import org.json.JSONObject;

import java.util.List;
import java.util.Map;

public class Adapter
        extends RecyclerView.Adapter<Adapter.MyView> {

    private List<BottomNavItem> list;
    private final int REQUEST_CODE_PRINT_BITMAP = 10023;

    public class MyView
            extends RecyclerView.ViewHolder {

        TextView textView;
        ImageView iconView;
        LinearLayout parent;

        public MyView(View view)
        {
            super(view);
            textView = (TextView)view
                    .findViewById(R.id.iconTextView);
            iconView = (ImageView) view
                    .findViewById(R.id.iconView);
            parent = (LinearLayout) view.findViewById(R.id.bottom_navigation_parent);
        }
    }

    public Adapter(List<BottomNavItem> horizontalList)
    {
        this.list = horizontalList;
    }

    @Override
    public MyView onCreateViewHolder(ViewGroup parent,
                                     int viewType)
    {
        View itemView
                = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.item_bottom_nav,
                        parent,
                        false);

        return new MyView(itemView);
    }

    @Override
    public void onBindViewHolder(final MyView holder,
                                 final int position)
    {
        BottomNavItem bottomNavItem = list.get(position);
        holder.textView.setText(bottomNavItem.getText());
        holder.iconView.setImageResource(bottomNavItem.getIcon());
        holder.parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment selectedFragment = null;
                switch (bottomNavItem.getText()) {
                    case "Add House":
                        Bundle bundle = new Bundle();
                        bundle.putString("url",bottomNavItem.getUrl());
                        HomeFragment frag = new HomeFragment();
                        frag.setArguments(bundle);
                        selectedFragment = frag;
                        changeFragment(view.getContext(), selectedFragment);
                        break;
                    case "Collection":
                        selectedFragment = new SearchFragment();
                        changeFragment(view.getContext(), selectedFragment);
                        break;

                    case "Collection \n History":
                        Bundle b = new Bundle();
                        b.putString("url",bottomNavItem.getUrl());
                        HomeFragment f = new HomeFragment();
                        f.setArguments(b);
                        selectedFragment = f;
                        changeFragment(view.getContext(), selectedFragment);
                        break;

                        case "Re-print Last \n Receipt":
                        rePrintPopUp(holder.itemView.getContext());
                        break;

                        case "Logout":
                        logoutPopUp(holder.itemView.getContext());
                        break;
                }
            }
        });
    }

    private void setColors(int row_index, int position, MyView holder) {
        if(row_index==position){
            holder.parent.setBackgroundColor(Color.parseColor("#567845"));
//            holder.tv1.setTextColor(Color.parseColor("#ffffff"));
        }
        else
        {
            holder.parent.setBackgroundColor(Color.parseColor("#ffffff"));
//            holder.tv1.setTextColor(Color.parseColor("#000000"));
        }
    }

    private void changeFragment(Context context, Fragment selectedFragment) {
        AppCompatActivity activity = (AppCompatActivity) context;
        activity.getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, selectedFragment)
                .commit();
    }

    private void rePrintPopUp(Context context) {
        AlertDialog dialog;

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Do you want to re-print the last printed receipt?");

            builder
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            try {
                                JSONObject jsonRequest = new JSONObject();
                                JSONObject jsonImageObj = new JSONObject();
                                String encodedImageData = getEncodedImageData("imageData",context);
                                if(encodedImageData != null) {
                                    Log.d("TAG", "onClick: reprint" + encodedImageData);
                                    Toast.makeText(context, "Re-printing last receipt...", Toast.LENGTH_LONG).show();
                                    jsonImageObj.put("imageData", encodedImageData);
                                    jsonImageObj.put("imageType", "JPEG");
                                    jsonImageObj.put("height", "");// optional
                                    jsonImageObj.put("weight", "");// optional
                                    jsonRequest.put("image",
                                            jsonImageObj); // Pass this attribute when you have a valid captured signature image
                                    EzeAPI.printBitmap(context, REQUEST_CODE_PRINT_BITMAP, jsonRequest);
                                } else {
                                    Toast.makeText(context, "No receipt printed in this session yet!", Toast.LENGTH_SHORT).show();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });
            dialog = builder.create();
            dialog.show();
    }

    public String getEncodedImageData(String key, Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("app.db", Context.MODE_PRIVATE);
        Map<String, ?> map = (Map<String, ?>) sharedPreferences.getAll();
        String value = null;
        try {
            value = map.get(key).toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    private void logoutPopUp(Context context) {
        AlertDialog dialog;

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Do you want to logout?");

            builder
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            clearLoginPrefs(context);
                            Intent intent = new Intent(context, MainActivity.class);
                            context.startActivity(intent);
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });
            dialog = builder.create();
            dialog.show();
    }

    private void clearLoginPrefs(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("app.db",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();
    }

    @Override
    public int getItemCount()
    {
        return list.size();
    }
}


