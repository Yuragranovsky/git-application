package telran.git;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.*;

public class GitRepositoryImpl implements GitRepository {
	private static final long serialVersionUID = 1L;
	public static final String STARTS_POINT = "starts-with-point";
	public static final String TEST = "test";
	private static HashMap<String, String> ignoreExpressions;
	private static String ignoreRegExpression = STARTS_POINT;
	

	public static void setIgnoreRegExpression(String ignoreRegExpression) {
		GitRepositoryImpl.ignoreRegExpression = ignoreRegExpression;
	}


	static Logger LOG = Logger.getAnonymousLogger();
	static Handler handler;
	static {
		ignoreExpressions = new HashMap<>();
		ignoreExpressions.put(TEST, "\\..*|.*bin|.*src");
		ignoreExpressions.put(STARTS_POINT, "\\..*");
		handler = new ConsoleHandler();
		LOG.addHandler(handler);
	}
	public static void setLogLevel(Level level) {
		handler.setLevel(level);
		LOG.setLevel(level);
	}
	

	String gitPath;
	HashMap<String, Commit> commits;
	HashMap<Path, CommitFile> commitFiles;
	HashMap<String, Branch> branches;
	String head; //name of current branch or commit
	

	private GitRepositoryImpl(Path git) {
		this.gitPath = git.toString();
		commits = new HashMap<>();
		commitFiles = new HashMap<>();
		branches = new HashMap<>();

	}

	public static GitRepository init() throws Exception {

		Path path = Path.of(".").toAbsolutePath();
		Path git = path.resolve(GIT_FILE);
		GitRepository res = null;
		if (Files.exists(git)) {
			LOG.log(Level.FINE, String.format("Restored from file %s", git));
			res = restoreFromFile(git);
		} else {
			LOG.fine("New git");
			res = new GitRepositoryImpl(git.normalize());

		}
		return res;

	}

	private static GitRepository restoreFromFile(Path gitPath) throws Exception {
		try (ObjectInputStream input = new ObjectInputStream(Files.newInputStream(gitPath))) {
			return (GitRepository) input.readObject();
		} catch (Exception e) {
			LOG.log(Level.SEVERE, e.toString());
			throw e;
		}

	}

	private static class Commit implements Serializable{
		private static final long serialVersionUID = 1L;
		String commitName;
		String commitMessage;
		Commit prev;
		List<CommitFile> commitContent;

	}

	private static class CommitFile implements Serializable {
		
		private static final long serialVersionUID = 1L;
		Path filePath;
		Instant timeModified;
		String[] content;
		String commitName;
		CommitFile(Path filePath, Instant timeModified, String[] content, String commitName) {
			
			this.filePath = filePath;
			this.timeModified = timeModified;
			this.content = content;
			this.commitName = commitName;
		}

	}

	private static class Branch implements Serializable{
		private static final long serialVersionUID = 1L;
		String branchName;
		String commitName;
		int nCommits;
	}
	

	@Override
	public GitCode commit(String message) {
		
		return head == null ? commitHeadNull(message) : commitHeadNoNull(message);
	}

	private GitCode commitHeadNoNull(String message) {
		Branch branch = branches.get(head);
		if (branch == null) {
			throw new RuntimeException("Commit is allowed only to branch");
		}
		Commit commit = createCommit(message, commits.get(branch.commitName));
		branch.commitName = commit.commitName;
		branch.nCommits++;
		return GitCode.OK;
	}

	private GitCode commitHeadNull(String message) {
		Commit commit = createCommit(message, null);
		createInternalBranch("master", commit, 1);
		return GitCode.OK;
	}

	private void createInternalBranch(String branchName, Commit commit, int nCommits) {
		if (branches.containsKey(branchName)) {
			throw new IllegalArgumentException(String.format("Branch %s already exists",
					branchName));
		}
		Branch branch = new Branch();
		branch.branchName = branchName;
		branch.commitName = commit.commitName;
		branch.nCommits = nCommits;
		branches.put(branchName, branch);
		head = branchName;
		
	}

