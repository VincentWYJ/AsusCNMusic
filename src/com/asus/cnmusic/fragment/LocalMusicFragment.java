package com.asus.cnmusic.fragment;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.asus.cnmusic.info.GetLocalMusic;
import com.asus.cnmusic.info.LocalMusic;
import com.asus.cnmusic.util.LocalMusicUtils;
import com.asus.cnmusic.view.ViewHolder;
import com.asus.cnmusic.R;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

public class LocalMusicFragment extends BaseFragment {
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
            final Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.local_list, container, false);
        
        mMusicInfoListView = (ListView) mRootView.findViewById(R.id.music_info_list);
        
        return mRootView;
    }
    
    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
    	Log.i(TAG, "LocalMusicFragment onActivityCreated");
        
        mContext= getContext();
        
        mLocalFragment = (LocalFragment) getParentFragment();
        
        initListView();
    }
    
    @Override
    public void initListView() {
    	mInMusicList = true;
    	mInAlbumMusicList = false;
    	mInArtistMusicList = false;
    	mInHistoryMusicList = false;
    	
    	if(!new File(mPlayingMusicPath).exists()) {
    		mLocalFragment.handleFileDelete();
    	}
    	
    	mLocalMusicList = GetLocalMusic.getLocalMusic(mContext, null, null, LocalMusicUtils.mMusicSortOrder);
    	
      	if(mLocalPlayingList == null) {
      		mLocalPlayingList = new ArrayList<LocalMusic>(mLocalMusicList);
      	}
      	
        getLocalMusicMapList();
        getLocalMusicListAdapter();
        mMusicInfoListView.setAdapter(mLocalMusicListAdapter);
        mMusicInfoListView.setOnItemClickListener(new OnItemClickListener() {
        	
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				setParentMusicInfoList(false);
				
				if(mPlayingInMusicList && position == mPlayingPosition 
						&& mLocalFragment.isMusicEquals(mLocalMusicList.get(position))) {
					if(mLocalMusicPlaying) {
						return;
					}else {
						mLocalFragment.actionPauseOrPlay();
						return;
					}
				}
				
				mPlayingInMusicList = true;
				mPlayingInAlbumMusicList = false;
				mPlayingInArtistMusicList = false;
				mPlayingInHistoryMusicList = false;

				mLocalFragment.MusicPlay(position);
			}
		});
        mMusicInfoListView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
				LocalMusic localMusic = mLocalMusicList.get(position);
				
				File file = new File(localMusic.getPath());
				if(file.exists()) {
					//mLocalMusicPlaying = false;
					file.delete();
					
					mLocalFragment.deleteHistoryMusic(localMusic);
					
					if(mLocalFragment.isMusicEquals(localMusic)){  //不管在哪个列表, 正在播放歌曲做不存在处理
						mLocalFragment.handleFileDelete();
					}
					
					Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
			    	scanIntent.setData(Uri.fromFile(file));
			    	mContext.sendBroadcast(scanIntent);
				}
				
				((Activity) mContext).runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
		            	try{
							Thread.sleep(1000);
							if(mPlayingInMusicList) {
								mLocalFragment.UpdateMusicInfo(position);
							}
							else{
								mLocalMusicList = mLocalFragment.getHistoryMusicList();
						        getLocalMusicMapList();
								mLocalMusicListAdapter.notifyDataSetChanged();
							}
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				});
				
				return true;
			}
		});
    }
    
    public void getLocalMusicMapList() {
    	if(mLocalMusicMapList == null) {
    		mLocalMusicMapList = new ArrayList<Map<String, Object>>();
    	}
    	mLocalMusicMapList.clear();
        for(int i=0; i<mLocalMusicList.size(); ++i) {
        	LocalMusic localMusic = mLocalMusicList.get(i);
        	//Log.i(TAG, localMusic.getTitle());  //打印歌曲名称查看排序情况
        	Map<String, Object> map = new HashMap<String, Object>();
        	map.put("title", localMusic.getTitle());
        	map.put("path", localMusic.getPath());
        	map.put("album", localMusic.getAlbum());
        	map.put("artist", localMusic.getArtist());
        	float duration = (float) (localMusic.getDuration()/60.0/1000.0);
        	int pre = (int)duration;
        	float suf = (duration-pre)*60;
        	map.put("duration",String.valueOf(pre)+":"+LocalMusicUtils.decimalFormat.format(suf));
        	mLocalMusicMapList.add(map);
        }
	}

    public void getLocalMusicListAdapter() {
    	mLocalMusicListAdapter = new SimpleAdapter(mContext, mLocalMusicMapList, R.layout.local_item,
                new String[]{"title", "artist", "duration"},
                new int[]{R.id.left_top, R.id.left_bottom, R.id.right})
    	{
        	@Override
        	public View getView(int position, View convertView, ViewGroup parent) {
        		ViewHolder holder;
    			if(convertView == null)
    			{
    				convertView = LayoutInflater.from(mContext).inflate(
    						R.layout.local_item, parent, false);
    				holder = new ViewHolder();
    				holder.content = (ViewGroup) convertView;
    				holder.title = (TextView) convertView.findViewById(R.id.left_top);
    				holder.intro = (TextView) convertView.findViewById(R.id.left_bottom);
    				holder.status = (TextView) convertView.findViewById(R.id.right);
    				convertView.setTag(holder);
    			}
    			else
    			{
    				holder = (ViewHolder) convertView.getTag();
    			}
    			Map<String, Object> localMusicMap = mLocalMusicMapList.get(position);
    			holder.title.setText((CharSequence) localMusicMap.get("title"));
    			holder.intro.setText((CharSequence) localMusicMap.get("artist"));
    			holder.status.setText((CharSequence) localMusicMap.get("duration"));
    			//判断歌曲相同的条件为完整文件名(包括路径路径)
    			if(mPlayingInMusicList && mLocalFragment.mMediaPlayer!=null 
    					&& mLocalFragment.isMusicEquals(mLocalMusicList.get(position))) {
        	    	holder.content.setBackgroundResource(R.color.list_item_pressed_bg);
        	    }else{
        	    	holder.content.setBackgroundResource(R.color.list_item_normal_bg);
        	    }
    			return convertView;
        	}
    	};
    }
    
    @Override
    public void updateLocalMusicListAdapter(int targetPosition) {
    	setParentMusicInfoList(true);
    	
        getLocalMusicMapList();
		mLocalMusicListAdapter.notifyDataSetChanged();
    }
    
    public void setParentMusicInfoList(boolean needAccessFlag) {
    	if(needAccessFlag) {
    		mLocalMusicList = GetLocalMusic.getLocalMusic(mContext, null, null, LocalMusicUtils.mMusicSortOrder);
    	}
    	
    	if(mLocalPlayingList != null) {
			mLocalPlayingList.clear();
			mLocalPlayingList = null;
		}
		mLocalPlayingList = new ArrayList<LocalMusic>(mLocalMusicList);
		
		mLocalFragment.isPlayingListEmpty();
    }
    
    @Override
	public void onDestroyView() {
    	Log.i(TAG, "LocalMusicFragment onDestroyView");
    	
    	if(mLocalMusicList != null) {
	    	mLocalMusicList.clear();
	    	mLocalMusicList = null;
    	}
    	if(mLocalMusicMapList != null) {
	    	mLocalMusicMapList.clear();
	        mLocalMusicMapList = null;
    	}
        mLocalMusicListAdapter = null;
        
    	super.onDestroyView();
    }
}
