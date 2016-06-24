package com.asus.cnmusic.fragment;

import java.util.List;
import java.util.Map;

import com.asus.cnmusic.info.LocalMusic;
import com.ximalaya.ting.android.opensdk.datatrasfer.CommonRequest;
import com.ximalaya.ting.android.opensdk.player.XmPlayerManager;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RemoteViews;
import android.widget.SimpleAdapter;

public class BaseFragment extends Fragment{
	public final String TAG = "AsusCNMusic";
	
	public Context mContext;
	
	public RemoteViews mRemoteView;
	
	public ImageView mMusicPlayPre;
	public ImageView mMusicPlayPause;
	public ImageView mMusicPlayNext;
	public ViewPager mViewPager;
	public List<BaseFragment> mFragmentList;
	public View mRootView;
	public ListView mMusicInfoListView;
	
	public LocalFragment mLocalFragment;
	public OnlineFragment mOnlineFragment;
	
	public XmPlayerManager mXmPlayerManager;
	public CommonRequest mCommonRequest;

	public List<LocalMusic> mLocalMusicList;
	public List<Map<String, Object>> mLocalMusicMapList;
	public SimpleAdapter mLocalMusicListAdapter;
	
	public static boolean mPlayingInLocal = false;
	
	public static List<LocalMusic> mLocalPlayingList;
	
	public static int mPlayingPosition = 0;
	
	public static boolean mMusicPlaying = false;
	
	public static boolean mInMusicList = false;
	public static boolean mInAlbumMusicList = false;
	public static boolean mInArtistMusicList = false;
	public static boolean mInHistoryMusicList = false;
	
	public static boolean mPlayingInMusicList = false;
	public static boolean mPlayingInAlbumMusicList = false;
	public static boolean mPlayingInArtistMusicList = false;
	public static boolean mPlayingInHistoryMusicList = false;
	
	public static String mPlayingMusicTitle = "";
	public static String mPlayingMusicPath = "";
	
	public void refresh(){
		
	}
	
	public void initListView() {
		
	}
	
	public void updateLocalMusicListAdapter(int targetPosition) {
		
    }
}

