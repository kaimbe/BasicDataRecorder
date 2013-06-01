BasicDataRecorder
==================

CS 3715 Term Project

Author: Matthew Newell

This repository represents the submission that I made for my CS 3715 (Network Computing with Web Applications) term project. (Winter 2012 Semester) 
The overall aim of the project was to create a web application that can be used to keep track of school projects. 
Instructors (or any user of the system) can set up a project and then other users are able to make data entries to contribute towards a selected project as well as view other users entries for that project.
The requirements also specified that users should be able to view graphs and statistical information about a selected project. This feature is not implemented in the current version.


This repository contains the following:

1)	“jetty” directory: jetty-all library and a servlet API library
2)	“lib” directory: gson library and the sqlite JDBC library
3)	“logs” directory: the jetty server uses this directory to store log files
4)	“proj” directory: the project web app (for more information, see the readme in this directory)
5)	“server” directory: jetty server 
6)	Ant build file(build.xml)


To run the server and configure it to serve the “proj” web app: 

1)	From a terminal session, change directory to the root of this repository (i.e. "…/BasicDataRecorder") 
2)	Run the command “ant”. This should give you a listing of all the possible commands. If not, you might not be in the correct directory, the build file could be missing, or ant isn’t installed/configured properly on your machine.
3)	Upon successful completion of the previous step, run the command “ant jetty”. This will compile the source code, configure the server to run the “proj” web app and start the jetty server.


If there were no errors, the server should now be running. To test out the web app, open a web browser and go to the address “localhost:8000/proj”. The system requires credentials for all users.  To login, go to the login page and use the credentials: (username: “matt”, password: “matt”). Using these credentials will log you in to the system as an administrator. 
