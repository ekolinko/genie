package edu.ucsd.genie.userinterface;

import java.util.ArrayList;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.Spinner;

import com.astuetz.viewpager.extensions.PagerSlidingTabStrip;

import edu.ucsd.genie.GenieService;
import edu.ucsd.genie.R;
import edu.ucsd.genie.connectionmanager.ConnectionManager;
import edu.ucsd.genie.datamanager.DataManager;
import edu.ucsd.genie.datamanager.datastructures.User;
import edu.ucsd.genie.userinterface.about.AboutFragment;
import edu.ucsd.genie.userinterface.advanced.AdvancedFragment;
import edu.ucsd.genie.userinterface.feedback.FeedbackFragment;
import edu.ucsd.genie.userinterface.home.HomeFragment;
import edu.ucsd.genie.userinterface.settings.SettingsFragment;

/**
 * Genie tab activity that allows switching between different views.
 */
public class TabActivity extends FragmentActivity {
	/**
	 * The tabs available to the adapter.
	 */
	private final TabFragment[] mTabs = {new HomeFragment(), new FeedbackFragment(), new SettingsFragment(), new AdvancedFragment(), new AboutFragment()};
	
	/**
	 * Service responsible for doing notification updates.
	 */
	private GenieService mService;
	
	/**
	 * Flag indicating whether this activity is bound to the service or not.
	 */
	private boolean mIsBound;
	
	/**
	 * Handler used for executing UI operations.
	 */
	private Handler mHandler = new Handler();
	
	/**
	 * User interface fields located in the header.
	 */
	private Button mLogoutButton;

	/**
	 * The spinner for selecting a room.
	 */
	private Spinner mRoomSpinner;
	
    private GeniePagerAdapter mDemoCollectionPagerAdapter;
    private PagerSlidingTabStrip mPagerSlidingTabStrip;
    private ViewPager mViewPager;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        doBindService();
        refreshContent();
    }
    
    /**
	 * Stop the timer when this activity is paused.
	 */
	@Override
	public void onPause() {
		if (mService != null)
			mService.setActivity(null);
		super.onPause();
		
	}

	/**
	 * Start the timer when this activity is resumed.
	 */
	@Override
	public void onResume() {
		if (mService != null)
			mService.setActivity(this);
		super.onResume();
	}
    
    private void refreshContent() {
    	setContentView(R.layout.main);
        mLogoutButton = (Button) findViewById(R.id.logout);
		mLogoutButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new LogoutTask().execute();
			}
		});

		User user = DataManager.getInstance(this).getUser();
		mRoomSpinner = (Spinner) findViewById(R.id.room_spinner);
		if (mRoomSpinner != null) {
			final ArrayList<String> rooms = user.getRooms();
			RoomAdapter adapter = new RoomAdapter(this,
					R.layout.genie_spinner_item, getString(R.string.building), rooms);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			mRoomSpinner.setAdapter(adapter);
			mRoomSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
				boolean mFirstTrigger = true;
				@Override
				public void onItemSelected(AdapterView<?> parent, View view,
						int position, long id) {
					if (mFirstTrigger) {
						mFirstTrigger = false;
						return;
					}
					String room = rooms.get(position);
					DataManager dataManager = DataManager.getInstance(TabActivity.this);
					dataManager.setCurrentRoom(room);
					if (mService != null) {
						mService.requestRoomInformationUpdate();
						mService.requestDataUpdate();
					}
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
				}
			});
		}
		
        mDemoCollectionPagerAdapter =
                new GeniePagerAdapter(
                        getSupportFragmentManager());
        mPagerSlidingTabStrip = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        mPagerSlidingTabStrip.setIndicatorColor(getResources().getColor(R.color.genie_green));
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mDemoCollectionPagerAdapter);
		mPagerSlidingTabStrip.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
		    @Override
		    public void onPageSelected(int position) {
		    	final InputMethodManager imm = (InputMethodManager)getSystemService(
		                Context.INPUT_METHOD_SERVICE);
		            imm.hideSoftInputFromWindow(mPagerSlidingTabStrip.getWindowToken(), 0);
		        mViewPager.clearFocus();
		    }

		    @Override
		    public void onPageScrolled(int position, float offset, int offsetPixels) {
		    }

		    @Override
		    public void onPageScrollStateChanged(int state) {
		    }
		});
		mPagerSlidingTabStrip.setViewPager(mViewPager);
    }
    
    /**
     * Update all the views that are part of this activity.
     */
    public void update() {
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				if(mViewPager != null) {
					int currentTab = mViewPager.getCurrentItem();
					if (currentTab >= 0 && currentTab < mTabs.length)
						mTabs[currentTab].update();
				}
			}
		});
	}
    
    /**
	 * On the back key, put this activity in the background rather than finishing it.
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK
				|| keyCode == KeyEvent.KEYCODE_HOME) {
			this.moveTaskToBack(true);
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}
	
	private void doBindService() {
	    bindService(new Intent(this.getApplicationContext(), GenieService.class), mConnection, Context.BIND_AUTO_CREATE);
	    mIsBound = true;
	}

	private void doUnbindService() {
	    if (mIsBound) {
	        unbindService(mConnection);
	        mIsBound = false;
	    }
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		menu.clear();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	/**
	 * Handle selection of menu items.
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_settings:
			startActivity(new Intent(this, Preferences.class));
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	@Override
	protected void onDestroy() {
	    super.onDestroy();
	    doUnbindService();
	}
    
	/**
	 * Adapter for showing the tabs.
	 */
	private class GeniePagerAdapter extends FragmentStatePagerAdapter {
		
	    public GeniePagerAdapter(FragmentManager fm) {
	        super(fm);
	    }

	    @Override
	    public Fragment getItem(int position) {
	        Fragment fragment = mTabs[position];
	        return fragment;
	    }

	    @Override
	    public int getCount() {
	        return mTabs.length;
	    }

	    @Override
	    public CharSequence getPageTitle(int position) {
	        return mTabs[position].getTabTitle(TabActivity.this);
	    }
	    
	    @Override
	    public void setPrimaryItem(ViewGroup container, int position, Object object) {
	    	super.setPrimaryItem(container, position, object);
	    }
	}
	
    /**
	 * Task for logging out from Genie.
	 */
	private class LogoutTask extends
			AsyncTask<Void, Void, ConnectionManager.Status> {

		@Override
		protected void onPreExecute() {
			mLogoutButton.setEnabled(false);
			Intent intent = new Intent(TabActivity.this,
					LoginActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			finish();
		}

		@Override
		protected ConnectionManager.Status doInBackground(Void... params) {
			return ConnectionManager.getInstance(TabActivity.this).logout();
		}

		@Override
		protected void onCancelled() {
		}

		@Override
		protected void onPostExecute(ConnectionManager.Status status) {
		};
	};
	
	/**
	 * Connection to the Genie Service.
	 */
	private ServiceConnection mConnection = new ServiceConnection() {
	    public void onServiceConnected(ComponentName className, IBinder service) {
	        mService = ((GenieService.LocalBinder)service).getService();
	        mService.setActivity(TabActivity.this);
	    }

	    public void onServiceDisconnected(ComponentName className) {
	        mService = null;
	    }
	};
}
