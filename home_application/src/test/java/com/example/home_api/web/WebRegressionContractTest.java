package com.example.home_api.web;

import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;

class WebRegressionContractTest {
    private static final Path MAIN = Path.of("src/main");
    private static final Pattern POST_FORM = Pattern.compile("<form\\b(?=[^>]*\\bmethod=\"post\")[^>]*>", Pattern.CASE_INSENSITIVE);

    @Test void everyPostFormUsesThymeleafActionSoCsrfTokenCanBeInserted() throws IOException {
        try (Stream<Path> files = Files.walk(MAIN.resolve("resources/templates"))) {
            var violations = files.filter(p -> p.toString().endsWith(".html")).flatMap(path -> {
                try {var matcher=POST_FORM.matcher(Files.readString(path));var result=new java.util.ArrayList<String>();while(matcher.find())if(!matcher.group().contains("th:action="))result.add(path+" -> "+matcher.group());return result.stream();}
                catch(IOException e){throw new java.io.UncheckedIOException(e);}
            }).toList();
            assertTrue(violations.isEmpty(),"CSRF tokenを挿入できないPOSTフォーム: "+violations);
        }
    }

    @Test void schemaChangesAreVersionedAndNotHiddenInAdminInitializer() throws IOException {
        String initializer=Files.readString(MAIN.resolve("java/com/example/home_api/web/AdminInitializer.java"));
        assertFalse(initializer.contains("CREATE TABLE"));assertFalse(initializer.contains("ALTER TABLE"));
        assertTrue(Files.exists(MAIN.resolve("resources/db/migration/V1__web_application_schema.sql")));
    }

    @Test void validationErrorsKeepSubmittedManagementFormValues() throws IOException {
        String masters=Files.readString(MAIN.resolve("java/com/example/home_api/web/GroupMasterController.java"));
        String patterns=Files.readString(MAIN.resolve("java/com/example/home_api/web/PurchasePatternController.java"));
        assertTrue(masters.contains("addFlashAttribute(\"formName\""));assertTrue(patterns.contains("addFlashAttribute(\"patternForm\""));
    }

    @Test void adminBackupIncludesAndRestoresGroupHomeImages() throws IOException {
        String controller=Files.readString(MAIN.resolve("java/com/example/home_api/web/AdminDataController.java"));
        assertTrue(controller.contains("home-application-backup-v3"));
        assertTrue(controller.contains("groupHomeImages"));
        assertTrue(controller.contains("validateGroupHomeImageReferences"));
        assertTrue(controller.contains("replaceGroupHomeImages"));
        assertTrue(controller.contains("safeGroupHomeImagePath"));
        assertTrue(controller.contains("isSupportedImage"));
    }

    @Test void adminBackupIncludesKanbanAndUploadedFonts() throws IOException {
        String controller=Files.readString(MAIN.resolve("java/com/example/home_api/web/AdminDataController.java"));
        assertTrue(controller.contains("\"kanban_lane\",\"kanban_card\""));
        assertTrue(controller.contains("files.put(\"fonts\",fonts)"));
        assertTrue(controller.contains("validateFontReferences"));
        assertTrue(controller.contains("replaceFiles(FONT_DIR,fonts"));
        assertTrue(controller.contains("isSupportedFont"));
    }

    @Test void kanbanOrderingIsSerializedAndDoesNotSaveEveryLaneConcurrently() throws IOException {
        String controller=Files.readString(MAIN.resolve("java/com/example/home_api/web/KanbanController.java"));
        String template=Files.readString(MAIN.resolve("resources/templates/group/kanban.html"));
        assertTrue(controller.contains("FOR UPDATE"));
        assertTrue(controller.contains("lockGroup(group)"));
        assertFalse(template.contains("lists.forEach(save)"));
        assertTrue(template.contains("saveQueue"));
        assertTrue(template.contains("saveChangedLists(dragSourceList,destination)"));
    }

    @Test void everyGroupMasterRowProvidesColorForSharedTemplate() throws IOException {
        String controller=Files.readString(MAIN.resolve("java/com/example/home_api/web/GroupMasterController.java"));
        assertTrue(controller.contains("NULL::varchar AS color"));
    }
}
