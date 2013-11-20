package com.cozybit.zucchini;

import java.util.ArrayList;

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
import android.widget.TextView;

import com.cozybit.chord_chat.R;

public class ChannelFragment extends ListFragment {

	public static final String MESSAGE_LIST = "message_list";
	public static final String TAB_NUMBER = "tab_number";
	public static final String CHANNEL_NAME = "channel_name";
	public static final String PUBLIC_CHANNEL_TAB_TEXT = "PUBLIC";
	public static final String MAGNET_DEFAULT_CHANNEL = "Magnet";

	private String channelName = null;

	private ArrayList<String> messageList;

	private static ArrayAdapter<String> messageListAdapter;


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

		channelName = this.getArguments().getString(ChannelFragment.CHANNEL_NAME);

		View view = inflater.inflate(R.layout.channel, container, false);
		ListView listView = (ListView) view.findViewById(android.R.id.list);
		reverseListView(listView);

		final Button button = (Button) view.findViewById(R.id.btn_send_message);
		final EditText editText = (EditText) view.findViewById(R.id.chat_text);

		button.setOnClickListener (new OnClickListener () {
			@Override
			public void onClick (View v) {

				String msg = editText.getText().toString();

				// Check if the message starts by a hashtag
				if (msg.startsWith("#")) {

					if(msg.contains(" ")){
						String channel = msg.substring(1, msg.indexOf(" "));
						msg = msg.substring(msg.indexOf(" ") + 1);
						if (msg.length() > 0)
							((ChordChatActivity) getActivity()).handleMessageSend(channel, msg);
					}
				} else {
					((ChordChatActivity) getActivity()).handleMessageSend(channelName, editText.getText().toString());
				}
				editText.getText().clear();
			}
		});

		return view;
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		String s = ((TextView)v.findViewById(android.R.id.text1)).getText().toString();

		if (s.contains("#")) {
			s = s.substring(s.indexOf("#") + 1);
			if (s.length() > 0) {
				((ChordChatActivity) getActivity()).setSelectedTabByName(s);
			}
		}

	}

	public String getChannelName() {
		return channelName;
	}

	public void notifyDataSetChanged() {
		messageListAdapter.notifyDataSetChanged();
	}

}