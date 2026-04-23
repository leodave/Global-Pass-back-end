package global_pass.contact;

import global_pass.users.UserApiResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/contact")
@RequiredArgsConstructor
public class ContactController {

    private final ContactRepository contactRepository;

    @PostMapping
    public ResponseEntity<UserApiResponseDto<Void>> submit(@Valid @RequestBody ContactRequestDto request) {
        // Honeypot check — if filled, it's a bot
        if (request.getWebsite() != null && !request.getWebsite().isBlank()) {
            log.warn("Honeypot triggered from: {}", request.getEmail());
            // Return success to not tip off the bot
            return ResponseEntity.status(HttpStatus.CREATED).body(UserApiResponseDto.<Void>builder()
                    .status(201)
                    .message("Message sent successfully")
                    .build());
        }
        ContactMessage msg = new ContactMessage();
        msg.setEmail(request.getEmail());
        msg.setMessage(request.getMessage());
        contactRepository.save(msg);
        log.info("Contact message received from: {}", request.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(UserApiResponseDto.<Void>builder()
                .status(201)
                .message("Message sent successfully")
                .build());
    }

    // Admin: view all messages
    @GetMapping
    public ResponseEntity<UserApiResponseDto<List<ContactMessage>>> getAll() {
        return ResponseEntity.ok(UserApiResponseDto.<List<ContactMessage>>builder()
                .status(200)
                .message("Messages retrieved")
                .data(contactRepository.findAllByOrderByCreatedAtDesc())
                .build());
    }
}
