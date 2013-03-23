package db;

import java.util.List;
import java.util.ArrayList;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.PreparedStatement;

public class SQLiteBPM implements BloodPressureRecordsManager {
    static private boolean dbLoaded = false;
    static {
        try {
            Class clazz = Class.forName("org.sqlite.JDBC");
            dbLoaded = true;
        }
        catch ( Exception ex ) {
            dbLoaded = false;
        }
    }

    private final String dbPath;

    public SQLiteBPM( String path ) throws BPRMException {
        dbPath = path;
        Connection conn = null;
        try {
            conn = openConnection();
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(
                "create table if not exists bloodpressure " +
                "(rowid integer primary key, user text, timestamp integer," +
                "systolic real, diastolic real, pulseRate real)" );
            stmt.close();
        }
        catch( SQLException ex ) {
            throw new BPRMException( ex.getMessage() );
        }
        finally {
            closeConnection( conn );
        }
    }

    private Connection openConnection() throws BPRMException {
        String url = "jdbc:sqlite:" + dbPath;
        Connection conn = null;
        try {
            conn = DriverManager.getConnection( url );
        }
        catch( SQLException ex ) {
            throw new BPRMException( ex.getMessage() );
        }
        return conn;
    }

    private void closeConnection( Connection conn ) throws BPRMException {
        if ( conn == null ) return;
        try {
            conn.close();
        }
        catch( SQLException ex ) {
            throw new BPRMException( ex.getMessage() );
        }
    }

    public String[] getUsers() throws BPRMException {
        Connection conn = null;
        ArrayList<String> users = new  ArrayList<String>();
        try {
            conn = openConnection();
            PreparedStatement stmt = conn.prepareStatement(
                "select distinct user from bloodpressure");
            ResultSet rs = stmt.executeQuery();
            while( rs.next() ) {
                users.add( rs.getString("user"));
            }
            rs.close();
            stmt.close();
        }
        catch( SQLException ex ) {
            throw new BPRMException( ex.getMessage() );
        }
        finally {
            closeConnection( conn );
        }
        String[] arr = new String[ users.size() ];
        return users.toArray( arr );
    } 

    public List<BloodPressure> getRecords( ReportSelect sel )
    throws BPRMException
    {
        String baseQuery = 
            "select * from bloodpressure where " +
               "(systolic >= ? and systolic <= ?) and " +
               "(diastolic >= ? and diastolic <= ?) and " +
               "(pulseRate >= ? and pulseRate <= ?) and" +
               "(timestamp >= ? and timestamp <= ?)";
        Connection conn = null;
        List<BloodPressure> list = null;
        try {
            // add users to query
            if ( sel.users.length == 1 ) {
                baseQuery += " and (user=?);";
            }
            else if ( sel.users.length >= 2 ) {
                baseQuery += " and (";
                for( int i = 0 ; i < sel.users.length-1; i++ ) {
                    baseQuery += "(user=?) or ";
                }
                baseQuery += "(user=?));";
            }
            else {
                return new ArrayList<BloodPressure>(); // no users
            }
            conn = openConnection();
            PreparedStatement stmt = conn.prepareStatement( baseQuery );
            stmt.setDouble(1, sel.lowSys); 
            stmt.setDouble(2, sel.hiSys ); 
            stmt.setDouble(3, sel.lowDia); 
            stmt.setDouble(4, sel.hiDia ); 
            stmt.setDouble(5, sel.lowPulse ); 
            stmt.setDouble(6, sel.hiPulse ); 
            stmt.setDouble(7, sel.startDate.getTime() ); 
            stmt.setDouble(8, sel.endDate.getTime() ); 
            for( int i = 0 ; i < sel.users.length; i++ ) {
                stmt.setString( i+9, sel.users[i] );
            }
            ResultSet rs = stmt.executeQuery();
            list = listFromResultSet( rs );
            rs.close();
            stmt.close();
        }
        catch( SQLException ex ) {
            throw new BPRMException( ex.getMessage() );
        }
        finally {
            closeConnection( conn );
        }
        return list;
    }

    public List<BloodPressure> getRecords() throws BPRMException {
        Connection conn = null;
        List<BloodPressure> list = null;
        try {
            conn = openConnection();
            PreparedStatement stmt = conn.prepareStatement(
                "select * from bloodpressure");
            ResultSet rs = stmt.executeQuery();
            list = listFromResultSet( rs );
            rs.close();
            stmt.close();
        }
        catch( SQLException ex ) {
            throw new BPRMException( ex.getMessage() );
        }
        finally {
            closeConnection( conn );
        }
        return list;
    }

    private List<BloodPressure> listFromResultSet( ResultSet rs )
    throws SQLException
    {
        ArrayList<BloodPressure> list = new ArrayList<BloodPressure>();
        while( rs.next() ) {
            String name = rs.getString("user");
            long rowid = rs.getLong("rowid");
            long ts = rs.getLong("timestamp");
            double sys = rs.getDouble("systolic");
            double dia = rs.getDouble("diastolic");
            double pulse = rs.getDouble("pulseRate");
            BloodPressure bp = new BloodPressure(name, ts, sys, dia, pulse, rowid );
            list.add( bp );
        }
        return list;
    }

