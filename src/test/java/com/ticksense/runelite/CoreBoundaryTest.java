package com.ticksense.runelite;

import static org.junit.Assert.assertFalse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;
import org.junit.Test;

public class CoreBoundaryTest
{
    @Test
    public void corePackagesDoNotImportRuneliteTypes() throws IOException
    {
        assertNoRuneliteImports(Paths.get("src/main/java/com/ticksense/core"));
        assertNoRuneliteImports(Paths.get("src/main/java/com/ticksense/telemetry"));
        assertNoRuneliteImportsOutsideActivityIdCatalogs(Paths.get("src/main/java/com/ticksense/activities"));
        assertNoRuneliteImports(Paths.get("src/main/java/com/ticksense/analytics"));
        assertNoRuneliteImports(Paths.get("src/main/java/com/ticksense/storage"));
    }

    private static void assertNoRuneliteImports(Path root) throws IOException
    {
        if (!Files.exists(root))
        {
            return;
        }
        try (Stream<Path> files = Files.walk(root))
        {
            for (Path file : (Iterable<Path>) files.filter(path -> path.toString().endsWith(".java"))::iterator)
            {
                final String source = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
                assertFalse(file + " imports RuneLite APIs", source.contains("net.runelite."));
            }
        }
    }

    private static void assertNoRuneliteImportsOutsideActivityIdCatalogs(Path root) throws IOException
    {
        if (!Files.exists(root))
        {
            return;
        }
        try (Stream<Path> files = Files.walk(root))
        {
            for (Path file : (Iterable<Path>) files.filter(path -> path.toString().endsWith(".java"))::iterator)
            {
                final String source = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
                assertFalse(file + " imports RuneLite APIs outside an activity ID catalog",
                    source.contains("net.runelite.") && !isActivityIdCatalog(file));
            }
        }
    }

    private static boolean isActivityIdCatalog(Path file)
    {
        final String fileName = file.getFileName().toString();
        return fileName.endsWith("Ids.java") || fileName.endsWith("ContainerIds.java");
    }
}
