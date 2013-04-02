package db;

import java.util.List;

public interface ProjectManager {
	public boolean doesTableExist(String name) throws PMException;

	public String getProjectName(long projid) throws PMException;

	public long addProject(Project proj) throws PMException;

	public void updateProject(Project proj) throws PMException;

	public void deleteProject(long id) throws PMException;

	public List<Project> getProjects(String user) throws PMException;

	public List<Project> getOwnerProjects(String owner) throws PMException;

	public List<Project> getAllProjects() throws PMException;

	public void createProject(long projid) throws PMException;

	public long addDataDefn(long projid, DataDefinition defn)
			throws PMException;

	public void updateDataDefn(long projid, DataDefinition defn)
			throws PMException;

	public void deleteDataDefn(long projid, long recid) throws PMException;

	public void deleteAllDataDefn(long projid) throws PMException;

	public void updateProjectSettings(long projid, ProjectSetting sett)
			throws PMException;

	public ProjectSetting getProjectSetting(long projid) throws PMException;

	public List<String> getRecordHeads(long projid) throws PMException;

	public long numOfCols(long projid) throws PMException;

	public List<List<String>> getAllRecords(long projid) throws PMException;

	public List<List<String>> getUserRecords(long projid, String user)
			throws PMException;

	public long addRecord(long projid, String user, String[] values)
			throws PMException;

	public void updateRecord(long projid, String[] values) throws PMException;

	public void deleteRecord(long projid, long recid) throws PMException;
}
