package telran.git;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Level;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class GitTest {
	private static final String FILE1_NAME = "file1";
	private static final String FILE2_NAME = "file2";
	static GitRepository repository;
	static int order;

	@BeforeAll
	static void setUp() {
		GitRepositoryImpl.setLogLevel(Level.FINE);
		GitRepositoryImpl.setIgnoreRegExpression(GitRepositoryImpl.TEST);
	}

	

	@Test
	@Order(1)
	void initTest() throws Exception {
		
		Path path = Path.of(GitRepository.GIT_FILE);
		Files.deleteIfExists(path);
		Files.list(Path.of(".")).filter(p -> Files.isRegularFile(p))
		.forEach(t -> {
			try {
				Files.delete(t);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
		order++;
		assertEquals(1, order);
		repository = GitRepositoryImpl.init();
		System.out.println(repository.getDirectoryPathName());
		repository.save();
		repository = GitRepositoryImpl.init();
		System.out.println(repository.getDirectoryPathName());
	}
	@Test
	@Order(2)
	/**
	 * 
	 * @throws Exception
	 * 
	 */
	@DisplayName(" two files in UNTRACKED")
	void infoUntrackedTest() throws Exception {
		
		createFile(FILE1_NAME);
		createFile(FILE2_NAME);
		List<FileStatus> expected = Arrays.asList(new FileStatus(Path.of(FILE1_NAME),
				Status.UNTRACKED),new FileStatus(Path.of(FILE2_NAME),
						Status.UNTRACKED) );
		assertEquals(expected, repository.info());
		
	}



	private void createFile(String fileName) throws FileNotFoundException {
		PrintStream output1 = new PrintStream(fileName);
		output1.println("abcd");
		output1.close();
	}
	@Test
	@Order(3)
	@DisplayName("commit two files")
	void commitTest() {
		repository.commit("two files with abcd");
		Collection<String> commitNames = repository.commits();
		assertEquals(1, commitNames.size());
		commitNames.forEach(n -> assertTrue(n.length() == 7));
		Collection<String> branchNames = repository.branches();
		assertEquals(1, branchNames.size());
		assertTrue(branchNames.contains("master"));
	}
	@Test
	@Order(4) 
	@DisplayName("two files in COMMITTED")
	void infoCommittedTest() throws Exception {
		List<FileStatus> expected = Arrays.asList(new FileStatus(Path.of(FILE1_NAME),
				Status.COMMITTED),new FileStatus(Path.of(FILE2_NAME),
						Status.COMMITTED) );
		assertEquals(expected, repository.info());
	}
	

}
