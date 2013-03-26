package db;

public class BloodPressure {
    private String name;           // user name for record
    private long epoch1970TimeMS;  // number of milliseconds since 1970
    private double systolic;       // maximum blood pressure
    private double diastolic;      // minimum blood pressure
    private double pulseRate;      // pulse in beats per minute
    private long recordID;          // unique integer identifying this record

    public BloodPressure(
        String user, long time, double sys, double dia, double pulse, long id)
    {
        name = user;
        epoch1970TimeMS = time;
        systolic = sys;
        diastolic = dia;
        pulseRate = pulse;
        recordID = id;
    }

    public long getTimeStamp() {
        return epoch1970TimeMS;
    }

    public double getSystolic() {
        return systolic;
    }

    public double getDiastolic() {
        return diastolic;
    }

    public double getPulseRate() {
        return pulseRate;
    }

    public long getRecordID() {
        return recordID;
    }

    public void setRecordID( long id ) {
        recordID = id;
    }

    public String getName() {
        return name;
    }


    public String toString() {
        return String.format("%s %d %d %g %g %g",
            name, recordID, epoch1970TimeMS, systolic, diastolic, pulseRate);
    }
}
