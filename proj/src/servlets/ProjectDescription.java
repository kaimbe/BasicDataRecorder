package servlets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import db.PMException;
import db.Project;
import db.ProjectSetting;
import db.SQLitePM;

public class ProjectDescription extends HttpServlet {
	private util.HTMLTemplates html;
    private SQLitePM pm;
    private Gson gson = new Gson();

	@Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        log( request.getRequestURI() );
        util.HTTPUtils.nocache( response );
        String context = request.getContextPath();
        String user = request.getRemoteUser();
        String pathInfo = request.getPathInfo();
        String[] pathParts = pathInfo.split("/");
        String projName = pathParts[1];
        long projid = Long.parseLong(projName);
        
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        html.printHtmlStart(out);
        out.println("<body>");
        out.println("<div class='nav'>");
        
        if (request.isUserInRole("admin")) {
        	html.printAdminNav(out);
        }
        else if (request.isUserInRole("user")) {
        	html.printUserNav(out);
        }
        
        List<String> heads = null;
        String rProjName = "";
        
        try{
            heads = pm.getRecordHeads(projid);
            rProjName = pm.getProjectName(projid);
            }
            catch (PMException ex) {
            	log(ex.getMessage());
            }
        
        out.println("</div>");
        
        out.println("<div class='usersplash'>");
        out.println("<h1>Project Description</h1>");
        out.println("<h2>Project Name: " + rProjName + "</h2>");
        out.println("<table class='editor'>");
        
        out.println("<tr>");
        
       //create cols based on table
        for (String s : heads) {
        	out.println("<td>" + s + "</td>");
        }
   
        out.println("</tr>");
        
        try {
            List<List<String>> recs = pm.getAllRecords( projid );
            for( List<String> rows : recs ) { // for every row
                out.println("<tr>");
                String recno = rows.get(0);
                // for every col
                for (Object entry : rows) {
                	out.printf("<td>%s</td>%n", entry );
                }
                //end col
              
                out.println("</tr>");
            }
        }
        catch( PMException ex ) {
            log( ex.getMessage() );
            out.println("<tr><td>Data base error</td></tr>");
        }
        
        out.println("</table><br>");
        
        try {
        	ProjectSetting setting = pm.getProjectSetting(projid);
            out.println("<label>Project Description: <br><p>" + setting.getDescription() + "</p></label><br>");
            out.println("<label>Project Users: <br><p>" + setting.getUsers() + "</p></label><br>");
            
        }
        catch (PMException ex) {
        	log( ex.getMessage() );
            out.println("<p>Data base error</p>");
        }
        
        out.println("<input type='button' id='edit_project' value='Edit Project'>");
        
        out.println("</div>");
        out.println("</body>");
        html.printHtmlEnd(out);
    }
	
	@Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
		log( request.getRequestURI() );
        util.HTTPUtils.nocache( response );
        PrintWriter out = response.getWriter();
        String user = request.getRemoteUser();
        String context = request.getContextPath();
        
        if ( pm == null ) {
            response.sendRedirect( context + Constants.DB_ERR_PAGE );
            return;
        }
        if ( ! request.isUserInRole("user") ) {
            response.sendError( HttpServletResponse.SC_FORBIDDEN, "No premission");
            return;
        }
        
        String pathInfo = request.getPathInfo();
        String[] pathParts = pathInfo.split("/");
        String projName = pathParts[1];
        String cmd = "/" + pathParts[2];
        
        
        if ( pathInfo != null && cmd.startsWith("/edit") ) {
            log("redir: " + user + "=" + projName );
			response.setContentType("application/json");
			out.print( gson.toJson(projName) );
			return;
        }
        else {
            response.sendRedirect( context + Constants.INVALID_URL_PAGE );
            return;
        }
    }

	@Override
    public void init(ServletConfig config) throws ServletException {
        super.init( config ); // super.init call is required
        html = util.HTMLTemplates.newHTMLTemplates( this );
        
        try {
            pm = new SQLitePM( Constants.DB_PATH );
        }
        catch( PMException ex ) {
            pm = null;
            log( ex.getMessage() );
        }
    }
}
