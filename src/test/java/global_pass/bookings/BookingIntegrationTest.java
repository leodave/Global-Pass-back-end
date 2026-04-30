/*

package global_pass.bookings;

import global_pass.users.User;
import global_pass.users.UserRepository;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BookingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private BookingMapper bookingMapper;

    @Autowired
    private UserRepository userRepository;

    private static final String BASE_URL = "/public/api/v1/users/{userId}/bookings";

    private User testUser;

    private BookingRequestDto validRequest;

    private BookingRequestDto validRequest_2;

    @BeforeEach
    void setUp() {
        bookingRepository.deleteAll();
        userRepository.deleteAll();

        // create a real user in DB for every test
        testUser = new User();
        testUser = userRepository.save(testUser);

        validRequest = BookingRequestDto.builder()
                .name("Netflix")
                .description("Streaming service")
                .pageLink("https://netflix.com")
                .loginUsername("user@mail.com")
                .loginPassword("secret123")
                .amount(15.99)
                .currency("USD")
                .otherDetails("4K plan")
                .build();

        validRequest_2 = BookingRequestDto.builder()
                .name("Spotify")
                .description("Music streaming")
                .pageLink("https://spotify.com")
                .loginUsername("user@mail.com")
                .loginPassword("secret456")
                .amount(9.99)
                .currency("USD")
                .otherDetails("Premium plan")
                .build();
    }

    // ──────────────────────────────────────────────
    // GET all bookings
    // ──────────────────────────────────────────────

    @Test
    void getAllBookings_emptyDb_returns200AndEmptyList() throws Exception {
        mockMvc.perform(get(BASE_URL, testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data", hasSize(0)));
    }

    @Test
    void getAllBookings_withSeededData_returns200AndAllBookings() throws Exception {
        saveBookingForUser(validRequest);
        saveBookingForUser(validRequest_2);

        mockMvc.perform(get(BASE_URL, testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[*].name", containsInAnyOrder("Netflix", "Spotify")));
    }

    @Test
    void getAllBookings_doesNotReturnOtherUsersBookings() throws Exception {
        // second user with their own booking
        User otherUser = userRepository.save(new User());
        BookingEntity otherBooking = bookingMapper.toEntity(validRequest_2);
        otherBooking.setUser(otherUser);
        bookingRepository.save(otherBooking);

        // booking for testUser
        saveBookingForUser(validRequest);

        mockMvc.perform(get(BASE_URL, testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].name").value("Netflix"));
    }

    // ──────────────────────────────────────────────
    // GET booking by id
    // ──────────────────────────────────────────────

    @Test
    void getBookingById_existingId_returns200AndBooking() throws Exception {
        BookingEntity saved = saveBookingForUser(validRequest);

        mockMvc.perform(get(BASE_URL + "/{id}", testUser.getId(), saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(saved.getId()))
                .andExpect(jsonPath("$.data.userId").value(testUser.getId()))
                .andExpect(jsonPath("$.data.name").value("Netflix"))
                .andExpect(jsonPath("$.data.description").value("Streaming service"))
                .andExpect(jsonPath("$.data.pageLink").value("https://netflix.com"))
                .andExpect(jsonPath("$.data.loginUsername").value("user@mail.com"))
                .andExpect(jsonPath("$.data.amount").value(15.99))
                .andExpect(jsonPath("$.data.currency").value("USD"))
                .andExpect(jsonPath("$.data.otherDetails").value("4K plan"));
    }

    @Test
    void getBookingById_nonExistingId_returns404() throws Exception {
        mockMvc.perform(get(BASE_URL + "/{id}", testUser.getId(), "non-existing-uuid"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Booking not found with id: non-existing-uuid")));
    }

    @Test
    void getBookingById_belongsToOtherUser_returns404() throws Exception {
        User otherUser = userRepository.save(new User());
        BookingEntity otherBooking = bookingMapper.toEntity(validRequest);
        otherBooking.setUser(otherUser);
        BookingEntity saved = bookingRepository.save(otherBooking);

        // testUser tries to access otherUser's booking
        mockMvc.perform(get(BASE_URL + "/{id}", testUser.getId(), saved.getId()))
                .andExpect(status().isNotFound());
    }

    // ──────────────────────────────────────────────
    // POST create booking
    // ──────────────────────────────────────────────

    @Test
    void createBooking_validRequest_returns201AndPersists() throws Exception {
        mockMvc.perform(post(BASE_URL, testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").isNotEmpty())
                .andExpect(jsonPath("$.data.userId").value(testUser.getId()))
                .andExpect(jsonPath("$.data.name").value("Netflix"))
                .andExpect(jsonPath("$.data.description").value("Streaming service"))
                .andExpect(jsonPath("$.data.pageLink").value("https://netflix.com"))
                .andExpect(jsonPath("$.data.loginUsername").value("user@mail.com"))
                .andExpect(jsonPath("$.data.amount").value(15.99))
                .andExpect(jsonPath("$.data.currency").value("USD"));

        assertThat(bookingRepository.count()).isEqualTo(1);
    }

    @Test
    void createBooking_nonExistingUser_returns404() throws Exception {
        mockMvc.perform(post(BASE_URL, 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void createBooking_missingName_returns400WithValidationError() throws Exception {
        BookingRequestDto invalidRequest = BookingRequestDto.builder()
                .description("Streaming service")
                .pageLink("https://netflix.com")
                .loginUsername("user@mail.com")
                .loginPassword("secret123")
                .amount(15.99)
                .currency("USD")
                .build();

        mockMvc.perform(post(BASE_URL, testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").value("Name is required"));
    }

    @Test
    void createBooking_invalidPageLink_returns400WithValidationError() throws Exception {
        BookingRequestDto invalidRequest = BookingRequestDto.builder()
                .name("Netflix")
                .pageLink("not-a-valid-url")
                .loginUsername("user@mail.com")
                .loginPassword("secret123")
                .amount(15.99)
                .currency("USD")
                .build();

        mockMvc.perform(post(BASE_URL, testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.pageLink").value("Page link must be a valid URL starting with http:// or https://"));
    }

    @Test
    void createBooking_invalidCurrencyLength_returns400WithValidationError() throws Exception {
        BookingRequestDto invalidRequest = BookingRequestDto.builder()
                .name("Netflix")
                .pageLink("https://netflix.com")
                .loginUsername("user@mail.com")
                .loginPassword("secret123")
                .amount(15.99)
                .currency("USDD")
                .build();

        mockMvc.perform(post(BASE_URL, testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.currency").value("Currency must be a 3-letter ISO code (e.g. USD, EUR)"));
    }

    @Test
    void createBooking_negativeAmount_returns400WithValidationError() throws Exception {
        BookingRequestDto invalidRequest = BookingRequestDto.builder()
                .name("Netflix")
                .pageLink("https://netflix.com")
                .loginUsername("user@mail.com")
                .loginPassword("secret123")
                .amount(-5.00)
                .currency("USD")
                .build();

        mockMvc.perform(post(BASE_URL, testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.amount").value("Amount must be a positive value"));
    }

    @Test
    void createBooking_malformedJson_returns400() throws Exception {
        mockMvc.perform(post(BASE_URL, testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ this is not valid json }"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Malformed request body"));
    }

    // ──────────────────────────────────────────────
    // PUT update booking
    // ──────────────────────────────────────────────

    @Test
    void updateBooking_existingId_returns200AndUpdatesDb() throws Exception {
        BookingEntity saved = saveBookingForUser(validRequest);

        BookingRequestDto updateRequest = BookingRequestDto.builder()
                .name("Netflix Premium")
                .description("Streaming HD")
                .pageLink("https://netflix.com")
                .loginUsername("newuser@mail.com")
                .loginPassword("newSecret")
                .amount(22.99)
                .currency("EUR")
                .otherDetails("8K plan")
                .build();

        mockMvc.perform(put(BASE_URL + "/{id}", testUser.getId(), saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(saved.getId()))
                .andExpect(jsonPath("$.data.userId").value(testUser.getId()))
                .andExpect(jsonPath("$.data.name").value("Netflix Premium"))
                .andExpect(jsonPath("$.data.amount").value(22.99))
                .andExpect(jsonPath("$.data.currency").value("EUR"));

        BookingEntity updated = bookingRepository.findById(saved.getId()).orElseThrow();
        assertThat(updated.getName()).isEqualTo("Netflix Premium");
        assertThat(updated.getAmount()).isEqualTo(22.99);
        assertThat(updated.getCurrency()).isEqualTo("EUR");
    }

    @Test
    void updateBooking_nonExistingId_returns404() throws Exception {
        mockMvc.perform(put(BASE_URL + "/{id}", testUser.getId(), "non-existing-uuid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Booking not found with id: non-existing-uuid")));
    }

    @Test
    void updateBooking_belongsToOtherUser_returns404() throws Exception {
        User otherUser = userRepository.save(new User());
        BookingEntity otherBooking = bookingMapper.toEntity(validRequest);
        otherBooking.setUser(otherUser);
        BookingEntity saved = bookingRepository.save(otherBooking);

        // testUser tries to update otherUser's booking
        mockMvc.perform(put(BASE_URL + "/{id}", testUser.getId(), saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateBooking_invalidRequest_returns400() throws Exception {
        BookingEntity saved = saveBookingForUser(validRequest);

        BookingRequestDto invalidRequest = BookingRequestDto.builder()
                .name("")
                .pageLink("https://netflix.com")
                .loginUsername("user@mail.com")
                .loginPassword("secret123")
                .amount(15.99)
                .currency("USD")
                .build();

        mockMvc.perform(put(BASE_URL + "/{id}", testUser.getId(), saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").exists());
    }

    // ──────────────────────────────────────────────
    // DELETE booking
    // ──────────────────────────────────────────────

    @Test
    void deleteBooking_existingId_returns200AndRemovesFromDb() throws Exception {
        BookingEntity saved = saveBookingForUser(validRequest);

        mockMvc.perform(delete(BASE_URL + "/{id}", testUser.getId(), saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Booking deleted"));

        assertThat(bookingRepository.findById(saved.getId())).isEmpty();
    }

    @Test
    void deleteBooking_nonExistingId_returns404() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/{id}", testUser.getId(), "non-existing-uuid"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Booking not found with id: non-existing-uuid")));
    }

    @Test
    void deleteBooking_alreadyDeleted_returns404() throws Exception {
        BookingEntity saved = saveBookingForUser(validRequest);
        bookingRepository.deleteById(saved.getId());

        mockMvc.perform(delete(BASE_URL + "/{id}", testUser.getId(), saved.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteBooking_belongsToOtherUser_returns404() throws Exception {
        User otherUser = userRepository.save(new User());
        BookingEntity otherBooking = bookingMapper.toEntity(validRequest);
        otherBooking.setUser(otherUser);
        BookingEntity saved = bookingRepository.save(otherBooking);

        // testUser tries to delete otherUser's booking
        mockMvc.perform(delete(BASE_URL + "/{id}", testUser.getId(), saved.getId()))
                .andExpect(status().isNotFound());
    }

    // ──────────────────────────────────────────────
    // Helper
    // ──────────────────────────────────────────────

    private BookingEntity saveBookingForUser(BookingRequestDto request) {
        BookingEntity entity = bookingMapper.toEntity(request);
        entity.setUser(testUser);
        return bookingRepository.save(entity);
    }
}
*/
