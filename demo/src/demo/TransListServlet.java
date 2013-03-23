package demo;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Cookie;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.util.Enumeration;
import java.net.URLDecoder;

import java.util.Arrays;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;

/**
 *
 * Demonstrates sessions.
 *
 * @author Rod Byrne
 */
public class TransListServlet extends HttpServlet {
    private util.HTMLTemplates html;
    private Gson gson = new Gson();

    @Override
    protected void doGet(
        HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException
    {
        log( request.getRequestURI() );
        util.HTTPUtils.nocache( response );
        response.setContentType("text/html");
        HttpSession session = request.getSession( true );
        if( session.isNew() ) {
            session.setMaxInactiveInterval( 3600 ); // max session time
            ArrayList<String> itemList = new ArrayList<String>();
            itemList.add("one");
            itemList.add("two");
            itemList.add("three");
            session.setAttribute("items", itemList );
        }
        ArrayList<String> items =
            (ArrayList<String>)session.getAttribute("items");

        PrintWriter out = response.getWriter();
        html.printHtmlStart(out);
        out.println("<body>");
        out.println("<h1>Transient List</h1>");
        out.println("<ul>");
        if ( items != null ) {
            out.println("<div id='items'>");
            for( String s : items ) {
                out.print("<li><span>");
                out.print(s);
                out.print("</span>");
                out.println("<button class='delete'>delete</button>");
                out.println("</li>");
            }
            out.println("</div>");
        }
        out.println("<li><button id='additem'>add</button>");
        out.print("<input id='addcontent' type='text' size='20'></li>");
        out.println("</ul>");

        out.println("<h2>Echoed List</h2>");
        out.print("<div id='result'></div>");
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
        response.setContentType("text/html");
        util.HTTPUtils.nocache( response );
        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession( true );

        BufferedReader rd = request.getReader();

        String json = readAll( rd );
        log( json );
        String[] items = gson.fromJson(json, String[].class); 
        ArrayList<String> itemList = new ArrayList<String>( items.length );
        for( String s : items ) itemList.add(s);

        // Save the current list to the current session
        session.setAttribute("items", itemList );
        out.write( gson.toJson(itemList) );
        out.flush();
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init( config ); // super.init call is required

        html = util.HTMLTemplates.newHTMLTemplates( this );
    }
}
