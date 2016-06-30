import org.testng.annotations.Test;

public class RegexTest {

    @Test
    public void test1() {
        String testFilename = "foo/lib/jcl-over-slf4j-1.7.14.jar";
        
//        testFilename.replaceAll("/", "\\");
        String replaced = testFilename.replace("/", "\\");
        System.out.println(replaced);
    }
}
