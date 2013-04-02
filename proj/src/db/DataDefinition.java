package db;

public class DataDefinition {
	private long index;
	private String name;
	private String type;

	/**
	 * @param index
	 * @param name
	 * @param type
	 */
	public DataDefinition(long index, String name, String type) {
		super();
		this.index = index;
		this.name = name;
		this.type = type;
	}

	/**
	 * @return the index
	 */
	public long getIndex() {
		return index;
	}

	/**
	 * @param index
	 *            the index to set
	 */
	public void setIndex(long index) {
		this.index = index;
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
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	public String toString() {
		return String.format("%d %s %s", index, name, type);
	}
}
