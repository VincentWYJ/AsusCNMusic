package com.asus.cnmusic.fragment;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.asus.cnmusic.info.LocalMusic;
import com.asus.cnmusic.src_gen.HistoryMusicNote;
import com.asus.cnmusic.src_gen.dao.DaoMaster;
import com.asus.cnmusic.src_gen.dao.DaoSession;
import com.asus.cnmusic.src_gen.dao.HistoryMusicNoteDao;
import com.asus.cnmusic.util.LocalMusicUtils;
import com.ximalaya.ting.android.opensdk.player.XmPlayerManager;
import com.asus.cnmusic.R;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import de.greenrobot.dao.query.Query;

@SuppressLint({ "ResourceAsColor", "InflateParams", "CutPasteId" })
public class LocalFragment extends BaseFragment implements OnClickListener {
	public static LocalFragment mInstance;
	
	private Uri mUri;
	private String mSongPath;
	public MediaPlayer mMediaPlayer;

	private int mViewPagerIndex;
	private int mViewPagerPreIndex;
	
	private List<Button> mTitlesBtnList;
	
	private int mCursorOffset;
	
	private static ImageView mImageViewCursor;
	private static SeekBar mMusicPlaySeekBar;
	private static TextView mMusicPlayName;
	private static TextView mMusicPlayTime;
	private static TextView mMusicEndTime;
	private static FragmentAdapter mFragmentAdapter;
	
	public static LocalFragment getInstance() {
        if(mInstance == null) {
        	mInstance = new LocalFragment();
        }
        return mInstance;
    }
	

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int tempPageIndex = -1;
		mViewPagerPreIndex = mViewPagerIndex;
		switch(v.getId()) {
		case R.id.music_play_pre:
			if(!isPlayingListEmpty() && mMediaPlayer!=null) {
				MusicPlay((mLocalPlayingList.size()+mPlayingPosition-1) % mLocalPlayingList.size());
			}
			break;
		case R.id.music_play_next:
			if(!isPlayingListEmpty() && mMediaPlayer!=null) {
				MusicPlay((mPlayingPosition+1) % mLocalPlayingList.size());
			}
			break;
		case R.id.music_play_pause:
			if(!isPlayingListEmpty()) {
				actionPauseOrPlay();
			}
			break;
		case R.id.local_music_title:
			tempPageIndex = mViewPagerIndex = 0;
			if(mViewPagerIndex != mViewPagerPreIndex) {
				mViewPager.setCurrentItem(mViewPagerIndex);
			}
			break;
		case R.id.local_album_title:
			tempPageIndex = mViewPagerIndex = 1;
			if(mViewPagerIndex != mViewPagerPreIndex) {
				mViewPager.setCurrentItem(mViewPagerIndex);
			}
			break;
		case R.id.local_artist_title:
			tempPageIndex = mViewPagerIndex = 2;
			if(mViewPagerIndex != mViewPagerPreIndex) {
				mViewPager.setCurrentItem(mViewPagerIndex);
			}
			break;
		case R.id.local_history_title:
			tempPageIndex = mViewPagerIndex = 3;
			if(mViewPagerIndex != mViewPagerPreIndex) {
				mViewPager.setCurrentItem(mViewPagerIndex);
			}
			break;
		default:
			break;
		}
		
		if(tempPageIndex != -1 && mViewPagerIndex == mViewPagerPreIndex) {
			mFragmentList.get(tempPageIndex).initListView();
		}
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mRootView = inflater.inflate(R.layout.local_fragment, container, false);
        
        mViewPager = (ViewPager) mRootView.findViewById(R.id.musicinfo_list_viewpager);
        mMusicPlaySeekBar = (SeekBar)mRootView.findViewById(R.id.music_play_seekbar);
        mMusicPlayTime = (TextView)mRootView.findViewById(R.id.music_time_play);
        mMusicPlayName = (TextView)mRootView.findViewById(R.id.music_play_name);
        mMusicEndTime = (TextView)mRootView.findViewById(R.id.music_time_end);
        mMusicPlayPause = (ImageView)mRootView.findViewById(R.id.music_play_pause);
        mMusicPlayPre = (ImageView)mRootView.findViewById(R.id.music_play_pre);
        mMusicPlayNext = (ImageView)mRootView.findViewById(R.id.music_play_next);
        mImageViewCursor= (ImageView) mRootView.findViewById(R.id.cursor);

