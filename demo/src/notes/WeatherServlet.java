package  notes;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.TreeMap;
import java.net.URLDecoder;
import java.io.UnsupportedEncodingException;

public class WeatherServlet extends HttpServlet {

    private static String htmlHead = "<html><head></head><body>";
    private static String htmlTail = "</body></html>";

    public static class Pair {
        public String name;
        public String value;
        public Pair( String name, String value ) {
            this.name = name;
            this.value = value;
        }
    }

    protected void processQuery(
        List<Pair> list,
        HttpServletRequest request,
        HttpServletResponse response )
        throws ServletException, IOException
    {
        String path = request.getPathInfo();
        path = path.substring(1);
        String[] parts = path.split("/");
        if ( parts[0].equals("update") ) {
            update( list, request, response );
        }
        else if ( parts[0].equals("list") ) {
            report( response );
        }
    }

    private TreeMap<String,WeatherDesc> weather =
        new TreeMap<String,WeatherDesc>();

    private synchronized void updateWeatherDesc( 
        List<Pair> list )
    {
        String location = null;
        double temp = Double.NaN;
        String synopsis = null;

        Iterator<Pair> it = list.iterator();
        while ( it.hasNext() ) {
            Pair p = it.next();
            if ( p.name.equals( "location" ) ) {
                location = p.value;
            }
            else if ( p.name.equals( "temp" ) ) {
                temp = Double.parseDouble( p.value );
            }
            else if ( p.name.equals( "synopsis" ) ) {
                synopsis = p.value;
            }
        }
        weather.put(
            location,
            new WeatherDesc(location,temp,synopsis) );
    }

    private void update(
        List<Pair> list,
        HttpServletRequest request,
        HttpServletResponse response )
        throws ServletException, IOException
    {
        updateWeatherDesc( list );
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println( htmlHead );
        out.println("<p>Thank you for your report.</p>");
        out.println( htmlTail );
    }

    private void report( HttpServletResponse response )
        throws ServletException, IOException
    {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println( htmlHead );

        out.println("<table border='1'>");
        synchronized ( this ) {
            Iterator<WeatherDesc> it = weather.values().iterator();
            while ( it.hasNext() ) {
                WeatherDesc w = it.next();
                out.print("<tr>");
                out.print("<td>"+w.getLocation()+"</td>");
                out.print("<td>"+w.getTemperature()+"</td>");
                out.print("<td>"+w.getSynopsis()+"</td>");
                out.print("</tr>");
            }
        }
        out.println("</table>");
        out.println( htmlTail );
    }

    protected void doGet(
        HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {
        List<Pair> list = decodeQuery(request);
        processQuery( list, request, response );
    }

    protected void doPost(
        HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {
        List<Pair> list = decodeQuery( request );
        processQuery( list, request, response );
    }

    private List<Pair> decodeQuery( HttpServletRequest request ) {
        ArrayList<Pair> list = new ArrayList<Pair>();

        Enumeration<String> params = request.getParameterNames();
        while( params.hasMoreElements() ) {
            String name = params.nextElement();
            String value = request.getParameter( name );
            list.add( new Pair( name, value ) );
        }

        return list;
    }
}
