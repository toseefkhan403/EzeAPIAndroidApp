package com.android.ezepaymentsapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;


import com.android.ezepaymentsapp.fragments.CollectionHistoryFragment;
import com.android.ezepaymentsapp.fragments.HomeFragment;
import com.android.ezepaymentsapp.fragments.SearchFragment;
import com.android.ezepaymentsapp.utils.Adapter;
import com.android.ezepaymentsapp.utils.BottomNavItem;
import com.android.ezepaymentsapp.utils.BottomNavigationViewHelper;
import com.android.ezepaymentsapp.utils.EqualSpacingItemDecoration;
import com.eze.api.EzeAPI;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class SearchBoxActivity extends AppCompatActivity {

    private final int REQUEST_CODE_PRINT_BITMAP = 10023;
    // todo change values here
    public static final String MERCHANT_NAME_VALUE = "NAGAR_NIGAM_AGRA";
    public static final String API_KEY_VALUE = "44da1040-5309-45a7-9ac8-c76bf756d2e1";
    public static final String USER_NAME_VALUE = "7300740645";
    public static final String APP_MODE_VALUE = "DEMO";
    private final int REQUEST_CODE_INITIALIZE = 10001;

    RecyclerView recyclerView;
    ArrayList<BottomNavItem> source;
    RecyclerView.LayoutManager RecyclerViewLayoutManager;
    Adapter adapter;
    LinearLayoutManager HorizontalLayout;
    JSONObject jsonObject;

    @Override
    public void onBackPressed() {
        askIfCloseApp();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        jsonObject = getBottomIconsInfo();
        if(jsonObject != null) {
//            setupBottomRV();
            BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
            bottomNav.setSelectedItemId(R.id.collection);
            bottomNav.setOnNavigationItemSelectedListener(navListener);
            BottomNavigationViewHelper.disableShiftMode(bottomNav);
            BottomNavigationViewHelper.fixBottomNavigationText(bottomNav);

            try {
                if(jsonObject.getString("add_house").equals("null")) {
                    bottomNav.getMenu().removeItem(R.id.add_house);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                bottomNav.getMenu().removeItem(R.id.add_house);
            }
        } else {
            Toast.makeText(this, "Unauthorized access", Toast.LENGTH_SHORT).show();
        }

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new SearchFragment())
                .commit();
    }

   private BottomNavigationView.OnNavigationItemSelectedListener navListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment selectedFragment = null;
            try {
                switch (item.getItemId()) {
                    case R.id.add_house:
                        Bundle bundle = new Bundle();
                        bundle.putString("url", jsonObject.getString("add_house"));
                        HomeFragment frag = new HomeFragment();
                        frag.setArguments(bundle);
                        selectedFragment = frag;
                        changeFragment(SearchBoxActivity.this, selectedFragment);
                        break;
                    case R.id.collection:
                        selectedFragment = new SearchFragment();
                        changeFragment(SearchBoxActivity.this, selectedFragment);
                        break;

                    case R.id.collection_history:
                        Bundle b = new Bundle();
                        b.putString("url", jsonObject.getString("collection_history"));
                        CollectionHistoryFragment f = new CollectionHistoryFragment();
                        f.setArguments(b);
                        selectedFragment = f;
                        changeFragment(SearchBoxActivity.this, selectedFragment);
                        break;

                    case R.id.reprint_receipt:
                        rePrintPopUp(SearchBoxActivity.this);
                        break;

                    case R.id.logout:
                        logoutPopUp(SearchBoxActivity.this);
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        }
    };


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
                            doInitializeEzeTap();
                            JSONObject jsonRequest = new JSONObject();
                            JSONObject jsonImageObj = new JSONObject();
                            String encodedImageData = getEncodedImageData("imageData",context);
                            String encodedImageData2 =
                                    "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";
                            if(encodedImageData != null) {
                                Log.d("TAG", "onClick: reprint" + encodedImageData);
                                Toast.makeText(context, "Re-printing last receipt...", Toast.LENGTH_LONG).show();
                                jsonImageObj.put("imageData", encodedImageData);
                                jsonImageObj.put("imageType", "PNG");
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

    private void doInitializeEzeTap() {
        try {
            JSONObject jsonRequest = new JSONObject();
            jsonRequest.put("demoAppKey", API_KEY_VALUE);
            jsonRequest.put("prodAppKey", API_KEY_VALUE);
            jsonRequest.put("merchantName", MERCHANT_NAME_VALUE);
            jsonRequest.put("userName", USER_NAME_VALUE);
            jsonRequest.put("currencyCode", "INR");
            jsonRequest.put("appMode", APP_MODE_VALUE);
            jsonRequest.put("captureSignature", "true");
            jsonRequest.put("prepareDevice", "false");
            jsonRequest.put("captureReceipt", "false");
            EzeAPI.initialize(this, REQUEST_CODE_INITIALIZE, jsonRequest);
            Log.d("TAG", "doInitializeEzeTap: initialized");
        }catch (Exception e) {
            e.printStackTrace();
        }
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

    private JSONObject getBottomIconsInfo() {
        String error = ""; // string field
        String params = getParams();
        String loginIdUrl = "https://agrapropertytax.com/doortodoor/mobile_api.php?action=login&" + params;
        String result = null;
        int resCode;
        InputStream in;
        try {
            URL url = new URL(loginIdUrl);

            URLConnection urlConn;
            urlConn = url.openConnection();

            HttpsURLConnection httpsConn = (HttpsURLConnection) urlConn;
            httpsConn.setAllowUserInteraction(false);
            httpsConn.setInstanceFollowRedirects(true);
            httpsConn.setRequestMethod("GET");
            httpsConn.connect();
            resCode = httpsConn.getResponseCode();

            if (resCode == HttpURLConnection.HTTP_OK) {
                in = httpsConn.getInputStream();

                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        in, "iso-8859-1"), 8);
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                in.close();
                result = sb.toString();
            } else {
                error += resCode;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            if(result == null) return null;
            return new JSONObject(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getParams() {
        SharedPreferences sharedPreferences = this.getSharedPreferences("app.db", Context.MODE_PRIVATE);
        Map<String, ?> map = (Map<String, ?>) sharedPreferences.getAll();
        String value = null;
        try {
            value = map.get("loginId").toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    private void setupBottomRV() {
        recyclerView
                = (RecyclerView)findViewById(
                R.id.bottom_navigation);
        RecyclerViewLayoutManager
                = new LinearLayoutManager(
                getApplicationContext());

        recyclerView.setLayoutManager(
                RecyclerViewLayoutManager);

        addItemsToRecyclerViewArrayList();
        adapter = new Adapter(source);

        HorizontalLayout
                = new LinearLayoutManager(
                SearchBoxActivity.this,
                LinearLayoutManager.HORIZONTAL,
                false);
        recyclerView.setLayoutManager(HorizontalLayout);
        recyclerView.addItemDecoration(new EqualSpacingItemDecoration(80, EqualSpacingItemDecoration.HORIZONTAL)); // 16px. In practice, you'll want to use getDimensionPixelSize

        // Set adapter on recycler view
        recyclerView.setAdapter(adapter);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new SearchFragment()).commit();
    }

    public void addItemsToRecyclerViewArrayList() {
        // Adding items to ArrayList
        source = new ArrayList<>();
        try {
            String url = jsonObject.getString("add_house");
            Log.d("TAG", "addItemsToRecyclerViewArrayList: " + url);
            if(!jsonObject.getString("add_house").equals("null")) {
                source.add(new BottomNavItem("Add House", R.drawable.ic_home,url));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        source.add(new BottomNavItem("Collection", R.drawable.ic_collection,null));
        try {
            source.add(new BottomNavItem("Collection \n History", R.drawable.ic_collection_history, jsonObject.getString("collection_history")));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        source.add(new BottomNavItem("Re-print Last \n Receipt", R.drawable.ic_receipt, null));
        source.add(new BottomNavItem("Logout", R.drawable.ic_logout, null));
    }

    private void askIfCloseApp() {
        AlertDialog dialog;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Do you want to exit the app?");

        builder
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                            finishAffinity();
                        }
                        System.exit(0);
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
}
