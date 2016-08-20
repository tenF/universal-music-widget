package com.voiche.universalmusicwidget;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaMetadataEditor;
import android.media.MediaMetadataRetriever;
import android.media.RemoteController;
import android.media.RemoteController.MetadataEditor;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.view.KeyEvent;
import android.widget.Toast;

@SuppressLint("NewApi")
public class MusicService_v19 extends NotificationListenerService implements
		RemoteController.OnClientUpdateListener {

	boolean mIsRegistered = false;
	long curDuration = 0;
	int curState = -1;
	static boolean registered = false;
	static AudioManager audioManager;
	static boolean adjustui = false;
	Timer myTimer;
	RemoteController rc;
	public static boolean typeL = false, typeS = false, typeM = false,
			typeXL = false, typeC = false;
	UniversalMusicWidget small;
	UniversalMusicWidget_4x3 large;
	UniversalMusicWidget_4x2 medium;
	UniversalMusicWidget_simple simple;
	UniversalMusicWidget_4x4 xlarge;
	boolean destroy = false;
	int dtap = 0;
	boolean htc = false;
	boolean playingCur = false;
	String artist = "", title = "", album = "";
	int ccc = 0;
	Bitmap curBitmap;

	@Override
	public void onCreate() {
		registered = true;

		xlarge = new UniversalMusicWidget_4x4();
		large = new UniversalMusicWidget_4x3();
		medium = new UniversalMusicWidget_4x2();
		small = new UniversalMusicWidget();
		simple = new UniversalMusicWidget_simple();
		// xlarge.create(this);
		// large.create(this);
		// medium.create(this);
		// small.create(this);
		// simple.create(this);

		registerBroadcastReceivers();

		audioManager = (AudioManager) this
				.getSystemService(Context.AUDIO_SERVICE);

		rc = new RemoteController(getBaseContext(), this);

		rc.setArtworkConfiguration(1024, 1024);

		/*try {
			if (rc.getRemoteControlClientPackageName() == null
					|| rc.getRemoteControlClientPackageName() == "") {
				Toast.makeText(
						getBaseContext(),
						"Start music playback in order the widget to recognize your music player.",
						Toast.LENGTH_LONG).show();
			}
		} catch (Exception e) {
			Toast.makeText(
					getBaseContext(),
					"Start music playback in order the widget to recognize your music player.",
					Toast.LENGTH_LONG).show();
		}*/

		if (audioManager.registerRemoteController(rc) == false) {
			Toast.makeText(this,
					"Please allow Notification Access at Settings > Security",
					Toast.LENGTH_LONG).show();
			timer();
		}

	}

	private void timer() {
		myTimer = new Timer();
		myTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				// ccc++;
				if (audioManager.registerRemoteController(rc) != false) {
					myTimer.cancel();
					myTimer.purge();
					curDuration = -2;
				}
				System.out.println("W8ing");
				if (ccc >= 30) {
					Toast.makeText(
							getBaseContext(),
							"Please allow Notification Access at Settings > Security",
							Toast.LENGTH_LONG).show();
					// myTimer.cancel();
					// myTimer.purge();
				}
			}
		}, 0, 1000);
	}

	private void updateInfo(String title, String artist, String album) {
		this.title = title;
		this.artist = artist;
		this.album = album;
		if (typeS)
			small.updateInfo(title, artist, album);
		if (typeM)
			medium.updateInfo(title, artist, album);
		if (typeL)
			large.updateInfo(title, artist, album);
		if (typeXL)
			xlarge.updateInfo(title, artist, album);
	}

	private void updateButtons(boolean playing, boolean refresh) {
		if (playingCur == playing && refresh != true)
			return;

		playingCur = playing;

		if (typeC)
			simple.updateButtons(playing);
		if (typeS)
			small.updateButtons(playing);
		if (typeM)
			medium.updateButtons(playing);
		if (typeL)
			large.updateButtons(playing);
		if (typeXL)
			xlarge.updateButtons(playing);
	}

	public void simpleState(boolean bool) {
		if (typeC == bool)
			return;
		if (bool) {
			typeC = true;
			simple.create(this);
		} else
			typeC = false;
	}

	public void smallState(boolean bool) {
		if (typeS == bool)
			return;
		if (bool) {
			typeS = true;
			small.create(this);
		} else
			typeS = false;
	}

	public void mediumState(boolean bool) {
		if (typeM == bool)
			return;
		if (bool) {
			typeM = true;
			medium.create(this);
		} else
			typeM = false;
	}

	public void largeState(boolean bool) {
		if (typeL == bool)
			return;
		if (bool) {
			typeL = true;
			large.create(this);
		} else
			typeL = false;
	}

	public void xlargeState(boolean bool) {
		if (typeXL == bool)
			return;
		if (bool) {
			typeXL = true;
			xlarge.create(this);
		} else
			typeXL = false;
	}

	private void checkActiveWidgets() {
		simpleState(true);
		mediumState(true);
		largeState(true);
		xlargeState(true);
		smallState(true);

		/*
		 * int ids[] = AppWidgetManager.getInstance(this).getAppWidgetIds( new
		 * ComponentName(this, UniversalMusicWidget.class)); if (ids.length > 0
		 * && typeS == false) smallState(true); else if (ids.length < 1 && typeS
		 * == true) smallState(false);
		 * 
		 * ids = AppWidgetManager.getInstance(this).getAppWidgetIds( new
		 * ComponentName(this, UniversalMusicWidget_4x2.class)); if (ids.length
		 * > 0 && typeM == false) mediumState(true); else if (ids.length < 1 &&
		 * typeM == true) mediumState(false);
		 * 
		 * ids = AppWidgetManager.getInstance(this).getAppWidgetIds( new
		 * ComponentName(this, UniversalMusicWidget_4x3.class)); if (ids.length
		 * > 0 && typeL == false) largeState(true); else if (ids.length < 1 &&
		 * typeL == true) largeState(false);
		 * 
		 * ids = AppWidgetManager.getInstance(this).getAppWidgetIds( new
		 * ComponentName(this, UniversalMusicWidget_4x4.class)); if (ids.length
		 * > 0 && typeXL == false) xlargeState(true); else if (ids.length < 1 &&
		 * typeXL == true) xlargeState(false);
		 * 
		 * ids = AppWidgetManager.getInstance(this).getAppWidgetIds( new
		 * ComponentName(this, UniversalMusicWidget_simple.class)); if
		 * (ids.length > 0 && typeC == false) simpleState(true); else if
		 * (ids.length < 1 && typeC == true) simpleState(false);
		 * 
		 * if (!typeS && !typeM && !typeL && !typeXL && !typeC) { destroy =
		 * true; stopSelf(); }
		 */
	}

	@Override
	public void onDestroy() {
		if (registered == true) {
			unregisterReceiver(mMediaPlaybackListener);
			registered = false;
		}
		audioManager.unregisterRemoteController(rc);
		if (destroy != true) {
			final Context context = this;
			Handler handler = new Handler();
			handler.postDelayed(new Runnable() {
				public void run() {
					startService(new Intent(context, MusicService_v19.class));
				}
			}, 0);
		}
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	private void findSong(String title, String artist, String album) {
		if (artist == null || title == null)
			return;
		if (artist.length() < 1 && title.length() < 1)
			return;
		ContentResolver cr = getContentResolver();

		Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
		String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
		String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";
		Cursor cur = cr.query(uri, null, selection, null, sortOrder);
		int count = 0;

		if (cur != null) {
			count = cur.getCount();

			if (count > 0) {
				while (cur.moveToNext()) {
					if ((artist.equals(cur.getString(cur
							.getColumnIndex(MediaStore.Audio.Media.ARTIST))) || artist
							.equals(cur.getString(cur
									.getColumnIndex(MediaStore.Audio.Media.COMPOSER))))
							&& (title
									.equals(cur.getString(cur
											.getColumnIndex(MediaStore.Audio.Media.TITLE))) || title
									.equals(cur.getString(cur
											.getColumnIndex(MediaStore.Audio.Media.TRACK))))) {
						Long duration = Long
								.parseLong(cur.getString(cur
										.getColumnIndex(MediaStore.Audio.Media.DURATION)));
						long albumId = cur
								.getLong(cur
										.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
						if (curDuration != duration || duration == -1) {
							updateInfo(title, artist, album);
							curDuration = duration;

							updateAlbumArt(albumId);
						}
					}
				}
			}
		}
		cur.close();
	}

	private void findAlbumArt(String title, String artist) {
		setAlbumArt(BitmapFactory.decodeResource(getResources(),
				R.drawable.default_album));
		if (1 == 1)
			return;

		if (artist == null || title == null)
			return;
		if (artist.length() < 1 && title.length() < 1)
			return;
		ContentResolver cr = getContentResolver();

		Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
		String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
		String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";
		Cursor cur = cr.query(uri, null, selection, null, sortOrder);
		int count = 0;

		if (cur != null) {
			count = cur.getCount();

			if (count > 0) {
				while (cur.moveToNext()) {
					if ((artist.equals(cur.getString(cur
							.getColumnIndex(MediaStore.Audio.Media.ARTIST))) || artist
							.equals(cur.getString(cur
									.getColumnIndex(MediaStore.Audio.Media.COMPOSER))))
							&& (title
									.equals(cur.getString(cur
											.getColumnIndex(MediaStore.Audio.Media.TITLE))) || title
									.equals(cur.getString(cur
											.getColumnIndex(MediaStore.Audio.Media.TRACK))))) {
						long albumId = cur
								.getLong(cur
										.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));

						Uri sArtworkUri = Uri
								.parse("content://media/external/audio/albumart");
						Uri albumArtUri = ContentUris.withAppendedId(
								sArtworkUri, albumId);

						Bitmap bitmap = null;
						try {
							bitmap = MediaStore.Images.Media.getBitmap(
									getBaseContext().getContentResolver(),
									albumArtUri);

						} catch (FileNotFoundException exception) {
							exception.printStackTrace();
						} catch (IOException e) {

							e.printStackTrace();
						}
						if (bitmap == null) {
							bitmap = BitmapFactory.decodeResource(
									getResources(), R.drawable.default_album);
						}
						setAlbumArt(bitmap);
						cur.close();
						return;
					}
				}
			}

		}
		setAlbumArt(BitmapFactory.decodeResource(getResources(),
				R.drawable.default_album));
		cur.close();
	}

	private void updateAlbumArt(long albumId) {
		if (albumId == -1) {
			// albumview.setImageViewResource(R.id.imageView1,
			// drawable.default_album);
			return;
		}
		Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
		Bitmap art = null;
		try {
			Uri albumArtUri = ContentUris.withAppendedId(sArtworkUri, albumId);
			try {
				art = MediaStore.Images.Media.getBitmap(getBaseContext()
						.getContentResolver(), albumArtUri);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				// ystem.out.println(e);
				e.printStackTrace();
			}
		} catch (Exception e) {

		}
		if (art == null) {
			art = BitmapFactory.decodeResource(getResources(),
					R.drawable.default_album);
		}

		setAlbumArt(art);
	}

	private void setAlbumArt(Bitmap bitmap) {

		File cacheDir = getBaseContext().getCacheDir();
		File f = new File(cacheDir, "pic");

		try {
			FileOutputStream out = new FileOutputStream(f);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
			out.flush();
			out.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(f);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Bitmap art = BitmapFactory.decodeStream(fis);
		curBitmap = art;
		// if (typeS)small.updateAlbumArt(art);
		updateAlbumArt(art);
	}

	private void updateAlbumArt(Bitmap art) {
		if (art == null)
			art = BitmapFactory.decodeResource(getResources(),
					R.drawable.default_album);
		if (typeS)
			small.updateAlbumArt(art);
		if (typeM)
			medium.updateAlbumArt(art);
		if (typeL)
			large.updateAlbumArt(art);
		if (typeXL)
			xlarge.updateAlbumArt(art);
	}

	private void registerBroadcastReceivers() {
		if (!this.mIsRegistered) {
			this.mIsRegistered = true;
			IntentFilter localIntentFilter1 = new IntentFilter();
			// localIntentFilter1.addAction("com.htc.music.metachanged");
			// localIntentFilter1.addAction("com.htc.music.playstatechanged");
			localIntentFilter1
					.addAction("com.voiche.universalmusicwidget.UPDATE");
			localIntentFilter1
					.addAction("com.voiche.universalmusicwidget_4x4.OPENSETTINGS");
			localIntentFilter1
					.addAction("com.voiche.universalmusicwidget_4x2.OPENSETTINGS");
			localIntentFilter1
					.addAction("com.voiche.universalmusicwidget_4x3.OPENSETTINGS");
			localIntentFilter1
					.addAction("com.voiche.universalmusicwidget_simple.OPENSETTINGS");
			localIntentFilter1
					.addAction("com.voiche.universalmusicwidget.OPENSETTINGS");
			localIntentFilter1
					.addAction("com.voiche.universalmusicwidget.STATE_CHANGED");
			localIntentFilter1
					.addAction("com.voiche.universalmusicwidget.PLAYPAUSE");
			localIntentFilter1
					.addAction("com.voiche.universalmusicwidget.PREV");
			localIntentFilter1
					.addAction("com.voiche.universalmusicwidget.NEXT");
			localIntentFilter1
					.addAction("com.voiche.universalmusicwidget.OPENPLAYER");
			localIntentFilter1.addAction("com.htc.music.playstatechanged");
			localIntentFilter1.addAction("com.htc.music.metachanged");
			localIntentFilter1.addAction("com.htc.music.playbackcomplete");
			localIntentFilter1.addAction(Intent.ACTION_SCREEN_ON);
			localIntentFilter1.addAction(Intent.ACTION_SCREEN_OFF);
			localIntentFilter1.addAction(Intent.ACTION_CONFIGURATION_CHANGED);
			registerReceiver(mMediaPlaybackListener, localIntentFilter1);
		}
	}

	private final BroadcastReceiver mMediaPlaybackListener = new BroadcastReceiver() {
		public void onReceive(Context context, Intent paramAnonymousIntent) {
			String str = paramAnonymousIntent.getAction();
			// ystem.out.println(str);
			checkActiveWidgets();

			if ((str.equals("com.htc.music.playstatechanged"))
					|| (str.equals("com.htc.music.metachanged"))) {
				htc = true;
				boolean isPlaying = paramAnonymousIntent.getBooleanExtra(
						"isplaying", false);
				String artist = paramAnonymousIntent.getStringExtra("artist");
				String title = paramAnonymousIntent.getStringExtra("track");
				String album = paramAnonymousIntent.getStringExtra("album");
				updateInfo(title, artist, album);
				findSong(title, artist, album);
				updateButtons(isPlaying, false);

			} else if (str.equals("com.htc.music.playbackcomplete")) {
				htc = false;
				updateButtons(false, false);
			} else if (str.equals("com.htc.music.playstatechanged")
					|| str.equals("com.htc.music.metachanged")) {
				boolean playing = paramAnonymousIntent.getBooleanExtra(
						"isplaying", false);
				String title = paramAnonymousIntent.getStringExtra("track");
				String artist = paramAnonymousIntent.getStringExtra("artist");
				String album = paramAnonymousIntent.getStringExtra("album");
				Long album_id = paramAnonymousIntent.getLongExtra("album_id",
						-1);
				updateButtons(playing, false);

				updateInfo(title, artist, album);

				updateAlbumArt(album_id);
			} else if (str.equals("com.voiche.universalmusicwidget.UPDATE")
					|| str.equals("android.intent.action.CONFIGURATION_CHANGED")) {
				Handler handler = new Handler();
				handler.postDelayed(new Runnable() {
					public void run() {
						updateInfo(title, artist, album);
						updateButtons(playingCur, true);
						updateAlbumArt(curBitmap);
					}
				}, 1000);
			} else if (str
					.equals("com.voiche.universalmusicwidget_4x4.OPENSETTINGS")) {
				dtap++;
				SharedPreferences shared = getSharedPreferences("settings_4x4",
						MODE_PRIVATE);
				if (dtap >= 2)
					context.startActivity(new Intent(context,
							Configure_4x4.class)
							.setAction(
									"android.appwidget.action.APPWIDGET_CONFIGURE")
							.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
									shared.getInt("awID", 0))
							.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
							.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
				Handler handler = new Handler();
				handler.postDelayed(new Runnable() {
					public void run() {
						if (dtap == 1)
							sendBroadcast(new Intent()
									.setAction("com.voiche.universalmusicwidget.OPENPLAYER"));
						dtap = 0;
					}
				}, 250);
			} else if (str
					.equals("com.voiche.universalmusicwidget_4x2.OPENSETTINGS")) {
				dtap++;
				SharedPreferences shared = getSharedPreferences("settings_4x2",
						MODE_PRIVATE);
				if (dtap >= 2)
					context.startActivity(new Intent(context,
							Configure_4x2.class)
							.setAction(
									"android.appwidget.action.APPWIDGET_CONFIGURE")
							.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
									shared.getInt("awID", 0))
							.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
							.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
				Handler handler = new Handler();
				handler.postDelayed(new Runnable() {
					public void run() {
						dtap = 0;
					}
				}, 250);
			} else if (str
					.equals("com.voiche.universalmusicwidget_4x3.OPENSETTINGS")) {
				dtap++;
				SharedPreferences shared = getSharedPreferences("settings_4x3",
						MODE_PRIVATE);
				if (dtap >= 2)
					context.startActivity(new Intent(context,
							Configure_4x3.class)
							.setAction(
									"android.appwidget.action.APPWIDGET_CONFIGURE")
							.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
									shared.getInt("awID", 0))
							.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
							.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
				Handler handler = new Handler();
				handler.postDelayed(new Runnable() {
					public void run() {
						dtap = 0;
					}
				}, 250);
			} else if (str
					.equals("com.voiche.universalmusicwidget.OPENSETTINGS")) {
				dtap++;
				SharedPreferences shared = getSharedPreferences("settings",
						MODE_PRIVATE);
				if (dtap >= 2)
					context.startActivity(new Intent(context, Configure.class)
							.setAction(
									"android.appwidget.action.APPWIDGET_CONFIGURE")
							.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
									shared.getInt("awID", 0))
							.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
							.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
				Handler handler = new Handler();
				handler.postDelayed(new Runnable() {
					public void run() {
						dtap = 0;
					}
				}, 250);
			} else if (str
					.equals("com.voiche.universalmusicwidget_simple.OPENSETTINGS")) {
				dtap++;
				SharedPreferences shared = getSharedPreferences(
						"settings_simple", MODE_PRIVATE);
				if (dtap >= 2)
					context.startActivity(new Intent(context,
							Configure_simple.class)
							.setAction(
									"android.appwidget.action.APPWIDGET_CONFIGURE")
							.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
									shared.getInt("awID", 0))
							.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
							.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
				Handler handler = new Handler();
				handler.postDelayed(new Runnable() {
					public void run() {
						dtap = 0;
					}
				}, 250);
			} else if (str
					.equals("com.voiche.universalmusicwidget.STATE_CHANGED")) {
				String state = paramAnonymousIntent.getStringExtra("STATE");
				// ystem.out.println(state);
				if (state.equals("XLARGE_VISIBLE"))
					xlargeState(true);
				else if (state.equals("XLARGE_DELETED"))
					xlargeState(false);
				else if (state.equals("LARGE_VISIBLE"))
					largeState(true);
				else if (state.equals("LARGE_DELETED"))
					largeState(false);
				else if (state.equals("MEDIUM_VISIBLE"))
					mediumState(true);
				else if (state.equals("MEDIUM_DELETED"))
					mediumState(false);
				else if (state.equals("SMALL_VISIBLE"))
					smallState(true);
				else if (state.equals("SMALL_DELETED"))
					smallState(false);
				else if (state.equals("SIMPLE_VISIBLE"))
					simpleState(true);
				else if (state.equals("SIMPLE_DELETED"))
					simpleState(false);
				curDuration = -1;
				if (!typeM && !typeS && !typeL && !typeC) {
					destroy = true;
					stopSelf();
				}
			} else if (str.equals("android.intent.action.SCREEN_OFF")) {
				// mProvider.dropRemoteControls(true);
			} else if (str.equals("android.intent.action.SCREEN_ON")) {
				// timer();
			} else if (str.equals("com.voiche.universalmusicwidget.OPENPLAYER")) {
				if (htc)
					if (isAppInstalled(context, "com.htc.music")) {
						Intent intent = getPackageManager()
								.getLaunchIntentForPackage("com.htc.music");
						intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						try {
							startActivity(intent);
							return;
						} catch (Exception e) {
						}
					}
				Intent intent = new Intent(Intent.ACTION_MAIN);
				intent = getPackageManager().getLaunchIntentForPackage(
						rc.getRemoteControlClientPackageName());
				try {
					String chk = intent.toString();
					String chk2 = chk.replaceAll("android.apps.plus", "");
					if (!chk.equals(chk2))
						return;
					context.startActivity(intent);
				} catch (Exception e) {
					// ystem.out.println(e);
				}
			} else if (str.equals("com.voiche.universalmusicwidget.PLAYPAUSE")) {
				sendKeyEvent(context, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE);
			} else if (str.equals("com.voiche.universalmusicwidget.PREV")) {
				sendKeyEvent(context, KeyEvent.KEYCODE_MEDIA_PREVIOUS);
			} else if (str.equals("com.voiche.universalmusicwidget.NEXT")) {
				sendKeyEvent(context, KeyEvent.KEYCODE_MEDIA_NEXT);
			}
		}
	};

	private boolean isAppInstalled(Context context, String packageName) {
		PackageManager pm = context.getPackageManager();
		boolean exists = false;
		try {
			pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
			exists = true;
		} catch (PackageManager.NameNotFoundException e) {
			exists = false;
		}
		return exists;
	}

	private void sendKeyEvent(Context context, int keyCode) {
		KeyEvent keyEvent = new KeyEvent(KeyEvent.ACTION_DOWN, keyCode);
		if (!rc.sendMediaKeyEvent(keyEvent)) {
			Intent intent = new Intent(Intent.ACTION_MEDIA_BUTTON);
			intent.putExtra(Intent.EXTRA_KEY_EVENT, keyEvent);
			sendOrderedBroadcast(intent, null);
		}
		keyEvent = new KeyEvent(KeyEvent.ACTION_UP, keyCode);
		if (!rc.sendMediaKeyEvent(keyEvent)) {
			Intent intent = new Intent(Intent.ACTION_MEDIA_BUTTON);
			intent = new Intent(Intent.ACTION_MEDIA_BUTTON);
			intent.putExtra(Intent.EXTRA_KEY_EVENT, keyEvent);
			sendOrderedBroadcast(intent, null);
		}
	}

	@Override
	public void onClientChange(boolean clearing) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onClientMetadataUpdate(MetadataEditor metadataEditor) {
		Long duration = metadataEditor.getLong(
				MediaMetadataRetriever.METADATA_KEY_DURATION, -1);
		if (curDuration != duration || duration <= 0) {
			Bitmap defaultBitmap = BitmapFactory.decodeResource(getResources(),
					R.drawable.default_album);
			Bitmap art = metadataEditor.getBitmap(
					MediaMetadataEditor.BITMAP_KEY_ARTWORK, defaultBitmap);
			String title = metadataEditor.getString(
					MediaMetadataRetriever.METADATA_KEY_TITLE, "");
			String artist = metadataEditor.getString(
					MediaMetadataRetriever.METADATA_KEY_ARTIST, "");
			if (artist.equals("") || artist.equals(null))
				artist = metadataEditor.getString(
						MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST, "");
			if (artist.equals("") || artist.equals(null))
				artist = metadataEditor.getString(
						MediaMetadataRetriever.METADATA_KEY_AUTHOR, "");
			String album = metadataEditor.getString(
					MediaMetadataRetriever.METADATA_KEY_ALBUM, "");
			if ((title != null && !title.equals("") && !title.equals("unknown") || artist != null
					&& !artist.equals("") && !artist.equals("unknown")))
				htc = false;
			if (htc == true)
				return;
			updateInfo(title, artist, album);

			if (art == defaultBitmap)
				findAlbumArt(title, artist);
			else
				setAlbumArt(art);

			curDuration = duration;
		}

	}

	@Override
	public void onClientPlaybackStateUpdate(int state) {
		// TODO Auto-generated method stub
		if (curState != state) {
			boolean playing = false;
			if (state == 3)
				playing = true;
			updateButtons(playing, false);
			curState = state;
		}
	}

	@Override
	public void onClientPlaybackStateUpdate(int state, long stateChangeTimeMs,
			long currentPosMs, float speed) {
		// TODO Auto-generated method stub
		if (curState != state) {
			boolean playing = false;
			if (state == 3)
				playing = true;
			updateButtons(playing, false);
			curState = state;
		}
	}

	@Override
	public void onClientTransportControlUpdate(int transportControlFlags) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onNotificationPosted(StatusBarNotification arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onNotificationRemoved(StatusBarNotification arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_STICKY;
	}

}
