package telran.git;

import java.io.Serializable;
import java.nio.file.Path;
import java.util.*;
public interface GitRepository extends Serializable{
	public static final String GIT_FILE = ".mygit";
GitCode commit(String message);
List<FileStatus>info();
GitCode switchTo(String branch);
GitCode discardChange();
GitCode createBranch(String branchName);
GitCode deleteBranch(String branchName);
GitCode renameBranch(String branchName, String newName);
GitCode mergeBranch(String branchName);
GitCode save();
String getDirectoryPathName();
String getHead();
List<Path> getCommitFiles(String commitName);
Collection<String> branches();
Collection<String> commits();

}
