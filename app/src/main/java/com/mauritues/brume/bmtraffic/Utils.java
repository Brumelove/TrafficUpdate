package com.mauritues.brume.bmtraffic;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Spinner;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

class Utils {

    static String getDate() {
        SimpleDateFormat formatter = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
        return formatter.format(Calendar.getInstance().getTime());
    }

    static String getIme() {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return formatter.format(Calendar.getInstance().getTime());
    }

    static ContextWrapper changeLocale(Context ctx, String locale) {

        Resources rs = ctx.getResources();
        Configuration config = rs.getConfiguration();
        Locale l = new Locale(locale);
        Locale.setDefault(l);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(l);
        } else {
            config.locale = l;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            ctx = ctx.createConfigurationContext(config);
        } else {
            ctx.getResources().updateConfiguration(config, ctx.getResources().getDisplayMetrics());
        }
        saveStringToPref(ctx, "lang", locale);
        return new ContextWrapper(ctx);
    }

    static String getLang(Context ctx) {
        return ctx.getSharedPreferences("traffic", Context.MODE_PRIVATE).getString("lang", "en");
    }

    static String getToken(Context ctx) {
        return ctx.getSharedPreferences("traffic", Context.MODE_PRIVATE).getString("token", "");
    }

    static void saveStringToPref(Context ctx, String key, String v) {
        SharedPreferences prefs = ctx.getSharedPreferences("traffic", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putString(key, v);
        edit.apply();
    }

    static void showLanguageDialog(final AppCompatActivity ctx, final LanguageCallback callback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        @SuppressLint("InflateParams") View v = LayoutInflater.from(ctx).inflate(R.layout.language_dialog, null);
        final Spinner languages = v.findViewById(R.id.lang);
        builder.setTitle(R.string.change_app_lang);
        builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String value = languages.getSelectedItem().toString();
                if (value.equalsIgnoreCase("English") ||
                        value.equalsIgnoreCase("Anglais")) {
                    saveStringToPref(ctx, "lang", "en");
                } else if (value.equalsIgnoreCase("French") ||
                        value.equalsIgnoreCase("Français")) {
                    saveStringToPref(ctx, "lang", "fr");
                }
                dialog.dismiss();
                callback.onLanguageChanged();
                Log.e("Dialog", "Confirm pressed");
            }
        });
        builder.setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.e("Dialog", "Close pressed");
                dialog.dismiss();
            }
        });
        builder.setView(v);
        if (!ctx.isFinishing())
            builder.create().show();
    }
}
