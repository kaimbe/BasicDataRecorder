package servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import db.PMException;
import db.Project;
import db.ProjectSetting;
import db.SQLitePM;

public class ProjectDescription extends HttpServlet {
	private util.HTMLTemplates html;
    private SQLitePM pm;

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
        long colNum = 0;
        
        try{
            heads = pm.getRecordHeads(projName);
            
            }
            catch (PMException ex) {
            	log(ex.getMessage());
            }
        
        out.println("</div>");
        
        out.println("<div class='usersplash'>");
        out.println("<h1>Project Description</h1>");
        out.println("<table class='editor'>");
        
        out.println("<tr>");
        
       //create cols based on table
        for (String s : heads) {
        	out.println("<td>" + s + "</td>");
        }
   
        out.println("</tr>");
        
        try {
            List<List<String>> recs = pm.getRecords( projName );
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
        	ProjectSetting setting = pm.getProjectSetting(projName);
            out.println("<label>Project Description: <br><p>" + setting.getDescription() + "</p></label><br>");
            out.println("<label>Project Users: <br><p>" + setting.getUsers() + "</p></label><br>");
            
        }
        catch (PMException ex) {
        	log( ex.getMessage() );
            out.println("<p>Data base error</p>");
        }
        
        // TODO: put contribute button here
        
        out.println("</div>");
        out.println("</body>");
        html.printHtmlEnd(out);
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
