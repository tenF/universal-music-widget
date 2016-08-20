package com.voiche.universalmusicwidget;

import android.R.color;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.view.View;
import android.widget.RemoteViews;

import com.voiche.universalmusicwidget.R.drawable;

public class UniversalMusicWidget extends AppWidgetProvider {
	static RemoteViews views, albumviews, buttonviews, infoviews;
	static AppWidgetManager manager;
	static ComponentName appWidgetIds;
	static int buttonType = 0;
	static int lsm = 0;
	static boolean adjustui = false;
	static Context context;
	static SharedPreferences shared;
	static String daa = "true";
	static boolean playing = false;

	@Override
	public void onReceive(Context context, Intent intent) {
		// ystem.out.println("A"+intent.getAction());
		shared = context.getSharedPreferences("settings", context.MODE_PRIVATE);

		boolean wait = shared.getBoolean("wait", true);

		if (!(intent.getAction().equals(
				"android.appwidget.action.APPWIDGET_DELETED") || intent
				.getAction().equals(
						"android.appwidget.action.APPWIDGET_DISABLED"))
				&& wait != true) {
			// context.sendBroadcast(new Intent()
			// .setAction("com.voiche.universalmusicwidget.UPDATE"));
			// SharedPreferences.Editor editor = shared.edit();
			// editor.putBoolean("update", true);
			// editor.commit();
			if (Build.VERSION.SDK_INT < 18)
				context.startService(new Intent(context, MusicService.class));
			else if (Build.VERSION.SDK_INT == 18)
				context.startService(new Intent(context, MusicService_v18.class));
			else if (Build.VERSION.SDK_INT < 21)
				context.startService(new Intent(context, MusicService_v19.class));
			else
				context.startService(new Intent(context, MusicService_v21.class));

			context.sendBroadcast(new Intent()
					.setAction("com.voiche.universalmusicwidget.UPDATE"));
		}

		super.onReceive(context, intent);
	}

	@Override
	public void onDeleted(Context context, int[] appWidgetId) {
		super.onDeleted(context, appWidgetId);
		/*
		 * Intent intnt = new Intent();
		 * intnt.setAction("com.voiche.universalmusicwidget.STATE_CHANGED");
		 * intnt.putExtra("STATE", "SMALL_DELETED");
		 * context.sendBroadcast(intnt); if (Build.VERSION.SDK_INT < 18)
		 * context.stopService(new Intent(context, MusicService.class)); else if
		 * (Build.VERSION.SDK_INT == 18) context.stopService(new Intent(context,
		 * MusicService_v18.class)); else context.stopService(new
		 * Intent(context, MusicService_v19.class));
		 */
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		// TODO Auto-generated method stub

		super.onUpdate(context, appWidgetManager, appWidgetIds);
	}

	public boolean isActive() {
		try {
			int ids[] = manager.getAppWidgetIds(appWidgetIds);
			if (ids.length > 0)
				return true;
		} catch (Exception e) {

		}
		return false;
	}

