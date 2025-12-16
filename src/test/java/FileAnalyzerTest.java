import com.example.filesystemanalyzer.service.FileAnalyzer;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class FileAnalyzerTest {

    @Test
    void testAnalyzeExistingPath() {
        FileAnalyzer analyzer = new FileAnalyzer();
        var result = analyzer.analyze(Path.of("."));
        assertNotNull(result);
    }

    @Test
    void testAnalyzeNonExistingPath() {
        FileAnalyzer analyzer = new FileAnalyzer();
        var result = analyzer.analyze(Path.of("/no/such/path"));
        assertNull(result);
    }
}