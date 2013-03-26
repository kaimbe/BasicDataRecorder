import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.server.Connector;

import org.eclipse.jetty.util.log.StdErrLog;
import org.eclipse.jetty.util.log.JavaUtilLog;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.security.JDBCLoginService;
import org.eclipse.jetty.security.LoginService;

import java.io.File;

/**
 * SingleWebApp - runs a single web application.
 *
 * @author Rod Byrne
 */

public class SingleWebApp {
    public static void main(String[] args) throws Exception {
        if ( args.length < 2 || args.length > 4  ) {
            System.out.println("usage: java SingleWebApp [-h path|-j path] war-dir context");
            System.exit(1);
        }
        int argIndex = 0;
        LoginService loginService = null;
        // check for login service
        if ( args[0].equals( "-h") ) {
            String passwordPath = args[1];
            File f = new File( passwordPath );
            if ( ! f.exists() ) {
                System.err.println("missing password file");
            }
            else {
                HashLoginService hs = new HashLoginService();
                hs.setName( "Test Realm" );
                hs.setConfig( passwordPath );
                hs.setRefreshInterval( 5 );
                loginService = hs;
            }
            argIndex += 2;
        }
        else if ( args[0].equals( "-j") ) {
            String dbPath = args[1];
            File f = new File( dbPath );
            if ( ! f.exists() ) {
                System.err.println("missing data base config file");
            }
            else {
                JDBCLoginService hs = new JDBCLoginService();
                hs.setName( "Test Realm" );
                hs.setConfig( dbPath );
                loginService = hs;
            }
            argIndex += 2;
        }

        if ( (argIndex+2) > args.length ) {
            System.out.println("usage: java SingleWebApp [-h path|-j path] war-dir context");
            System.exit(1);
        }
        String warDirName = args[argIndex];
        String contextPath = args[argIndex+1];

        File warDir = new File(warDirName);
        if ( ! warDir.exists() ) {
            System.out.println("SingleWebApp: missing web app directory");
            System.exit(1);
        }

        // use java.util.logging for logging
        // check if logs directory exists
        File logDir = new File("./logs");
        if ( ! logDir.exists() ) {
            System.out.println("SingleWebApp: missing logs directory");
            System.exit(1);
        }
        String logName = "logs/" + warDirName + ".log";
        java.util.logging.FileHandler fh =
            new java.util.logging.FileHandler( logName );
        // get default logger
        java.util.logging.Logger.getLogger("").addHandler(fh);
        Log.setLog( new JavaUtilLog() );

        //Server server = new Server( 8080 );
        Server server = new Server();
        SelectChannelConnector conn = new SelectChannelConnector();
        conn.setPort(8000);
        conn.setHost("localhost"); // uncomment to listen only locally
        Connector[] conns = { conn };
        server.setConnectors( conns );
        
        WebAppContext webapp = new WebAppContext(warDirName, contextPath);
        // http://www.eclipse.org/jetty/documentation/current/configuring-security.html
        
        
        if ( loginService != null) {
            SecurityHandler sh = webapp.getSecurityHandler();
            sh.setLoginService( loginService );
        }
        

        server.setHandler(webapp);
        server.setGracefulShutdown(0);
        server.start();
        server.join();
    }
}
