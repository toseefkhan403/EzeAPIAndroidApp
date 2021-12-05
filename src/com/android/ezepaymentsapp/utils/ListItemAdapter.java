package com.android.ezepaymentsapp.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.ezepaymentsapp.PayOptionsActivity;
import com.android.ezepaymentsapp.R;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

public class ListItemAdapter extends ArrayAdapter<ListItem> {
    private final Context context;
    public ListItemAdapter(Context context, ArrayList<ListItem> items) {
        super(context, 0, items);
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ListItem item = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_list_items, parent, false);
        }

        CardView cv = convertView.findViewById(R.id.cardView);
        if(position%2==1){
            cv.setCardBackgroundColor(context.getResources().getColor(R.color.skyBlue));
        } else {
            cv.setCardBackgroundColor(context.getResources().getColor(R.color.defaultWhite));
        }
        TextView tvName = (TextView) convertView.findViewById(R.id.nameTv);
        TextView tvWard = (TextView) convertView.findViewById(R.id.wardTv);
        TextView tvMohalla = (TextView) convertView.findViewById(R.id.mohallaTv);
        TextView tvHouseNo = (TextView) convertView.findViewById(R.id.houseNoTv);
        TextView tvQrcode = (TextView) convertView.findViewById(R.id.qrCodeTv);
        TextView tvContactNo = (TextView) convertView.findViewById(R.id.contactNoTv);
        TextView tvTorrent = (TextView) convertView.findViewById(R.id.torrentTv);
        TextView tvUniqueId = (TextView) convertView.findViewById(R.id.uniqueIdTv);
        TextView tvMonthlyCharge = (TextView) convertView.findViewById(R.id.monthlyChargeTv);
        TextView tvMonthYear = (TextView) convertView.findViewById(R.id.monthYearTv);
