package servlets;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletConfig;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.util.List;

import db.UserAuthDB;
import java.sql.SQLException;

import com.google.gson.Gson;

/**
 * 
 * Manage Users
 * 
 * @author Rod Byrne
 * @author Matthew Newell
 */
public class ManageUsers extends HttpServlet {
	private util.HTMLTemplates html;
	private UserAuthDB userAuth;
	private Gson gson = new Gson();

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		log(request.getRequestURI());
		util.HTTPUtils.nocache(response);
		String context = request.getContextPath();

		String user = request.getRemoteUser();
		if (userAuth == null) {
			response.sendRedirect(context + Constants.DB_ERR_PAGE);
			return;
		}
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		html.printHtmlStart(out);
		out.println("<body>");
		out.println("<div class='nav'>");
		html.printAdminNav(out);
		out.println("</div>");
		out.println("<div class='usersplash'>");
		out.println("<h1>Manage Users</h1>");
		out.println("<p>Add/edit/delete users. To update the role of a user, check a box and click the update button.</p><br>");
		userSection(out);
		out.println("</div>");
		out.println("</body>");
		html.printHtmlEnd(out);
	}

	private void userSection(PrintWriter out) throws IOException {
		out.println("<div id='new-user'>");
		out.println("<h2>New User</h2>");
		out.print("<label>");
		out.print("User Name: <input id='un' type='text' size='20'>");
		out.println("</label><br>");
		out.print("<label>");
		out.print("Password: <input id='pw' type='password' size='20'>");
		out.println("</label><br>");
		out.println("<button id='add-user'>Add User</button><br>");
		out.println("</div>");

		out.println("<div id='user-list'>");
		out.println("<h2>Current Users</h2>");
		try {
			List<String> roles = userAuth.getRoleNames();
			out.println("<table>");
			out.print("<tr><td>Delete User</td>");
			out.print("<td>User</td><td>Roles</td>");
			out.println("<td>Update Roles</td></tr>");
			for (String user : userAuth.getUserNames()) {
				out.println("<tr>");
				out.printf(
						"<td><button user='%s' class='delete-user'>Delete</button></td>%n",
						user);
				out.printf("<td>%s</td>%n", user);
				out.print("<td>");
				List<String> userRoles = userAuth.getUserRoles(user);
				for (String role : roles) {
					String checked = userRoles.contains(role) ? "checked" : "";
					String cb = String.format(
							"<input %s type='checkbox' value='%s'/>", checked,
							role);
					out.printf("<label>%s %s</label> ", cb, role);
				}
				out.println("</td>");
				out.printf(
						"<td><button user='%s' class='update-roles'>Update</button></td>",
						user);
				out.println("</tr>");
			}
			out.println("</table>");
		} catch (SQLException ex) {
			out.println("DATABASE Error"); // XXX
			log(ex.getMessage());
		}
		out.println("</div>");
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

	private void update_user_role(String authUser, String[] roles,
			HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("application/json");
		PrintWriter out = response.getWriter();
		try {
			if (roles.length >= 1) {
				String user = roles[0];
				log("deleting: " + user);
				userAuth.deleteUserRoles(user);
				for (int i = 1; i < roles.length; i++) {
					log("adding: " + roles[i]);
					userAuth.addUserRole(user, roles[i]);
				}
			}
			out.print(gson.toJson("ok"));
		} catch (SQLException ex) {
			log(ex.getMessage());
		}
	}

	private static class UserPasswordEntry {
		String user;
		String password;

		public String toString() {
			return "u: " + user + " pw:" + password;
		}
	}

	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		log(request.getRequestURI());
		String user = request.getRemoteUser();
		String context = request.getContextPath();

		if (userAuth == null) {
			response.sendRedirect(context + Constants.DB_ERR_PAGE);
			return;
		}
		String pathInfo = request.getPathInfo();
		if (pathInfo != null && pathInfo.startsWith("/update_roles")) {
			BufferedReader rd = request.getReader();
			String json = readAll(rd);
			log(json);
			String[] user_roles = gson.fromJson(json, String[].class);
			update_user_role(user, user_roles, response);
		} else if (pathInfo != null && pathInfo.startsWith("/add_user")) {
			PrintWriter out = response.getWriter();
			response.setContentType("application/json");
			BufferedReader rd = request.getReader();
			String json = readAll(rd);
			log(json);
			try {
				// demonstrate using a class with json
				UserPasswordEntry entry = gson.fromJson(json,
						UserPasswordEntry.class);
				userAuth.addUser(entry.user, entry.password);
				out.print("{\"redirect\":\"manage_users\"}");
			} catch (SQLException ex) {
				log(ex.getMessage());
				out.print(gson.toJson("error"));
			}
		} else if (pathInfo != null && pathInfo.startsWith("/delete_user")) {
			PrintWriter out = response.getWriter();
			response.setContentType("application/json");
			BufferedReader rd = request.getReader();
			String json = readAll(rd);
			log(json);
			try {
				String[] userArray = gson.fromJson(json, String[].class);
				userAuth.deleteUser(userArray[0]);
				out.print(gson.toJson(1)); // ok
			} catch (SQLException ex) {
				log(ex.getMessage());
				out.print(gson.toJson("error"));
			}
		} else {
			PrintWriter out = response.getWriter();
			response.setContentType("application/json");
			out.write(gson.toJson("not valid url"));
		}
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config); // super.init call is required
		html = util.HTMLTemplates.newHTMLTemplates(this);
		try {
			userAuth = new UserAuthDB(Constants.UA_DB_PATH);
		} catch (SQLException ex) {
			userAuth = null;
			log(ex.getMessage());
		}
	}
}
