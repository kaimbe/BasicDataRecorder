package db;

import java.util.List;

public interface ProjectManager {
	long addProject(Project proj) throws PMException;
	long getProjID(String name) throws PMException;
	long addDataDefn(String projectName, DataDefinition defn) throws PMException;
	void createProject(String name) throws PMException;
	List<Project> getProjects(String user) throws PMException;
	List<Project> getAllProjects() throws PMException;
}
