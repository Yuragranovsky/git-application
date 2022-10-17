package telran.git;

import java.util.logging.Level;

public class GitAppl {

	public static void main(String[] args) {
		try {
			setLogLevel(args);
			GitRepository gitRepository = GitRepositoryImpl.init();
			gitRepository.save();
			System.out.println(gitRepository.getDirectoryPathName());
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

	}

	private static void setLogLevel(String[] args) throws Exception {
		if (args.length > 0) {
			try {
				Level level = Level.parse(args[0].toUpperCase());
				GitRepositoryImpl.setLogLevel(level);
			} catch (IllegalArgumentException e) {
				throw new Exception(String.format("%s - wrong logging level", args[0]));
			}
		}
	}

}
