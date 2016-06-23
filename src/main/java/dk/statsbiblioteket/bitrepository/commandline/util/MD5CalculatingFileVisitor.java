package dk.statsbiblioteket.bitrepository.commandline.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;

import org.apache.commons.codec.digest.DigestUtils;

/**
 * Helper class to traverse a file tree and calculate MD5 checksums for each file met.
 * The class uses an MD5SumFileWriter to output the calculated checksums.  
 */
public class MD5CalculatingFileVisitor implements FileVisitor<Path> {
    private MD5SumFileWriter writer;

    /**
     * Constructor
     * @param writer {@link MD5SumFileWriter} for writing the found checksums.  
     */
    public MD5CalculatingFileVisitor(MD5SumFileWriter writer) {
        this.writer = writer;
    }
    
    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        try (InputStream is = new BufferedInputStream(Files.newInputStream(file, StandardOpenOption.READ))) {
            String fileChecksum = DigestUtils.md5Hex(is);
            writer.writeChecksumLine(file, fileChecksum);
        }
        
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        return FileVisitResult.TERMINATE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        return FileVisitResult.CONTINUE;
    }
}
