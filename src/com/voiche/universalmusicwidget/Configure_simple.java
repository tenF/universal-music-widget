package com.voiche.universalmusicwidget;

import net.margaritov.preference.colorpicker.ColorPickerPreference;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.tjeannin.apprate.AppRate;

public class Configure_simple extends PreferenceActivity {
	int awID;
	private InterstitialAd interstitial;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// StartAppSDK.init(this, "102075883", "202764065", true);
		// StartAppAd.showSplash(this, savedInstanceState,
		// new SplashConfig().setTheme(SplashConfig.Theme.ASHEN_SKY));
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.layout.configure_simple);
		setContentView(R.layout.ad);

		new AppRate(this).setMinDaysUntilPrompt(1)
				.setMinLaunchesUntilPrompt(10).setShowIfAppHasCrashed(false)
				.init();

		interstitial = new InterstitialAd(this);
		interstitial.setAdUnitId("ca-app-pub-4066751096341361/6531930339");

		// Create ad request.
		AdRequest adRequest = new AdRequest.Builder().build();

		// Begin loading your interstitial.
		interstitial.loadAd(adRequest);

		interstitial.setAdListener(new AdListener() {
			public void onAdLoaded() {
				displayInterstitial();
			}
		});

		Intent i = getIntent();
		Bundle extras = i.getExtras();
		if (extras != null)
			awID = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
					AppWidgetManager.INVALID_APPWIDGET_ID);
		if (awID == AppWidgetManager.INVALID_APPWIDGET_ID)
			finish();

		SharedPreferences shared = getSharedPreferences("settings_simple",
				MODE_PRIVATE);
		SharedPreferences.Editor editor = shared.edit();
		editor.putBoolean("wait", true);
		editor.putInt("awID", awID);
		editor.commit();

		// Get the custom preference
		Preference customPref = (Preference) findPreference("customPref");
		customPref
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {

					public boolean onPreferenceClick(Preference preference) {
						Intent emailIntent = new Intent(Intent.ACTION_SENDTO,
								Uri.fromParts("mailto", "voichestd@gmail.com",
										null));
						emailIntent.putExtra(Intent.EXTRA_SUBJECT,
								"Universal Music Widget");
						startActivity(Intent.createChooser(emailIntent,
								"Send email..."));
						return true;
					}

				});
		Preference cpTwitter = (Preference) findPreference("cpTwitter");
		cpTwitter.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			public boolean onPreferenceClick(Preference preference) {
				String url = "http://www.twitter.com/BuzzyMind";
				Intent i = new Intent(Intent.ACTION_VIEW);
				Uri u = Uri.parse(url);
				i.setData(u);
				startActivity(i);
				return true;
			}

		});

		Preference cpRC = (Preference) findPreference("cpRC");
		cpRC.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			public boolean onPreferenceClick(Preference preference) {
				startActivity(new Intent(
						Intent.ACTION_VIEW,
						Uri.parse("market://details?id=com.voiche.universalmusicwidget")));
				return true;
			}

		});

		Preference cpFL = (Preference) findPreference("cpFL");
		cpFL.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			public boolean onPreferenceClick(Preference preference) {
				startActivity(new Intent(Intent.ACTION_VIEW, Uri
						.parse("market://details?id=com.voiche.floatinglyrics")));
				return true;
			}

		});

		Preference cpOA = (Preference) findPreference("cpOA");
		cpOA.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			public boolean onPreferenceClick(Preference preference) {
				startActivity(new Intent(Intent.ACTION_VIEW, Uri
						.parse("market://search?q=pub:Voiche")));
				return true;
			}

		});

		final ListPreference buttonstyle = (ListPreference) getPreferenceManager()
				.findPreference("buttonPref_simple");
		buttonstyle.setSummary(buttonstyle.getEntry());

		buttonstyle
				.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
					public boolean onPreferenceChange(Preference preference,
							Object newValue) {
						SharedPreferences shared = getSharedPreferences(
								"settings_simple", MODE_PRIVATE);
						SharedPreferences.Editor editor = shared.edit();
						editor.putString("buttons", newValue.toString());
						editor.commit();
						buttonstyle.setValue(newValue.toString());
						preference.setSummary(buttonstyle.getEntry());
						return true;
					}
				});

		final CheckBoxPreference lsm = (CheckBoxPreference) getPreferenceManager()
				.findPreference("lsm_simple");

		lsm.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference preference,
					Object newValue) {
				SharedPreferences shared = getSharedPreferences(
						"settings_simple", MODE_PRIVATE);
				SharedPreferences.Editor editor = shared.edit();
				editor.putString("lsm", newValue.toString());
				editor.commit();
				return true;
			}
		});

		final ColorPickerPreference bgrcolor = (ColorPickerPreference) getPreferenceManager()
				.findPreference("bgrcolor_simple");

		bgrcolor.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference preference,
					Object newValue) {
				SharedPreferences shared = getSharedPreferences(
						"settings_simple", MODE_PRIVATE);
				SharedPreferences.Editor editor = shared.edit();
				editor.putString("bgrcolor", newValue.toString());
				editor.commit();
				return true;
			}
		});
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	private void Finish() {
		Intent intent = new Intent();
		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, awID);
		setResult(RESULT_OK, intent);

		if (Build.VERSION.SDK_INT < 18) {
			// this.stopService(new Intent(this, MusicService.class));
			this.startService(new Intent(this, MusicService.class));
		} else if (Build.VERSION.SDK_INT == 18) {
			// this.stopService(new Intent(this, MusicService_v18.class));
			this.startService(new Intent(this, MusicService_v18.class));
		} else if (Build.VERSION.SDK_INT < 21) {
			// ystem.out.println("MW19");
			// this.stopService(new Intent(this, MusicService_v19.class));
			this.startService(new Intent(this, MusicService_v19.class));
		} else {
			// ystem.out.println("MW19");
			// this.stopService(new Intent(this, MusicService_v19.class));
			this.startService(new Intent(this, MusicService_v21.class));
		}
		SharedPreferences shared = getSharedPreferences("settings_simple",
				MODE_PRIVATE);
		SharedPreferences.Editor editor = shared.edit();
		editor.putBoolean("update", true);
		editor.putBoolean("wait", false);
		editor.commit();

		sendBroadcast(new Intent()
				.setAction("com.voiche.universalmusicwidget.UPDATE"));

		finish();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.configure, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			Finish();
			return true;

		case R.id.done:
			Finish();
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onBackPressed() {
		Finish();
	}

	public void displayInterstitial() {
		if (interstitial.isLoaded()) {
			interstitial.show();
		}
	}
}
