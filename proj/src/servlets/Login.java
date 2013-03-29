package servlets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.Gson;

import db.BPRMException;
import db.User;
import db.UserAuthDB;

public class Login extends HttpServlet{
	
	private util.HTMLTemplates html;
	private UserAuthDB userAuth;
    private Gson gson = new Gson();
	
	@Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        log( request.getRequestURI() );
        util.HTTPUtils.nocache( response );
        String context = request.getContextPath();
        String reqSesID = request.getRequestedSessionId();
        String username = request.getRemoteUser();
        System.out.println("REQ SESSION ID: " + reqSesID);
        System.out.println("REMOTE USER: " + username);
       
        
        
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        html.printHtmlStart(out);
        out.println("<body>");
        out.println("<h1>Login</h1>");
        out.println("<div id='login'>");
        out.println("<p>Please Enter Your Credentials:</p>");
        out.println("<form method='POST' action='j_security_check'>");
        out.print("<label>");
        out.print("Username: <input id='un' type='text' size='15' name='j_username'>");
        out.println("</label><br>");
        out.print("<label>");
        out.print("Password: <input id='pw' type='password' size='15' name='j_password'>");
        out.println("</label><br>");
        out.println("<input name='submit' type='submit' value='Login'>");
        out.println("</form>");
        //out.println("<button id='loginBut'>Login</button><br>");
       // out.println("<button id='newAccBut'>Create New Account</button><br>");
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
        String username = request.getRemoteUser();
        String reqSesID = request.getRequestedSessionId();
        String context = request.getContextPath();
        
        response.setContentType("text/html");
        
		/*
        if ( userAuth == null ) {
            response.sendRedirect( context + Constants.DB_ERR_PAGE );
            return;
        }
        
		BufferedReader rd = request.getReader();
        String json = readAll( rd );
        
        User user = (User) gson.fromJson(json, User.class);
        
        // Debugging
        System.out.println("JSON: " + json);
        System.out.println("REMOTE USER: " + username);
        System.out.println("REQ SESSION ID: " + reqSesID);
        System.out.println();
        System.out.println("USERNAME: " + user.getUsername());
        System.out.println("PASSWORD: " + user.getPassword());
        
        boolean pwdOk = false;
        try {
			long userID = userAuth.getUserId(user.getUsername());
			System.out.println("USER ID: " + userID);
			if(userID == -1) {
				// INVALID CREDS
				// USERNAME
				
				return;
			}
			else {
				pwdOk = userAuth.checkPassword(user.getUsername(), user.getPassword());
				System.out.println("IS PWD OK? " + pwdOk);
			}
			
			if (pwdOk) {
	        	HttpSession session = request.getSession(true);
	        	System.out.println("SESSION ID: " + session.getId());
	        	
	        	userAuth.addSession( user.getUsername(), session.getId() );
		        System.out.println("added session to db");
		   
	        	List<String> roles = userAuth.getUserRoles(user.getUsername());
	        	
	        	for (int i = 0; i < roles.size(); i++) {
	        		//System.out.println(roles.get(i));
	        		if (roles.get(i).equals("admin")) {
	        			// TAKE USER TO ADMIN SPLASH
	        			
	        			response.sendRedirect( context + "/user" );
	        			return;
	        			
		        	}
		        	else if (roles.get(i).equals("user")) {
		        		// TAKE USER TO USER SPLASH
		        		response.sendRedirect( context + "/user" );
		        	}
		        	else {
		        		// PERMISSION ERROR
		        	}
	        	}
	        }
	        else {
	        	// INVALID CREDS
	        	// PASSWORD
	        }
		} catch (SQLException e) {
			log( e.getMessage() );
		}
		*/
    }
	
	@Override
    public void init(ServletConfig config) throws ServletException {
        super.init( config ); // super.init call is required
        html = util.HTMLTemplates.newHTMLTemplates( this );
        
        try {
            userAuth = new UserAuthDB( Constants.UA_DB_PATH );
        }
        catch( SQLException ex ) {
            userAuth = null;
            log( ex.getMessage() );
        }
    }
}
