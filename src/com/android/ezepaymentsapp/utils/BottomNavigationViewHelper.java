package com.android.ezepaymentsapp.utils;

import android.support.design.internal.BottomNavigationItemView;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.util.Arrays;

public class BottomNavigationViewHelper {
    public static void disableShiftMode(BottomNavigationView view) {
        BottomNavigationMenuView menuView = (BottomNavigationMenuView) view.getChildAt(0);
        try {
            Field shiftingMode = menuView.getClass().getDeclaredField("mShiftingMode");
            Log.d("TAG", "disableShiftMode: "+ Arrays.toString(menuView.getClass().getDeclaredFields()));;

            shiftingMode.setAccessible(true);
            shiftingMode.setBoolean(menuView, false);
            shiftingMode.setAccessible(false);
            for (int i = 0; i < menuView.getChildCount(); i++) {
                BottomNavigationItemView item = (BottomNavigationItemView) menuView.getChildAt(i);
                //noinspection RestrictedApi
                item.setShiftingMode(false);

                // set once again checked value, so view will be updated
                //noinspection RestrictedApi
                item.setChecked(item.getItemData().isChecked());
            }
        } catch (NoSuchFieldException e) {
            Log.e("BNVHelper", "Unable to get shift mode field", e);
        } catch (IllegalAccessException e) {
            Log.e("BNVHelper", "Unable to change value of shift mode", e);
        }
    }

    public static void fixBottomNavigationText(BottomNavigationView bottomNavigationView) {
        for (int i = 0; i < bottomNavigationView.getChildCount(); i++) {
            View item = bottomNavigationView.getChildAt(i);

            if (item instanceof BottomNavigationMenuView) {
                BottomNavigationMenuView menu = (BottomNavigationMenuView) item;

                for (int j = 0; j < menu.getChildCount(); j++) {
                    View menuItem = menu.getChildAt(j);

                    View small = menuItem.findViewById(android.support.design.R.id.smallLabel);
                    if (small instanceof TextView) {
                        ((TextView) small).setLines(2);
                    }
                    View large = menuItem.findViewById(android.support.design.R.id.largeLabel);
                    if (large instanceof TextView) {
                        ((TextView) large).setLines(2);
                    }
                }
            }
        }
    }
}