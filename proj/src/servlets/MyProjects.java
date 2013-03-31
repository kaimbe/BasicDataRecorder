package servlets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import db.BPRMException;
import db.BloodPressure;
import db.PMException;
import db.Project;
import db.SQLiteBPM;
import db.SQLitePM;
import db.User;

public class MyProjects extends HttpServlet{
	private util.HTMLTemplates html;
    private Gson gson = new Gson();
    private SQLitePM pm;

	@Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        log( request.getRequestURI() );
        util.HTTPUtils.nocache( response );
        String context = request.getContextPath();
        String user = request.getRemoteUser();
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
        
        out.println("</div>");
        
        out.println("<div class='usersplash'>");
        out.println("<h1>My Projects</h1>");
        out.println("<table class='editor'>");
        out.println("<tr>");
        out.print("<td>No</td><td>Project Name</td>");
        out.println("<td>Project Owner</td>");
        out.println("<td>Command</td>");
        out.println("</tr>");
        
        try {
            List<Project> list = pm.getOwnerProjects( user );
            for( Project proj : list ) {
                out.println("<tr>");
                out.printf("<td>%d</td>%n", proj.getRecordID() );
                out.printf("<td>%s</td>%n", proj.getName());
                out.printf("<td>%s</td>%n", proj.getOwner());
               
                String ed = String.format("<button class='edit' recno='%d'>Edit</button>",
                    proj.getRecordID() );
                String del = String.format("<button class='del' recno='%d'>Delete</button>",
                    proj.getRecordID() );
                out.print( "<td>" + ed + del + "</td>" );
                out.println("</tr>");
            }
        }
        catch( PMException ex ) {
            log( ex.getMessage() );
            out.println("<tr><td>Data base error</td></tr>");
        }
        
        // the add a project form
        out.println("<tr>");
        out.println("<td>Edit</td>");
        out.println("<td><input type='text' id='proj-name' size='10'></td>");
        out.println("<td><input type='text' id='proj-owner' size='10'></td>");
        out.println("<td><input type='button' id='add-proj' value='Add'></td>");
        out.println("</tr>");
        out.println("</table>");
        
        out.println("</div>");
        out.println("</body>");
        html.printHtmlEnd(out);
    }

	private String readAll( BufferedReader rd ) throws IOException {
        int amt;
        StringBuilder sb = new StringBuilder();
        char[] buf = new char[10240];
        while ( (amt=rd.read(buf, 0, buf.length)) != -1 ) {
            sb.append( buf, 0, amt );
        }
        return sb.toString();
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
        if ( pathInfo != null && pathInfo.startsWith("/add") ) {
            BufferedReader rd = request.getReader();
            String json = readAll( rd );
            try {
                Project proj = (Project)gson.fromJson(json, Project.class); 
                long id = pm.addProject( proj );
                log("adding: " + user + "=" + proj );
                response.setContentType("application/json");
                out.print( gson.toJson( id ) ); // return rec number
                return;
            }
            catch( PMException ex ) {
                log( ex.getMessage() );
                response.sendRedirect( context + Constants.DB_ERR_PAGE );
                return;
            }
        }
        else if ( pathInfo != null && pathInfo.startsWith("/update") ) {
            BufferedReader rd = request.getReader();
            String json = readAll( rd );
            /*
            try {
            	
                BloodPressure bp =
                    (BloodPressure)gson.fromJson(json, BloodPressure.class); 
                bpm.update( user, bp );
                log("updating: " + user + "=" + bp );
                response.setContentType("application/json");
                out.print( gson.toJson( bp.getRecordID() ) ); // return rec number
                
                return;
            }
            catch( PMException ex ) {
                log( ex.getMessage() );
                response.sendRedirect( context + Constants.DB_ERR_PAGE );
                return;
            }
            */
        }
        else if ( pathInfo != null && pathInfo.startsWith("/delete") ){
            BufferedReader rd = request.getReader();
            String json = readAll( rd );
            /*
            try {
            	
                long recid = gson.fromJson(json, long.class);
                bpm.delete( user, recid );
                log("deleting: " + user + "=" + recid );
                response.setContentType("application/json");
                out.print( gson.toJson("ok") ); // ok
                
            }
            catch( NumberFormatException ex ) {
                log( ex.getMessage() );
                response.sendRedirect( context + Constants.DATA_ERR_PAGE );
                return;
            }
            catch( PMException ex ) {
                log( ex.getMessage() );
                response.sendRedirect( context + Constants.DB_ERR_PAGE );
                return;
            }
            */
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
