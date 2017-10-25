package io.virtdata.basicsmappers.from_long.to_string;

import io.virtdata.api.ThreadSafeMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.function.LongFunction;
import java.util.regex.Pattern;

@ThreadSafeMapper
public class DirectoryLines implements LongFunction<String> {

    private final static Logger logger = LoggerFactory.getLogger(DirectoryLines.class);
    private final Pattern namePattern;
    private final String basepath;
    private final List<Path> allFiles;
    private Iterator<String> stringIterator;
    private Iterator<Path> pathIterator;

    public DirectoryLines(String basepath, String namePattern) {
        this.basepath = basepath;
        this.namePattern = Pattern.compile(namePattern);
        allFiles = getAllFiles();
        pathIterator = allFiles.iterator();
        try {
            stringIterator = Files.readAllLines(pathIterator.next()).iterator();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public synchronized String apply(long value) {
        while (!stringIterator.hasNext()) {
            if (pathIterator.hasNext()) {
                Path nextPath = pathIterator.next();
                try {
                    stringIterator = Files.readAllLines(nextPath).iterator();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                logger.debug("Resetting path iterator after exhausting input.");
                pathIterator = allFiles.iterator();
            }
        }
        return stringIterator.next();
    }

    private List<Path> getAllFiles() {
        logger.debug("Loading file paths from " + basepath);
        Set<FileVisitOption> options = new HashSet<>();
        options.add(FileVisitOption.FOLLOW_LINKS);
        FileList fileList = new FileList(namePattern);
        try {
            Files.walkFileTree(Paths.get(basepath), options, 10, fileList);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        logger.debug("Loaded " + fileList.paths.size() + " file paths from " + basepath);

        return fileList.paths;
    }

    private static class FileList implements FileVisitor<Path> {
        public final Pattern namePattern;
        public List<Path> paths = new ArrayList<>();

        private FileList(Pattern namePattern) {
            this.namePattern = namePattern;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            if (attrs.isRegularFile()) {
                if (file.toString().matches(namePattern.pattern())) {
                    paths.add(file);
                }
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            return FileVisitResult.CONTINUE;
        }
    }
}