//        TextView currentMonthTv = (TextView) convertView.findViewById(R.id.currentMonthTv);
        Button payNow = (Button) convertView.findViewById(R.id.collectButton);

        tvName.setText(item.getName());
        tvWard.setText(item.getWard());
        tvMohalla.setText(item.getMohalla());
        tvHouseNo.setText(item.getHouse_no());
        tvQrcode.setText(item.getQrcode());
        tvContactNo.setText(item.getContact_no());
        tvTorrent.setText(item.getTorrent());
        tvUniqueId.setText(item.getUnique_id());
        tvMonthlyCharge.setText(item.getMonthly_charge());

        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("MM/yyyy", Locale.getDefault());
        String currentDate = df.format(c);

        // todo testing
        if(!item.getMonth_year().equals("/0")) {
//            currentMonthTv.setText(changeDateToChars(item.getMonth_year())+" - "+changeDateToChars(currentDate));
            try {
                Date dateCurrent = df.parse(currentDate);
                Date dateMonthYear = df.parse(item.getMonth_year());
                if(dateMonthYear.after(dateCurrent)) {

                    tvMonthYear.setText(changeDateToChars(item.getMonth_year()));
                    payNow.setText("Pay Rs " + item.getMonthly_charge());
                    payNow.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            Intent i = new Intent(context, PayOptionsActivity.class);
                            i.putExtra("name", item.getName());
                            i.putExtra("charge", item.getMonthly_charge());
                            i.putExtra("contactNo", item.getContact_no());
                            i.putExtra("torrent", item.getTorrent());
                            i.putExtra("qrCode", item.getQrcode());
                            i.putExtra("address", item.getHouse_no()+", "+item.getMohalla());
                            i.putExtra("month_year", changeDateToChars(item.getMonth_year()));
                            i.putExtra("total_amount", item.getMonthly_charge());
                            i.putExtra("month_year_numbers", item.getMonth_year().replace("/",""));

                            context.startActivity(i);
                        }
                    });

                } else if(dateMonthYear.equals(dateCurrent)) {
                    tvMonthYear.setText(changeDateToChars(item.getMonth_year()));
                    payNow.setText("Pay Rs " + item.getMonthly_charge());
                    payNow.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            Intent i = new Intent(context, PayOptionsActivity.class);
                            i.putExtra("name", item.getName());
                            i.putExtra("charge", item.getMonthly_charge());
                            i.putExtra("contactNo", item.getContact_no());
                            i.putExtra("torrent", item.getTorrent());
                            i.putExtra("qrCode", item.getQrcode());
                            i.putExtra("address", item.getHouse_no()+", "+item.getMohalla());
                            i.putExtra("month_year", changeDateToChars(item.getMonth_year()));
                            i.putExtra("total_amount", item.getMonthly_charge());
                            i.putExtra("month_year_numbers", item.getMonth_year().replace("/",""));

                            context.startActivity(i);
                        }
                    });
                } else {
                    tvMonthYear.setText(changeDateToChars(item.getMonth_year()) + " - " + changeDateToChars(currentDate));
                    payNow.setText("Select Time Period");
                    payNow.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            openPopUp(item, currentDate);
                        }
                    });
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
//            currentMonthTv.setText(changeDateToChars(currentDate));
            tvMonthYear.setText(changeDateToChars(currentDate));
            payNow.setText("Pay Rs " + item.getMonthly_charge());
            payNow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Intent i = new Intent(context, PayOptionsActivity.class);
                    i.putExtra("name", item.getName());
                    i.putExtra("charge", item.getMonthly_charge());
                    i.putExtra("contactNo", item.getContact_no());
                    i.putExtra("torrent", item.getTorrent());
                    i.putExtra("qrCode", item.getQrcode());
                    i.putExtra("address", item.getHouse_no()+", "+item.getMohalla());
                    i.putExtra("month_year", changeDateToChars(currentDate));
                    i.putExtra("total_amount", item.getMonthly_charge());
                    i.putExtra("month_year_numbers", currentDate.replace("/",""));

                    context.startActivity(i);
                }
            });
        }

        return convertView;
    }

    private void openPopUp(ListItem item, String currentDate) {
        AlertDialog dialog;

        final ArrayList<String> list = getCheckBoxListFromMonthYear(item.getMonth_year());
        String[] items = new String[list.size()];
        String[] itemsNumbers = new String[list.size()];

        for (int i =0; i<list.size(); i++) {
            items[i] = changeDateToChars(list.get(i));
            itemsNumbers[i] = list.get(i);
        }

        final ArrayList<Integer> itemsSelected = new ArrayList();
        TextView title = new TextView(context);
        title.setText("Total Amount To Pay: Rs 0");
        title.setPadding(10, 25, 10, 10);
        title.setGravity(Gravity.CENTER);
        title.setTextColor(Color.BLACK);
        title.setTextSize(20);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Select Time Period For Payment: ");

        builder.setMultiChoiceItems(items, null,
                new DialogInterface.OnMultiChoiceClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int selectedItemId,
                                        boolean isSelected) {
                        if (isSelected) {
                            itemsSelected.add(selectedItemId);
                        } else if (itemsSelected.contains(selectedItemId)) {
                            itemsSelected.remove(Integer.valueOf(selectedItemId));
                        }
                        title.setText("Total Amount To Pay: Rs. " + String.valueOf(Integer.parseInt(item.getMonthly_charge())*itemsSelected.size()));
                    }
                })
                .setCustomTitle(title)
                .setPositiveButton("Pay Now", null)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        dialog = builder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Collections.sort(itemsSelected);
                if(checkContinuous(itemsSelected)) {

                    Intent i = new Intent(context, PayOptionsActivity.class);
                    i.putExtra("name", item.getName());
                    i.putExtra("torrent", item.getTorrent());
                    i.putExtra("qrCode", item.getQrcode());
                    i.putExtra("address", item.getHouse_no()+", "+item.getMohalla());
                    i.putExtra("charge", String.valueOf(Integer.parseInt(item.getMonthly_charge())*itemsSelected.size()));
                    i.putExtra("contactNo", item.getContact_no());
                    i.putExtra("total_amount", String.valueOf(list.size()*Integer.parseInt(item.getMonthly_charge())));
                    if(itemsSelected.size() == 1){
                        i.putExtra("month_year", items[itemsSelected.get(0)]);
                        i.putExtra("month_year_numbers", itemsNumbers[itemsSelected.get(0)].replace("/",""));
                    } else {
                        i.putExtra("month_year", items[itemsSelected.get(0)] +
                                " - " + items[itemsSelected.get(itemsSelected.size()-1)]);
                        // itemsselected to
                        i.putExtra("month_year_numbers", getNumbersString(itemsNumbers,itemsSelected));
                    }
                    context.startActivity(i);
                    Toast.makeText(context, "Proceeding to Payment Page...", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                } else {
                    Toast.makeText(context, "Please select consecutive time period only", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private String getNumbersString(String[] itemsNumbers, ArrayList<Integer> itemsSelected) {
        StringBuilder returnString = new StringBuilder();
        for (int i = 0; i < itemsSelected.size(); i++) {
            returnString.append(itemsNumbers[itemsSelected.get(i)]);

            if(i != itemsSelected.size()-1) returnString.append(",");
        }
        return String.valueOf(returnString).replace("/","");
    }

    private boolean checkContinuous(ArrayList<Integer> list) {
        if(list.isEmpty()) return false;
        for (int i = 0; i < list.size() - 1; i++) {
            if (list.get(i) != list.get(i + 1) - 1) {
                return false;
            }
        }
        return true;
    }

    private ArrayList<String> getCheckBoxListFromMonthYear(String month_year) {

        ArrayList<String> list = new ArrayList();

        DateFormat formater = new SimpleDateFormat("MM/yyyy");

        Calendar beginCalendar = Calendar.getInstance();
        Calendar finishCalendar = Calendar.getInstance();

        // todo testing
        String date1 = month_year;
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("MM/yyyy", Locale.getDefault());
        String date2 = df.format(c); // current date

        try {
            beginCalendar.setTime(formater.parse(date1));
            finishCalendar.setTime(formater.parse(date2));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        while (beginCalendar.before(finishCalendar)) {
            // add one month to date per loop
            String date = formater.format(beginCalendar.getTime()).toUpperCase();
            list.add(date);
            beginCalendar.add(Calendar.MONTH, 1);
        }

        list.add(date2);

        return list;
    }

    private String changeDateToChars(String time) {
        String inputPattern = "MM/yyyy";
        String outputPattern = "MMM-yyyy";
        SimpleDateFormat inputFormat = new SimpleDateFormat(inputPattern);
        SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern);

        Date date = null;
        String str = null;

        try {
            date = inputFormat.parse(time);
            str = outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return str;
    }
}
