package com.voiche.universalmusicwidget;

import android.R.color;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.view.View;
import android.widget.RemoteViews;

import com.voiche.universalmusicwidget.R.drawable;

public class UniversalMusicWidget_simple extends AppWidgetProvider {
	static RemoteViews views, albumviews, buttonviews, infoviews;
	static AppWidgetManager manager;
	static ComponentName appWidgetIds;
	static int buttonType = 0;
	static int lsm = 0;
	static boolean adjustui = false;
	static Context context;
	static SharedPreferences shared;
	static boolean playing = false;

	@Override
	public void onReceive(Context context, Intent intent) {
		// ystem.out.println(intent.getAction());
		shared = context.getSharedPreferences("settings_simple",
				context.MODE_PRIVATE);

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
		 * intnt.putExtra("STATE", "SIMPLE_DELETED");
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
		// ystem.out.println("simpleCreate");
		context = cntxt;

		shared = context.getSharedPreferences("settings_simple",
				context.MODE_PRIVATE);

		views = new RemoteViews(context.getApplicationContext()
				.getPackageName(), R.layout.main_simple);
		buttonviews = new RemoteViews(context.getApplicationContext()
				.getPackageName(), R.layout.main_simple);
		infoviews = new RemoteViews(context.getApplicationContext()
				.getPackageName(), R.layout.main_simple);
		albumviews = new RemoteViews(context.getApplicationContext()
				.getPackageName(), R.layout.main_simple);
		appWidgetIds = new ComponentName(context.getApplicationContext(),
				UniversalMusicWidget_simple.class);
		manager = AppWidgetManager.getInstance(context.getApplicationContext());

		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			public void run() {
				adjustUI();
			}
		}, 500);

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
				new Intent(
						"com.voiche.universalmusicwidget_simple.OPENSETTINGS"),
				0);

		views.setOnClickPendingIntent(R.id.mainlayout, OpenSettings);
		views.setOnClickPendingIntent(R.id.imageView1, OpenPlayer);
		views.setOnClickPendingIntent(R.id.prev_button, Prev);
		views.setOnClickPendingIntent(R.id.play_button, PlayPause);
		views.setOnClickPendingIntent(R.id.next_button, Next);

		manager.updateAppWidget(appWidgetIds, views);
	}

	private void adjustUI() {

		boolean update = shared.getBoolean("update", false);
		if (adjustui != true)
			update = true;

		if (update != true)
			return;

		adjustui = true;
		// ystem.out.println("adjusted");

		views = new RemoteViews(context.getApplicationContext()
				.getPackageName(), R.layout.main_simple);
		buttonviews = new RemoteViews(context.getApplicationContext()
				.getPackageName(), R.layout.main_simple);

		String bgrcolor = (shared.getString("bgrcolor", "16777215"));// (16777215)-1509949440
																		// for
																		// normal
		String buttons = (shared.getString("buttons", "2"));// (2)1 for normal
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

		views.setInt(R.id.backgroundIV, "setColorFilter",
				Integer.parseInt(bgrcolor));
		if (Build.VERSION.SDK_INT >= 16)
			views.setInt(R.id.backgroundIV, "setImageAlpha",
					Color.alpha(Integer.parseInt(bgrcolor)));
		else
			views.setInt(R.id.backgroundIV, "setAlpha",
					Color.alpha(Integer.parseInt(bgrcolor)));

		buttonListeners();
		// ystem.out.println("adjustUIUpdate");

		SharedPreferences.Editor editor = shared.edit();
		editor.putBoolean("update", false);
		editor.commit();
	}

	public void updateUI() {
		adjustUI();
		updateButtons(playing);
	}

	public void updateButtons(boolean bool) {
		playing = bool;

		if (buttonType == 0) {
			String buttons = (shared.getString("buttons", "2"));// (2)1-for-normal
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
