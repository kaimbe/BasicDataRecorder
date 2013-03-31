package db;

import java.util.List;

public interface ProjectManager {
	long addProject(Project proj) throws PMException;
	int getProjID(String name) throws PMException;
	void addEntryDefn(String projectName, int dataFieldIndex, String dataFieldName, String dataFieldType) throws PMException;
	void createProject(String name) throws PMException;
	List<Project> getProjects(String user) throws PMException;
	List<Project> getAllProjects() throws PMException;
}
