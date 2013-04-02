package db;

import java.util.Date;

public class ReportSelect {
	public String[] users;
	public Date startDate;
	public Date endDate;
	public double lowSys, hiSys;
	public double lowDia, hiDia;
	public double lowPulse, hiPulse;

	public String toString() {
		return String.format("%tc %tc %.1f %.1f %.1f %.1f %.1f %.1f",
				startDate, endDate, lowSys, hiSys, lowDia, hiDia, lowPulse,
				hiPulse);
	}
}
