package com.android.ezepaymentsapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.graphics.BitmapCompat;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.eze.api.EzeAPI;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

public class PayOptionsActivity extends Activity implements View.OnClickListener {

    private final int REQUEST_CODE_PREPARE = 10002;
    private final int REQUEST_CODE_SALE_TXN = 10005;
    private final int REQUEST_CODE_CASH_TXN = 10008;
    private final int REQUEST_CODE_QR_CODE_PAY = 10019;
    private final int REQUEST_CODE_PRINT_RECEIPT = 10022;
    private final int REQUEST_CODE_PRINT_BITMAP = 10023;

    private String strTxnId = null;
    private String name, contactNo, torrent, charge, address, qrCode, period, periodNumbers, totalAmount;
    private RadioGroup paymentRadioGroup;

    // todo change values here
    public static final String MERCHANT_NAME_VALUE = "NAGAR_NIGAM_AGRA";
    public static final String API_KEY_VALUE = "44da1040-5309-45a7-9ac8-c76bf756d2e1";
    public static final String USER_NAME_VALUE = "7300740645";
    public static final String APP_MODE_VALUE = "DEMO";
    private final int REQUEST_CODE_INITIALIZE = 10001;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_options);
        doInitializeEzeTap();
        initWidgets();
    }

    @Override
    public void onBackPressed() {
        Intent intentNext = new Intent(this,
                SearchBoxActivity.class);
        intentNext.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK |Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intentNext);
    }

    private JSONObject buildRequest() {

        if(torrent == null || name == null || contactNo == null || charge == null){
            Toast.makeText(this, "Transaction timed out. Start a new transaction", Toast.LENGTH_SHORT).show();
            finish();
        }

        try {
            JSONObject jsonRequest = new JSONObject();
            JSONObject jsonOptionalParams = new JSONObject();
            JSONObject jsonReferences = new JSONObject();
            JSONObject jsonCustomer = new JSONObject();

            jsonCustomer.put("name", name);
            jsonCustomer.put("mobileNo", contactNo);
//            jsonCustomer.put("email", name);

            Random random = new Random();
            //Building References Object
            jsonReferences.put("reference1", String.format("%04d", random.nextInt(10000)));
            jsonReferences.put("reference2", torrent);

            //Passing Additional References
            JSONArray array = new JSONArray();
            array.put("addRef1");
            array.put("addRef2");
            jsonReferences.put("additionalReferences", array);

            //Building Optional params Object
            //Cannot have amount cashback in cash transaction.
            jsonOptionalParams.put("amountCashback","00.00");
            jsonOptionalParams.put("amountTip","00.00");
            jsonOptionalParams.put("references",jsonReferences);
            jsonOptionalParams.put("customer",jsonCustomer);

            //Building final request object
            jsonRequest.put("amount", Double.parseDouble(charge));
            jsonRequest.put("options", jsonOptionalParams);

            return jsonRequest;
        }catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.continueButton:
                onContinueButtonPressed();
                break;
            case R.id.backImage:
                Intent intentNext = new Intent(this,
                        SearchBoxActivity.class);
                intentNext.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK |Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intentNext);
                break;
            default:
                break;
        }
    }

    public void onContinueButtonPressed(){
        int selectedId = paymentRadioGroup.getCheckedRadioButtonId();
        RadioButton radioButton = (RadioButton) findViewById(selectedId);
        if(selectedId == -1) {
            Toast.makeText(this,"Please select a payment option", Toast.LENGTH_SHORT).show();
        } else {
            switch (radioButton.getId()) {
                case R.id.payCash:
                    doCashTxn(buildRequest());
                    break;
                case R.id.payCard:
                    doPrepareDeviceEzeTap();
                    doSaleTxn(buildRequest());
                    break;
                case R.id.payQr:
                    doQrCodePayTxn(buildRequest());
                    break;
                default:
                    Toast.makeText(this,"Please select a payment option" + selectedId, Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    private void doPrepareDeviceEzeTap() {
        EzeAPI.prepareDevice(this, REQUEST_CODE_PREPARE);
    }

    private void doSaleTxn(JSONObject jsonRequest) {
        try {
            jsonRequest.put("mode", "SALE");
            EzeAPI.cardTransaction(this, REQUEST_CODE_SALE_TXN, jsonRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void doQrCodePayTxn(JSONObject jsonRequest) {
        EzeAPI.qrCodeTransaction(this, REQUEST_CODE_QR_CODE_PAY, jsonRequest);
    }

    private void doCashTxn(JSONObject jsonRequest) {
        EzeAPI.cashTransaction(this, REQUEST_CODE_CASH_TXN, jsonRequest);
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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        Log.d("SampleAppLogs", "requestCode = " + requestCode + "resultCode = " + resultCode);
        try {
            if (intent != null && intent.hasExtra("response")) {
                Log.d("SampleAppLogs", intent.getStringExtra("response"));
            }
            switch (requestCode) {
                case REQUEST_CODE_CASH_TXN:
                case REQUEST_CODE_SALE_TXN:
                case REQUEST_CODE_QR_CODE_PAY:

                    if (resultCode == RESULT_OK) {
                        JSONObject response = new JSONObject(intent.getStringExtra("response"));
                        response = response.getJSONObject("result");
                        String date =  response.getJSONObject("receipt").getString("receiptDate");
                        response = response.getJSONObject("txn");
                        strTxnId = response.getString("txnId");
                        if (strTxnId != null && !strTxnId.equals("null")) {
                            Toast.makeText(this, "Transaction Successful! Printing receipt...", Toast.LENGTH_LONG).show();
                            printBitmap(date, response.getString("paymentMode"));
                            HashMap<String , String> postDataParams = new HashMap<String, String>();
                            postDataParams.put("torrent", torrent);
                            postDataParams.put("bank_transaction_id", strTxnId);
                            postDataParams.put("amount", charge);
//                            postDataParams.put("paid_monthyear", periodNumbers);
//                            postDataParams.put("payment_type", response.getString("paymentMode"));
//                            postDataParams.put("username", getUsername(getParams()));
                            JSONObject rr =
                                    sendConfirmationRequest("https://agrapropertytax.com/doortodoor/mobile_api.php?action=save_payment&torrent="+torrent
                                    +"&bank_transaction_id="+strTxnId+"&amount="+charge+"&paid_monthyear="+periodNumbers+
                                    "&payment_type="+response.getString("paymentMode").toLowerCase()+"&username="+getUsername(getParams()));
                            Log.d("TAG", "onActivityResult: sendConfirmationRequest " + rr.toString()); // todo check params
                        } else {
                            Toast.makeText(this, "Incorrect txn Id, please make a Txn.", Toast.LENGTH_SHORT).show();
                        }

                        Intent intentNext = new Intent(this,
                                SearchBoxActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK |Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                        startActivity(intentNext);
                    } else if (resultCode == RESULT_CANCELED) {
                        JSONObject response = new JSONObject(intent.getStringExtra("response"));
                        response = response.getJSONObject("error");
                        String errorCode = response.getString("code");
                        String errorMessage = response.getString("message");
                        Toast.makeText(this, "Transaction Failed! " + errorMessage, Toast.LENGTH_LONG).show();
                        Intent intentNext = new Intent(this,
                                SearchBoxActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK |Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intentNext);
                    }
                    break;
                case REQUEST_CODE_PREPARE:
                    if (resultCode == RESULT_OK) {
                        JSONObject response = new JSONObject(intent.getStringExtra("response"));
                        response = response.getJSONObject("result");
                        Log.d("SampleAppLogs", "onActivityResult: device prepared : " + response);
                    } else if (resultCode == RESULT_CANCELED) {
                        JSONObject response = new JSONObject(intent.getStringExtra("response"));
                        response = response.getJSONObject("error");
                        String errorCode = response.getString("code");
                        String errorMessage = response.getString("message");
                        Log.d("SampleAppLogs", "onActivityResult: device failed to prepare : " + errorMessage);
                    }
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getUsername(String s) {
        // get data between equals and &
        return s.substring(s.indexOf("=") + 1, s.indexOf("&"));
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

    private JSONObject sendConfirmationRequest(String params) {
        String error = ""; // string field
        String loginIdUrl = params;
        Log.d("TAG", "sendConfirmationRequest: params" + loginIdUrl);
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

/*
    public String sendConfirmationRequest(String requestURL) {
        URL url;
        String response = "";
        try {
            url = new URL(requestURL);
            Log.d("TAG", "sendConfirmationRequest: url" + url.toString());

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(15000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(getPostDataString(postDataParams));

            writer.flush();
            writer.close();
            os.close();
            int responseCode=conn.getResponseCode();

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                String line;
                BufferedReader br=new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((line=br.readLine()) != null) {
                    response+=line;

                    Log.e("Res:", response);
                }
            }
            else {
                response="";

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return response;
    }
*/

    private String getPostDataString(HashMap<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for(Map.Entry<String, String> entry : params.entrySet()){
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }

        return result.toString();
    }

    private Bitmap getBitmapFromView(View view) {
        //320*570
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        Drawable bgDrawable =view.getBackground();
        if (bgDrawable!=null) {
            bgDrawable.draw(canvas);
        } else{
            canvas.drawColor(Color.WHITE);
        }
        view.draw(canvas);
        return returnedBitmap;
    }

    public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        bm.recycle();
        return resizedBitmap;
    }

    public Bitmap toGrayScale(Bitmap bmpOriginal) {

        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();

        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        bmpOriginal.recycle();
        return bmpGrayscale;
    }

    private void printBitmap(String date, String paymentMode) {

        LinearLayout parent = findViewById(R.id.receiptParent);
        TextView dateTv = findViewById(R.id.receiptDate);
        TextView nameTv = findViewById(R.id.receiptName);
        TextView number = findViewById(R.id.receiptNumber);
        TextView totalAmountTv = findViewById(R.id.receiptTotalAmount);
        TextView paidAmount = findViewById(R.id.receiptPaidAmount);
        TextView pendingAmount = findViewById(R.id.receiptPendingAmount);
        TextView periodTv = findViewById(R.id.receiptPeriod);
        TextView modeOfPayment = findViewById(R.id.receiptModeOfPayment);
        ImageView imageView = findViewById(R.id.resultt);
        TextView orderDetails = findViewById(R.id.orderDetails);
        LinearLayout paymentParent = findViewById(R.id.paymentParent);

        String time = date.split("T")[1].split(Pattern.quote("+"))[0];
        date = date.split("T")[0];
        dateTv.setText(date + "/" + time);
        nameTv.setText(name);
        number.setText(contactNo);
        paidAmount.setText(charge);
        periodTv.setText(period);
        modeOfPayment.setText(paymentMode);
        totalAmountTv.setText(totalAmount);
        pendingAmount.setText(String.valueOf(Integer.parseInt(totalAmount) - Integer.parseInt(charge)));

        JSONObject jsonRequest = new JSONObject();
        JSONObject jsonImageObj = new JSONObject();

        try {
            doInitializeEzeTap();
            Bitmap bmap = getResizedBitmap(toGrayScale(getBitmapFromView(parent)),320,570);
            int bitmapByteCount= BitmapCompat.getAllocationByteCount(bmap);
            Log.d("TAG", "printBitmap: bitmapByteCount size" + bitmapByteCount);


            paymentParent.setVisibility(View.INVISIBLE);
            imageView.setImageBitmap(bmap);
//            imageView.buildDrawingCache();
//            Bitmap bmapp = imageView.getDrawingCache();
            orderDetails.setText("Receipt Details");

            String encodedImageData = getEncoded64ImageStringFromBitmap(bmap);
            saveEncodedImageData("imageData", encodedImageData, this);
            // Building Image Object
            jsonImageObj.put("imageData", encodedImageData
//                    "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"
            );
            jsonImageObj.put("imageType", "PNG");
            jsonImageObj.put("height", "");// optional
            jsonImageObj.put("weight", "");// optional
            jsonRequest.put("image",
                    jsonImageObj); // Pass this attribute when you have a valid captured signature image
            EzeAPI.printBitmap(this, REQUEST_CODE_PRINT_BITMAP, jsonRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void saveEncodedImageData(String key, String value, Activity activity) {
        if (value != null && value.length() > 0) {
            SharedPreferences sharedPreferences = activity.getSharedPreferences("app.db", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(key, value);
            editor.commit();
        }
    }

    public String getEncoded64ImageStringFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
        byte[] byteFormat = stream.toByteArray();
        return Base64.encodeToString(byteFormat, Base64.NO_WRAP);
    }

    private void printReceipt() {
        if (strTxnId != null) {
            EzeAPI.printReceipt(this, REQUEST_CODE_PRINT_RECEIPT,
                    strTxnId);
        } else {
            Toast.makeText(this, "Incorrect txn Id, please make a Txn.", Toast.LENGTH_SHORT).show();
        }
    }

    private void initWidgets() {
        Intent i = getIntent();
        name = i.getStringExtra("name");
        contactNo = i.getStringExtra("contactNo");
        charge = i.getStringExtra("charge");
        torrent = i.getStringExtra("torrent");
        address = i.getStringExtra("address");
        qrCode = i.getStringExtra("qrCode");
        period = i.getStringExtra("month_year");
        periodNumbers = i.getStringExtra("month_year_numbers");
        Log.d("TAG", "initWidgets: " + periodNumbers);
        totalAmount = i.getStringExtra("total_amount");

        Button continueButton = (Button) findViewById(R.id.continueButton);
        paymentRadioGroup = (RadioGroup) findViewById(R.id.paymentRadioGroup);
        TextView nameTv = (TextView) findViewById(R.id.paymentNameTv);
        TextView contactNoTv = (TextView) findViewById(R.id.paymentContactNoTv);
        TextView addressTv = (TextView) findViewById(R.id.paymentAddressTv);
        TextView periodTv = (TextView) findViewById(R.id.paymentPeriodTv);
        TextView torrentTv = (TextView) findViewById(R.id.paymentTorrentTv);
        TextView qrCodeTv = (TextView) findViewById(R.id.paymentQrCodeTv);
        TextView totalChargesTv = (TextView) findViewById(R.id.paymentTotalChargesTv);

        nameTv.setText("Name: "+name);
        contactNoTv.setText("Contact No: "+contactNo);
        addressTv.setText("Address: "+ address);
        periodTv.setText("Period: "+ period);
        torrentTv.setText("Torrent: "+ torrent);
        qrCodeTv.setText("QR Code: "+ qrCode);
        totalChargesTv.setText("Total Charges: Rs "+ charge);

        continueButton.setOnClickListener(this);
        ImageView backImage = (ImageView) findViewById(R.id.backImage);
        backImage.setOnClickListener(this);
    }
}
