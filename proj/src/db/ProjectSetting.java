package db;

public class ProjectSetting {
	private String description;
	private String users;

	/**
	 * @param description
	 * @param users
	 */
	public ProjectSetting(String description, String users) {
		super();
		this.description = description;
		this.users = users;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description
	 *            the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the users
	 */
	public String getUsers() {
		return users;
	}

	/**
	 * @param users
	 *            the users to set
	 */
	public void setUsers(String users) {
		this.users = users;
	}

	public String toString() {
		return String.format("%s %s", description, users);
	}
}
