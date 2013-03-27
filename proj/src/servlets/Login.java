package servlets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import db.BPRMException;

public class Login extends HttpServlet{
	
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
        out.println("<body>");
        out.println("<h1>Login</h1>");
        out.println("<div id='login'>");
        out.println("<p>Please Enter Your Credentials:</p><br>");
        out.print("<label>");
        out.print("User Name: <input id='un' type='text' size='10'>");
        out.println("</label><br>");
        out.print("<label>");
        out.print("Password: <input id='pw' type='text' size='10'>");
        out.println("</label><br>");
        out.println("<button id='loginBut'>Login</button><br>");
        out.println("<button id='newAccBut'>Create New Account</button><br>");
        out.println("</div>");
        
        out.println("</body>");
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
