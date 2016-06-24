package com.asus.cnmusic.fragment;

import java.util.HashMap;
import java.util.Map;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.ximalaya.ting.android.opensdk.constants.DTransferConstants;
import com.ximalaya.ting.android.opensdk.datatrasfer.CommonRequest;
import com.ximalaya.ting.android.opensdk.datatrasfer.IDataCallBack;
import com.ximalaya.ting.android.opensdk.model.PlayableModel;
import com.ximalaya.ting.android.opensdk.model.track.Track;
import com.ximalaya.ting.android.opensdk.model.track.TrackHotList;
import com.ximalaya.ting.android.opensdk.player.XmPlayerManager;
import com.ximalaya.ting.android.opensdk.player.service.IXmPlayerStatusListener;
import com.ximalaya.ting.android.opensdk.player.service.XmPlayerException;
import com.asus.cnmusic.view.ViewHolder;
import com.asus.cnmusic.R;
import com.bumptech.glide.Glide;

public class OnlineClassFragment extends BaseFragment{
	private final static String mClassIDKey = "ClassID";
	
	private TrackAdapter mTrackAdapter;

	private int mPageId;
	private TrackHotList mTrackHotList;
	private boolean mLoading;
	
	private int mPageIndex;
	
	public static final OnlineClassFragment newInstance(int classIDValue) {
        OnlineClassFragment f = new OnlineClassFragment();
        
        Bundle args = new Bundle();
        args.putInt(mClassIDKey, classIDValue);
        f.setArguments(args);
        
        return f;
    }
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState)
	{
		mRootView = inflater.inflate(R.layout.online_list, container, false);
		
		mMusicInfoListView = (ListView) mRootView.findViewById(R.id.listview);
		
		return mRootView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		
		mContext= getContext();
		
		mPageIndex = getArguments() != null ? getArguments().getInt(mClassIDKey) : 0;
		mPageId = 1;
		mLoading = false;
		
		Log.i(TAG, "Fragment_BangDan onActivityCreated "+mPageIndex);

		mCommonRequest = CommonRequest.getInstanse();
		mXmPlayerManager = XmPlayerManager.getInstance(mContext);
		mXmPlayerManager.addPlayerStatusListener(mPlayerStatusListener);

		mTrackAdapter = new TrackAdapter();
		mMusicInfoListView.setAdapter(mTrackAdapter);
		mMusicInfoListView.setOnScrollListener(new OnScrollListener()
		{

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState)
			{
				if(scrollState == SCROLL_STATE_IDLE)
				{
					int count = view.getCount();
					count = count - 5 > 0 ? count - 5 : count - 1;  //这里的5是很据实际页面显示的item数目来设定的
					if(view.getLastVisiblePosition() > count
							&& (mTrackHotList == null || mPageId <= mTrackHotList.getTotalPage()))  
						//允许加载最大页数, 记住加载了一页, 其值变为2, 所以要想加载完20页, 当值为20时还应该加载一次
					{
						loadData();
					}
				}
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount)
			{
			}
		});

		mMusicInfoListView.setOnItemClickListener(new OnItemClickListener()
		{

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id)
			{
				mXmPlayerManager.playList(mTrackHotList, position);
			}
		});

		loadData();
	}

	private IXmPlayerStatusListener mPlayerStatusListener = new IXmPlayerStatusListener()
	{

		@Override
		public void onSoundSwitch(PlayableModel laModel, PlayableModel curModel)
		{
			if(mTrackAdapter != null)  //注意当设置同时缓存的页面为5(左右各2)时, 其中注册的监听器也都会同时存在且响应变化, 故切换歌曲时这个这些个函数会执行五次, 有待优化
			{
				//Log.i(TAG, "BangDan sound switch");
				mTrackAdapter.notifyDataSetChanged();
			}
		}

		@Override
		public void onSoundPrepared()
		{
		}

		@Override
		public void onSoundPlayComplete()
		{
		}

		@Override
		public void onPlayStop()
		{
		}

		@Override
		public void onPlayStart()
		{
		}

		@Override
		public void onPlayProgress(int currPos, int duration)
		{
		}

		@Override
		public void onPlayPause()
		{
		}

		@Override
		public boolean onError(XmPlayerException exception)
		{
			return false;

		}

		@Override
		public void onBufferingStop()
		{
		}

		@Override
		public void onBufferingStart()
		{
		}

		@Override
		public void onBufferProgress(int percent)
		{
		}

	};

	public void refresh()
	{
		//Log.i(TAG, "---refresh");
		if(hasMore())
		{
			loadData();
		}
	}

	private boolean hasMore()
	{
		//if(mTrackHotList != null && mTrackHotList.getTotalPage() <= mPageId)
		if(mTrackHotList != null)  //是否设定为不为null更合适, 即之前已经有数据则靠向下滚动来加载，而不是靠按钮加载
		{
			return false;
		}
		return true;
	}

	private void loadData()
	{
		if(mLoading)
		{
			return;
		}
		mLoading = true;
		Map<String, String> param = new HashMap<String, String>();
		param.put(DTransferConstants.CATEGORY_ID, "" + mPageIndex);
		param.put(DTransferConstants.PAGE, "" + mPageId);  //获取也索引必须从1开始, 和SDK中的设定值有关
		param.put(DTransferConstants.PAGE_SIZE, "" + mCommonRequest.getDefaultPagesize());  //一页加载条目数, 在MainActivity进行设置了50, 可调
		CommonRequest.getHotTracks(param, new IDataCallBack<TrackHotList>()
		{

			@Override
			public void onSuccess(TrackHotList object)
			{
//				Log.i(TAG, "onSuccess " + (object != null));
				
				if(object != null && object.getTracks() != null
						&& object.getTracks().size() != 0)
				{
					mPageId++;
					if(mTrackHotList == null)
					{
						mTrackHotList = object;
					}
					else
					{
						mTrackHotList.getTracks().addAll(object.getTracks());
					}
//					Log.i(TAG, ""+mTrackHotList.getTracks().size());
//					Log.i(TAG, ""+mTrackHotList.getTotalCount());
					mTrackAdapter.notifyDataSetChanged();
				}
				mLoading = false;
			}

			@Override
			public void onError(int code, String message)
			{
				Log.i(TAG, "onError " + code + ", " + message);
				
				mLoading = false;
			}
		});
	}

	public class TrackAdapter extends BaseAdapter
	{

		@Override
		public int getCount()
		{
			if(mTrackHotList == null || mTrackHotList.getTracks() == null)
			{
				return 0;
			}
			return mTrackHotList.getTracks().size();
		}

		@Override
		public Object getItem(int position)
		{
			return mTrackHotList.getTracks().get(position);

		}

		@Override
		public long getItemId(int position)
		{
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			ViewHolder holder;
			if(convertView == null)
			{
				convertView = LayoutInflater.from(mContext).inflate(
						R.layout.online_item, parent, false);
				holder = new ViewHolder();
				holder.content = (ViewGroup) convertView;
				holder.image = (ImageView) convertView
						.findViewById(R.id.imageview);
				holder.title = (TextView) convertView
						.findViewById(R.id.trackname);
				holder.intro = (TextView) convertView.findViewById(R.id.intro);
				convertView.setTag(holder);
			}
			else
			{
				holder = (ViewHolder) convertView.getTag();
			}
			Track sound = mTrackHotList.getTracks().get(position);
			holder.title.setText(sound.getTrackTitle());
			holder.intro.setText(sound.getAnnouncer() == null ? sound
					.getTrackTags() : sound.getAnnouncer().getNickname());
			Glide.with(mContext).load(sound.getCoverUrlLarge()).into(holder.image);
			PlayableModel curr = mXmPlayerManager.getCurrSound();
			if(sound.equals(curr))
			{
				holder.content.setBackgroundResource(R.color.list_item_pressed_bg);
			}
			else
			{
				holder.content.setBackgroundResource(R.color.list_item_normal_bg);
			}
			return convertView;
		}
	}

	@Override
	public void onDestroyView()
	{
		Log.i(TAG, "Fragment_BangDan onDestroyView ");
		
		if(mXmPlayerManager != null)
		{
			mXmPlayerManager.removePlayerStatusListener(mPlayerStatusListener);
		}
		
		super.onDestroyView();
	}
}
