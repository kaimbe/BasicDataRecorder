package servlets;

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

import db.PMException;
import db.ProjectManager;
import db.ReportSelect;


// TODO: implement this!!!

public class Reports extends HttpServlet {
	/*
	private util.HTMLTemplates html;
	private ProjectManager pm;
	private ReportSelect defaultSelect;

	private SimpleDateFormat dtFormat;

	private void report(HttpServletResponse response, ReportSelect select)
			throws ServletException, IOException {
		String[] users;
		try {
			users = pm.getUsers(); // update the users
		} catch (PMException ex) {
			users = new String[] {};
		}
		if (select == null) {
			select = defaultSelect;
			select.users = users;
		}
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		html.printHtmlStart(out);
		out.println("<body>");
		out.println("<h1>Blood Pressure Report</h1>");

		out.println("<h2>Report Selection</h2>");
		out.println("<form method='post' action='report'>");
		out.println("<p>");
		out.println("Users:");
		for (String u : users) {
			String checked = "";
			for (String selUser : select.users) {
				if (selUser.equals(u)) {
					checked = "checked";
					break;
				}
			}
			out.printf(
					"<label><input type='checkbox' %s name='users' value='%s'>%s</label> ",
					checked, u, u);
		}
		out.println("<br>");
		out.println("Dates:");
		String sd = dtFormat.format(select.startDate);
		out.printf(
				"<label>Start <input name='start_date' type='text' size='15' value='%s'></label>",
				sd);
		String ed = dtFormat.format(select.endDate);
		out.printf(
				"<label>End <input name='end_date' type='text' size='15' value='%s'></label><br>",
				ed);
		out.println("Systolic:");
		out.printf(
				"<label>Low <input name='low_sys' type='text' size='5' value='%.1f'></label>",
				select.lowSys);
		out.printf(
				"<label>High <input name='hi_sys' type='text' size='5' value='%.1f'></label><br>",
				select.hiSys);
		out.println("Diastolic:");
		out.printf(
				"<label>Low <input name='low_dia' type='text' size='5' value='%.1f'></label>",
				select.lowDia);
		out.printf(
				"<label>High <input name='hi_dia' type='text' size='5' value='%.1f'></label><br>",
				select.hiDia);
		out.println("Pulse Rate:");
		out.printf(
				"<label>Low <input name='low_pulse' type='text' size='5' value='%.1f'></label>",
				select.lowPulse);
		out.printf(
				"<label>High <input name='hi_pulse' type='text' size='5' value='%.1f'></label><br>",
				select.hiPulse);
		out.println("<input type='submit'>");
		out.println("</p></form>");

		try {
			out.println("<h2>Report</h2>");
			out.println("<table class='report'>");
			out.println("<tr>");
			out.print("<td>No</td><td>User</td><td>YY/MM/DD HH:MM</td><td>Systolic</td>");
			out.println("<td>Diastolic</td><td>Pulse</td>");
			out.println("</tr>");
			List<String> records = pm.getAllRecords(select);
			double sumSystolic = 0.0, sumDiastolic = 0.0, sumPulse = 0.0;
			for (String rec : records) {
				out.println("<tr>");
				Date d = new Date(rec.getTimeStamp());
				out.printf("<td>%d</td>%n", rec.getRecordID());
				out.printf("<td>%s</td>%n", rec.getName());
				out.printf("<td>%s</td>%n", dtFormat.format(d));
				out.printf("<td>%.1f</td>%n", rec.getSystolic());
				out.printf("<td>%.1f</td>%n", rec.getDiastolic());
				out.printf("<td>%.1f</td>%n", rec.getPulseRate());
				out.println("</tr>");
				sumSystolic += rec.getSystolic();
				sumDiastolic += rec.getDiastolic();
				sumPulse += rec.getPulseRate();
			}
			sumSystolic /= records.size();
			sumDiastolic /= records.size();
			sumPulse /= records.size();
			out.print("<tr><td>Avg</td><td></td></td><td></td>");
			out.printf("<td>%.1f</td><td>%.1f</td><td>%.1f</td></tr>",
					sumSystolic, sumDiastolic, sumPulse);
		} catch (PMException ex) {
			log(ex.getMessage());
			out.println("<tr><td>Data base error</td></tr>");
		}
		out.println("</table>");
		out.println("</body>");
		html.printHtmlEnd(out);
	}

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		log(request.getRequestURI());
		util.HTTPUtils.nocache(response);
		String context = request.getContextPath();

		String user = request.getRemoteUser();
		if (pm == null) {
			response.sendRedirect(context + Constants.DB_ERR_PAGE);
			return;
		}
		report(response, null);
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

		ReportSelect sel = mkReportSelect(request);
		log("sel = " + sel);
		report(response, sel);
	}

	private ReportSelect mkReportSelect(HttpServletRequest request) {
		try {
			ReportSelect select = new ReportSelect();
			select.users = request.getParameterValues("users");
			if (select.users == null) {
				select.users = new String[0];
			}
			select.startDate = dtFormat.parse(request
					.getParameter("start_date"));
			select.endDate = dtFormat.parse(request.getParameter("end_date"));
			select.lowSys = Double.parseDouble(request.getParameter("low_sys"));
			select.hiSys = Double.parseDouble(request.getParameter("hi_sys"));
			select.lowDia = Double.parseDouble(request.getParameter("low_dia"));
			select.hiDia = Double.parseDouble(request.getParameter("hi_dia"));
			select.lowPulse = Double.parseDouble(request
					.getParameter("low_pulse"));
			select.hiPulse = Double.parseDouble(request
					.getParameter("hi_pulse"));
			return select;
		} catch (Exception ex) {
			return null;
		}
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config); // super.init call is required
		html = util.HTMLTemplates.newHTMLTemplates(this);
		dtFormat = new SimpleDateFormat("yy/MM/dd HH:mm");
		dtFormat.setTimeZone(TimeZone.getDefault());
		Date twentyFirstCentury = new GregorianCalendar(2001, 1, 1).getTime();
		dtFormat.set2DigitYearStart(twentyFirstCentury);
		defaultSelect = new ReportSelect();
		try {
			defaultSelect.startDate = dtFormat.parse("02/01/01 12:00");
			defaultSelect.endDate = dtFormat.parse("99/12/31 12:00");
		} catch (java.text.ParseException ex) {
			// ignore
		}
		defaultSelect.lowSys = 80.0;
		defaultSelect.hiSys = 200.0;
		defaultSelect.lowDia = 40.0;
		defaultSelect.hiDia = 150.0;
		defaultSelect.lowPulse = 30.0;
		defaultSelect.hiPulse = 210.0;
		try {
			pm = new SQLitePM(Constants.DB_PATH);
			defaultSelect.users = pm.getUsers();
		} catch (PMException ex) {
			pm = null;
			log(ex.getMessage());
		}
	}
	*/
}