        mTitlesBtnList = new ArrayList<Button>();
        mTitlesBtnList.add((Button)mRootView.findViewById(R.id.local_music_title));
        mTitlesBtnList.add((Button)mRootView.findViewById(R.id.local_album_title));
        mTitlesBtnList.add((Button)mRootView.findViewById(R.id.local_artist_title));
        mTitlesBtnList.add((Button)mRootView.findViewById(R.id.local_history_title));
        
		return mRootView;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
        Log.i(TAG, "LocalFragment onActivityCreated");
        
        mContext= getContext();
        
        mRemoteView = LocalMusicUtils.getRemoteViews();
        
        mMusicPlayPause.setOnClickListener(this);
        mMusicPlayPre.setOnClickListener(this);
        mMusicPlayNext.setOnClickListener(this);
        
        mTitlesBtnList.get(0).setSelected(true);
        
        mCountPages = mTitlesBtnList.size();
        
        for(int i=0; i<mCountPages; ++i) {
        	mTitlesBtnList.get(i).setOnClickListener(this);
        }

        mFragmentList = new ArrayList<BaseFragment>();
        mFragmentList.add(new LocalMusicFragment());
        mFragmentList.add(new LocalAlbumFragment());
        mFragmentList.add(new LocalArtistFragment());
        mFragmentList.add(new LocalHistoryFragment());

        mFragmentAdapter = new FragmentAdapter(getChildFragmentManager());
        mViewPager.setAdapter(mFragmentAdapter);
        mViewPager.setOffscreenPageLimit(mCountPages);