	private Commit createCommit(String message, Commit prev) {
		
		Commit res = new Commit();
		res.commitName = getCommitName();
		res.commitMessage = message;
		res.prev = prev;
		if (commits.putIfAbsent(res.commitName, res) != null) {
			throw new IllegalStateException(String.format("Commit with name %s already exist",
					res.commitName));
		}
		res.commitContent = getCommitContent(res.commitName);
		return res;
	}

	private List<CommitFile> getCommitContent(String commitName) {
		List<FileStatus> files = info();
		return files.stream().filter(fs -> fs.status == Status.UNTRACKED ||
				fs.status == Status.MODIFIED)
				.map(fs -> toCommitFile(fs, commitName)).toList();
	}
	private CommitFile toCommitFile(FileStatus fs, String commitName) {
		
		Instant timeModified;
		try {
			timeModified = Files.getLastModifiedTime(fs.path).toInstant();
		} catch (IOException e) {
			throw new RuntimeException(e.toString());
		}
		String[] content = getFileContent(fs.path);
		CommitFile res = new CommitFile(fs.path, timeModified, content, commitName);
		commitFiles.put(fs.path, res);
		return res;
	}
	private String[] getFileContent(Path path) {
		
		try {
			return Files.lines(path).toArray(String[]::new);
		} catch (IOException e) {
			throw new RuntimeException(e.toString());
		}
	}

	private String getCommitName() {
		String res = "";
		do {
			res = Integer.toString(ThreadLocalRandom.current()
					.nextInt(0x1000000, 0xfffffff), 16);
		} while(commits.containsKey(res));
		return res;
	}

	@Override
	public GitCode createBranch(String branchName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GitCode deleteBranch(String branchName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GitCode renameBranch(String branchName, String newName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GitCode mergeBranch(String branchName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GitCode switchTo(String branch) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GitCode discardChange() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GitCode save() {
		GitCode res = GitCode.OK;
		try (ObjectOutputStream output = new ObjectOutputStream(Files.newOutputStream(Path.of(gitPath)))) {
			output.writeObject(this);
			LOG.log(Level.FINE, String.format("saved to %s", gitPath));
		} catch (Exception e) {
			res = GitCode.ERROR_SAVED;
			LOG.log(Level.SEVERE, e.toString());
		}
		return res;
	}

	@Override
	public String getDirectoryPathName() {

		return gitPath.toString();
	}

	@Override
	public List<FileStatus> info() {
		
		Path directoryPath = Path.of(".");
		
		
		
		try {
			return Files.list(directoryPath)
					
					.map(p -> p.normalize())
					.filter(p -> !ignoreFilter(p))
					.map(p -> {
						try {
							return new FileStatus(p, getStatus(p));
						} catch (IOException e) {
							throw new RuntimeException(e.toString());
						}
					})
					.toList();
		} catch (IOException e) {
			LOG.log(Level.SEVERE, e.toString());
			return Collections.emptyList();
		}
				
	}

	private boolean ignoreFilter(Path path) {
		return path.toString().matches(ignoreExpressions.get(ignoreRegExpression))
				|| Files.isDirectory(path); //FIXME - no nested directories
	}

	private Status getStatus(Path p) throws IOException {
		CommitFile commitFile = commitFiles.get(p);
		
		return commitFile == null ? Status.UNTRACKED : getStatusFromCommitFile(commitFile, p);
	}

	private Status getStatusFromCommitFile(CommitFile commitFile, Path p) throws IOException {
		
		return Files.getLastModifiedTime(p).toInstant().compareTo(commitFile.timeModified) > 0 ? 
				Status.MODIFIED : Status.COMMITTED;
	}

	@Override
	public String getHead() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Path> getCommitFiles(String commitName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<String> branches() {
		
		return branches.values().stream().map(b -> b.branchName).toList();
	}

	@Override
	public Collection<String> commits() {
		
		return commits.values().stream().map(c -> c.commitName).toList();
	}

	
}
