package com.asus.cnmusic.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.asus.cnmusic.info.GetLocalMusic;
import com.asus.cnmusic.info.LocalMusic;
import com.asus.cnmusic.util.LocalMusicUtils;
import com.asus.cnmusic.view.ViewHolder;
import com.asus.cnmusic.R;

import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class LocalArtistFragment extends BaseFragment {
	private String mArtistName;
	private List<LocalMusic> mArtistInfoList;
	private List<Map<String, Object>> mArtistInfoMapList;

    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
            final Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.local_list, container, false);
        
        mMusicInfoListView = (ListView) mRootView.findViewById(R.id.music_info_list);
        
        return mRootView;
    }
    
    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
        Log.i(TAG, "LocalArtistFragment onActivityCreated");
        
        mContext= getContext();
        
        mLocalFragment = (LocalFragment) getParentFragment();
        
        initListView();
    }
    
    @Override
    public void initListView() {
    	mInMusicList = false;
    	mInAlbumMusicList = false;
    	mInArtistMusicList = false;
    	mInHistoryMusicList = false;
    	
        mArtistInfoList = GetLocalMusic.getLocalMusic(mContext, null, null, LocalMusicUtils.mArtistSortOrder);
        getArtistInfoMapList();
        getArtistInfoListAdapter();
        if(mMusicInfoListView == null) {
        	mMusicInfoListView = (ListView) mRootView.findViewById(R.id.music_info_list);
        }
    	mMusicInfoListView.setAdapter(mLocalMusicListAdapter);
    	mMusicInfoListView.setOnItemClickListener(new OnItemClickListener() {
        	
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				mInArtistMusicList = true;
				mArtistName = (String) mArtistInfoMapList.get(position).get("artist");
				mLocalMusicList = GetLocalMusic.getLocalMusic(mContext, MediaStore.Audio.Media.ARTIST+"=?", 
						new String[]{mArtistName}, LocalMusicUtils.mMusicSortOrder);
				getLocalMusicMapList();
				getLocalMusicListAdapter();
				mMusicInfoListView.setAdapter(mLocalMusicListAdapter);
				mMusicInfoListView.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
						setParentMusicInfoList(false);	
						
						if(mPlayingInArtistMusicList && position == mPlayingPosition 
								&& mLocalFragment.isMusicEquals(mLocalMusicList.get(position))) {
							if(mLocalMusicPlaying) {
								return;
							}else {
								mLocalFragment.actionPauseOrPlay();
								return;
							}
						}
						
						mPlayingInMusicList = false;
						mPlayingInAlbumMusicList = false;
						mPlayingInArtistMusicList = true;
						mPlayingInHistoryMusicList = false;

						mLocalFragment.MusicPlay(position);
					}
				});
			}
		});
    }
    
    public void getArtistInfoMapList() {
    	if(mArtistInfoMapList == null) {
    		mArtistInfoMapList = new ArrayList<Map<String, Object>>();
    	}
    	mArtistInfoMapList.clear();
    	
    	Set<String> artistNameSet = new TreeSet<String>(LocalMusicUtils.comparatorString);
    	for(int i=0; i<mArtistInfoList.size(); ++i) {
        	artistNameSet.add(mArtistInfoList.get(i).getArtist());
        }
    	
    	int artistCountArray[] = new int[artistNameSet.size()];
    	int index = 0;
    	for(Iterator<String>iter = artistNameSet.iterator(); iter.hasNext();) {
    		String artistNameInSet = iter.next();
    		for(int i=0; i<mArtistInfoList.size(); ++i) {
            	if(artistNameInSet.equals(mArtistInfoList.get(i).getArtist())) {
            		artistCountArray[index] += 1;
            	}
            }
    		Map<String, Object> map = new HashMap<String, Object>();
    		map.put("artist", artistNameInSet);
    		map.put("count", artistCountArray[index]+" 首 ");
    		mArtistInfoMapList.add(map);
        	++index;
    	}
    }
    
    public void getArtistInfoListAdapter() {
    	mLocalMusicListAdapter = new SimpleAdapter(mContext, mArtistInfoMapList, R.layout.local_item,
                new String[]{"artist", "count"},
                new int[]{R.id.left_top, R.id.left_bottom});
    }
    
    public void getLocalMusicMapList() {
    	if(mLocalMusicMapList == null) {
    		mLocalMusicMapList = new ArrayList<Map<String, Object>>();
    	}
    	mLocalMusicMapList.clear();
    	
        for(int i=0; i<mLocalMusicList.size(); ++i) {
        	LocalMusic localMusic = mLocalMusicList.get(i);
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
                new int[]{R.id.left_top, R.id.left_bottom, R.id.right}) {
        	@Override
        	public View getView(int position, View convertView, ViewGroup parent) {
        		ViewHolder holder;
    			if(convertView == null) {
    				convertView = LayoutInflater.from(mContext).inflate(
    						R.layout.local_item, parent, false);
    				holder = new ViewHolder();
    				holder.content = (ViewGroup) convertView;
    				holder.title = (TextView) convertView.findViewById(R.id.left_top);
    				holder.intro = (TextView) convertView.findViewById(R.id.left_bottom);
    				holder.status = (TextView) convertView.findViewById(R.id.right);
    				convertView.setTag(holder);
    			}else {
    				holder = (ViewHolder) convertView.getTag();
    			}
    			Map<String, Object> localMusicMap = mLocalMusicMapList.get(position);
    			holder.title.setText((CharSequence) localMusicMap.get("title"));
    			holder.intro.setText((CharSequence) localMusicMap.get("artist"));
    			holder.status.setText((CharSequence) localMusicMap.get("duration"));
    			//判断歌曲相同的条件为完整文件名(包括路径路径)
    			if(mPlayingInArtistMusicList && mLocalFragment.mMediaPlayer!=null 
    					&& mLocalFragment.isMusicEquals(mLocalMusicList.get(position))) {
        	    	holder.content.setBackgroundResource(R.color.list_item_pressed_bg);
        	    }else {
        	    	holder.content.setBackgroundResource(R.color.list_item_normal_bg);
        	    }
    			
    			return convertView;
        	}
    	};
    }
    
    @Override
    public void updateLocalMusicListAdapter(int targetPosition) {
    	String removeArtistName = "";
    	if(mLocalMusicList != null) {
    		removeArtistName = mLocalMusicList.get(targetPosition).getArtist();
    	}
    	
    	setParentMusicInfoList(true);
    	
    	if(mInArtistMusicList && removeArtistName.equals(mArtistName)) {
        	getLocalMusicMapList();
        	mLocalMusicListAdapter.notifyDataSetChanged();
    	}else if(!mInMusicList && !mInHistoryMusicList) {
    		initListView();
    	}
    }
    
    public void setParentMusicInfoList(boolean needAccessFlag) {
    	if(needAccessFlag) {
    		mLocalMusicList = GetLocalMusic.getLocalMusic(mContext, MediaStore.Audio.Media.ARTIST+"=?", 
    				new String[]{mArtistName}, LocalMusicUtils.mMusicSortOrder);
    	}
    	
    	if(mLocalPlayList != null) {
			mLocalPlayList.clear();
			mLocalPlayList = null;
		}
		mLocalPlayList = new ArrayList<LocalMusic>(mLocalMusicList);
		
		mLocalFragment.isPlayingListEmpty();
    }
    
    @Override
	public void onDestroyView() {
    	Log.i(TAG, "LocalArtistFragment onDestroyView");
    	
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