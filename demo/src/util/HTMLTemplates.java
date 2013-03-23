package util;

import java.util.Arrays;
import java.io.PrintWriter;
import javax.servlet.GenericServlet;
import javax.servlet.ServletContext;

/**
 * HTMLTemplates provide a very simple html template.
 *
 * @author Rod Byrne
 */
public class HTMLTemplates {
    private String contextPath;
    private String[] jsSrc;
    private String[] cssSrc;
    private String title;

    // html5 doctype
    private static String htmlStartStr = "<!DOCTYPE html>\n<html>\n";
    private static String htmlEndStr = "</html>\n";

    public HTMLTemplates(
        String contextPath, String[] jsSrc, String[] cssSrc, String title)
    {
        this.contextPath = contextPath;
        this.jsSrc = Arrays.copyOf( jsSrc, jsSrc.length ); 
        this.cssSrc = Arrays.copyOf( cssSrc, cssSrc.length ); 
        this.title = title;
    }

    public void printHtmlStart( PrintWriter pw, String extra ) {
        pw.print( htmlStartStr );
        printHead( pw, extra );
    }

    public void printHtmlStart( PrintWriter pw ) {
        printHtmlStart( pw, null );
    }

    public void printHead( PrintWriter pw ) {
        printHead(pw, null);
    }

    public void printHead( PrintWriter pw, String extra ) {
        pw.println("<head>");
        pw.print("<title>");
        pw.print(title);
        pw.println("</title>");
        for( String css : cssSrc ) {
            pw.print("<link rel='stylesheet' type='text/css' href='");
            pw.print( contextPath );
            pw.print( "/css/" );
            pw.print( css );
            pw.println("'>");
        }
        for( String js : jsSrc ) {
            pw.print("<script type='text/javascript' src='");
            pw.print( contextPath );
            pw.print( "/js/" );
            pw.print( js );
            pw.println("'>");
            pw.println("</script>");
        }
        if ( extra != null ) {
            pw.print(extra);
        }
        pw.println("</head>");
    }

    public void printHtmlEnd( PrintWriter pw ) {
        pw.print( htmlEndStr );
    }

    public String escape( String str ) {
        StringBuilder sb = new StringBuilder();
        for( int i = 0 ; i < str.length(); i++ ) {
            char ch = str.charAt(i);
            switch( ch ) {
            case '&':
                sb.append("&amp;");
                break;
            case '<':
                sb.append("&lt;");
                break;
            case '>':
                sb.append("&gt;");
                break;
            default:
                sb.append(ch);
                break;
            }
        }
        return sb.toString();
    }

    /**
     * newHTMLTemplates uses init parameters to
     * create a HTMLTemplates.
     */
    public static HTMLTemplates newHTMLTemplates(GenericServlet servlet) {
        ServletContext context = servlet.getServletContext();
        String contextPath = context.getContextPath();
        String[] js = { };
        String[] css = { };
        String cssParam = servlet.getInitParameter("css");
        if ( cssParam != null ) {
            css = cssParam.split(",");
        }
        String jsParam = servlet.getInitParameter("js");
        if ( jsParam != null ) {
            js = jsParam.split(",");
        }
        String title = servlet.getInitParameter("title");
        if ( title == null ) {
            title = servlet.getClass().getName();
        }
        return new HTMLTemplates( contextPath, js, css, title );
    }
}
