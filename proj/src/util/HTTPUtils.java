package util;

import javax.servlet.http.HttpServletResponse;

public class HTTPUtils {
    public static void nocache( HttpServletResponse response ) {
        response.setHeader("Cache-Control","no-cache,no-store,must-revalidate");
        response.setHeader("Pragma","no-cache"); // HTTP 1.0
        response.setDateHeader ("Expires", 0 );
    }
}
