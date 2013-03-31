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
import db.SQLitePM;

public class ManageProjects extends HttpServlet{
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
        
        String recName = "";
        
        
        
        
        out.println("<div class='usersplash'>");
        out.println("<table class='editor'>");
        out.println("<tr>");
        out.print("<td>No</td><td>Project Name</td>");
        out.println("<td>Project Owner</td>");
        out.println("</tr>");
        /*
        try {
            List<Project> list = pm.getAllProjects();
            for( Project proj : list ) { //TODO
                out.println("<tr>");
                out.printf("<td>%d</td>%n", proj.getRecordID() );
                Date d = new Date( rec.getTimeStamp() );
                out.printf("<td>%s</td>%n", dtFormat.format( d ));
                out.printf("<td>%6.1f</td>%n", rec.getSystolic());
                out.printf("<td>%6.1f</td>%n", rec.getDiastolic());
                out.printf("<td>%6.1f</td>%n", rec.getPulseRate());
                String ed = String.format("<button class='edit' recno='%d'>Edit</button>",
                    rec.getRecordID() );
                String del = String.format("<button class='del' recno='%d'>Delete</button>",
                    rec.getRecordID() );
                out.print( "<td>" + ed + del + "</td>" );
                out.println("</tr>");
            }
        }
        catch( BPRMException ex ) {
            log( ex.getMessage() );
            out.println("<tr><td>Data base error</td></tr>");
        }
        */
        // the add a reading form
        out.println("<tr>");
        out.println("<td>Edit</td>");
        out.println("<td><input type='text' id='date' size='12'></td>");
        out.println("<td><input type='text' id='systolic' size='5'></td>");
        out.println("<td><input type='text' id='diastolic' size='5'></td>");
        out.println("<td><input type='text' id='pulse' size='5'></td>");
        out.println("<td><input type='button' id='add-bp' value='Add'></td>");
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
        response.setContentType("application/json");
		BufferedReader rd = request.getReader();
        String json = readAll( rd );
        
        
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
