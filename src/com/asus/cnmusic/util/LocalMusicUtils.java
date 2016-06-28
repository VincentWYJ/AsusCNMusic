package com.asus.cnmusic.util;

import java.io.File;
import java.text.Collator;
import java.text.DecimalFormat;
import java.util.Comparator;
import java.util.Locale;

import com.asus.cnmusic.MainActivity;
import com.asus.cnmusic.R;
import com.asus.cnmusic.info.LocalMusic;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.provider.MediaStore;
import android.support.v4.app.NotificationCompat;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RemoteViews;

public class LocalMusicUtils{
	
	public static Context mContext= null;
	
	public static String mMusicSortOrder = MediaStore.Audio.Media.TITLE_KEY+" ASC";
	public static String mAlbumSortOrder = MediaStore.Audio.Media.ALBUM_KEY+" ASC";
	public static String mArtistSortOrder = MediaStore.Audio.Media.ARTIST_KEY+" ASC";
	
	public static DecimalFormat decimalFormat = new DecimalFormat("00");
	public static Collator collator = Collator.getInstance(Locale.CHINA);
	public static Comparator<String> comparatorString = new Comparator<String>() {

	    @Override
	    public int compare(String lhs, String rhs) {
	        // TODO Auto-generated method stub
	        return collator.compare(lhs, rhs); //前面加个“-”号为降序排列
	    }
	};
	public static Comparator<LocalMusic> comparatorLocalMusic = new Comparator<LocalMusic>() {

	    @Override
	    public int compare(LocalMusic lhs, LocalMusic rhs) {
	        // TODO Auto-generated method stub
	        return collator.compare(lhs.getTitle(), rhs.getTitle());
	    }
	};

	public static String ACTION_CONTROL_PLAY_PAUSE = "com.asus.cnmusic.ACTION_CONTROL_PLAY_PAUSE";
	public static String ACTION_CONTROL_PLAY_NEXT = "com.asus.cnmusic.ACTION_CONTROL_PLAY_NEXT";
	public static String ACTION_CONTROL_PLAY_PRE = "com.asus.cnmusic.ACTION_CONTROL_PLAY_PRE";
	
	public static RemoteViews mRemoteView;
	public static NotificationManager mNotificationManager;
	public static Notification mNotification;
	public static int mNotificationId;
	
	public static RemoteViews getRemoteViews() {
		if(mRemoteView == null) {
			mRemoteView = new RemoteViews(mContext.getPackageName(), R.layout.main_notification);
			
			mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
			mNotification = createNotification();
			mNotificationId = (int) System.currentTimeMillis();
		}
		
		return mRemoteView;
	}

	public static Notification createNotification() {
		NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext);

		Intent mainI = new Intent(mContext, MainActivity.class);
		PendingIntent mainPI = PendingIntent.getActivity(mContext, 0, mainI, 0);
		
		Intent playI = new Intent(LocalMusicUtils.ACTION_CONTROL_PLAY_PAUSE);
		PendingIntent playPI = PendingIntent.getBroadcast(mContext, 0, playI, 0);
		
		Intent nextI = new Intent(LocalMusicUtils.ACTION_CONTROL_PLAY_NEXT);
		PendingIntent nextPI = PendingIntent.getBroadcast(mContext, 0, nextI, 0);
		
		Intent preI = new Intent(LocalMusicUtils.ACTION_CONTROL_PLAY_PRE);
		PendingIntent prePI = PendingIntent.getBroadcast(mContext, 0, preI, 0);

		mRemoteView.setOnClickPendingIntent(R.id.img_notifyIcon, mainPI);
		mRemoteView.setOnClickPendingIntent(R.id.img_notifyPlayOrPause, playPI);
		mRemoteView.setOnClickPendingIntent(R.id.img_notifyNext, nextPI);
		mRemoteView.setOnClickPendingIntent(R.id.img_notifyPre, prePI);
		
		builder.setContent(mRemoteView)
				.setSmallIcon(R.drawable.ic_status)
				.setContentTitle("名称").setContentText("信息")
				.setContentIntent(mainPI);
		
		return builder.build();
	}
	
	public static void sendNotification() {
		mNotificationManager.notify(mNotificationId, mNotification);
	}
	
	public static void deleteNotification() {
		mNotificationManager.cancel(mNotificationId);
	}
	
	public static void deleteFiles(File file) { 
	     if (file.isFile()) { 
	         file.delete(); 
	         return; 
	     } 
	     if(file.isDirectory()) { 
	         File[] childFiles = file.listFiles(); 
	         if (childFiles == null || childFiles.length == 0) { 
	             file.delete(); 
	             return; 
	         } 
	         for (int i = 0; i < childFiles.length; i++) { 
	        	 deleteFiles(childFiles[i]); 
	         } 
	         file.delete(); 
	     } 
	} 
	
	public static void initStatusBarColor(Activity activity) {
        if(android.os.Build.VERSION.SDK_INT > 18) {
			Window window = activity.getWindow();
			window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
			WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
			SystemBarTintManager tintManager = new SystemBarTintManager(activity);
			tintManager.setStatusBarTintEnabled(true);
			tintManager.setNavigationBarTintEnabled(true);
			tintManager.setStatusBarTintResource(R.color.colorPrimaryDark);
		}
	}
}