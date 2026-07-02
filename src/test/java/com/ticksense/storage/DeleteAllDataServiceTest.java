package com.ticksense.storage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.junit.Test;

public class DeleteAllDataServiceTest
{
    @Test
    public void deletesOnlyTickSenseDataRoot() throws IOException
    {
        final Path workspaceRoot = Files.createTempDirectory("ticksense-delete");
        final Path tickSenseRoot = workspaceRoot.resolve("ticksense");
        final Path siblingRoot = workspaceRoot.resolve("other-plugin");
        Files.createDirectories(tickSenseRoot.resolve("reports"));
        Files.createDirectories(tickSenseRoot.resolve("indexes"));
        Files.createDirectories(siblingRoot);
        Files.write(tickSenseRoot.resolve("reports").resolve("report-1.json"), java.util.Collections.singletonList("{}"), StandardCharsets.UTF_8);
        Files.write(tickSenseRoot.resolve("indexes").resolve("report-index.json"), java.util.Collections.singletonList("{}"), StandardCharsets.UTF_8);
        Files.write(siblingRoot.resolve("keep.txt"), java.util.Collections.singletonList("keep"), StandardCharsets.UTF_8);

        final DeleteAllDataService service = new DeleteAllDataService(new TickSenseDataPaths(tickSenseRoot));
        service.deleteAll();

        assertTrue(Files.exists(tickSenseRoot));
        try (Stream<Path> stream = Files.list(tickSenseRoot))
        {
            assertEquals(0L, stream.count());
        }
        assertTrue(Files.exists(siblingRoot.resolve("keep.txt")));
    }
}
