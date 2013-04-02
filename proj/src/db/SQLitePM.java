package db;

import java.sql.Array;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
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
	                "(projid integer, data_field_index integer, data_field_name text, data_field_type text, primary key (projid, data_field_index));");
	            stmt.executeUpdate(		
		                "create table if not exists user_projects " +
		                "(projid integer, user text);");
	            stmt.executeUpdate(		
		                "create table if not exists project_properties " +
		                "(projid integer primary key, description text, users text);");
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
	    
	    private List<Project> listProjectsFromResultSet( ResultSet rs ) throws SQLException {
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
	    
	    public boolean doesTableExist(String name) throws PMException{
			Connection conn = null;
			boolean res = false;
			try {
	            conn = openConnection();
	            PreparedStatement stmt = conn.prepareStatement("select name from sqlite_master where type=? and name=?");
	            stmt.setString(1, "table"); 
	            stmt.setString(2, name);
	            ResultSet rs = stmt.executeQuery();
	            String rsName = "";
	            while( rs.next() ) {
					rsName = rs.getString("name");
				}
	            rs.close();
	            stmt.close();
	            
	            if (name.equals(rsName)) {
	            	res = true;
	            }
	            else {
	            	res = false;
	            }
	        }
	        catch( SQLException ex ) {
	            throw new PMException( ex.getMessage() );
	        }
	        finally {
	            closeConnection( conn );
	        }
			return res;
		}
	    
	    @Override
		public long getProjID(String name) throws PMException {
			Connection conn = null;
			long id = -1;
	        try {
	            conn = openConnection();
	            PreparedStatement proj = conn.prepareStatement("select projid from projects where projname=?;");
	            proj.setString(1, name);
	            ResultSet rs = proj.executeQuery();
	            
	            while( rs.next() ) {
	                id = rs.getLong(1);
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
		
		public void updateProject( Project proj ) throws PMException {
	        Connection conn = null;
	        try {
	            conn = openConnection();
	            PreparedStatement stmt = conn.prepareStatement(
	                "update projects set projname=?, " +
	                "owner=?" +
	                "where projid=?" );
	            stmt.setString(1, proj.getName());
	            stmt.setString(2, proj.getOwner());
	            stmt.setLong(3, proj.getRecordID());
	            stmt.executeUpdate();
	            stmt.close();
	        }
	        catch( SQLException ex ) {
	            throw new PMException( ex.getMessage() );
	        }
	        finally {
	            closeConnection( conn );
	        }
	    }
		
		public void deleteProject( long id ) throws PMException {
	        Connection conn = null;
	        try {
	        	
	            conn = openConnection();
	            String projName = "";
	            PreparedStatement name = conn.prepareStatement("select projname from projects where projid=?");
	            name.setLong(1, id);
	            ResultSet rs = name.executeQuery();
	            
	            while( rs.next() ) {
	                projName = rs.getString(1);
	            }
	            rs.close();
	            name.close();
	            
	            PreparedStatement proj = conn.prepareStatement(
	                "delete from projects where projid=?");
	            proj.setLong(1, id);
	            proj.executeUpdate();
	            proj.close();
	            
	            PreparedStatement usr = conn.prepareStatement(
		                "delete from user_projects where projid=?");
	            usr.setLong(1, id);
	            usr.executeUpdate();
	            usr.close();
	            
	            PreparedStatement defn = conn.prepareStatement(
		                "delete from projs_data_definition where projid=?");
	            defn.setLong(1, id);
	            defn.executeUpdate();
	            defn.close();
	            
	            PreparedStatement prop = conn.prepareStatement(
		                "delete from project_properties where projid=?");
	            prop.setLong(1, id);
	            prop.executeUpdate();
	            prop.close();
	            
	            String begin = "drop table if exists ";
	            PreparedStatement table = conn.prepareStatement(begin + projName);
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
	            list = listProjectsFromResultSet( rs );
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
	            list = listProjectsFromResultSet( rs );
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
			Connection conn = null;
	        List<Project> list = null;
	        try {
	            conn = openConnection();
	            PreparedStatement stmt = conn.prepareStatement(
	                "select * from projects");
	            ResultSet rs = stmt.executeQuery();
	            list = listProjectsFromResultSet( rs );
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
		public void createProject(String name) throws PMException {
			Connection conn = null;
			 
	        try {
	            long id = getProjID(name);
	            conn = openConnection();
	            PreparedStatement stmt = conn.prepareStatement("select data_field_name,data_field_type from projs_data_definition where projid=?");
	            stmt.setLong(1, id); 
	            ResultSet rs = stmt.executeQuery();
	            
	            String tableStart = "create table if not exists " + name + " (rowid integer primary key,";
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
	            String tableCreate = tableStart + cols + ");";
	            PreparedStatement table = conn.prepareStatement(tableCreate);
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

		@Override
		public long addDataDefn(String projectName, DataDefinition defn) throws PMException {
			long rowid = defn.getIndex();
			Connection conn = null;
			 
	        try {
	            long projid = getProjID(projectName);
	            conn = openConnection();
	            PreparedStatement stmt = conn.prepareStatement("insert into projs_data_definition values(?,?,?,?);");
	            stmt.setLong(1, projid); 
	            stmt.setLong(2, rowid); 
	            stmt.setString(3, defn.getName());
	            stmt.setString(4, defn.getType());
	            stmt.executeUpdate();
	            stmt.close();
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
		
		public void updateDataDefn(String projName, DataDefinition defn) throws PMException{
			Connection conn = null;
	        try {
	        	long id = getProjID(projName);
	            conn = openConnection();
	            PreparedStatement stmt = conn.prepareStatement(
	                "update projs_data_definition set data_field_name=?, " +
	                "data_field_type=?" +
	                "where projid=? and data_field_index=?" );
	            stmt.setString(1, defn.getName());
	            stmt.setString(2, defn.getType());
	            stmt.setLong(3, id);
	            stmt.setLong(4, defn.getIndex());
	            stmt.executeUpdate();
	            stmt.close();
	        }
	        catch( SQLException ex ) {
	            throw new PMException( ex.getMessage() );
	        }
	        finally {
	            closeConnection( conn );
	        }
		}
		
		public void deleteDataDefn(String projName, long recid) throws PMException {
	        Connection conn = null;
	        try {
	        	long id = getProjID(projName);
	            conn = openConnection();
	            PreparedStatement stmt = conn.prepareStatement(
	                "delete from projs_data_definition where projid=? and data_field_index=?");
	            stmt.setLong(1, id);
	            stmt.setLong(2, recid);
	            stmt.executeUpdate();
	            stmt.close();
	        }
	        catch( SQLException ex ) {
	            throw new PMException( ex.getMessage() );
	        }
	        finally {
	            closeConnection( conn );
	        }
		}
		
		public void updateProjectSettings(String projectName, ProjectSetting sett) throws PMException{
			Connection conn = null;
	        try {
	        	long id = getProjID(projectName);
	        	String users = sett.getUsers();
	        	String[] usrs = users.split("\\s*,\\s*");
	            conn = openConnection();
	            PreparedStatement stmt = conn.prepareStatement("insert or replace into project_properties values(?,?,?);");
	            stmt.setLong(1, id); 
	            stmt.setString(2, sett.getDescription()); 
	            stmt.setString(3, users);
	            stmt.executeUpdate();
	            stmt.close();
	            
	            PreparedStatement own = conn.prepareStatement("select owner from projects where projid=?");
	            own.setLong(1, id);
	            ResultSet rs = own.executeQuery();
	            String owner = "";
	            
	            while( rs.next() ) {
	            	owner = rs.getString(1);
				}
	            
	            rs.close();
	            own.close();
	            
	            PreparedStatement del = conn.prepareStatement("delete from user_projects where projid=? and user!=?");
	            del.setLong(1, id);
	            del.setString(2, owner);
	            del.executeUpdate();
	            del.close();
	            
	            if (! (usrs[0] == null || usrs[0].equals(""))) {
	            for (int i = 0; i < usrs.length; i++) {
	            	PreparedStatement usr = conn.prepareStatement("insert or replace into user_projects values(?,?);");
	            	usr.setLong(1, id);
	            	usr.setString(2, usrs[i]);
	            	usr.executeUpdate();
	            	usr.close();
	            }
	            }
	        }
	        catch( SQLException ex ) {
	             throw new PMException( ex.getMessage() );
	        }
	        finally {
	            closeConnection( conn );
	        }
		}
		
		public ProjectSetting getProjectSetting(String projectName) throws PMException{
			Connection conn = null;
	        ProjectSetting setting = new ProjectSetting(null,null);
	        try {
	        	long id = getProjID(projectName);
	            conn = openConnection();
	            PreparedStatement stmt = conn.prepareStatement("select description,users from project_properties where projid=?");
	            stmt.setLong(1, id); 
	            ResultSet rs = stmt.executeQuery();
	            
	            while( rs.next() ) {
	            	String desc = rs.getString(1);
	            	String usrs = rs.getString(2);
					setting = new ProjectSetting(desc, usrs);
				}
	            
	            rs.close();
	            stmt.close();
	        }
	        catch( SQLException ex ) {
	            throw new PMException( ex.getMessage() );
	        }
	        finally {
	            closeConnection( conn );
	        }
	        return setting;
		}
		
		public List<String> getRecordHeads(String projName) throws PMException{
			Connection conn = null;
	        ArrayList<String> list = new ArrayList<String>();
	        try {
	            conn = openConnection();
	            PreparedStatement stmt = conn.prepareStatement("pragma table_info(" + projName + ")");
	            ResultSet rs = stmt.executeQuery();
	            
	            while (rs.next()) {
	            	list.add(rs.getString("name"));
	            }
	            
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
		
		public long numOfCols(String projName) throws PMException{
			Connection conn = null;
			long colCount = 0;
			try {
				conn = openConnection();
			PreparedStatement cols = conn.prepareStatement("pragma table_info(" + projName + ")");
            ResultSet rs = cols.executeQuery();
            
            while (rs.next()) {
            	colCount++;
            }
            rs.close();
            cols.close();
			}
			catch( SQLException ex ) {
	            throw new PMException( ex.getMessage() );
	        }
	        finally {
	            closeConnection( conn );
	        }
			return colCount;
		}
		
		public List<List<String>> getRecords(String projName) throws PMException{
			Connection conn = null;
	        List<List<String>> list = new ArrayList<List<String>>();
	        try {
	            conn = openConnection();
	            long colCount = numOfCols(projName);
	            PreparedStatement stmt = conn.prepareStatement("select * from " + projName); 
	            ResultSet rs = stmt.executeQuery();
	            
	            while (rs.next()) {
	            	List<String> recs = new ArrayList<String>();
	            	for (int i = 1; i < (colCount + 1); i++) {
	            		String value = rs.getString(i);
	            		recs.add(value);
	            	}
	            	list.add(recs);
	            }
	            
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
		
		public long addRecord(String projName, String[] values) throws PMException{
			long rowid = -1;
			Connection conn = null;
	        try {
	            conn = openConnection();
	            conn.setAutoCommit(false);
	            String addStart = "insert into " + projName + " values(null,";
	            long colNum = values.length;
	            String addMid = "";
	            
	            for (int i = 0; i < colNum; i++) {
	            	addMid += "'" + values[i] + "',";
	            }
	            
	            addMid = addMid.substring(0, addMid.length() - 1);
	            String addRow = addStart + addMid + ");";
	            PreparedStatement stmt = conn.prepareStatement(addRow);
	            stmt.executeUpdate();
	            stmt.close();
	            		
	            PreparedStatement maxid = conn.prepareStatement("select last_insert_rowid() from " + projName);
	          
	            ResultSet rs = maxid.executeQuery();
	            conn.commit();
	            if ( rs.next() ) {
	                 rowid = rs.getLong(1);
	            }
	            else {
	                throw new PMException("last row rowid missing");
	            }
	            
	            rs.close();
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
	             throw new PMException( ex.getMessage() );
	        }
	        finally {
	            closeConnection( conn );
	        }
	        return rowid;
		}
		
		
		public void updateRecord(String projName, String[] values) throws PMException {
			Connection conn = null;
	        try {
	            conn = openConnection();
	            String addStart = "update " + projName + " set ";
	            String addEnd = " where rowid='" + values[0] + "'";
	            
	            List<String> heads = getRecordHeads(projName);     
	            
	            long colNum = values.length;
	            String addMid = "";
	            
	            for (int i = 1; i < colNum; i++) {
	            	addMid += heads.get(i) + "='" + values[i] + "',";
	            }
	            
	            addMid = addMid.substring(0, addMid.length() - 1);
	            
	            String addRow = addStart + addMid + addEnd;
	            PreparedStatement stmt = conn.prepareStatement(addRow);
	            
	            stmt.executeUpdate();
	            stmt.close();
	        }
	        catch( SQLException ex ) {
	            throw new PMException( ex.getMessage() );
	        }
	        finally {
	            closeConnection( conn );
	        }
		}
		
		public void deleteRecord(String projName, long recid) throws PMException{
			Connection conn = null;
	        try {
	            conn = openConnection();
	            PreparedStatement stmt = conn.prepareStatement("delete from " + projName + " where rowid=?");
	            stmt.setLong(1, recid);
	            stmt.executeUpdate();
	            stmt.close();
	        }
	        catch( SQLException ex ) {
	            throw new PMException( ex.getMessage() );
	        }
	        finally {
	            closeConnection( conn );
	        }
		}
		
}
