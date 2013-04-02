package servlets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 
 * The user splash
 * 
 * @author Matthew Newell
 * 
 */
public class UserSplash extends HttpServlet {

	private util.HTMLTemplates html;

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		log(request.getRequestURI());
		util.HTTPUtils.nocache(response);
		String context = request.getContextPath();
		String remoteUser = request.getRemoteUser();

		if (request.isUserInRole("admin")) {
			response.sendRedirect(context + "/admin");
			return;
		}

		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		html.printHtmlStart(out);
		out.println("<body>");

		out.println("<div class='nav'>");
		html.printUserNav(out);
		out.println("</div>");
		out.println("<div class='usersplash'>");
		out.println("<h1> Welcome " + remoteUser + "!</h1>");
		out.println("<p>To manage your projects, click my projects. To contribute towards other users projects, click projects.</p><br>");
		out.println("</div>");
		out.println("</body>");
		html.printHtmlEnd(out);
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config); // super.init call is required
		html = util.HTMLTemplates.newHTMLTemplates(this);
	}
}