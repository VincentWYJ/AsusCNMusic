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

public class LocalAlbumFragment extends BaseFragment {
	private String mAlbumName;
	private List<LocalMusic> mAlbumInfoList;
	private List<Map<String, Object>> mAlbumInfoMapList;

    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
            final Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.local_list, container, false);
        
        mMusicInfoListView = (ListView) mRootView.findViewById(R.id.music_info_list);
        
        return mRootView;
    }
    
    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
        Log.i(TAG, "LocalAlbumFragment onActivityCreated");
        
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
    	
        mAlbumInfoList = GetLocalMusic.getLocalMusic(mContext, null, null, LocalMusicUtils.mAlbumSortOrder);
        getAlbumInfoMapList();
        getAlbumInfoListAdapter();
        if(mMusicInfoListView == null) {
        	mMusicInfoListView = (ListView) mRootView.findViewById(R.id.music_info_list);
        }
    	mMusicInfoListView.setAdapter(mLocalMusicListAdapter);
    	mMusicInfoListView.setOnItemClickListener(new OnItemClickListener() {
        	
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				mInAlbumMusicList = true;
				mAlbumName = (String) mAlbumInfoMapList.get(position).get("album");
				mLocalMusicList = GetLocalMusic.getLocalMusic(mContext, MediaStore.Audio.Media.ALBUM+"=?", 
						new String[]{mAlbumName}, LocalMusicUtils.mMusicSortOrder);
				getLocalMusicMapList();
				getLocalMusicListAdapter();
				mMusicInfoListView.setAdapter(mLocalMusicListAdapter);
				mMusicInfoListView.setOnItemClickListener(new OnItemClickListener() {
		        	
					@Override
					public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
						setParentMusicInfoList(false);
						
						if(mPlayingInAlbumMusicList && position == mPlayingPosition) {
							if(mMusicPlaying) {
								return;
							}else {
								mLocalFragment.actionPauseOrPlay();
								return;
							}
						}
						
						mPlayingInMusicList = false;
						mPlayingInAlbumMusicList = true;
						mPlayingInArtistMusicList = false;
						mPlayingInHistoryMusicList = false;

						mLocalFragment.MusicPlay(position);
					}
				});
			}
		});
    }
    
	public void getAlbumInfoMapList() {
		if(mAlbumInfoMapList == null) {
			mAlbumInfoMapList = new ArrayList<Map<String, Object>>();
    	}
		mAlbumInfoMapList.clear();
		
    	Set<String> albumNameSet = new TreeSet<String>(LocalMusicUtils.comparatorString);
    	for(int i=0; i<mAlbumInfoList.size(); ++i) {
    		albumNameSet.add(mAlbumInfoList.get(i).getAlbum());
    	}

    	int albumCountArray[] = new int[albumNameSet.size()];
    	int index = 0;
    	for(Iterator<String>iter = albumNameSet.iterator(); iter.hasNext();) {
    		String albumNameInSet = iter.next();
    		String albumArtist = null;
    		for(int i=0; i<mAlbumInfoList.size(); ++i) {
    			LocalMusic mLocalPlayingList = mAlbumInfoList.get(i);
    			if(albumNameInSet.equals(mLocalPlayingList.getAlbum())) {
    				albumCountArray[index] += 1;
    				albumArtist = mLocalPlayingList.getArtist();
    			}
    		}
    		Map<String, Object> map = new HashMap<String, Object>();
    		map.put("album", albumNameInSet);
    		map.put("count", albumCountArray[index]+" 首 - "+albumArtist);
    		mAlbumInfoMapList.add(map);
    		++index;
    	}
    }

    public void getAlbumInfoListAdapter() {
    	mLocalMusicListAdapter = new SimpleAdapter(mContext, mAlbumInfoMapList, R.layout.local_item,
    			new String[]{"album", "count"},
    			new int[]{R.id.left_top, R.id.left_bottom});
    }
    
    public void getLocalMusicMapList() {
    	if(mLocalMusicMapList == null) {
    		mLocalMusicMapList = new ArrayList<Map<String, Object>>();
    	}
    	mLocalMusicMapList.clear();
    	
        for(int i=0; i<mLocalMusicList.size(); ++i) {
        	LocalMusic mLocalPlayingList = mLocalMusicList.get(i);
        	Map<String, Object> map = new HashMap<String, Object>();
        	map.put("title", mLocalPlayingList.getTitle());
        	map.put("path", mLocalPlayingList.getPath());
        	map.put("album", mLocalPlayingList.getAlbum());
        	map.put("artist", mLocalPlayingList.getArtist());
        	float duration = (float) (mLocalPlayingList.getDuration()/60.0/1000.0);
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
    			Map<String, Object> mLocalPlayingListMap = mLocalMusicMapList.get(position);
    			holder.title.setText((CharSequence) mLocalPlayingListMap.get("title"));
    			holder.intro.setText((CharSequence) mLocalPlayingListMap.get("artist"));
    			holder.status.setText((CharSequence) mLocalPlayingListMap.get("duration"));
    			//判断歌曲相同的条件为完整文件名(包括路径路径)
    			if(mPlayingInAlbumMusicList && mLocalFragment.mMediaPlayer!=null 
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
    	String removeAlbumName = "";
    	if(mLocalMusicList != null) {
    		removeAlbumName = mLocalMusicList.get(targetPosition).getArtist();
    	}
    	
    	setParentMusicInfoList(true);
    	
    	if(mInAlbumMusicList && removeAlbumName.equals(mAlbumName)) {
        	getLocalMusicMapList();
        	mLocalMusicListAdapter.notifyDataSetChanged();
    	}else if(!mInMusicList && !mInHistoryMusicList) {
    		initListView();
    	}
    }
    
    public void setParentMusicInfoList(boolean needAccessFlag) {
    	if(needAccessFlag) {
    		mLocalMusicList = GetLocalMusic.getLocalMusic(mContext, MediaStore.Audio.Media.ALBUM+"=?", 
    				new String[]{mAlbumName}, LocalMusicUtils.mMusicSortOrder);
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