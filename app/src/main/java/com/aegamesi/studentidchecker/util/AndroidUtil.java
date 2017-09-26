package com.aegamesi.studentidchecker.util;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.content.res.AppCompatResources;
import android.view.MenuItem;
import android.widget.Toast;

import com.aegamesi.studentidchecker.R;

import java.io.File;

public class AndroidUtil {
	public static void setMenuItemIcon(Context context, MenuItem item, int drawable) {
		Drawable settingsIcon = DrawableCompat.wrap(AppCompatResources.getDrawable(context, drawable));
		DrawableCompat.setTint(settingsIcon, Color.WHITE);
		item.setIcon(settingsIcon);
	}

	public static void shareFile(Context c, File file, String type, int prompt) {
		if (file != null) {
			Uri uri = FileProvider.getUriForFile(c, "com.aegamesi.studentidchecker.fileprovider", file);
			Intent sendIntent = new Intent();
			sendIntent.setAction(Intent.ACTION_SEND);
			sendIntent.putExtra(Intent.EXTRA_STREAM, uri);
			sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
			sendIntent.setType(type);
			c.startActivity(Intent.createChooser(sendIntent, c.getString(prompt)));
		} else {
			Toast.makeText(c, R.string.error, Toast.LENGTH_LONG).show();
		}
	}

	public static boolean haveCameraPermission(Context context) {
		if (Build.VERSION.SDK_INT < 23) {
			return true;
		}
		return context.checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
	}
}
