//package global_pass.payments;
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Profile;
//import org.springframework.core.io.ByteArrayResource;
//import org.springframework.core.io.Resource;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.IOException;
//import java.net.URI;
//import java.net.http.HttpClient;
//import java.net.http.HttpRequest;
//import java.net.http.HttpResponse;
//import java.util.UUID;
//
//@Slf4j
//@Service
//@Profile("!dev")
//public class SupabaseFileStorageService implements FileStorageService {
//
//    @Value("${app.supabase.url}")
//    private String supabaseUrl;
//
//    @Value("${app.supabase.key}")
//    private String supabaseKey;
//
//    @Value("${app.supabase.bucket:payments}")
//    private String bucket;
//
//    private final HttpClient httpClient = HttpClient.newHttpClient();
//
//    @Override
//    public String store(MultipartFile file, String subDir, String fileName) {
//        try {
//            String objectPath = subDir + "/" + fileName;
//
//            HttpRequest request = HttpRequest.newBuilder()
//                    .uri(URI.create(supabaseUrl + "/storage/v1/object/" + bucket + "/" + objectPath))
//                    .header("Authorization", "Bearer " + supabaseKey)
//                    .header("Content-Type", file.getContentType())
//                    .POST(HttpRequest.BodyPublishers.ofByteArray(file.getBytes()))
//                    .build();
//
//            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
//
//            if (response.statusCode() != 200 && response.statusCode() != 201) {
//                throw new RuntimeException("Supabase upload failed: " + response.body());
//            }
//
//            log.info("File stored in Supabase: {}", objectPath);
//            return objectPath;
//        } catch (IOException | InterruptedException e) {
//            Thread.currentThread().interrupt();
//            throw new RuntimeException("Failed to store file in Supabase", e);
//        }
//    }
//
//    @Override
//    public Resource load(String fileName) {
//        try {
//            HttpRequest request = HttpRequest.newBuilder()
//                    .uri(URI.create(supabaseUrl + "/storage/v1/object/" + bucket + "/" + fileName))
//                    .header("Authorization", "Bearer " + supabaseKey)
//                    .GET()
//                    .build();
//
//            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
//
//            if (response.statusCode() != 200) {
//                throw new RuntimeException("File not found in Supabase: " + fileName);
//            }
//
//            return new ByteArrayResource(response.body());
//        } catch (IOException | InterruptedException e) {
//            Thread.currentThread().interrupt();
//            throw new RuntimeException("Failed to load file from Supabase", e);
//        }
//    }
//
//    @Override
//    public void delete(String fileName) {
//        try {
//            HttpRequest request = HttpRequest.newBuilder()
//                    .uri(URI.create(supabaseUrl + "/storage/v1/object/" + bucket + "/" + fileName))
//                    .header("Authorization", "Bearer " + supabaseKey)
//                    .DELETE()
//                    .build();
//
//            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
//            log.info("File deleted from Supabase: {} (status={})", fileName, response.statusCode());
//        } catch (IOException | InterruptedException e) {
//            Thread.currentThread().interrupt();
//            log.warn("Could not delete file from Supabase: {}", fileName, e);
//        }
//    }
//
//    private String getExtension(String filename) {
//        if (filename == null) return "";
//        int dot = filename.lastIndexOf('.');
//        return dot >= 0 ? filename.substring(dot) : "";
//    }
//}
