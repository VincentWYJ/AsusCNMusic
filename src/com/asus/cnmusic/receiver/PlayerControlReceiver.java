package com.asus.cnmusic.receiver;

import com.asus.cnmusic.fragment.BaseFragment;
import com.asus.cnmusic.fragment.LocalFragment;
import com.asus.cnmusic.util.LocalMusicUtils;
import com.ximalaya.ting.android.opensdk.player.XmPlayerManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class PlayerControlReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		XmPlayerManager mXmPlayerManager = XmPlayerManager.getInstance(context);
		LocalFragment mLocalFragment = LocalFragment.getInstance();
		String action = intent.getAction();
		if(BaseFragment.mPlayInLocal) {
			if(!mLocalFragment.isPlayingListEmpty()) {
				int size = BaseFragment.mLocalPlayList.size();
				int targetPosition;
				if(LocalMusicUtils.ACTION_CONTROL_PLAY_PAUSE.equals(action)) {
					mLocalFragment.actionPauseOrPlay();
				}else if(LocalMusicUtils.ACTION_CONTROL_PLAY_NEXT.equals(action)) {
					targetPosition = (BaseFragment.mPlayingPosition+1) % size;
					if(targetPosition != BaseFragment.mPlayingPosition) {
						mLocalFragment.MusicPlay(targetPosition);
					}
				}else if(LocalMusicUtils.ACTION_CONTROL_PLAY_PRE.equals(action)) {
					targetPosition = (size+LocalFragment.mPlayingPosition-1) % size;
					if(targetPosition != BaseFragment.mPlayingPosition) {
						mLocalFragment.MusicPlay(targetPosition);
					}
				}
			}
		}else if(LocalMusicUtils.ACTION_CONTROL_PLAY_PAUSE.equals(action)) {
			if(mXmPlayerManager.isPlaying()) {
				mXmPlayerManager.pause();
			}else {
				mXmPlayerManager.play();
			}
		}else if(LocalMusicUtils.ACTION_CONTROL_PLAY_NEXT.equals(action)) {
			mXmPlayerManager.playNext();
		}else if(LocalMusicUtils.ACTION_CONTROL_PLAY_PRE.equals(action)) {
			mXmPlayerManager.playPre();
		}
	}
}

