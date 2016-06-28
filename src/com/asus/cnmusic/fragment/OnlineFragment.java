package com.asus.cnmusic.fragment;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import com.asus.cnmusic.util.LocalMusicUtils;
import com.asus.cnmusic.util.ToolUtil;
import com.asus.cnmusic.R;
import com.bumptech.glide.Glide;
import com.ximalaya.ting.android.opensdk.datatrasfer.CommonRequest;
import com.ximalaya.ting.android.opensdk.model.PlayableModel;
import com.ximalaya.ting.android.opensdk.model.advertis.Advertis;
import com.ximalaya.ting.android.opensdk.model.advertis.AdvertisList;
import com.ximalaya.ting.android.opensdk.model.live.radio.Radio;
import com.ximalaya.ting.android.opensdk.model.live.schedule.Schedule;
import com.ximalaya.ting.android.opensdk.model.track.Track;
import com.ximalaya.ting.android.opensdk.player.XmPlayerManager;
import com.ximalaya.ting.android.opensdk.player.advertis.IXmAdsStatusListener;
import com.ximalaya.ting.android.opensdk.player.service.IXmPlayerStatusListener;
import com.ximalaya.ting.android.opensdk.player.service.XmPlayerException;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import net.tsz.afinal.FinalHttp;

@SuppressLint({ "ViewHolder", "SdCardPath" })
public class OnlineFragment extends BaseFragment implements OnClickListener {
	private static OnlineFragment mInstance;
	
	private String mAppSecret = "4d8e605fa7ed546c4bcb33dee1381179";
	
	private String[] mOnlineTitles;

	private TextView mTextView;
	private ImageView mSoundCover;
	private SeekBar mSeekBar;
	private TabLayout mTabLayout;
	private Button mMoreClassBtn;
	
	private PagerAdapter mViewPagerAdapter;
	
	private Dialog mClassDialog;
	private GridView mGridView;
	private Button mCancelBtn;
	private SimpleAdapter mSimpleAdapter;

	private boolean mUpdateProgress;
	
	private String playingName;
	private String playingArtist;
	private Bitmap playingBitmap;
	private String coverLarge;
	
	public static OnlineFragment getInstance() {
        if(mInstance == null) {
        	mInstance = new OnlineFragment();
        }
        return mInstance;
    }

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.sound_cover:
			Log.i(TAG, "refresh data");
			
