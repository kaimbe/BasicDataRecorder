package demo;


// XXX see HttpServletRequest.isUserInRole()

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.util.Enumeration;
import java.net.URLDecoder;

/**
 *
 * HttpRequestServlet shows the http protocol.
 *
 * @author Rod Byrne
 */
public class HttpRequestServlet extends HttpServlet {
    private util.HTMLTemplates html;

    private void fieldInfo(
        PrintWriter o, String name, String info )
    {
        o.print("<dt>");
        o.print( name );
        o.println("</dt>");
        o.println("<dd>");
        o.print( info );
        o.println( "</dd>" );
    }

    private void dumpInfo(
        PrintWriter o, HttpServletRequest request )
    {
        o.print("<h2>Url:</h2>");
        o.println("<dl>");
        fieldInfo(o, "method", request.getMethod() );
        fieldInfo(o, "servlet-path", request.getServletPath() );
        fieldInfo(o, "path-info", request.getPathInfo() );
        String auth = request.getAuthType();
        if ( auth != null ) {
            fieldInfo(o, "auth-type", auth );
        }
        String remoteUser = request.getRemoteUser();
        if ( remoteUser != null ) {
            fieldInfo(o, "remote-user", remoteUser );
        }
        String q = request.getQueryString();
        if ( q != null ) {
            fieldInfo(o, "query", html.escape(q) );
        }
        o.println("</dl>");
        o.print("<h2>Headers:</h2>");
        o.print("<dl>");
        Enumeration<String> names = request.getHeaderNames();
        while ( names.hasMoreElements() ) {
            String name = names.nextElement().toString();
            fieldInfo(o, name, request.getHeader(name) );
        }
        o.print("</dl>");
        Cookie[] cookies = request.getCookies();
        if ( cookies.length != 0 ) {
            o.print("<h2>Cookies:</h2>");
            o.println("<dl>");
            for( Cookie c : cookies ) {
                o.printf("<dt>%s</dt>%n", c.getName());
                o.printf("<dd>%s</dd>%n", c.getValue());
            }
            o.println("</dl>");
        }
    }

    private void dumpParameters( PrintWriter o, HttpServletRequest request ){
        Enumeration<String> names = request.getParameterNames();
        if ( names.hasMoreElements() ) {
            o.print("<h2>Parameters</h2>");
            o.print("<dl>");
            while( names.hasMoreElements() ) {
                String name = (String)names.nextElement();
                o.println( "<dt>" + name + "</dt>");
                o.println( "<dd>" );
                String[] values = request.getParameterValues(name);
                for( String v : values ) {
                    o.print( v + ", " );
                }
                o.println( "</dd>" );
            }
            o.println("</dl>");
        }
    }

    @Override
    protected void doGet(
        HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException
    {
        log( request.getRequestURI() );
        util.HTTPUtils.nocache( response );
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        html.printHtmlStart(out);
        out.println("<body>");
        dumpInfo( out, request );
        dumpParameters( out, request );
        out.println("</body>");
        html.printHtmlEnd(out);
    }

    @Override
    protected void doPost(
        HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {
        log( request.getRequestURI() );
        util.HTTPUtils.nocache( response );
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        html.printHtmlStart(out);
        out.println("<body>");
        dumpInfo( out, request );
        BufferedReader rd = request.getReader();
        out.println("<h2>content</h2>");

        int ch;
        StringBuilder sb = new StringBuilder();
        while ( (ch=rd.read()) != -1 ) {
            sb.append( (char)ch);
        }
        out.println("<pre>" + sb.toString() + "</pre>");
        out.println("</body>");
        html.printHtmlEnd(out);
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init( config ); // super.init call is required
        html = util.HTMLTemplates.newHTMLTemplates( this );
    }
}
