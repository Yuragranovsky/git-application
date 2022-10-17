package telran.git;

import java.nio.file.Path;
import java.util.Objects;

public  class FileStatus {
	
	public Path path;
	public Status status;
	public FileStatus() {
		
	}
	public FileStatus(Path path, Status status) {
		super();
		this.path = path;
		this.status = status;
	}
	@Override
	public String toString() {
		return "FileStatus [path=" + path + ", status=" + status + "]";
	}
	@Override
	public int hashCode() {
		return Objects.hash(path, status);
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FileStatus other = (FileStatus) obj;
		return Objects.equals(path, other.path) && status == other.status;
	}
}
