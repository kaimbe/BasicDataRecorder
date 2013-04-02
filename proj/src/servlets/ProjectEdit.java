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
import db.SQLitePM;


public class ProjectEdit extends HttpServlet{
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
            colNum= pm.numOfCols(projName) - 1;
            }
            catch (PMException ex) {
            	log(ex.getMessage());
            }
        
        out.println("</div>");
        
        out.println("<div class='usersplash'>");
        out.println("<h1>Edit Project</h1>");
        out.println("<table class='editor' colnum='" + colNum + "'>");
        
        out.println("<tr>");
        
       //create cols based on table
        for (String s : heads) {
        	out.println("<td>" + s + "</td>");
        }
        out.println("<td>Command</td>");
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
              
                String ed = String.format("<button class='edit' recno='%s'>Edit</button>",
                    recno );
                String del = String.format("<button class='del' recno='%s'>Delete</button>",
                    recno );
                
                out.print( "<td>" + ed + del + "</td>" );
                
                out.println("</tr>");
            }
        }
        catch( PMException ex ) {
            log( ex.getMessage() );
            out.println("<tr><td>Data base error</td></tr>");
        }
        
        // the add a record form
        out.println("<tr>");
        out.println("<td>Edit</td>");
        
        	// for every col
            for (int i = 1; i < (colNum + 1); i++) {
            	String row = String.format("<input type='text' class='add' id='%s%d' size='10'>", "add-", i);
            	out.println("<td>" + row + "</td>");
            }
            //end col
        
        
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
        String[] pathParts = pathInfo.split("/");
        String projName = pathParts[1];
        String cmd = "/" + pathParts[2];
        
        if ( pathInfo != null && cmd.startsWith("/add") ) {
            BufferedReader rd = request.getReader();
            String json = readAll( rd );
            try {
                String[] vals = (String[])gson.fromJson(json, String[].class); 
                long id = pm.addRecord( projName, vals );
                log("adding: " + user + "=" + vals );
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
            	String[] vals = (String[])gson.fromJson(json, String[].class);  
            	pm.updateRecord( projName, vals );
                log("updating: " + user + "=" + vals );
                response.setContentType("application/json");
                out.print( gson.toJson( vals[0] ) ); // return rec number
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
                pm.deleteRecord( projName, recid );
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
