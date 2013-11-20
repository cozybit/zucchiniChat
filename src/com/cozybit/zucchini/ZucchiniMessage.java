package com.cozybit.zucchini;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class ZucchiniMessage {
	
	private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
	
	
	private final String timestamp;
	private final String hashTag;
	private final String message;

	private static final int TIMESTAMP_LENGTH = 28;
	
	public ZucchiniMessage(String hashTag, String message, boolean incoming) {
		
		this.hashTag = hashTag;
		
		if (incoming) {
			this.timestamp = message.substring(0, TIMESTAMP_LENGTH -1);
			this.message = message.substring(TIMESTAMP_LENGTH);
		} else {
			SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.US);
			sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
			
			this.timestamp = sdf.format(new Date());
			this.message = message;
		}
	}

	public String getHashTag() {
		return hashTag;
	}
	
	public String getMessage() {
		return message;
	}
	
	public String printRecordMessage() {
		return timestamp + " " + message;
	}
	
	@Override
	public boolean equals(Object object) {

		if (object != null && object.getClass() == getClass()) {
			ZucchiniMessage zm = (ZucchiniMessage) object;
			
			if (zm.hashTag.equals(this.hashTag) &&
				zm.timestamp.equals(this.timestamp) &&
				zm.message.equals(this.message))
				return true;
		}
		return false;
	}

	@Override
	public int hashCode() {
		int hash = 3;
		hash = 7 * hash + this.hashTag.hashCode();
		hash = 7 * hash + this.timestamp.hashCode();
		hash = 7 * hash + this.message.hashCode();
		return hash;
	}
}
