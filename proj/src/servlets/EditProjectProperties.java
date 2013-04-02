package servlets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import db.DataDefinition;
import db.PMException;
import db.Project;
import db.ProjectManager;
import db.ProjectSetting;
import db.SQLitePM;

public class EditProjectProperties extends HttpServlet {
	private util.HTMLTemplates html;
	private Gson gson = new Gson();
	private ProjectManager pm;

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		log(request.getRequestURI());
		util.HTTPUtils.nocache(response);
		String context = request.getContextPath();
		String user = request.getRemoteUser();
		String pathInfo = request.getPathInfo();
		String[] pathParts = pathInfo.split("/");
		String projName = pathParts[1];
		long projid = Long.parseLong(projName);

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
		String rProjName = "";
		try {
			rProjName = pm.getProjectName(projid);
		} catch (PMException ex) {
			log(ex.getMessage());
		}

		out.println("</div>");

		out.println("<div class='usersplash'>");
		out.println("<h1>Project Properties</h1>");
		out.println("<h2>Project Name: " + rProjName + "</h2>");
		out.println("<p>You may edit the properties of a project here. Edit the text areas and click update properties to commit the changes. NOTE: Multiple usernames are seperated by a comma. The owner of this project does not need to be included here.</p><br>");
		try {
			ProjectSetting setting = pm.getProjectSetting(projid);
			if (((setting.getUsers() == null) || (setting.getUsers().equals("")))
					&& ((setting.getDescription() == null) || (setting
							.getDescription().equals("")))) {
				out.println("<label>Project Description: <br><textarea id='des' rows='5' cols='30'>Enter a description of the project...</textarea></label><br>");
				out.println("<label>Project Users: <br><textarea id='usrs' rows='5' cols='30'>Enter the users that are allowed to contribute towards this project...</textarea></label><br>");
			} else {
				out.println("<label>Project Description: <br><textarea id='des' rows='5' cols='30'>"
						+ setting.getDescription() + "</textarea></label><br>");
				out.println("<label>Project Users: <br><textarea id='usrs' rows='5' cols='30'>"
						+ setting.getUsers() + "</textarea></label><br>");
			}
		} catch (PMException ex) {
			log(ex.getMessage());
			out.println("<p>Data base error</p>");
		}

		out.println("<input type='button' id='update_properties' value='Update Properties'>");

		out.println("</div>");
		out.println("</body>");
		html.printHtmlEnd(out);
	}

	private String readAll(BufferedReader rd) throws IOException {
		int amt;
		StringBuilder sb = new StringBuilder();
		char[] buf = new char[10240];
		while ((amt = rd.read(buf, 0, buf.length)) != -1) {
			sb.append(buf, 0, amt);
		}
		return sb.toString();
	}

	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		log(request.getRequestURI());
		util.HTTPUtils.nocache(response);
		PrintWriter out = response.getWriter();
		String user = request.getRemoteUser();
		String context = request.getContextPath();

		if (pm == null) {
			response.sendRedirect(context + Constants.DB_ERR_PAGE);
			return;
		}
		if (!request.isUserInRole("user")) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN,
					"No premission");
			return;
		}

		String pathInfo = request.getPathInfo();
		String[] pathParts = pathInfo.split("/");
		String projName = pathParts[1];
		String cmd = "/" + pathParts[2];
		long projid = Long.parseLong(projName);

		if (pathInfo != null && cmd.startsWith("/update")) {
			BufferedReader rd = request.getReader();
			String json = readAll(rd);
			try {
				ProjectSetting sett = (ProjectSetting) gson.fromJson(json,
						ProjectSetting.class);
				pm.updateProjectSettings(projid, sett);
				log("updating: " + user + "=" + sett);
				response.setContentType("application/json");
				out.print(gson.toJson("ok")); // ok
				return;
			} catch (PMException ex) {
				log(ex.getMessage());
				response.sendRedirect(context + Constants.DB_ERR_PAGE);
				return;
			}
		} else {
			response.sendRedirect(context + Constants.INVALID_URL_PAGE);
			return;
		}
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
