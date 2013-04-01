package servlets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import db.DataDefinition;
import db.PMException;
import db.Project;
import db.SQLitePM;

public class ProjectSetup extends HttpServlet{
	private util.HTMLTemplates html;
    private Gson gson = new Gson();
    private SQLitePM pm;
    
    private SimpleDateFormat dtFormat;

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
        
        out.println("</div>");
      
        out.println("<div class='usersplash'>");
        out.println("<h1>Project Setup</h1>");
        
        try {
        if (pm.doesTableExist(projName)) {
        	out.println("<p>This project has already been set up.<p>");
        }
        else {
        	out.println("<table class='editor'>");
            out.println("<tr>");
            out.println("<td>No</td><td>Data Name</td><td>Data Type</td><td>Command</td>");
            out.println("</tr>");
            
            // add data entry form
            out.println("<tr>");
            out.println("<td>Edit</td>");
            out.println("<td><input type='text' id='data-name' size='10'></td>");
            out.println("<td><select id='data-type'><option>text</option><option>real</option><option>integer</option></select></td>");
            out.println("<td><input type='button' id='add-entry' value='Add'></td>");
            out.println("</tr>");
            out.println("</table><br>");
            out.println("<input type='button' id='create-proj' value='Create Project'>");
        }
        }
        catch (PMException e) {
        	log(e.getMessage());
        	response.sendRedirect( context + Constants.DB_ERR_PAGE );
            return;
        }
        
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
        String[] pathParts = pathInfo.split("/");
        String projName = pathParts[1];
        String cmd = "/" + pathParts[2];
        
        if ( pathInfo != null && cmd.startsWith("/add") ) {
            BufferedReader rd = request.getReader();
            String json = readAll( rd );
            try {
                DataDefinition defn = (DataDefinition)gson.fromJson(json, DataDefinition.class); 
                long id = pm.addDataDefn(projName, defn );
                
                log("adding: " + user + "=" + defn );
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
        else if ( pathInfo != null && cmd.startsWith("/update") ) {
            BufferedReader rd = request.getReader();
            String json = readAll( rd );
            try {
            	
            	DataDefinition defn = (DataDefinition)gson.fromJson(json, DataDefinition.class); 
            	pm.updateDataDefn(projName, defn);
                log("updating: " + user + "=" + defn );
                response.setContentType("application/json");
                out.print( gson.toJson( defn.getIndex() ) ); // return rec number
                return;
            }
            catch( PMException ex ) {
                log( ex.getMessage() );
                response.sendRedirect( context + Constants.DB_ERR_PAGE );
                return;
            }
        }
        else if ( pathInfo != null && cmd.startsWith("/delete") ){
            BufferedReader rd = request.getReader();
            String json = readAll( rd );
            try {
                long recid = gson.fromJson(json, long.class);
                pm.deleteDataDefn(projName, recid);
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
        }
        else if ( pathInfo != null && cmd.startsWith("/create") ) {
        	
            try {
            	pm.createProject(projName);
            	response.setContentType("application/json");
                out.print( gson.toJson(projName) ); 
            	return;
            }
            catch( PMException ex ) {
                log( ex.getMessage() );
                response.sendRedirect( context + Constants.DB_ERR_PAGE );
                return;
            }
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
        dtFormat = new SimpleDateFormat("yy/MM/dd HH:mm");
        dtFormat.setTimeZone( TimeZone.getDefault() );
        Date twentyFirstCentury = new GregorianCalendar(2001,1,1).getTime();
        dtFormat.set2DigitYearStart( twentyFirstCentury );
        
        try {
            pm = new SQLitePM( Constants.DB_PATH );
        }
        catch( PMException ex ) {
            pm = null;
            log( ex.getMessage() );
        }
    }
}
