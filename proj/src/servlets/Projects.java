package servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import db.PMException;
import db.Project;
import db.ProjectManager;
import db.SQLitePM;

public class Projects extends HttpServlet {
	private util.HTMLTemplates html;
	private ProjectManager pm;

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		log(request.getRequestURI());
		util.HTTPUtils.nocache(response);
		String context = request.getContextPath();
		String user = request.getRemoteUser();
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		html.printHtmlStart(out);
		out.println("<body>");
		out.println("<div class='nav'>");

		if (request.isUserInRole("admin")) {
			html.printAdminNav(out);
		} else if (request.isUserInRole("user")) {
			html.printUserNav(out);
		}

		out.println("</div>");

		out.println("<div class='usersplash'>");
		out.println("<h1>Available Projects</h1>");
		out.println("<p>Listed are the projects that you may contribute towards. Click on the project name to go to the project description for that project.</p><br>");
		out.println("<table class='editor'>");
		out.println("<tr>");
		out.print("<td>No</td><td>Project Name</td>");
		out.println("<td>Project Owner</td>");
		out.println("</tr>");

		try {
			List<Project> list = pm.getProjects(user);
			for (Project proj : list) {
				out.println("<tr>");
				out.printf("<td>%d</td>%n", proj.getRecordID());
				out.printf("<td><a href='" + context + Constants.PROJECT_DESC
						+ "/" + proj.getRecordID() + "'>" + "%s</a></td>%n",
						proj.getName());
				out.printf("<td>%s</td>%n", proj.getOwner());
				out.println("</tr>");
			}
		} catch (PMException ex) {
			log(ex.getMessage());
			out.println("<tr><td>Data base error</td></tr>");
		}

		out.println("</div>");
		out.println("</body>");
		html.printHtmlEnd(out);
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config); // super.init call is required
		html = util.HTMLTemplates.newHTMLTemplates(this);

		try {
			pm = new SQLitePM(Constants.DB_PATH);
		} catch (PMException ex) {
			pm = null;
			log(ex.getMessage());
		}
	}
}
