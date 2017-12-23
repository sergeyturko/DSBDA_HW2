package ru.turko.mephi;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

/**
 * Class for storing data about single log record
 */
public class RecordSyslog {
	private int priority;
    private String message;
    private Date date;

    /**
     * Constructor
     */
    public RecordSyslog(String message) {
        this.message = message;
		
		// get priorty of message
		int start = message.indexOf("<");
        int pos = message.indexOf(">",start);
        String logLevel = message.substring(start + 1,pos);
        System.out.println(logLevel);
        priority = Integer.parseInt(logLevel);
		
		// get data of message
		pos = message.indexOf(" ");
		String dateString = message.substring(pos+1, pos + 16);
        System.out.println(dateString);
		DateFormat format = new SimpleDateFormat("MMM dd HH:mm:ss", Locale.ENGLISH);
        try {
            date = format.parse(dateString);
        }
		catch(Exception ex) {
            ex.printStackTrace();
        }
    }


    /**
     * Get date of message
     */
    public Date getDate() {
        return date;
    }

    /**
     * Get log priority
     */
    public int getPriority() {
        return priority;
    }

    /**
     * Convert LogRecord To HourPriorityPair object
     */
    public HourPriority getHourPriorityPair(){
        int hour = date.getHours();
        return new HourPriority(hour, priority);
    }
}
