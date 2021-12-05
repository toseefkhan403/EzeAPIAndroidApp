package com.android.ezepaymentsapp.fragments;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.android.ezepaymentsapp.utils.ListItemAdapter;
import com.android.ezepaymentsapp.utils.ListItem;
import com.android.ezepaymentsapp.R;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

public class SearchFragment extends Fragment {

    public SearchFragment(){
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        Button searchButton = (Button) view.findViewById(R.id.searchButton);
        EditText query = (EditText) view.findViewById(R.id.query);
        ListView listView = (ListView) view.findViewById(R.id.listView);

//        ImageView exitApp = (ImageView) findViewById(R.id.exitAppImage);
//        exitApp.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                askIfCloseApp();
//            }
//        });

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(query.getText().toString().equals("")){
                    Toast.makeText(getContext().getApplicationContext(), "Enter some value to search", Toast.LENGTH_SHORT).show();
                } else {
                    ArrayList<ListItem> arrayOfUsers = new ArrayList<>();
                    ListItemAdapter adapter = new ListItemAdapter(getActivity(), arrayOfUsers);
                    listView.setAdapter(adapter);

                    JSONArray jsonArray = getJsonArray(query.getText().toString());
                    ArrayList<ListItem> newItems;
                    if (jsonArray != null) {
                        newItems = ListItem.fromJson(jsonArray);
                        adapter.addAll(newItems);
                        View keyboardView = getActivity().getCurrentFocus();
                        if (keyboardView != null) {
                            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(keyboardView.getWindowToken(), 0);
                        }
                    } else {
                        Toast.makeText(getContext().getApplicationContext(), "No matches found!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        return view;
    }

    private JSONArray getJsonArray(String params) {
        String error = ""; // string field
        String loginIdUrl = "https://agrapropertytax.com/doortodoor/mobile_api.php?action=searchhouse&q=" + params;
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

                //BufferedReader reader = new BufferedReader(new InputStreamReader(
                //                        in, "iso-8859-1"), 8);
                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        in, "utf-8"), 8);
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                in.close();
                result = Html.fromHtml(sb.toString()).toString();
            } else {
                error += resCode;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            JSONArray jsonArr = new JSONArray(result);
            return jsonArr;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}

