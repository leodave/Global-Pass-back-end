package global_pass.payments;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    String store(MultipartFile file, String subDir, String fileName);

    Resource load(String fileName);

    void delete(String fileName);
}
