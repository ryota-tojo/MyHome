package com.example.home_api.web;

import jakarta.servlet.http.HttpSession;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Controller
public class GroupCustomizationController {
    private static final long MAX_IMAGE_SIZE = 10L * 1024 * 1024;
    private static final Path IMAGE_DIR = Path.of("data", "group-home-images").toAbsolutePath().normalize();
    private static final Set<String> IMAGE_TYPES = Set.of("image/jpeg", "image/png", "image/webp", "image/gif");
    private final JdbcTemplate jdbc;

    public GroupCustomizationController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @PostMapping("/group/settings/home-image")
    String saveHomeImage(@RequestParam MultipartFile homeImage, HttpSession session) {
        String group = leaderGroup(session);
        String contentType = homeImage.getContentType() == null ? "" : homeImage.getContentType().toLowerCase(Locale.ROOT);
        if (homeImage.isEmpty() || homeImage.getSize() > MAX_IMAGE_SIZE || !IMAGE_TYPES.contains(contentType))
            return "redirect:/group/settings?imageError=invalid";
        String extension = switch (contentType) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            case "image/gif" -> ".gif";
            default -> throw new IllegalArgumentException("未対応の画像形式です");
        };
        String previous = setting(group, "home_image_file");
        String fileName = UUID.randomUUID() + extension;
        Path newImage = IMAGE_DIR.resolve(fileName);
        try {
            Files.createDirectories(IMAGE_DIR);
            homeImage.transferTo(newImage);
        } catch (IOException | IllegalStateException ex) {
            deleteQuietly(newImage);
            return "redirect:/group/settings?imageError=save";
        }
        try {
            upsert(group, "home_image_file", fileName);
        } catch (RuntimeException ex) {
            deleteQuietly(newImage);
            return "redirect:/group/settings?imageError=save";
        }
        if (!previous.isBlank()) deleteQuietly(safeImagePath(previous));
        return "redirect:/group/settings?imageSaved";
    }

    @PostMapping("/group/settings/home-image/delete")
    String deleteHomeImage(HttpSession session) {
        String group = leaderGroup(session);
        String fileName = setting(group, "home_image_file");
        try {
            jdbc.update("DELETE FROM group_setting WHERE group_id=? AND key='home_image_file'", group);
        } catch (RuntimeException ex) {
            return "redirect:/group/settings?imageError=delete";
        }
        if (!fileName.isBlank()) deleteQuietly(safeImagePath(fileName));
        return "redirect:/group/settings?imageDeleted";
    }

    @GetMapping("/group/home-image")
    @ResponseBody
    ResponseEntity<ByteArrayResource> homeImage(HttpSession session) throws IOException {
        String group = activeGroup(session);
        String fileName = setting(group, "home_image_file");
        if (fileName.isBlank()) return ResponseEntity.notFound().build();
        Path image = safeImagePath(fileName);
        if (!Files.isRegularFile(image)) return ResponseEntity.notFound().build();
        String type = Files.probeContentType(image);
        MediaType mediaType = type == null ? MediaType.APPLICATION_OCTET_STREAM : MediaType.parseMediaType(type);
        return ResponseEntity.ok().cacheControl(CacheControl.noCache()).contentType(mediaType).body(new ByteArrayResource(Files.readAllBytes(image)));
    }

    @PostMapping("/group/settings/line")
    String saveLine(@RequestParam(defaultValue = "false") boolean lineEnabled,
                    @RequestParam(defaultValue = "") String lineUrl,
                    @RequestParam(defaultValue = "") String lineToken,
                    @RequestParam(defaultValue = "") String lineDestinationId,
                    HttpSession session) {
        String group = leaderGroup(session);
        try {
            saveLineValues(group, lineEnabled, lineUrl, lineToken, lineDestinationId);
        } catch (RuntimeException ex) {
            return "redirect:/group/settings?lineError=save";
        }
        return "redirect:/group/settings?lineSaved";
    }

    @PostMapping("/group/settings/line/test")
    String testLine(@RequestParam(defaultValue = "false") boolean lineEnabled,
                    @RequestParam(defaultValue = "") String lineUrl,
                    @RequestParam(defaultValue = "") String lineToken,
                    @RequestParam(defaultValue = "") String lineDestinationId,
                    @RequestParam(defaultValue = "false") boolean saveBeforeTest,
                    HttpSession session) {
        String group = leaderGroup(session);
        if (saveBeforeTest) {
            try {
                saveLineValues(group, lineEnabled, lineUrl, lineToken, lineDestinationId);
            } catch (RuntimeException ex) {
                session.setAttribute("groupLineTestError", "LINE設定を保存できませんでした。Springを再ビルドしてDBマイグレーションを適用してください。");
                return "redirect:/group/settings";
            }
        }
        String url = valueOrStored(group, "line_url", lineUrl);
        String token = valueOrStored(group, "line_token", lineToken);
        String destination = valueOrStored(group, "line_destination_id", lineDestinationId);
        try {
            sendLineTest(url, token, destination);
            session.setAttribute("groupLineTestSuccess", "LINEテスト通知を送信しました。");
        } catch (Exception ex) {
            session.setAttribute("groupLineTestError", "LINEテスト通知に失敗しました：" + safeMessage(ex));
        }
        return "redirect:/group/settings";
    }

    private void saveLineValues(String group, boolean enabled, String url, String token, String destination) {
        upsert(group, "line_enabled", String.valueOf(enabled));
        upsert(group, "line_url", url.strip());
        if (!token.isBlank()) upsert(group, "line_token", token.strip());
        upsert(group, "line_destination_id", destination.strip());
    }

    private void sendLineTest(String url, String token, String destination) throws Exception {
        if (url.isBlank() || token.isBlank() || destination.isBlank())
            throw new IllegalArgumentException("LINE API URL、トークン、送信先IDを入力してください。");
        URI uri = URI.create(url);
        if (!Set.of("http", "https").contains(uri.getScheme())) throw new IllegalArgumentException("LINE API URLが正しくありません。");
        String body = "{\"to\":\"" + json(destination) + "\",\"messages\":[{\"type\":\"text\",\"text\":\"家計簿アプリの所属グループLINE通知テストです。\"}]}";
        HttpRequest request = HttpRequest.newBuilder(uri).timeout(Duration.ofSeconds(15)).header("Authorization", "Bearer " + token).header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(body)).build();
        HttpResponse<String> response = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build().send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) throw new IllegalStateException("LINE APIがHTTP " + response.statusCode() + "を返しました。");
    }

    private String valueOrStored(String group, String key, String input) {
        return input == null || input.isBlank() ? setting(group, key) : input.strip();
    }

    private String safeMessage(Exception ex) {
        String message = ex.getMessage();
        return message == null || message.isBlank() ? ex.getClass().getSimpleName() : message.replaceAll("[\\r\\n]+", " ");
    }

    private String json(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private Path safeImagePath(String fileName) {
        Path path = IMAGE_DIR.resolve(Path.of(fileName).getFileName()).normalize();
        if (!path.startsWith(IMAGE_DIR)) throw new IllegalArgumentException("画像パスが不正です");
        return path;
    }

    private void deleteQuietly(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
        }
    }

    private String setting(String group, String key) {
        return jdbc.query("SELECT value FROM group_setting WHERE group_id=? AND key=?", rs -> rs.next() && rs.getString(1) != null ? rs.getString(1) : "", group, key);
    }

    private void upsert(String group, String key, String value) {
        jdbc.update("INSERT INTO group_setting(group_id,key,value,create_date,update_date) VALUES (?,?,?,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP) ON CONFLICT(group_id,key) DO UPDATE SET value=EXCLUDED.value,update_date=CURRENT_TIMESTAMP", group, key, value);
    }

    private String leaderGroup(HttpSession session) {
        String group = activeGroup(session);
        if (!Boolean.TRUE.equals(session.getAttribute("activeGroupLeader"))) throw new IllegalStateException("リーダー権限が必要です");
        return group;
    }

    private String activeGroup(HttpSession session) {
        Object group = session.getAttribute("activeGroupId");
        if (group == null) throw new IllegalStateException("所属グループを選択してください");
        return group.toString();
    }
}
