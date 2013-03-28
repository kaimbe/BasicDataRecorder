package db;

import java.util.List;
import java.util.ArrayList;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.PreparedStatement;

public class UserAuthDB{
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

    public UserAuthDB(String path) throws SQLException {
        dbPath = path;
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = openConnection();
            stmt = conn.createStatement();
            stmt.executeUpdate(
                "create table if not exists users (" +
                "    id integer primary key," +
                "    username text not null unique," +
                "    pwd text not null);" );
            stmt.executeUpdate(
                "create table if not exists roles (" +
                    "id integer primary key," +
                    "role text not null unique);" );
            stmt.executeUpdate(
                "create table if not exists user_roles (" +
                    "user_id integer not null," +
                    "role_id integer not null," +
                    "primary key (user_id, role_id) );" );
            stmt.executeUpdate(
                    "create table if not exists sessions (" +
                        "username text not null," +
                        "session_id text primary key);");
        }
        finally {
            closeConnection( conn );
            if ( stmt != null ) stmt.close();
        }
    }

    private Connection openConnection() throws SQLException {
        String url = "jdbc:sqlite:" + dbPath;
        Connection conn = null;
        conn = DriverManager.getConnection( url );
        return conn;
    }

    private void closeConnection( Connection conn ) throws SQLException {
        if ( conn != null ) {
            conn.close();
        }
    }

    public List<String> getUserNames() throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ArrayList<String> list = new ArrayList<String>();
        try {
            conn = openConnection();
            stmt = conn.prepareStatement(
                "select username from users order by username;");
            ResultSet rs = stmt.executeQuery();
            while( rs.next() ) {
                list.add( rs.getString(1) );
            }
            rs.close();
        }
        finally {
            if ( stmt != null ) stmt.close();
            closeConnection( conn );
        }
        return list;
    }

    public List<String> getRoleNames() throws SQLException {
        Connection conn = openConnection();
        PreparedStatement stmt = null;
        ArrayList<String> list = new ArrayList<String>();
        try {
            stmt = conn.prepareStatement(
                "select role from roles order by role;");
            ResultSet rs = stmt.executeQuery();
            while( rs.next() ) {
                list.add( rs.getString( 1 ) );
            }
            rs.close();
        }
        finally {
            if ( stmt != null ) stmt.close();
            closeConnection( conn );
        }
        return list;
    }

    public long getUserId( String user ) throws SQLException {
        long id = -1;
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = openConnection();
            stmt = conn.prepareStatement(
                "select id from users where username = ?");
            stmt.setString(1, user);
            ResultSet rs = stmt.executeQuery();
            if ( rs.next() ) {
                id = rs.getLong(1);
            }
        }
        finally {
            if ( stmt != null ) stmt.close();
            closeConnection( conn );
        }
        return id;
    }

    public long getRoleId( String role ) throws SQLException {
        long id = -1;
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = openConnection();
            stmt = conn.prepareStatement(
                "select id from roles where role = ?");
            stmt.setString(1, role);
            ResultSet rs = stmt.executeQuery();
            if ( rs.next() ) {
                id = rs.getLong(1);
            }
        }
        finally {
            if ( stmt != null ) stmt.close();
            closeConnection( conn );
        }
        return id;
    }

    public long addUser(String user, String pw) throws SQLException {
        long rowid = -1;
        Connection conn = null;
        try {
            conn = openConnection();
            conn.setAutoCommit(false);
            PreparedStatement stmt = conn.prepareStatement(
                "insert into users values(null,?,?);");
            stmt.setString(1, user); 
            stmt.setString(2, pw); 
            stmt.executeUpdate();
            stmt.close();
            PreparedStatement maxid = conn.prepareStatement(
                "select last_insert_rowid() from users;");
            ResultSet rs = maxid.executeQuery();
            conn.commit();
            if ( rs.next() ) {
                 rowid = rs.getLong(1);
            }
            else {
                throw new SQLException("last row rowid missing");
            }
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
            throw ex;
        }
        finally {
            closeConnection( conn );
        }
        return rowid;
    }

    public boolean addUserRole( String user, String role ) throws SQLException {
        Connection conn = null;
        try {
            conn = openConnection();
            // check if role already set
            PreparedStatement count = conn.prepareStatement(
                "select count(*) from user_roles where " +
                "user_id = (select id from users where username = ?) " +
                "and role_id = (select id from roles where role = ?);");
            count.setString(1, user); 
            count.setString(2, role); 
            ResultSet rs = count.executeQuery();
            if ( rs.next() ) {
                 long c = rs.getLong(1);
                 if ( c == 1 ) {
                    count.close();
                    return true;
                 }
            }
            count.close();

            PreparedStatement stmt = conn.prepareStatement(
                "insert into user_roles values( " +
                " (select id from users where username = ?), " +
                " (select id from roles where role = ?) );");
            stmt.setString(1, user); 
            stmt.setString(2, role); 
            stmt.executeUpdate();
            stmt.close();
        }
        finally {
            closeConnection( conn );
        }
        return true;
    }
  
    public List<String> getUserRoles( String user ) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ArrayList<String> list = new ArrayList<String>();
        try {
            conn = openConnection();
            stmt = conn.prepareStatement(
                "select roles.role from user_roles, roles " +
                "where user_roles.user_id = " + 
                "(select id from users where username = ?) " + 
                "and user_roles.role_id = roles.id order by roles.role;");
            stmt.setString( 1, user );
            ResultSet rs = stmt.executeQuery();
            while( rs.next() ) {
                list.add( rs.getString(1) );
            }
            rs.close();
        }
        finally {
            if ( stmt != null ) stmt.close();
            closeConnection( conn );
        }
        return list;
    }

    public void deleteUserRole( String user, String role) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = openConnection();
            stmt = conn.prepareStatement(
                "delete from user_roles where " +
                 "user_id=(select id from users where username = ?) and " +
                 "role_id=(select id from roles where role = ?);");
            stmt.setString(1, user); 
            stmt.setString(2, role); 
            stmt.executeUpdate();
        }
        finally {
            if ( stmt != null ) stmt.close();
            closeConnection( conn );
        }
    }

    public void deleteUserRoles( String user ) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = openConnection();
            stmt = conn.prepareStatement(
                "delete from user_roles where " +
                "user_id=(select id from users where username = ?);");
            stmt.setString(1, user); 
            stmt.executeUpdate();
        }
        finally {
            if ( stmt != null ) stmt.close();
            closeConnection( conn );
        }
    }

    public void deleteUser( String user ) throws SQLException {
        Connection conn = null;
        try {
            conn = openConnection();
            PreparedStatement stmt = conn.prepareStatement(
                "delete from user_roles where " +
                "user_id=(select id from users where username = ?);");
            stmt.setString(1, user); 
            stmt.executeUpdate();
            stmt.close();
            stmt = conn.prepareStatement(
                "delete from users where username=?;");
            stmt.setString(1, user); 
            stmt.executeUpdate();
            stmt.close();
        }
        finally {
            closeConnection( conn );
        }
    }
    
    public boolean checkPassword(String user, String pwd) throws SQLException {
    	boolean pwdOk = false;
    	Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = openConnection();
            stmt = conn.prepareStatement("select username,pwd from users where (username = ?) and (pwd = ?);");
            stmt.setString(1, user);
            stmt.setString(2, pwd);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
            	String un = rs.getString(1);
            	String pw = rs.getString(2);
            	
            	if (un.equals("") || pw.equals("") || (un == null) || (pw == null)) {
            		pwdOk = false;
            	}
            	else {
            		pwdOk = true;
            	}
            }
            rs.close();
            stmt.close();
        }
        catch( SQLException ex ) {
        	ex.printStackTrace();
        }
        finally {
            closeConnection( conn );
        }
    	
    	return pwdOk;
    }
    
    public void addSession(String user, String sessionID) throws SQLException{
    	Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = openConnection();
            stmt = conn.prepareStatement("insert into sessions values(?,?);");
            stmt.setString(1, user);
            stmt.setString(2, sessionID);
            stmt.executeUpdate();
            stmt.close();
        }
        catch( SQLException ex ) {
        	ex.printStackTrace();
        }
        finally {
            closeConnection( conn );
        }
    }
    
    public List<String> getUserSessions(String user) throws SQLException{
    	Connection conn = null;
        PreparedStatement stmt = null;
        ArrayList<String> list = new ArrayList<String>();
        try {
            conn = openConnection();
            stmt = conn.prepareStatement("select session_id from sessions where username = ?;");
            stmt.setString(1, user);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
            	list.add(rs.getString(1));
            }
            
            rs.close();
            stmt.close();
        }
        catch( SQLException ex ) {
        	ex.printStackTrace();
        }
        finally {
            closeConnection( conn );
        }
        
        return list;
    }
    
    public static void main( String[] args ) {
        UserAuthDB ua;
		try {
			ua = new UserAuthDB("ua.db");
			ua.addUser("rod", "rod");
			System.out.println("uid = " + ua.getUserId("rod"));
	        List<String> list = ua.getUserNames();
	        for( String s : list ) {
	            System.out.println( s );
	        }
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
    }
}