			for(int i=0; i<mCountPages; ++i) {
				mFragmentList.get(i).refresh();
			}
			break;
		case R.id.pre_sound:
			mXmPlayerManager.playPre();
			break;
		case R.id.play_or_pause:
			if(mXmPlayerManager.isPlaying()) {
				mXmPlayerManager.pause();
			}else {
				mXmPlayerManager.play();
			}
			break;
		case R.id.next_sound:
			mXmPlayerManager.playNext();
			break;
		case R.id.class_more:
			showMoreClassDialog();
			break;
		case R.id.class_cancel:
			mClassDialog.cancel();
		default:
			break;
		}
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		//Log.i(TAG, "XiMaLaYaFragment onCreateView");
		
		mRootView = inflater.inflate(R.layout.online_fragment, container, false);
		
		mTextView = (TextView) mRootView.findViewById(R.id.message);
		mSoundCover = (ImageView) mRootView.findViewById(R.id.sound_cover);
		mSeekBar = (SeekBar) mRootView.findViewById(R.id.seek_bar);
		mMusicPlayPre = (ImageView) mRootView.findViewById(R.id.pre_sound);
		mMusicPlayPause = (ImageView) mRootView.findViewById(R.id.play_or_pause);
		mMusicPlayNext = (ImageView) mRootView.findViewById(R.id.next_sound);
		mTabLayout = (TabLayout) mRootView.findViewById(R.id.table_layout);
		mMoreClassBtn = (Button) mRootView.findViewById(R.id.class_more);
		mViewPager = (ViewPager) mRootView.findViewById(R.id.pager);
		
		return mRootView;
	}
	
	@Override
	public void onActivityCreated(Bundle arg0) {
		super.onActivityCreated(arg0);
		
		Log.i(TAG, "XiMaLaYaFragment onActivityCreated");

		mContext= getContext();
		
		mRemoteView = LocalMusicUtils.getRemoteViews();
		
		mOnlineTitles = getResources().getStringArray(R.array.online_titles_array);
		
		mCountPages = mOnlineTitles.length;
		
		mUpdateProgress = true;
		
		new FinalHttp();
		
		mCommonRequest = CommonRequest.getInstanse();
		mCommonRequest.init(mContext, mAppSecret);
		mCommonRequest.setDefaultPagesize(50);  //设定一次加载的音乐条目数, 通过调试总条目数好像为1000, 即总页数为1000/50=20
		
		mXmPlayerManager = XmPlayerManager.getInstance(mContext);
		mXmPlayerManager.init(LocalMusicUtils.mNotificationId, null);
		mXmPlayerManager.addPlayerStatusListener(mPlayerStatusListener);
		mXmPlayerManager.addAdsStatusListener(mAdsListener);
		mXmPlayerManager.getPlayerStatus();
		
		mFragmentList = new ArrayList<BaseFragment>();
		for(int i=0; i<mCountPages; ++i) {
			int classIDValue = 0;
			if(i<11) {
				classIDValue = i;
			}else if(i<18) {
				classIDValue = i+1;
			}else if(i<23) {
				classIDValue = i+2;
			}else if(i<24) {
				classIDValue = i+8;
			}
			//mFragmentList.add(new Fragment_BangDan(mContext, classIDValue));
			mFragmentList.add(OnlineClassFragment.newInstance(classIDValue));
		}
		
		mSoundCover.setOnClickListener(this);
		mMusicPlayPause.setOnClickListener(this);
		mMusicPlayNext.setOnClickListener(this);
		mMusicPlayPre.setOnClickListener(this);
		mMoreClassBtn.setOnClickListener(this);
		
		mViewPagerAdapter = new SlidingPagerAdapter(getChildFragmentManager());
		
		mViewPager.setOffscreenPageLimit(mCountPages); //参数默认为1,即当不指定或者指定值小于1时系统会当做1处理,一般同时缓存的有三页; 如置为2则一般同时缓存的页面数为5
		mViewPager.setAdapter(mViewPagerAdapter);

		mTabLayout.setupWithViewPager(mViewPager);
		
		setOnSeekBarChangeListener();
	}
	
	public void showMoreClassDialog() {
		mClassDialog = new Dialog(mContext);
		mClassDialog.setContentView(R.layout.online_class_dialog);
		mClassDialog.setCanceledOnTouchOutside(true);
        mGridView = (GridView) mClassDialog.findViewById(R.id.class_gridview);
        mCancelBtn  =(Button) mClassDialog.findViewById(R.id.class_cancel);
        mSimpleAdapter = new SimpleAdapter(mContext, getTitlesList(), R.layout.online_class_item, 
    		new String[]{"title"}, new int[]{R.id.class_item_textview}) {
        	
        	@SuppressWarnings("deprecation")
			@Override
        	public View getView(int position, View convertView, ViewGroup parent) {
        		View view;
        		if(convertView == null) {
            		LayoutInflater mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            		view = mInflater.inflate(R.layout.online_class_item, parent, false);
        		}else {
        			view = convertView;
        		}
        	    TextView mTextView = (TextView) view.findViewById(R.id.class_item_textview);
        	    mTextView.setText(mOnlineTitles[position]);
        	    if(position == mViewPager.getCurrentItem()) {
        	    	mTextView.setBackgroundResource(R.color.online_class_item_pressed_bg);
        	    	mTextView.setTextColor(getResources().getColor(R.color.text_pressed));
        	    }else {
        	    	mTextView.setBackgroundResource(R.color.music_title_bg);
        	    }
        	    return view;
        	}
        };
        mGridView.setAdapter(mSimpleAdapter);
        
        mClassDialog.show();
        mCancelBtn.setOnClickListener(this);
        
        mGridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// TODO Auto-generated method stub
				if(mViewPager.getCurrentItem() != position) {
					mViewPager.setCurrentItem(position);
					mSimpleAdapter.notifyDataSetChanged();
					new Thread(new Runnable() {

					     @Override
					     public void run() {
					          // TODO Auto-generated method stub
					          try {
					               Thread.sleep(20);  //这里延迟时间设为0.01秒, 数据量大的刷新可能不够
					          }catch(InterruptedException e) {
					               // TODO Auto-generated catch block
					               e.printStackTrace();
					          }
					          mClassDialog.cancel();
					     }				
					}).start();
				}else {
					mClassDialog.cancel();
				}
			}
		});
	}
	
	public void setOnSeekBarChangeListener() {
		mSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				mXmPlayerManager.seekToByPercent(seekBar.getProgress() / (float) seekBar.getMax());
				mUpdateProgress = true;
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				mUpdateProgress = false;
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				
			}
		});
	}
	
	class SlidingPagerAdapter extends FragmentPagerAdapter{
		public SlidingPagerAdapter(FragmentManager fragmentManager) {
			super(fragmentManager);
		}

		@Override
		public Fragment getItem(int position) {
			return mFragmentList.get(position);
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return mOnlineTitles[position];
		}

		@Override
		public int getCount() {
			return mCountPages;
		}
	}
	
	private IXmAdsStatusListener mAdsListener = new IXmAdsStatusListener() {
		
		@Override
		public void onStartPlayAds(Advertis ad, int position) {
			//Log.i(TAG, "onStartPlayAds, Ad:" + ad.getName() + ", pos:" + position);
			
			if(ad != null) {
				//Glide.with(mContext).load(ad.getImageUrl()).into(mSoundCover); 
			}
		}
		
		@Override
		public void onStartGetAdsInfo() {
			//Log.i(TAG, "onStartGetAdsInfo");
			
			mMusicPlayPause.setEnabled(false);
			mSeekBar.setEnabled(false);
		}
		
		@Override
		public void onGetAdsInfo(AdvertisList ads) {
			//Log.i(TAG, "onGetAdsInfo " + (ads != null));
		}
		
		@Override
		public void onError(int what, int extra) {
			Log.i(TAG, "onError what:" + what + ", extra:" + extra);
		}
		
		@Override
		public void onCompletePlayAds() {
			//Log.i(TAG, "onCompletePlayAds");
			
			mMusicPlayPause.setEnabled(true);
			mSeekBar.setEnabled(true);
			PlayableModel model = mXmPlayerManager.getCurrSound();
			if(model != null && model instanceof Track) {
				//Glide.with(mContext).load(((Track) model).getCoverUrlLarge()).into(mSoundCover);
			}
		}
		
		@Override
		public void onAdsStopBuffering() {
			//Log.i(TAG, "onAdsStopBuffering");
		}
		
		@Override
		public void onAdsStartBuffering() {
			//Log.i(TAG, "onAdsStartBuffering");
		}
	};

	private IXmPlayerStatusListener mPlayerStatusListener = new IXmPlayerStatusListener() {

		@Override
		public void onSoundPrepared() {
			//Log.i(TAG, "onSoundPrepared");
			
			mSeekBar.setEnabled(true);
		}

		@Override
		public void onSoundSwitch(PlayableModel laModel, PlayableModel curModel) {
			//Log.i(TAG, "onSoundSwitch");
			
			PlayableModel model = mXmPlayerManager.getCurrSound();
			if(model != null) {
				if(model instanceof Track) {
					Track info = (Track) model;
					playingName = info.getTrackTitle();
					playingArtist = info.getAnnouncer() == null ? "" : info.getAnnouncer().getNickname();
					info.getCoverUrlLarge();
					coverLarge = info.getCoverUrlLarge();
				}
				mTextView.setText(playingName);  //设置播放栏位信息
				if(!TextUtils.isEmpty(coverLarge)) {
					Glide.with(mContext).load(coverLarge).into(mSoundCover);  //设置播放栏图标，可设置占位图/错误图
					updateRemoteViewIcon(coverLarge);  //更新notify栏图标
				}else {
					Log.i(TAG, "download img null");
					Glide.with(mContext).load(R.drawable.ic_launcher).into(mSoundCover);
				}
				updateNotification(playingName, playingArtist, true, true);  //设置notify栏位信息
			}
			updateButtonStatus();
		}

		private void updateNotification(String title, String msg, boolean isPlaying,
				boolean hasNext) {	
			if(!TextUtils.isEmpty(title)) {
				mRemoteView.setTextViewText(R.id.txt_notifyMusicName, title);
			}
			if(!TextUtils.isEmpty(msg)) {
				mRemoteView.setTextViewText(R.id.txt_notifyNickName, msg);
			}
			if(isPlaying) {
				mRemoteView.setImageViewResource(R.id.img_notifyPlayOrPause, R.drawable.ic_pause);
			}else {
				mRemoteView.setImageViewResource(R.id.img_notifyPlayOrPause, R.drawable.ic_play);
			}
			LocalMusicUtils.sendNotification();
		}

		private void updateRemoteViewIcon(final String coverUrl) {
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					 try {
						playingBitmap = Glide.with(mContext)  
						    .load(coverUrl)  
						    .asBitmap()
						    .centerCrop()  
						    .into(500, 500)  
						    .get();
						setNotificationIcon();
					}catch(InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}catch(ExecutionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}).start();
		}
		
		private void setNotificationIcon() {
			mRemoteView.setImageViewBitmap(R.id.img_notifyIcon, playingBitmap);
			LocalMusicUtils.sendNotification();
		}
		
		private void updateButtonStatus() {
			if(mXmPlayerManager.hasPreSound()) {
				mMusicPlayPre.setEnabled(true);
			}else {
				mMusicPlayPre.setEnabled(false);
			}
			if(mXmPlayerManager.hasNextSound()) {
				mMusicPlayNext.setEnabled(true);
			}else {
				mMusicPlayNext.setEnabled(false);
			}
		}

		@Override
		public void onPlayStop() {
			//Log.i(TAG, "onPlayStop");
			
			mMusicPlayPause.setBackgroundResource(R.drawable.music_pause_drawable);
			
			updateNotification(null, null, false, true);
		}

		@Override
		public void onPlayStart() {
			//Log.i(TAG, "onPlayStart");
			
			pauseLocalPlaying();
			
			mMusicPlayPause.setBackgroundResource(R.drawable.music_start_drawable);
			
			updateNotification(playingName, playingArtist, false, true);  //在线暂停+本地播放->在线播放，信息恢复
			if(!TextUtils.isEmpty(coverLarge)) {
				setNotificationIcon();
			}
		}

		@Override
		public void onPlayProgress(int currPos, int duration) {
			String title = "";
			PlayableModel info = mXmPlayerManager.getCurrSound();
			if(info != null) {
				if(info instanceof Track) {
					title = ((Track) info).getTrackTitle();
				}else if(info instanceof Schedule) {
					title = ((Schedule) info).getRelatedProgram().getProgramName();
				}else if(info instanceof Radio) {
					title = ((Radio) info).getRadioName();
				}
			}
			mTextView.setText(title + "[" + ToolUtil.formatTime(currPos) + "/" + ToolUtil.formatTime(duration) + "]");
			if(mUpdateProgress && duration != 0) {
				mSeekBar.setProgress((int) (100 * currPos / (float) duration));
			}
		}

		@Override
		public void onPlayPause() {
			//Log.i(TAG, "onPlayPause");
			
			mMusicPlayPause.setBackgroundResource(R.drawable.music_pause_drawable);
			
			if(!mLocalMusicPlaying) {
				updateNotification(null, null, true, true);
			}
		}

		@Override
		public void onSoundPlayComplete() {
			//Log.i(TAG, "onSoundPlayComplete");
			
			mMusicPlayPause.setBackgroundResource(R.drawable.music_pause_drawable);
		}

		@Override
		public boolean onError(XmPlayerException exception) {
			Log.i(TAG, "onError " + exception.getMessage());
			
			mMusicPlayPause.setBackgroundResource(R.drawable.music_pause_drawable);
			
			return false;
		}

		@Override
		public void onBufferProgress(int position) {
			mSeekBar.setSecondaryProgress(position);
		}

		public void onBufferingStart() {
			mSeekBar.setEnabled(false);
		}

		public void onBufferingStop() {
			mSeekBar.setEnabled(true);
		}

	};
	
	 private List<Map<String, Object>> getTitlesList() {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        
        for(int i=0; i<mCountPages; ++i) {
	        Map<String, Object> map = new HashMap<String, Object>();
	        map.put("title", mOnlineTitles[i]);
	        list.add(map);
        }
        
        return list;
	 }
	
	private void deleteBufferFiles() {
		/*
		 * 退出时将缓存内容清空, 否则多次运行原SDK方法缓存所造成的内存消耗是比较大的
		 */
		String clearPath = "/sdcard/Android/data/com.asus.cnmusic/files/player_caching/audio";
		File clearFile = new File(clearPath);
		LocalMusicUtils.deleteFiles(clearFile);
		clearPath = "/data/data/com.asus.cnmusic/cache";
		clearFile = new File(clearPath);
		LocalMusicUtils.deleteFiles(clearFile);
	}
	
	public void pauseLocalPlaying() {
		//only used to test--stop local music playing if it was *******************************
		if(mPlayingInLocal) {
			
			mPlayingInLocal = false;
			
			mLocalFragment = LocalFragment.getInstance();
			if(mLocalFragment.mMediaPlayer!=null && mLocalFragment.mMediaPlayer.isPlaying()) {
				mLocalFragment.actionPauseOrPlay();
			}
		}
	}

	@Override
	public void onDestroyView() {
		Log.i(TAG, "XiMaLaYaFragment onDestroyView");
		
		if(mXmPlayerManager != null) {
			mXmPlayerManager.stop();
			mXmPlayerManager.removePlayerStatusListener(mPlayerStatusListener);
			mXmPlayerManager.removeAdsStatusListener(mAdsListener);
			mXmPlayerManager.release();
		}
		
		deleteBufferFiles();
		
		super.onDestroyView();
	}
}
