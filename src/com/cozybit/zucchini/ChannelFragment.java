package com.cozybit.zucchini;

import java.util.ArrayList;

import com.cozybit.chord_chat.R;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

public class ChannelFragment extends ListFragment {

	public static final String MESSAGE_LIST = "message_list";
	public static final String TAB_NUMBER = "tab_number";
	public static final String CHANNEL_NAME = "channel_name";
	public static final String PUBLIC_CHANNEL_TAB_TEXT = "PUBLIC";
	public static final String MAGNET_DEFAULT_CHANNEL = "Magnet";
		
	private ArrayList<String> messageList;
	private static ArrayAdapter<String> messageListAdapter;
	private String channelName = null;
	
	public String getChannelName() {
		return channelName;
	}
	
	public void notifyDataSetChanged() {
		messageListAdapter.notifyDataSetChanged();
	}
	
	public ChannelFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		messageList = this.getArguments().getStringArrayList(ChannelFragment.MESSAGE_LIST);
	}
	
	protected static void reverseListView(ListView listView) {
		listView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
		listView.setStackFromBottom(true);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		final ChordChatActivity activity = (ChordChatActivity) getActivity();
		messageListAdapter = new ArrayAdapter<String>(activity, android.R.layout.simple_list_item_1, messageList);
		setListAdapter(messageListAdapter);
		
		int tab_num = this.getArguments().getInt(ChannelFragment.TAB_NUMBER, 0);
		channelName = this.getArguments().getString(ChannelFragment.CHANNEL_NAME);

		View view = inflater.inflate(R.layout.channel, container, false);
		ListView listView = (ListView) view.findViewById(android.R.id.list);
		reverseListView(listView);
		
		final Button button = (Button) view.findViewById(R.id.btn_send_message);
		final EditText editText = (EditText) view.findViewById(R.id.chat_text);
		/*final Button leaveButton = (Button) view.findViewById(R.id.btn_leave_channel);*/
		

		button.setOnClickListener (new OnClickListener () {
			@Override
			public void onClick (View v) {
				// send the message in the channel I'm in
				ChordChatActivity activity = (ChordChatActivity) getActivity();
				activity.handleMessageSend(channelName, editText.getText().toString());
				editText.getText().clear();
			}
		});
		
		return view;
	}
	
	private void showLeaveDialog () {

		new AlertDialog.Builder (getActivity()).setTitle ("Are you sure you want to leave the channel " + getChannelName() +"?")
		.setPositiveButton ("Yes", new DialogInterface.OnClickListener () {
			@Override
			public void onClick (DialogInterface dialog, int whichButton) {
				ChordChatActivity activity = (ChordChatActivity) getActivity();
				activity.leaveChannel(getChannelName());
				activity.removeCurrentTab();
			}
		})
		.setNegativeButton ("Cancel", new DialogInterface.OnClickListener () {
			@Override
			public void onClick (DialogInterface dialog, int whichButton) {
			}
		})
		.show ();
	}
}