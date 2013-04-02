package db;

public class Project {
	private String name;
	private long recordID;
	private String owner;

	/**
	 * @param name
	 * @param recordID
	 * @param owner
	 */
	public Project(String name, long recordID, String owner) {
		super();
		this.name = name;
		this.recordID = recordID;
		this.owner = owner;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the recordID
	 */
	public long getRecordID() {
		return recordID;
	}

	/**
	 * @param recordID
	 *            the recordID to set
	 */
	public void setRecordID(long recordID) {
		this.recordID = recordID;
	}

	/**
	 * @return the owner
	 */
	public String getOwner() {
		return owner;
	}

	/**
	 * @param owner
	 *            the owner to set
	 */
	public void setOwner(String owner) {
		this.owner = owner;
	}

	public String toString() {
		return String.format("%s %d %s", name, recordID, owner);
	}
}
