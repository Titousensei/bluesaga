package utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class TimeUtils {
	/**
	 * Constants
	 */
	
	public static final float NS_PER_SEC = 1000000000.0f;
	public static final float NS_PER_MS = 1000000.0f;
	public static final float MS_PER_SEC = 1000.0f;
	
	public final static ThreadLocal<SimpleDateFormat> FORMAT_DATETIME = new ThreadLocal() {
			@Override protected SimpleDateFormat initialValue()
			{ return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); }
	};
	
	public final static ThreadLocal<SimpleDateFormat> FORMAT_DATE = new ThreadLocal() {
			@Override protected SimpleDateFormat initialValue()
			{ return new SimpleDateFormat("yyyy-MM-dd"); }
	};
	
	/**
	 * Public Methods
	 */
	
	public static long nanos() {
		return System.nanoTime();
	}
	
	public static long millis() {
		return System.currentTimeMillis();
	}
	
	
	public static String getDate(String format) {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		return sdf.format(cal.getTime());
	}
	
	
	
	public static String now() {
		Calendar cal = Calendar.getInstance();
		return FORMAT_DATETIME.get().format(cal.getTime());
	}

	public static String getTimeAdd(int addedSeconds) {
		Calendar calender = Calendar.getInstance();
		calender.add(Calendar.SECOND, addedSeconds);
		return FORMAT_DATETIME.get().format(calender.getTime());
	}
	
	public static String addTimeToDate(String startDate, int addedSeconds){
		Calendar c = Calendar.getInstance();
		try {
			c.setTime(FORMAT_DATE.get().parse(startDate));
			c.add(Calendar.SECOND, addedSeconds);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return FORMAT_DATE.get().format(c.getTime());
	}
}
