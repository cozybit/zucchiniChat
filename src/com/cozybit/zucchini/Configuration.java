package com.cozybit.zucchini;

public class Configuration {
	private String mNickname;	
	private String mChatText;
	private String mChannelName;

	public String getChatText () {
		return mChatText;
	}

	public void setChatText (String mChatText) {
		this.mChatText = mChatText;
	}

	public String getNickname () {
		return mNickname;
	}

	public void setNickname (String mNickname) {
		this.mNickname = mNickname;
	}
	
	public boolean hasChannelName () {
		return (this.mChannelName!= null);
	}
	public String getChannelName () {
		return mChannelName;
	}

	public void setChannelname (String mChannelname) {
		this.mChannelName = mChannelname;
	}

	public boolean hasNickname () {
		return (this.mNickname != null);
	}
}
