package ru.turko.mephi;

/**
 * Class for match of hour and log priority
 */
public class HourPriority {
    private int hour;
    private int priority;

    /**
     * Constructor
     */
    public HourPriority(int hour, int priority){
        this.hour=hour;
        this.priority = priority;
    }

    /**
     * Get log creation hour
     */
    public int getHour() {
        return hour;
    }

    /**
     * Get log priority
     */
    public int getPriority() {
        return priority;
    }
	
	/**
     * Redefine equals
     */
    @Override
    public boolean equals(Object obj) {
        if(obj instanceof HourPriority)
        {
            HourPriority hppObj = (HourPriority)obj;
            return hour == hppObj.getHour() && priority == hppObj.getPriority();
        }
        else
			return super.equals(obj);
    }

	/**
     * Define hashCode
     */
    @Override
    public int hashCode() {
        return priority*100+hour;
    }
}
