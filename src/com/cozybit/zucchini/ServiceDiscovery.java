package com.cozybit.zucchini;

import java.util.Map;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ChannelListener;
import android.net.wifi.p2p.WifiP2pManager.DnsSdServiceResponseListener;
import android.net.wifi.p2p.WifiP2pManager.DnsSdTxtRecordListener;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.util.Log;
import android.widget.Toast;

import com.cozybit.zucchini.ChordChatActivity.DiscoveryStopListener;

public class ServiceDiscovery {

	private WifiP2pDnsSdServiceInfo mService;
	private WifiP2pManager mManager;
	private WifiP2pDnsSdServiceRequest mServiceRequest;
	private boolean isDiscovering = false; 

	private Channel mChannel;
	private DnsSdTxtRecordListener mRecordListener;
	private DnsSdServiceResponseListener mDnsListener;
	private Thread mDiscoveryThread;

	private String mServiceInstance;
	private String mServiceRegType;

	private static final String TAG = "ServiceDiscovery";

	public void registerLocalService(final Context context, Map<String, String> record, String serviceInstance, String serviceRegType) {
		if (mManager == null) {
			mManager = (WifiP2pManager) context.getSystemService(Context.WIFI_P2P_SERVICE);
			if (mManager == null)
				return;
		}

		mChannel = mManager.initialize(context, context.getMainLooper(), new ChannelListener() {

			@Override
			public void onChannelDisconnected() {
				Log.e(TAG, "onChannelDisconnected()");
			}
		}); 

		mServiceInstance = serviceInstance;
		mServiceRegType = serviceRegType;

		mService = WifiP2pDnsSdServiceInfo.newInstance(
				mServiceInstance, mServiceRegType, record);

		mManager.clearLocalServices(mChannel, new ActionListener() {

			@Override
			public void onSuccess() {
				mManager.addLocalService(mChannel, mService, new ActionListener() {

					@Override
					public void onSuccess() {
						Log.d(TAG, "Added local service");
					}

					@Override
					public void onFailure(int reason) {
						Log.e(TAG, "Failed adding local service");
						Toast.makeText(context, "Failure adding local service", Toast.LENGTH_SHORT).show();
					}
				});
			}

			@Override
			public void onFailure(int reason) {
				// TODO Auto-generated method stub

			}
		});


	}

	public void discoverRemoteServices(DnsSdServiceResponseListener dnsListener, DnsSdTxtRecordListener recordListener) {

		mDnsListener = dnsListener;
		mRecordListener = recordListener;

		mDiscoveryThread = new Thread(discoveryRunnable);
		mDiscoveryThread.start();
	}

	public void stop(final DiscoveryStopListener listener) {
		// This will stop discovering thread
		isDiscovering = false;
		// Interrupt to wake up the thread
		mDiscoveryThread.interrupt();
		// Wait until the thread is finished
		try {
			mDiscoveryThread.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		mManager.clearLocalServices(mChannel, new ActionListener() {
			@Override
			public void onSuccess() {
				Log.d(TAG, "FULLY STOPPED");
				if (listener != null)
					listener.onSuccess();
			}

			@Override
			public void onFailure(int reason) {
				Log.e(TAG, "Failed removing local service");
			}
		});

		mDnsListener = null;
		mRecordListener = null;

	}

	public boolean isDiscovering() {
		return isDiscovering;
	}

	public void setDiscovering(boolean isDiscovering) {
		this.isDiscovering = isDiscovering;
	}

	public Runnable discoveryRunnable = new Runnable()
	{
		@Override
		public void run()
		{

			/*
			 * Register listeners for DNS-SD services. These are callbacks invoked
			 * by the system when a service is actually discovered.
			 */
			mManager.setDnsSdResponseListeners(mChannel, mDnsListener, mRecordListener);

			// After attaching listeners, create a service request and initiate
			// discovery.
			mServiceRequest = WifiP2pDnsSdServiceRequest.newInstance(mServiceInstance, mServiceRegType);

			mManager.addServiceRequest(mChannel, mServiceRequest,
					new ActionListener() {

				@Override
				public void onSuccess() {
					Log.d(TAG, "Added service discovery request");
				}

				@Override
				public void onFailure(int arg0) {
					Log.e(TAG, "Failed adding service discovery request");
				}
			});

			isDiscovering = true;

			while (isDiscovering)
			{
				try
				{
					if (mManager == null)
						break;

					Log.d(TAG, "Discover...");
					mManager.discoverServices(mChannel, null);
					Thread.sleep(5000);// sleeps 5 seconds
				}
				catch (InterruptedException e) {}
			}
			//Clean up all the mess
			mManager.removeServiceRequest(mChannel, mServiceRequest, null);
			mServiceRequest = null;
		}
	};
}
