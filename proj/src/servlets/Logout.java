package servlets;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class Logout extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		log(request.getRequestURI());
		util.HTTPUtils.nocache(response);
		String context = request.getContextPath();

		try {
			HttpSession session = request.getSession(false);
			session.invalidate();
			request.logout();
			response.sendRedirect(context + "/successful-logout.html");
			return;
		} catch (NullPointerException e) {
			response.sendRedirect(context + "/successful-logout.html");
			return;
		}
	}
}
