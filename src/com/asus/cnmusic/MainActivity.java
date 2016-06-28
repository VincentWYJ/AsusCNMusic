package com.asus.cnmusic;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.asus.cnmusic.adapter.NavigationListAdapter;
import com.asus.cnmusic.fragment.BaseFragment;
import com.asus.cnmusic.fragment.LocalFragment;
import com.asus.cnmusic.fragment.OnlineFragment;
import com.asus.cnmusic.info.NavigationListBean;
import com.asus.cnmusic.util.LocalMusicUtils;
import com.asus.cnmusic.R;

@TargetApi(23)
@SuppressWarnings("deprecation")
public class MainActivity extends ActionBarActivity implements OnClickListener {
	private final String TAG = "AsusCNMusic";
	
	private final int REQUEST_CODE_ASK_PERMISSIONS = 111;
	
	private Context mContext;
	
	private String[] mNavigationTitles;

	private Toolbar mToolbar;
	
	private List<BaseFragment> mFragmentList;
	FragmentManager mFragmentManager;
	
    private DrawerLayout mDrawerLayout;
    private NavigationListAdapter mDrawerListAdapter;
    private ActionBarDrawerToggle mDrawerToggle;
    
    private ListView mNavigationListView;
    private LinearLayout mNavigationLayout;
    
    private int mCountPages;
    private int mCurrentPageIndex = 0;
    private int mPermissionPageIndex = 1;
    
