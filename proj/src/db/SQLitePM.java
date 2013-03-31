package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class SQLitePM implements ProjectManager{
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

	    public SQLitePM( String path ) throws PMException {
	        dbPath = path;
	        Connection conn = null;
	        try {
	            conn = openConnection();
	            Statement stmt = conn.createStatement();
	            stmt.executeUpdate(
	                "create table if not exists projects " +
	                "(projid integer primary key, projname text unique, owner text);"); 
	            stmt.executeUpdate(		
	                "create table if not exists projs_data_definition " +
	                "(projid integer, data_field_index integer, data_field_name text, data_field_type text);");
	            stmt.executeUpdate(		
		                "create table if not exists user_projects " +
		                "(projid integer, user text);");
	            stmt.close();
	        }
	        catch( SQLException ex ) {
	            throw new PMException( ex.getMessage() );
	        }
	        finally {
	            closeConnection( conn );
	        }
	    }

	    private Connection openConnection() throws PMException {
	        String url = "jdbc:sqlite:" + dbPath;
	        Connection conn = null;
	        try {
	            conn = DriverManager.getConnection( url );
	        }
	        catch( SQLException ex ) {
	            throw new PMException( ex.getMessage() );
	        }
	        return conn;
	    }

	    private void closeConnection( Connection conn ) throws PMException {
	        if ( conn == null ) return;
	        try {
	            conn.close();
	        }
	        catch( SQLException ex ) {
	            throw new PMException( ex.getMessage() );
	        }
	    }

		@Override
		public long addProject(Project proj) throws PMException {
			long rowid = -1;
			Connection conn = null;
	        try {
	            conn = openConnection();
	            conn.setAutoCommit(false);
	            PreparedStatement stmt = conn.prepareStatement("insert into projects values(null,?,?);");
	            PreparedStatement usr = conn.prepareStatement("insert into user_projects values(?,?);");
	            PreparedStatement maxid = conn.prepareStatement("select last_insert_rowid() from projects");
	            stmt.setString(1, proj.getName()); 
	            stmt.setString(2, proj.getOwner()); 
	            stmt.executeUpdate();
	            stmt.close();
	            
	            ResultSet rs = maxid.executeQuery();
	            conn.commit();
	            if ( rs.next() ) {
	                 rowid = rs.getLong(1);
	            }
	            else {
	                throw new PMException("last row rowid missing");
	            }
	            proj.setRecordID( rowid );
	            maxid.close();
	            
	            usr.setLong(1, rowid);
	            usr.setString(2, proj.getOwner());
	            usr.executeUpdate();
	            usr.close();
	            
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
	             throw new PMException( ex.getMessage() );
	        }
	        finally {
	            closeConnection( conn );
	        }
	        return rowid;
		}

		@Override
		public void addEntryDefn(String projectName, int dataFieldIndex, String dataFieldName, String dataFieldType) throws PMException {
			Connection conn = null;
			 
	        try {
	            int id = getProjID(projectName);
	            conn = openConnection();
	            PreparedStatement defn = conn.prepareStatement("insert into projs_data_definition values(?,?,?,?);");
	            defn.setInt(1, id); 
	            defn.setInt(2, dataFieldIndex); 
	            defn.setString(3, dataFieldName);
	            defn.setString(4, dataFieldType);
	            defn.executeUpdate();
	            defn.close();
	        }
	        catch( SQLException ex ) {
	            throw new PMException( ex.getMessage() );
	        }
	        finally {
	            closeConnection( conn );
	        }
		}

		@Override
		public int getProjID(String name) throws PMException {
			Connection conn = null;
			int id = 0;
	        try {
	            conn = openConnection();
	            PreparedStatement proj = conn.prepareStatement("select projid from projects where projname=?;");
	            proj.setString(1, name);
	            ResultSet rs = proj.executeQuery();
	            
	            while( rs.next() ) {
	                id = rs.getInt(1);
	            }
	            rs.close();
	            proj.close();
	        }
	        catch( SQLException ex ) {
	            throw new PMException( ex.getMessage() );
	        }
	        finally {
	            closeConnection( conn );
	        }
	       
			return id;
		}

		@Override
		public void createProject(String name) throws PMException {
			Connection conn = null;
			 
	        try {
	            int id = getProjID(name);
	            conn = openConnection();
	            PreparedStatement stmt = conn.prepareStatement("select data_field_name,data_field_type from projs_data_definition where id=?");
	            stmt.setInt(1, id); 
	            ResultSet rs = stmt.executeQuery();
	            
	            String tableStart = "create table if not exists ? (rowid integer primary key,";
	            String cols = "";
	            
	            while( rs.next() ) {
	                String fname = rs.getString(1);
	                cols += fname + " ";
	                String type = rs.getString(2);
	                cols += type + ","; 
	            }
	            
	            rs.close();
	            stmt.close();
	            
	            cols = cols.substring(0, cols.length() - 1);
	            PreparedStatement table = conn.prepareStatement(tableStart + cols + ");");
	            table.setString(1, name);
	            table.executeUpdate();
	            table.close();
	        }
	        catch( SQLException ex ) {
	            throw new PMException( ex.getMessage() );
	        }
	        finally {
	            closeConnection( conn );
	        }
		}
		
		private List<Project> listFromResultSet( ResultSet rs ) throws SQLException {
			ArrayList<Project> list = new ArrayList<Project>();
			while( rs.next() ) {
				long rowid = rs.getLong("projid");
			    String name = rs.getString("projname");
			    String owner = rs.getString("owner");
			    Project proj = new Project(name, rowid, owner);
			    list.add( proj );
			}
			return list;
		}

		@Override
		public List<Project> getProjects(String user) throws PMException{
			Connection conn = null;
	        List<Project> list = null;
	        try {
	            conn = openConnection();
	            PreparedStatement stmt = conn.prepareStatement(
	                "select projects.projid,projects.projname,projects.owner from projects,user_projects where user_projects.user = ? and user_projects.projid = projects.projid");
	            stmt.setString(1, user); 
	            ResultSet rs = stmt.executeQuery();
	            list = listFromResultSet( rs );
	            rs.close();
	            stmt.close();
	        }
	        catch( SQLException ex ) {
	            throw new PMException( ex.getMessage() );
	        }
	        finally {
	            closeConnection( conn );
	        }
	        return list;
		}
		
		public List<Project> getOwnerProjects(String owner) throws PMException{
			Connection conn = null;
	        List<Project> list = null;
	        try {
	            conn = openConnection();
	            PreparedStatement stmt = conn.prepareStatement(
	                "select * from projects where owner = ?");
	            stmt.setString(1, owner); 
	            ResultSet rs = stmt.executeQuery();
	            list = listFromResultSet( rs );
	            rs.close();
	            stmt.close();
	        }
	        catch( SQLException ex ) {
	            throw new PMException( ex.getMessage() );
	        }
	        finally {
	            closeConnection( conn );
	        }
	        return list;
		}

		@Override
		public List<Project> getAllProjects() throws PMException {
			// TODO Auto-generated method stub
			return null;
		}
}