    public List<BloodPressure> getRecords( String user )
    throws BPRMException
    {
        Connection conn = null;
        List<BloodPressure> list = null;
        try {
            conn = openConnection();
            PreparedStatement stmt = conn.prepareStatement(
                "select * from bloodpressure where user = ?");
            stmt.setString(1, user); 
            ResultSet rs = stmt.executeQuery();
            list = listFromResultSet( rs );
            rs.close();
            stmt.close();
        }
        catch( SQLException ex ) {
            throw new BPRMException( ex.getMessage() );
        }
        finally {
            closeConnection( conn );
        }
        return list;
    }

    public List<BloodPressure> getRecordsAfter( String user, long time )
    throws BPRMException
    {
        Connection conn = null;
        List<BloodPressure> list = null;
        try {
            conn = openConnection();
            PreparedStatement stmt = conn.prepareStatement(
                "select * from bloodpressure where user=? and timestamp >= ?");
            stmt.setString(1, user); 
            stmt.setLong(2, time); 
            ResultSet rs = stmt.executeQuery();
            list = listFromResultSet( rs );
            rs.close();
            stmt.close();
        }
        catch( SQLException ex ) {
            throw new BPRMException( ex.getMessage() );
        }
        finally {
            closeConnection( conn );
        }
        return list;
    }

    // returns a unique rowid for the added record
    public long add( String user, BloodPressure bp ) throws BPRMException {
        long rowid = -1;
        Connection conn = null;
        try {
            conn = openConnection();
            conn.setAutoCommit(false);
            //PreparedStatement maxid = conn.prepareStatement(
            //   "select rowid from bloodpressure order by rowid desc limit 1");
            PreparedStatement maxid = conn.prepareStatement(
                "select last_insert_rowid() from bloodpressure");
            PreparedStatement stmt = conn.prepareStatement(
                "insert into bloodpressure values(null,?,?,?,?,?)");
            stmt.setString(1, user); 
            stmt.setLong(2, bp.getTimeStamp() ); 
            stmt.setDouble(3, bp.getSystolic() ); 
            stmt.setDouble(4, bp.getDiastolic() ); 
            stmt.setDouble(5, bp.getPulseRate() ); 
            stmt.executeUpdate();
            stmt.close();
            ResultSet rs = maxid.executeQuery();
            conn.commit();
            if ( rs.next() ) {
                 rowid = rs.getLong(1);
            }
            else {
                throw new BPRMException("last row rowid missing");
            }
            bp.setRecordID( rowid );
            maxid.close();
            conn.setAutoCommit( true );
        }
        catch( SQLException ex ) {
            if ( conn != null ) {
                try {
                    conn.rollback();
                }
                catch( SQLException ex1 ) {
                }
            }
            throw new BPRMException( ex.getMessage() );
        }
        finally {
            closeConnection( conn );
        }
        return rowid;
    }

    public void delete( String user, long rowid ) throws BPRMException {
        Connection conn = null;
        try {
            conn = openConnection();
            PreparedStatement stmt = conn.prepareStatement(
                "delete from bloodpressure where rowid=? and user=?");
            stmt.setLong(1, rowid);
            stmt.setString(2, user);
            stmt.executeUpdate();
            stmt.close();
        }
        catch( SQLException ex ) {
            throw new BPRMException( ex.getMessage() );
        }
        finally {
            closeConnection( conn );
        }
    }

    public void update( String user, BloodPressure bp ) throws BPRMException {
        Connection conn = null;
        try {
            conn = openConnection();
            PreparedStatement stmt = conn.prepareStatement(
                "update bloodpressure set timestamp=?, " +
                "systolic=?, diastolic=?, pulseRate=? " +
                "where rowid=?" );
            stmt.setLong(1, bp.getTimeStamp() ); 
            stmt.setDouble(2, bp.getSystolic() ); 
            stmt.setDouble(3, bp.getDiastolic() ); 
            stmt.setDouble(4, bp.getPulseRate() ); 
            stmt.setLong(5, bp.getRecordID() ); 
            stmt.executeUpdate();
            stmt.close();
        }
        catch( SQLException ex ) {
            throw new BPRMException( ex.getMessage() );
        }
        finally {
            closeConnection( conn );
        }
    }

    public static void main( String[] args ) throws Exception {
        SQLiteBPM bpm = new SQLiteBPM("bp.db");
        BloodPressure bp = new BloodPressure("rod", 0, 130.0, 90.0, 68.0, 47);
        bpm.delete("rod", 12 );
        System.out.println( "rowid = " + bpm.add("rod", bp));
        BloodPressure bp1 = new BloodPressure("rod", 0, 120.0, 80.0, 68.0, 6);
        bpm.update( "rod", bp1 );
        List<BloodPressure> list = bpm.getRecords("rod");
        for( BloodPressure v : list ) {
            System.out.println( v );
        }
    }
}