    private Dialog mExitDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.main_activity);
        
        LocalMusicUtils.mContext= mContext= this;
        
        mNavigationTitles = getResources().getStringArray(R.array.navigation_titles_array);
        mCountPages = mNavigationTitles.length;
        
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle((Activity) mContext, mDrawerLayout, mToolbar,
                R.string.open, R.string.close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
        };

        mDrawerToggle.syncState();
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        mNavigationLayout = (LinearLayout) findViewById(R.id.navigation_layout);
        mNavigationListView = (ListView) findViewById(R.id.navigation_list);
        mDrawerListAdapter = new NavigationListAdapter(this, R.layout.main_navigation_item, initDrawerList());
        mNavigationListView.setAdapter(mDrawerListAdapter);
        mNavigationListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
        	
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int targetPageIndex, long id) {
            	if(mPermissionPageIndex == targetPageIndex) {
            		int hasWriteStoragePermission = ContextCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            		if (hasWriteStoragePermission != PackageManager.PERMISSION_GRANTED) {
            		    Log.i(TAG, TAG+" has not Write External Storage Permission");

            		    if (!shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            		         //用户拒绝了权限申请并选择了不再显示
            		        showPermissionDialog("You need to allow access to external store", 
            		        	new DialogInterface.OnClickListener() {
	            		            @Override
	            		            public void onClick(DialogInterface dialog, int which) {
	            		                showInstalledAppDetails(mContext, getPackageName());
	            		            }
	            		        }, 
            		        	new DialogInterface.OnClickListener() {
	            		            @Override
	            		            public void onClick(DialogInterface dialog, int which) {
	            		            	mDrawerLayout.closeDrawer(mNavigationLayout);
	            		            }
	            		        });
            		        return;
            		    }

            		    ActivityCompat.requestPermissions((Activity) mContext, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},
            		        REQUEST_CODE_ASK_PERMISSIONS);
            		    return;
            		}
            	}
            	
            	toPageOfLocalMusic(targetPageIndex);
            }
        });
        
        mFragmentList = new ArrayList<BaseFragment>();
        mFragmentList.add(OnlineFragment.getInstance());
        mFragmentList.add(LocalFragment.getInstance());
        mFragmentManager = getSupportFragmentManager();
        for(int i=0; i<mCountPages; ++i) {
        	if(i == mCurrentPageIndex) {
        		mFragmentManager.beginTransaction().add(R.id.container, mFragmentList.get(i)).commit();
        		
        		getSupportActionBar().setTitle(mNavigationTitles[mCurrentPageIndex]);
        	}else {
        		mFragmentManager.beginTransaction().add(R.id.container, mFragmentList.get(i)).hide(mFragmentList.get(i)).commit();
        	}
        }
    }
    
    public void toPageOfLocalMusic(int targetPageIndex) {
    	mFragmentManager.beginTransaction().hide(mFragmentList.get(mCurrentPageIndex))
    		.show(mFragmentList.get(targetPageIndex)).commit();
	    mCurrentPageIndex = targetPageIndex;
	    
	    mDrawerListAdapter.setSelectedPosition(targetPageIndex);
	    mDrawerListAdapter.notifyDataSetChanged();
	    
	    mDrawerLayout.closeDrawer(mNavigationLayout);
	    
	    getSupportActionBar().setTitle(mNavigationTitles[targetPageIndex]);
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
        case REQUEST_CODE_ASK_PERMISSIONS:
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission Granted
            	Log.i(TAG, "Manifest.permission.WRITE_EXTERNAL_STORAGE access succeed");
            	
                toPageOfLocalMusic(mPermissionPageIndex);
            }else {
                // Permission Denied
                Log.i(TAG, "Manifest.permission.WRITE_EXTERNAL_STORAGE access failed");
            }
            break;
        default:
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
    
    private void showPermissionDialog(String message, DialogInterface.OnClickListener okListener, DialogInterface.OnClickListener cancelListener) {
        new AlertDialog.Builder(mContext)
        .setMessage(message)
        .setPositiveButton(R.string.confirm, okListener)
        .setNegativeButton(R.string.cancel, cancelListener)
        .create()
        .show();
    }
    
    private static final String SCHEME = "package";
    private static final String APP_PKG_NAME_21 = "com.android.settings.ApplicationPkgName";
    private static final String APP_PKG_NAME_22 = "pkg";
    private static final String APP_DETAILS_PACKAGE_NAME = "com.android.settings";
    private static final String APP_DETAILS_CLASS_NAME = "com.android.settings.InstalledAppDetails";
    public static void showInstalledAppDetails(Context context, String packageName) {
        Intent intent = new Intent();
        final int apiLevel = Build.VERSION.SDK_INT;
        if (apiLevel >= 9) {
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts(SCHEME, packageName, null);
            intent.setData(uri);
        }else {
            final String appPkgName = (apiLevel == 8 ? APP_PKG_NAME_22 : APP_PKG_NAME_21);
            intent.setAction(Intent.ACTION_VIEW);
            intent.setClassName(APP_DETAILS_PACKAGE_NAME, APP_DETAILS_CLASS_NAME);
            intent.putExtra(appPkgName, packageName);
        }
        context.startActivity(intent);
    }

    public List<NavigationListBean> initDrawerList() {
        List<NavigationListBean> drawerList = new ArrayList<NavigationListBean>();
        
        NavigationListBean drawerListBean = new NavigationListBean(mNavigationTitles[0], R.drawable.online_ic_pressed_drawable);
        drawerList.add(drawerListBean);
        
        NavigationListBean drawerListBean1 = new NavigationListBean(mNavigationTitles[1], R.drawable.local_ic_pressed_drawable);
        drawerList.add(drawerListBean1);
        
        return drawerList;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
       switch (item.getItemId()) {
           case R.id.action_setting:
           	
               break;
       }
       
       return true;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int id = v.getId();
		switch(id) {
		case R.id.exit_comfirm:
			MainActivity.this.finish();
			break;
		case R.id.exit_minimize:
			moveTaskToBack(true);
			break;
		case R.id.exit_cancel:
			break;
		default:
			break;
		}
		
		mExitDialog.cancel();
	}

    @Override
    public void onBackPressed() {
	    Log.i(TAG, "onBackPressed");
	    
	    mExitDialog = new Dialog(mContext);
	    mExitDialog.setContentView(R.layout.main_exit_dialog);
	    mExitDialog.setCanceledOnTouchOutside(true);
	    ((Button) mExitDialog.findViewById(R.id.exit_comfirm)).setOnClickListener(this);
	    ((Button) mExitDialog.findViewById(R.id.exit_minimize)).setOnClickListener(this);
	    ((Button) mExitDialog.findViewById(R.id.exit_cancel)).setOnClickListener(this);
	    
	    mExitDialog.show();
    }
    
    @Override
    public void onDestroy() {
    	super.onDestroy();
    	
    	Log.i(TAG, "MainActivity onDestroy");
    	
    	LocalMusicUtils.deleteNotification();
    }
}
