package com.github.dreamhead.moco.util;

import com.github.dreamhead.moco.MocoException;
import com.google.common.collect.ImmutableList;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import static com.google.common.collect.ImmutableList.of;

public class Globs {
    public static ImmutableList<String> glob(final String glob) {
        Path path = Paths.get(glob);

        int globIndex = getGlobIndex(path);
        if (globIndex < 0) {
            return of(glob);
        }

        return doGlob(path, searchPath(path, globIndex));
    }

    private static Path searchPath(final Path path, final int globIndex) {
        Path root = path.getRoot();
        Path subpath = path.subpath(0, globIndex);
        if (root == null) {
            return subpath;
        }

        return Paths.get(root.toString(), subpath.toString());
    }

    private static ImmutableList<String> doGlob(final Path path, final Path searchPath) {
        final PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + path);

        try {
            final ImmutableList.Builder<String> builder = ImmutableList.builder();

            Files.walkFileTree(searchPath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
                    if (matcher.matches(file)) {
                        builder.add(file.toString());
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
            return builder.build();
        } catch (IOException e) {
            throw new MocoException(e);
        }
    }

    private static int getGlobIndex(Path path) {
        int nameCount = path.getNameCount();
        for (int i = 0; i < nameCount; i++) {
            String current = path.getName(i).toString();
            int length = current.length();
            for (int j = 0; j < length; j++) {
                if (isGlobMeta(current.charAt(j))) {
                    return i;
                }
            }
        }

        return -1;
    }

    private static final String globMetaChars = "\\*?[{";

    private static boolean isGlobMeta(char c) {
        return globMetaChars.indexOf(c) != -1;
    }

    private Globs() {
    }
}
