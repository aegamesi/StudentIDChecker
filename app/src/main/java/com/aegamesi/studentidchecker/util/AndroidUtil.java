package com.aegamesi.studentidchecker.util;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.content.res.AppCompatResources;
import android.view.MenuItem;

public class AndroidUtil {
	public static void setMenuItemIcon(Context context, MenuItem item, int drawable) {
		Drawable settingsIcon = DrawableCompat.wrap(AppCompatResources.getDrawable(context, drawable));
		DrawableCompat.setTint(settingsIcon, Color.WHITE);
		item.setIcon(settingsIcon);
	}
}
