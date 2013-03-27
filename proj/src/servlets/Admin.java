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

import db.UserAuthDB;
import java.sql.SQLException;

import com.google.gson.Gson;


public class Admin extends HttpServlet{
	
	private util.HTMLTemplates html;
   // private BloodPressureRecordsManager bpm;
    private Gson gson = new Gson();
	
	@Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        log( request.getRequestURI() );
        util.HTTPUtils.nocache( response );
        String context = request.getContextPath();
        
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        html.printHtmlStart(out);
        
        // body goes here
        
        html.printHtmlEnd(out);
    }
	
	@Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
		
    }
	
	@Override
    public void init(ServletConfig config) throws ServletException {
        super.init( config ); // super.init call is required
        html = util.HTMLTemplates.newHTMLTemplates( this );
        
        /* 
        try {
           // bpm = new SQLiteBPM( Constants.DB_PATH );
        }
        catch( BPRMException ex ) {
           // bpm = null;
            log( ex.getMessage() );
        }
        */
    }
}