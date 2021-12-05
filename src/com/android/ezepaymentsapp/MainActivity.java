package com.android.ezepaymentsapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.ezepaymentsapp.R;
import com.eze.api.EzeAPI;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

// login
public class MainActivity extends Activity {

	public static final String MERCHANT_NAME = "merchant";
	public static final String API_KEY = "api_key";
	public static final String USER_NAME = "username";
	public static final String APP_MODE = "appmode";

	// todo change values here
	public static final String MERCHANT_NAME_VALUE = "NAGAR_NIGAM_AGRA";
	public static final String API_KEY_VALUE = "44da1040-5309-45a7-9ac8-c76bf756d2e1";
	public static final String USER_NAME_VALUE = "7300740645";
	public static final String APP_MODE_VALUE = "DEMO";

	static Setting.ConfigHolder config;

	@Override
	public void onBackPressed() {
		askIfCloseApp();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		TypefaceUtil.overrideFont(getApplicationContext(), "SERIF", "fonts/Poppins-Regular.ttf"); // font from assets: "assets/fonts/Roboto-Regular.ttf
		setContentView(R.layout.activity_main);
		RelativeLayout mainParent = (RelativeLayout) findViewById(R.id.mainParent);
		if(loggedIn()){
			Intent intent = new Intent(MainActivity.this,
					SearchBoxActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK |Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
		} else{
			mainParent.setVisibility(View.VISIBLE);
		}

		Button submit = (Button) findViewById(R.id.submit);
		EditText loginID = (EditText) findViewById(R.id.loginId);
		EditText password = (EditText) findViewById(R.id.password);

		if (android.os.Build.VERSION.SDK_INT > 9)
		{
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
			StrictMode.setThreadPolicy(policy);
		}
		submit.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				if(loginID.getText().toString().equals("") ||
						password.getText().toString().equals("")) {
					Toast.makeText(getApplicationContext(), "Please enter username and password", Toast.LENGTH_SHORT).show();
				} else {
					String params = "username=" + loginID.getText().toString() + "&password=" + password.getText().toString();
					JSONObject result = verifyCreds(params);
					Log.d("TAG", "onClick: result" + result);

					try {
						if (result == null || result.getString("response").equals("failed")) {
							Toast.makeText(getApplicationContext(), "Invalid username or password", Toast.LENGTH_SHORT).show();
						} else if (result.getString("response").equals("success")) {
							savePrefs("loginId", params, MainActivity.this);
							Toast.makeText(getApplicationContext(), "Login Success", Toast.LENGTH_SHORT).show();
							Intent intent = new Intent(MainActivity.this,
									SearchBoxActivity.class);
							intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
							startActivity(intent);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});
	}

	private JSONObject verifyCreds(String params) {
		String error = ""; // string field
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

	private boolean loggedIn() {
		return getPrefs("loginId",this) != null;
	}

	private void saveData() {
		savePrefs(MERCHANT_NAME, MERCHANT_NAME_VALUE, this);
		savePrefs(API_KEY, API_KEY_VALUE, this);
		savePrefs(APP_MODE, APP_MODE_VALUE, this);
		savePrefs(USER_NAME, USER_NAME_VALUE, this);
		config = new Setting.ConfigHolder(API_KEY_VALUE, APP_MODE_VALUE, USER_NAME_VALUE, MERCHANT_NAME_VALUE);
	}

	public String getPrefs(String key, Activity activity) {
		SharedPreferences sharedPreferences = activity.getSharedPreferences("app.db", Context.MODE_PRIVATE);
		Map<String, ?> map = (Map<String, ?>) sharedPreferences.getAll();
		String value = null;
		try {
			value = map.get(key).toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return value;
	}

	public static void savePrefs(String key, String value, Activity activity) {
		if (value != null && value.length() > 0) {
			SharedPreferences sharedPreferences = activity.getSharedPreferences("app.db", Context.MODE_PRIVATE);
			SharedPreferences.Editor editor = sharedPreferences.edit();
			editor.putString(key, value);
			editor.commit();
		}
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
