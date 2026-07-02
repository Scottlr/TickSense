package com.ticksense.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class DeleteAllDataService
{
    private final TickSenseDataPaths dataPaths;

    public DeleteAllDataService()
    {
        this(TickSenseDataPaths.defaultPaths());
    }

    public DeleteAllDataService(TickSenseDataPaths dataPaths)
    {
        this.dataPaths = Objects.requireNonNull(dataPaths, "dataPaths");
    }

    public void deleteAll() throws IOException
    {
        final Path root = dataPaths.getTickSenseRoot().toAbsolutePath().normalize();
        if (Files.notExists(root))
        {
            return;
        }

        final List<Path> descendants;
        try (Stream<Path> walk = Files.walk(root))
        {
            descendants = walk
                .map(path -> path.toAbsolutePath().normalize())
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());
        }

        for (Path path : descendants)
        {
            if (path.equals(root))
            {
                continue;
            }
            if (!path.startsWith(root))
            {
                throw new IOException("Refusing to delete path outside TickSense root: " + path);
            }
            Files.deleteIfExists(path);
        }
    }
}
