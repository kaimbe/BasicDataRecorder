package db;

import java.util.List;

public interface BloodPressureRecordsManager {
    List<BloodPressure> getRecords() throws BPRMException;
    List<BloodPressure> getRecords( String user ) throws BPRMException;
    List<BloodPressure> getRecords( ReportSelect sel ) throws BPRMException;
    List<BloodPressure> getRecordsAfter( String user, long time )  throws BPRMException;
    String[] getUsers() throws BPRMException;
    // returns a unique rowid for the added record
    long add( String user, BloodPressure bp ) throws BPRMException;
    void delete( String user, long rowid ) throws BPRMException;
    void update( String user, BloodPressure bp ) throws BPRMException;
}
