package com.ticksense.activities;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.Test;

public class ActivityIdCatalogPolicyTest
{
    @Test
    public void activityIdCatalogsKeepRuneLiteConstantsAsSourceOwnedPrimitiveIds() throws IOException
    {
        final List<String> violations = new ArrayList<>();
        try (Stream<Path> paths = Files.walk(Paths.get("src/main/java/com/ticksense/activities")))
        {
            paths
                .filter(Files::isRegularFile)
                .filter(ActivityIdCatalogPolicyTest::isIdCatalog)
                .forEach(path -> collectRuneliteImportViolations(path, violations));
        }

        assertTrue(
            "Activity ID catalogs should use source-owned primitive IDs with provenance comments, not net.runelite imports: " + violations,
            violations.isEmpty());
    }

    private static boolean isIdCatalog(Path path)
    {
        final String fileName = path.getFileName().toString();
        return fileName.endsWith("Ids.java") || fileName.endsWith("ContainerIds.java");
    }

    private static void collectRuneliteImportViolations(Path path, List<String> violations)
    {
        try
        {
            final String source = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
            if (source.contains("import net.runelite."))
            {
                violations.add(path.toString());
            }
        }
        catch (IOException ex)
        {
            throw new IllegalStateException("Unable to read " + path, ex);
        }
    }
}
