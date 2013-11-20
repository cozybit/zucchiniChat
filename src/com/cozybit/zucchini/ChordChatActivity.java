package com.cozybit.zucchini;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.FragmentTransaction;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager.DnsSdServiceResponseListener;
import android.net.wifi.p2p.WifiP2pManager.DnsSdTxtRecordListener;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.cozybit.chord_chat.R;

public class ChordChatActivity extends FragmentActivity implements ActionBar.TabListener {

	private static final String LOG_TAG = "ChordChat";
	public static String DEFAULT_CHANNEL_DISPLAY_NAME = "ACTIVITY";

	private static final String SERVICE_INSTANCE = "_zucchini";
	private static final String SERVICE_REG_TYPE = "_a._b";

	private Configuration config;

	ServiceDiscovery mDiscovery;

	LimitedSizeList<ZucchiniMessage> mOutQueue;
	LimitedSizeList<ZucchiniMessage> mInQueue;

	private Map<String, ArrayList<String>> messageMap;
	private ArrayList<ArrayList<String>> tabList;
	private DnsSdTxtRecordListener mDnsSdTxtRecordListener;
	private DnsSdServiceResponseListener mDnsSdServiceResponseListener;

	protected ArrayList<String> addTab(String name) {

		ArrayList<String> messageList = new ArrayList<String>();

		ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		Tab tab = actionBar.newTab().setText(name);
		messageMap.put(name, messageList);
		tabList.add(messageList);
		tab.setTabListener(this);
		getActionBar().addTab(tab);
		actionBar.selectTab(tab);


		return messageList;
	}

	protected void removeCurrentTab() {

		int pos = getActionBar().getSelectedTab().getPosition();
		String name = getActionBar().getSelectedTab().getText().toString();

		tabList.remove(pos);
		messageMap.remove(name);

		Log.d(LOG_TAG, "tabList.size = " + tabList.size() + " messageMap.size = " + messageMap.size());

		getActionBar().removeTab(getActionBar().getSelectedTab());
	}

	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate (savedInstanceState);

		messageMap = new HashMap<String, ArrayList<String>>();
		tabList = new ArrayList<ArrayList<String>>();

		// Zuchinni message queues
		mInQueue = new LimitedSizeList<ZucchiniMessage>(1000);
		mOutQueue = new LimitedSizeList<ZucchiniMessage>(2);

		setContentView(R.layout.activity_main);

		addTab(DEFAULT_CHANNEL_DISPLAY_NAME);
		config = new Configuration ();

		if (!config.hasNickname ())
			config.setNickname("US");

		mDnsSdServiceResponseListener = new DnsSdServiceResponseListener() {
			@Override
			public void onDnsSdServiceAvailable(String instanceName,
					String registrationType, WifiP2pDevice srcDevice) {

				// A service has been discovered. Is this our app?
				if (instanceName.equalsIgnoreCase(SERVICE_INSTANCE)) {
					Log.d(LOG_TAG, "onBonjourServiceAvailable "
							+ instanceName);
				}

			}
		};

		mDnsSdTxtRecordListener = new DnsSdTxtRecordListener() {

			/**
			 * A new TXT record is available. Pick up the advertised
			 * buddy name.
			 */
			@Override
			public void onDnsSdTxtRecordAvailable(
					String fullDomainName, Map<String, String> record,
					WifiP2pDevice device) {

				Log.d(LOG_TAG,device.deviceName + " has " +
						record.size() + " records.");

				for (String key : record.keySet()) {
					ZucchiniMessage zm = new ZucchiniMessage(key, record.get(key), true);
					if (!mInQueue.contains(zm)) {
						mInQueue.offer(zm);
						addToMessageList(device.deviceName, zm.getHashTag(), zm.getMessage());
					} else
						Log.d(LOG_TAG, "Already in...");

				}
			}
		};

		mDiscovery = new ServiceDiscovery();

