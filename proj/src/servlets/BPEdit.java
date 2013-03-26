package servlets;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.util.Enumeration;
import java.net.URLDecoder;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import java.util.TimeZone;
import java.util.GregorianCalendar;
import java.text.SimpleDateFormat;

import db.BloodPressure;
import db.BPRMException;
import db.BloodPressureRecordsManager;
import db.SQLiteBPM;

import com.google.gson.Gson;

/**
 *
 * Edit blood pressure using forms.
 *
 * @author Rod Byrne
 */
public class BPEdit extends HttpServlet {
    private util.HTMLTemplates html;
    private BloodPressureRecordsManager bpm;
    private Gson gson = new Gson();

    private SimpleDateFormat dtFormat;

    @Override
    protected void doGet(
        HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException
    {
        log( request.getRequestURI() );
        util.HTTPUtils.nocache( response );
        String context = request.getContextPath();

        String user = request.getRemoteUser();
        if ( bpm == null ) {
            response.sendRedirect( context + Constants.DB_ERR_PAGE );
            return;
        }
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        html.printHtmlStart(out);
        out.println("<body>");
        out.printf("<h1>Blood Pressure Editor: %s</h1>%n", user);
        out.println("<table class='editor'>");
        out.println("<tr>");
        out.print("<td>No</td><td>YY/MM/DD HH:MM</td><td>Systolic</td>");
        out.println("<td>Diastolic</td><td>Pulse</td><td>Command</td>");
        out.println("</tr>");
        try {
            List<BloodPressure> list = bpm.getRecords( user );
            for( BloodPressure rec : list ) {
                out.println("<tr>");
                out.printf("<td>%d</td>%n", rec.getRecordID() );
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
    protected void doPost( 
        HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {
        log( request.getRequestURI() );
        util.HTTPUtils.nocache( response );
        PrintWriter out = response.getWriter();
        String user = request.getRemoteUser();
        String context = request.getContextPath();

        if ( bpm == null ) {
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
                BloodPressure bp =
                    (BloodPressure)gson.fromJson(json, BloodPressure.class); 
                long id = bpm.add( user, bp );
                log("adding: " + user + "=" + bp );
                response.setContentType("application/json");
                out.print( gson.toJson( id ) ); // return rec number
                return;
            }
            catch( BPRMException ex ) {
                log( ex.getMessage() );
                response.sendRedirect( context + Constants.DB_ERR_PAGE );
                return;
            }
        }
        else if ( pathInfo != null && pathInfo.startsWith("/update") ) {
            BufferedReader rd = request.getReader();
            String json = readAll( rd );
            try {
                BloodPressure bp =
                    (BloodPressure)gson.fromJson(json, BloodPressure.class); 
                bpm.update( user, bp );
                log("updating: " + user + "=" + bp );
                response.setContentType("application/json");
                out.print( gson.toJson( bp.getRecordID() ) ); // return rec number
                return;
            }
            catch( BPRMException ex ) {
                log( ex.getMessage() );
                response.sendRedirect( context + Constants.DB_ERR_PAGE );
                return;
            }
        }
        else if ( pathInfo != null && pathInfo.startsWith("/delete") ){
            BufferedReader rd = request.getReader();
            String json = readAll( rd );
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
            catch( BPRMException ex ) {
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
            bpm = new SQLiteBPM( Constants.DB_PATH );
        }
        catch( BPRMException ex ) {
            bpm = null;
            log( ex.getMessage() );
        }
    }
}