	public void create(Context cntxt) {
		context = cntxt;

		shared = context.getSharedPreferences("settings", context.MODE_PRIVATE);

		views = new RemoteViews(context.getApplicationContext()
				.getPackageName(), R.layout.main);
		buttonviews = new RemoteViews(context.getApplicationContext()
				.getPackageName(), R.layout.main);
		infoviews = new RemoteViews(context.getApplicationContext()
				.getPackageName(), R.layout.main);
		albumviews = new RemoteViews(context.getApplicationContext()
				.getPackageName(), R.layout.main);
		appWidgetIds = new ComponentName(context.getApplicationContext(),
				UniversalMusicWidget.class);
		manager = AppWidgetManager.getInstance(context.getApplicationContext());

		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			public void run() {
				adjustUI();
			}
		}, 500);

		buttonListeners();
	}

	public void updateUI() {
		System.out.println("AdjustUI");
		adjustUI();
		updateButtons(playing);
	}

	public void updateInfo(String title, String artist, String album) {
		adjustUI();
		infoviews = new RemoteViews(context.getApplicationContext()
				.getPackageName(), R.layout.main);
		infoviews.setTextViewText(R.id.track, title);
		infoviews.setTextViewText(R.id.artist, artist);
		// infoviews.setTextViewText(R.id.album, album);
		manager.updateAppWidget(appWidgetIds, infoviews);
		// buttonListeners();
	}

	public void updateAlbumArt(Bitmap art) {
		if (!daa.equals("true"))
			return;
		albumviews = new RemoteViews(context.getApplicationContext()
				.getPackageName(), R.layout.main);
		albumviews.setImageViewBitmap(R.id.imageView1, art);
		manager.updateAppWidget(appWidgetIds, albumviews);
		buttonListeners();
	}

	private void buttonListeners() {

		PendingIntent Prev = PendingIntent.getBroadcast(context, 0, new Intent(
				"com.voiche.universalmusicwidget.PREV"), 0);
		PendingIntent PlayPause = PendingIntent.getBroadcast(context, 0,
				new Intent("com.voiche.universalmusicwidget.PLAYPAUSE"), 0);
		PendingIntent Next = PendingIntent.getBroadcast(context, 0, new Intent(
				"com.voiche.universalmusicwidget.NEXT"), 0);
		PendingIntent OpenPlayer = PendingIntent.getBroadcast(context, 0,
				new Intent("com.voiche.universalmusicwidget.OPENPLAYER"), 0);
		PendingIntent OpenSettings = PendingIntent.getBroadcast(context, 0,
				new Intent("com.voiche.universalmusicwidget.OPENSETTINGS"), 0);

		views.setOnClickPendingIntent(R.id.mainlayout, OpenSettings);
		views.setOnClickPendingIntent(R.id.imageView1, OpenPlayer);
		views.setOnClickPendingIntent(R.id.prev_button, Prev);
		views.setOnClickPendingIntent(R.id.play_button, PlayPause);
		views.setOnClickPendingIntent(R.id.next_button, Next);

		manager.updateAppWidget(appWidgetIds, views);
	}

	@SuppressLint("NewApi")
	private void adjustUI() {

		boolean update = shared.getBoolean("update", false);
		if (adjustui != true)
			update = true;

		if (update != true)
			return;

		adjustui = true;
		// ystem.out.println("adjusted");

		views = new RemoteViews(context.getApplicationContext()
				.getPackageName(), R.layout.main);
		buttonviews = new RemoteViews(context.getApplicationContext()
				.getPackageName(), R.layout.main);

		String bgrcolor = (shared.getString("bgrcolor", "-1509949440"));// (16777215)-1509949440
																		// for
																		// normal
		String fontcolor = shared.getString("fontcolor", "-1");
		String tnfontcolor = shared.getString("tnfontcolor", "-1");
		String fontsize = shared.getString("fontsize", "12");// (14)12 for
																// normal
		String tnfontsize = shared.getString("tnfontsize", "14");// (16)14 for
																	// normal
		String buttons = (shared.getString("buttons", "1"));// (2)1 for normal
		daa = shared.getString("daa", "true");
		String dan = shared.getString("dan", "false"); // false for normal
		String lsms = shared.getString("lsm", "false");
		int margins = shared.getInt("margins", 0);
		if (Build.VERSION.SDK_INT >= 16)
			views.setViewPadding(R.id.mainlayout, 0, margins, 0, margins);

		buttonType = Integer.parseInt(buttons);

		if (buttonType == 2) {
			views.setImageViewResource(R.id.PrevIV, color.transparent);
			views.setImageViewResource(R.id.NextIV, color.transparent);
			buttonviews.setImageViewResource(R.id.PlayPauseIV,
					color.transparent);
			views.setInt(R.id.prev_button, "setBackgroundResource",
					drawable.prev_xs_xml);
			views.setInt(R.id.next_button, "setBackgroundResource",
					drawable.next_xs_xml);
			updateButtons(false);
		} else if (buttonType == 1) {
			views.setImageViewResource(R.id.PrevIV, drawable.prev);
			views.setImageViewResource(R.id.NextIV, drawable.next);
			views.setInt(R.id.play_button, "setBackgroundResource",
					drawable.button_background);
			views.setInt(R.id.prev_button, "setBackgroundResource",
					drawable.button_background);
			views.setInt(R.id.next_button, "setBackgroundResource",
					drawable.button_background);
		} else if (buttonType == 3) {
			views.setImageViewResource(R.id.PrevIV, color.transparent);
			views.setImageViewResource(R.id.NextIV, color.transparent);
			buttonviews.setImageViewResource(R.id.PlayPauseIV,
					color.transparent);
			views.setInt(R.id.prev_button, "setBackgroundResource",
					drawable.prev_filled_xml);
			views.setInt(R.id.next_button, "setBackgroundResource",
					drawable.next_filled_xml);
			updateButtons(false);
		}
		if (!daa.equals("true")) {
			views.setViewVisibility(R.id.imageView1, View.GONE);
			views.setViewVisibility(R.id.iV, View.GONE);
		} else {
			views.setViewVisibility(R.id.imageView1, View.VISIBLE);
			views.setViewVisibility(R.id.iV, View.VISIBLE);
		}
		/*
		 * if (!dan.equals("true")) views.setViewVisibility(R.id.album,
		 * View.GONE); else views.setViewVisibility(R.id.album, View.VISIBLE);
		 */
		if (!lsms.equals("false"))
			lsm = 2;
		else
			lsm = 1;

		views.setInt(R.id.backgroundIV, "setColorFilter",
				Integer.parseInt(bgrcolor));
		if (Build.VERSION.SDK_INT >= 16)
			views.setInt(R.id.backgroundIV, "setImageAlpha",
					Color.alpha(Integer.parseInt(bgrcolor)));
		else
			views.setInt(R.id.backgroundIV, "setAlpha",
					Color.alpha(Integer.parseInt(bgrcolor)));
		views.setTextColor(R.id.track, Integer.parseInt(tnfontcolor));
		views.setTextColor(R.id.artist, Integer.parseInt(fontcolor));
		// views.setTextColor(R.id.album, Integer.parseInt(fontcolor));
		views.setFloat(R.id.track, "setTextSize", Float.parseFloat(tnfontsize));
		views.setFloat(R.id.artist, "setTextSize", Float.parseFloat(fontsize));
		// views.setFloat(R.id.album, "setTextSize",
		// Float.parseFloat(fontsize));

		buttonListeners();
		// ystem.out.println("adjustUIUpdate");

		SharedPreferences.Editor editor = shared.edit();
		editor.putBoolean("update", false);
		editor.commit();
	}

	public void updateButtons(boolean bool) {
		playing = bool;

		if (buttonType == 0) {
			String buttons = (shared.getString("buttons", "1"));// (2)1-for-normal
			buttonType = Integer.parseInt(buttons);
		}

		if (buttonType == 2) {
			if (bool == true)
				buttonviews.setInt(R.id.play_button, "setBackgroundResource",
						drawable.pause_xs_xml);
			else
				buttonviews.setInt(R.id.play_button, "setBackgroundResource",
						drawable.play_xs_xml);
		} else if (buttonType == 1) {
			if (bool == true)
				buttonviews.setImageViewResource(R.id.PlayPauseIV,
						drawable.pause);
			else
				buttonviews.setImageViewResource(R.id.PlayPauseIV,
						drawable.play);
		} else if (buttonType == 3) {
			if (bool == true)
				buttonviews.setInt(R.id.play_button, "setBackgroundResource",
						drawable.pause_filled_xml);
			else
				buttonviews.setInt(R.id.play_button, "setBackgroundResource",
						drawable.play_filled_xml);
		}
		if (lsm == 0) {
			String lsms = shared.getString("lsm", "false");
			if (!lsms.equals("false"))
				lsm = 2;
			else
				lsm = 1;
		}
		if (lsm == 2 && bool != true)
			buttonviews.setViewVisibility(R.id.mainlayout, View.INVISIBLE);
		else if (lsm == 2)
			buttonviews.setViewVisibility(R.id.mainlayout, View.VISIBLE);
		manager.updateAppWidget(appWidgetIds, buttonviews);
		buttonListeners();
	}
}