		mDiscovery.registerLocalService(getApplicationContext(), toRecordsHash(mOutQueue), SERVICE_INSTANCE, SERVICE_REG_TYPE);
		mDiscovery.discoverRemoteServices(mDnsSdServiceResponseListener, mDnsSdTxtRecordListener);
	}

	@Override
	protected void onDestroy () {
		super.onDestroy ();
	}

	private void addToMessageList (String user, String channel, String message) {
		Log.i (LOG_TAG, "add to message list");


		List <String> messageList;

		// New Hashtag received let's add the tab...
		if (!messageMap.containsKey(channel))
			messageList = addTab(channel);

		if (channel != DEFAULT_CHANNEL_DISPLAY_NAME) {
			messageList = messageMap.get(channel);
			messageList.add("<" + user + "> " + message);
		}

		// Also add to the local conversations message list
		messageList = messageMap.get(DEFAULT_CHANNEL_DISPLAY_NAME);
		messageList.add(user + " says \"" + message + "\" on #" + channel);


		ChannelFragment fragment = (ChannelFragment)
				getSupportFragmentManager().findFragmentById(R.id.fragment_container);

		fragment.notifyDataSetChanged();
	}

	public void pushOutZucchiniMessage(String hashtag, String payload) {

		if (hashtag != null && !hashtag.isEmpty() && payload != null && !payload.isEmpty()) {
			ZucchiniMessage zm = new ZucchiniMessage(hashtag, payload, false);
			mOutQueue.offer(zm);
			Log.d(LOG_TAG, "Added " + hashtag + "=" + zm.printRecordMessage() + " to the records");
		}
	}

	public void handleMessageSend (final String channel, String message) {

		addToMessageList(config.getNickname(), channel, message);
		pushOutZucchiniMessage(channel, message);

		if (mDiscovery != null) {
			/* 
			 * We need this shit to ensure that WFD has stopped before 
			 * updating anything this MUST be gone at some point!!!
			 */
			mDiscovery.stop(new DiscoveryStopListener() {
				@Override
				public void onSuccess() {
					// TODO Auto-generated method stub
					mDiscovery.registerLocalService(getApplicationContext(), toRecordsHash(mOutQueue), SERVICE_INSTANCE, SERVICE_REG_TYPE);
					mDiscovery.discoverRemoteServices(mDnsSdServiceResponseListener, mDnsSdTxtRecordListener);
				}
			});
		}
	}

	// implements ActionBar.TabListener

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {

	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		// When the given tab is selected, show the tab contents in the
		// container view.
		Fragment fragment = new ChannelFragment();

		Bundle args = new Bundle();
		args.putInt(ChannelFragment.TAB_NUMBER, tab.getPosition() + 1);
		args.putString(ChannelFragment.CHANNEL_NAME, tab.getText().toString());
		args.putStringArrayList(ChannelFragment.MESSAGE_LIST, tabList.get(tab.getPosition()));

		fragment.setArguments(args);
		fragment.setRetainInstance(true);
		getSupportFragmentManager().beginTransaction()
		.replace(R.id.fragment_container, fragment)
		.commit();
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {

	}

	/* Interface to receive callbacks from ServiceDiscovery */
	public interface DiscoveryStopListener {
		public void onSuccess();
	}

	/* Convert the List of ZucchiniMessages to a record hash */
	public Map<String, String> toRecordsHash(LimitedSizeList<ZucchiniMessage> queue) {

		Map<String, String> map = new HashMap<String, String>();

		for (ZucchiniMessage zucchiniMessage : queue) {
			Log.d(LOG_TAG, "ZM " + zucchiniMessage.getHashTag() + " " + zucchiniMessage.printRecordMessage());
			map.put(zucchiniMessage.getHashTag(), zucchiniMessage.printRecordMessage());
		}

		return map;
	}

	private Tab getTabByName(String name) {
		if (messageMap.containsKey(name)) {
			int index = tabList.indexOf(messageMap.get(name));
			return getActionBar().getTabAt(index);
		}
		return null;
	}

	private void setSelectedTab (Tab tab) {
		if (tab != null)
			getActionBar().selectTab(tab);
	}

	public void setSelectedTabByName (String name) {
		setSelectedTab(getTabByName(name));
	}
}