        DisplayMetrics mDisplayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);
        int screenWidth = mDisplayMetrics.widthPixels;
        mCursorOffset = screenWidth / mCountPages;

        mMusicPlaySeekBar.setProgress(0);
        mMusicPlayTime.setText("0:00");
        mMusicPlayName.setText(R.string.music_no_playing);
        mMusicEndTime.setText("0:00");

        setViewPagerChangeListener();

        setSeekBarOnClickListener();
		setSeekBarMoveListener();
        
    	mViewPagerIndex = 0;
    	mViewPagerPreIndex = 0;
    }
    
    @SuppressWarnings("deprecation")
	public void setViewPagerChangeListener() {
    	mViewPager.setOnPageChangeListener(new OnPageChangeListener() {
    		
        	@Override
        	public void onPageScrollStateChanged(int arg0) {
        	}

        	@Override
        	public void onPageScrolled(int arg0, float arg1, int arg2) {
        	}

        	@Override
        	public void onPageSelected(int arg0) {
        		//Log.i(TAG, " "+preIndexViewPager+" "+arg0);
        		Animation animation = new TranslateAnimation(mCursorOffset*mViewPagerPreIndex, mCursorOffset*arg0, 0, 0);
                animation.setFillAfter(true);
                animation.setDuration(300);
                mImageViewCursor.startAnimation(animation);
                
        		mViewPagerIndex = arg0;
        		mViewPagerPreIndex = mViewPagerIndex;
        		
        		for(int i=0; i<mCountPages; ++i) {
        			mTitlesBtnList.get(i).setSelected(false);
        		}
        		
        		mTitlesBtnList.get(mViewPagerIndex).setSelected(true);
        		mFragmentList.get(mViewPagerIndex).initListView();
        	}
        });
    }
    
    public void setSeekBarOnClickListener() {
    	mMusicPlaySeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				if(mMediaPlayer != null) {
					mMediaPlayer.seekTo(seekBar.getProgress());
				}
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				float duration = progress/60.0f/1000.0f;
	        	int pre = (int)duration;
	        	int suf = (int)((duration-pre)*60);
	        	mMusicPlayTime.setText(String.valueOf(pre)+":"+LocalMusicUtils.decimalFormat.format(suf));
			}
		});
    }
    
    public void setSeekBarMoveListener() {
    	new Thread(new Runnable() {
			
			@Override
			public void run() {
				while (true) {
	            	try {
						Thread.sleep(500);
						if(mLocalMusicPlaying) {
							mMusicPlaySeekBar.setProgress(mMediaPlayer.getCurrentPosition());
						}
					}catch(InterruptedException e) {
						e.printStackTrace();
					}
	            }
			}
		}).start();
    }
	
    public void actionPauseOrPlay() {
    	LocalMusic localMusic = mLocalPlayingList.get(mPlayingPosition);
    	if(mLocalMusicPlaying) {
			mLocalMusicPlaying = false;
			mMediaPlayer.pause();
			mMusicPlayPause.setBackgroundResource(R.drawable.music_pause_drawable);
			mSongPath = localMusic.getPath();
			File songFile = new File(mSongPath);
			if(!songFile.exists()) {
				UpdateMusicInfo(mPlayingPosition);
				updateNotification("Title", "Artist", true, true);
				return;
			}
			updateNotification(localMusic.getTitle(), localMusic.getArtist(), true, true);
		}else {
			if(mMediaPlayer != null) {
				mSongPath = localMusic.getPath();
				if(mSongPath.equals(mPlayingMusicPath)) {
					File songFile = new File(mSongPath);
					if(!songFile.exists()) {
						UpdateMusicInfo(mPlayingPosition);
						return;
					}
					//only used to test--stop ximalaya music playing if it was *******************************
			    	pauseOnlinePlaying();
			    	
					mLocalMusicPlaying = true;
					mMediaPlayer.start();
					mMusicPlayPause.setBackgroundResource(R.drawable.music_start_drawable);
					updateNotification(localMusic.getTitle(), localMusic.getArtist(), false, true);
				}
			}
		}
    }
    
    public void MusicPlay(final int position) {
    	//only used to test--stop ximalaya music playing if it was *******************************
    	pauseOnlinePlaying();
    	
    	final LocalMusic localMusic = mLocalPlayingList.get(position);
    	
    	mLocalMusicPlaying = false;
		
		mSongPath = localMusic.getPath();
		
		File songFile = new File(mSongPath);
		if(!songFile.exists()) {
			UpdateMusicInfo(position);
			return;
		}
		
		mUri = Uri.fromFile(songFile);
		
		try {
			if(mMediaPlayer != null) {
				resetMediaPlayer();
			}else {
				mMediaPlayer = new MediaPlayer();
			}
			mMediaPlayer.setDataSource(mContext, mUri);
			mMediaPlayer.setOnPreparedListener(new OnPreparedListener() {
				
				@Override
				public void onPrepared(MediaPlayer mp) {
					mMediaPlayer.start();
					mLocalMusicPlaying = true;
					mPlayingPosition = position;
					setMusicViewInfos();
					updateNotification(localMusic.getTitle(), localMusic.getArtist(), false, true);
				}
			});
			mMediaPlayer.prepareAsync();
		}catch(IllegalStateException e) {
			e.printStackTrace();
		}catch(IOException e) {
			e.printStackTrace();
		}
    }
    
    private void resetMediaPlayer() {
    	if(mMediaPlayer.isPlaying()) {
			mMediaPlayer.pause();
			mMediaPlayer.stop();
		}
		mMediaPlayer.reset();
    }
    
    public void UpdateMusicInfo(int position) {
    	
    	Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
    	scanIntent.setData(Uri.fromFile(Environment.getExternalStorageDirectory()));
    	mContext.sendBroadcast(scanIntent);

		if(mMediaPlayer != null && mMediaPlayer.isPlaying() && mPlayingPosition != position) {
			mLocalMusicPlaying = true;
			if(mPlayingPosition > position) {
				mPlayingPosition -= 1;
			}
		}else {
			if(mMediaPlayer != null) {
				resetMediaPlayer();
			}

			resetPlayingInfo();
			
			if(mPlayingPosition == mLocalPlayingList.size()-1) {
				mPlayingPosition = 0;
			}
		}
		
		int tempPageIndex = getTempPageIndex();
		mFragmentList.get(tempPageIndex).updateLocalMusicListAdapter(position);
		
    }
    
    private int getTempPageIndex() {
    	return mPlayingInMusicList ? 0 : 
    		mPlayingInAlbumMusicList ? 1 : 
    		mPlayingInArtistMusicList ? 2 : 
    		mPlayingInHistoryMusicList ? 3 : 
    		-1;
    }
    
    private void resetPlayingInfo() {
    	mMusicPlayPause.setBackgroundResource(R.drawable.music_pause_drawable);
    	
		updateNotification("名称", "信息", true, true);
		
		mMusicPlaySeekBar.setProgress(0);
        mMusicPlayTime.setText("0:00");
        mMusicEndTime.setText("0:00");
    }
    
    public void handleFileDelete() {
    	mLocalMusicPlaying = false;
    	
    	if(mMediaPlayer != null) {
	    	resetPlayingInfo();
	    	
	    	mMusicPlayName.setText(R.string.music_no_playing);
    	}
    }
    
    public void setMusicViewInfos() {
    	LocalMusic localMusic = mLocalPlayingList.get(mPlayingPosition);
    	
    	mPlayingMusicTitle = localMusic.getTitle();
    	mPlayingMusicPath = localMusic.getPath();
    	
    	int tempPageIndex = getTempPageIndex();
    	mFragmentList.get(tempPageIndex).mLocalMusicListAdapter.notifyDataSetChanged();
    	
    	//Log.i(TAG, "tempPageIndex: "+tempPageIndex);
    	
    	mMusicPlaySeekBar.setMax(localMusic.getDuration());
    	mMusicPlayName.setText(localMusic.getTitle());
    	float duration = (float) (localMusic.getDuration()/60.0/1000.0);
    	int pre = (int)duration;
    	float suf = (duration-pre)*60;
        mMusicEndTime.setText(String.valueOf(pre)+":"+LocalMusicUtils.decimalFormat.format(suf));
		mMediaPlayer.setOnCompletionListener(new OnCompletionListener() {
			
			@Override
			public void onCompletion(MediaPlayer mp) {
				mMusicPlayPause.setBackgroundResource(R.drawable.music_pause_drawable);
				String str = mMusicEndTime.getText().toString();
				int result = Integer.parseInt(str.substring(str.indexOf(":")+1));
				float duration = (float)((mMediaPlayer.getCurrentPosition())/60.0/1000.0);
	        	int pre = (int)duration;
	        	int suf = (int)((duration-pre)*60);
	        	if(suf != result) {
	        		++suf;
	        		if(suf == 60) {
	        			suf = 0;
	        			++pre;
	        		}
	        	}
	        	mMusicPlayTime.setText(String.valueOf(pre)+":"+LocalMusicUtils.decimalFormat.format(suf));
	        	if(!isPlayingListEmpty()) {
	        		MusicPlay((mPlayingPosition+1) % mLocalPlayingList.size());
	        	}
			}
		});
		mMusicPlayPause.setBackgroundResource(R.drawable.music_start_drawable);
		
    	insertHistoryMusic(localMusic);
    }
    
    public class FragmentAdapter extends FragmentPagerAdapter{
        public FragmentAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }
        
        @Override
        public Fragment getItem(int arg0) {
            return mFragmentList.get(arg0);
        }
        
        @Override
        public int getCount() {
            return mCountPages;
        }
    } 
    
	public void updateNotification(String title, String artist, boolean isPlaying, boolean hasNext) {
		if(!TextUtils.isEmpty(title)) {
			mRemoteView.setTextViewText(R.id.txt_notifyMusicName, title);
		}
		if(!TextUtils.isEmpty(artist)) {
			mRemoteView.setTextViewText(R.id.txt_notifyNickName, artist);
		}
		if(isPlaying) {
			mRemoteView.setImageViewResource(R.id.img_notifyPlayOrPause, R.drawable.music_pause_drawable);
		}else {
			mRemoteView.setImageViewResource(R.id.img_notifyPlayOrPause, R.drawable.music_start_drawable);
		}
		mRemoteView.setImageViewResource(R.id.img_notifyIcon, R.drawable.ic_error_image);
		LocalMusicUtils.sendNotification();
	}
	
	public boolean isMusicEquals(LocalMusic localMusic) {
		return mPlayingMusicPath.equals(localMusic.getPath());
	}
	
	public boolean isPlayingListEmpty() {
		if(mLocalPlayingList == null || mLocalPlayingList.size() == 0) {
    		return true;
    	}
		
		return false;
	}
	
	private HistoryMusicNoteDao getHistoryMusicNoteDao() {
		DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(mContext, "AsusCNMusic.db", null);
		SQLiteDatabase db = helper.getWritableDatabase();
		DaoMaster daoMaster = new DaoMaster(db);
		DaoSession daoSession = daoMaster.newSession();
		
		return daoSession.getHistoryMusicNoteDao();
	}
	
	public List<LocalMusic> getHistoryMusicList() {
	    List<LocalMusic> mLocalMusicsMusicList = new ArrayList<LocalMusic>();

	    HistoryMusicNoteDao historyMusicNoteDao = getHistoryMusicNoteDao();
	    List<HistoryMusicNote> historyMusicList = historyMusicNoteDao.loadAll();
	    for(int i=0; i<historyMusicList.size(); ++i) {
	        HistoryMusicNote historyMusicNote = historyMusicList.get(i);
	        if(new File(historyMusicNote.getPath()).exists()) {
		        Bundle bundle = new Bundle();
		        bundle.putString(MediaStore.Audio.Media.TITLE, historyMusicNote.getTitle());
		        bundle.putString(MediaStore.Audio.Media.DATA, historyMusicNote.getPath());
		        bundle.putString(MediaStore.Audio.Media.ALBUM, historyMusicNote.getAlbum());
		        bundle.putString(MediaStore.Audio.Media.ARTIST, historyMusicNote.getArtist());
		        bundle.putInt(MediaStore.Audio.Media.DURATION, historyMusicNote.getDuration());
		        LocalMusic localMusic = new LocalMusic(bundle);
		        mLocalMusicsMusicList.add(localMusic);
	        }
	    }
	    
	    Collections.sort(mLocalMusicsMusicList, LocalMusicUtils.comparatorLocalMusic);

	    return mLocalMusicsMusicList;
	}

	public void insertHistoryMusic(LocalMusic localMusicArg) {
	    HistoryMusicNoteDao musicHistoryNoteDao = getHistoryMusicNoteDao();
	    HistoryMusicNote musicHistoryNote;
	    int count = 1;
	    Query<HistoryMusicNote> historyMusicQuery = musicHistoryNoteDao.queryBuilder().where(
	    		HistoryMusicNoteDao.Properties.Title.eq(localMusicArg.getTitle()), 
	    		HistoryMusicNoteDao.Properties.Path.eq(localMusicArg.getPath()),
	    		HistoryMusicNoteDao.Properties.Album.eq(localMusicArg.getAlbum()),
	    		HistoryMusicNoteDao.Properties.Artist.eq(localMusicArg.getArtist())
	    ).build();
	    List<HistoryMusicNote> historyMusicList = historyMusicQuery.list();
	    //Log.i(LocalMusicUtils.TAG, ""+historyMusicList.size());
	    /*
	    * 如果不为0则表示之前已有该歌曲的播放记录，这里只需要将其播放次数增加即可
	    * 条件写成historyMusicList.size == 1也无碍
	    */
	    if(historyMusicList.size() > 0) {
	        musicHistoryNote = (HistoryMusicNote) historyMusicList.get(0);
	        count = musicHistoryNote.getCount()+1;
	        musicHistoryNote.setCount(count);
	        musicHistoryNoteDao.update(musicHistoryNote);
	    }else {
	        musicHistoryNote = new HistoryMusicNote(
	        null, 
	        localMusicArg.getTitle(), 
	        localMusicArg.getAlbum(), 
	        localMusicArg.getArtist(), 
	        localMusicArg.getDuration(), 
	        localMusicArg.getPath(), 
	        count);
	        musicHistoryNoteDao.insert(musicHistoryNote);
	    }
	}

	public void deleteHistoryMusic(LocalMusic localMusicArg) {
	    HistoryMusicNoteDao musicHistoryNoteDao = getHistoryMusicNoteDao();
	    HistoryMusicNote musicHistoryNote;
	    Long key;
	    Query<HistoryMusicNote> historyMusicQuery = musicHistoryNoteDao.queryBuilder().where(
	    		HistoryMusicNoteDao.Properties.Title.eq(localMusicArg.getTitle()), 
	    		HistoryMusicNoteDao.Properties.Path.eq(localMusicArg.getPath()),
	    		HistoryMusicNoteDao.Properties.Album.eq(localMusicArg.getAlbum()),
	    		HistoryMusicNoteDao.Properties.Artist.eq(localMusicArg.getArtist())
	    ).build();
	    List<HistoryMusicNote> historyMusicList = historyMusicQuery.list();
	    if(historyMusicList.size() > 0) {
	        musicHistoryNote = (HistoryMusicNote) historyMusicList.get(0);
	        key = musicHistoryNoteDao.getKey(musicHistoryNote);
	        musicHistoryNoteDao.deleteByKey(key);
	    }
	}
	
	public void pauseOnlinePlaying() {
		//only used to test--stop online music playing if it was *******************************
		if(!mPlayingInLocal) {
			mPlayingInLocal = true;
			
			mXmPlayerManager = XmPlayerManager.getInstance(mContext);
	    	if(mXmPlayerManager.isPlaying())
			{
	    		mXmPlayerManager.pause();
			}
		}
	}
	
    @Override
	public void onDestroyView() {
    	Log.i(TAG, "LocalFragment onDestroyView");
    	
    	mLocalMusicPlaying = false;
    	
    	if(mLocalPlayingList != null) {
			mLocalPlayingList.clear();
			mLocalPlayingList = null;
		}
    	if(mFragmentList != null) {
	    	mFragmentList.clear();
	    	mFragmentList = null;
    	}
    	
    	if(mMediaPlayer != null) {
    		resetMediaPlayer();
			mMediaPlayer.release();
			mMediaPlayer = null;
		}
    	
    	super.onDestroyView();
    }
}