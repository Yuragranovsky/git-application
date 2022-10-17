package telran.git;

public enum GitCode {
  OK(""), ERROR_SAVED("Error during saving (see log for details)");
	String value;
	GitCode(String value) {
		this.value = value;
	}
	public String getValue() {
		return value;
	}
}
