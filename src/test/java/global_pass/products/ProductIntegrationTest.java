/*
package global_pass.products;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import tools.jackson.databind.ObjectMapper;
import global_pass.securityConfig.TestSecurityConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
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
class ProductIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductMapper productMapper;

    private static final String BASE_URL = "/public/api/v1/products";

    private ProductRequestDto validRequest;
    private ProductRequestDto validRequest_2;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();

        validRequest = ProductRequestDto.builder()
                .name("Netflix")
                .description("Streaming service")
                .pageLink("https://netflix.com")
                .loginUsername("user@mail.com")
                .loginPassword("secret123")
                .amount(15.99)
                .currency("USD")
                .otherDetails("4K plan")
                .build();

        validRequest_2 = ProductRequestDto.builder()
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

    // get all products when empty
    @Test
    void getAllProducts_emptyDb_returns200AndEmptyList() throws Exception {
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // get all products when data inside
    @Test
    void getAllProducts_withSeededData_returns200AndAllProducts() throws Exception {
        productRepository.save(productMapper.toEntity(validRequest));
        productRepository.save(productMapper.toEntity(validRequest_2));


        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].name", containsInAnyOrder("Netflix", "Spotify")));
    }

    //get by Id
    @Test
    void getProductById_existingId_returns200AndProduct() throws Exception {
        ProductEntity saved = productRepository.save(productMapper.toEntity(validRequest));

        mockMvc.perform(get(BASE_URL + "/{id}", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.name").value("Netflix"))
                .andExpect(jsonPath("$.description").value("Streaming service"))
                .andExpect(jsonPath("$.pageLink").value("https://netflix.com"))
                .andExpect(jsonPath("$.loginUsername").value("user@mail.com"))
                .andExpect(jsonPath("$.amount").value(15.99))
                .andExpect(jsonPath("$.currency").value("USD"))
                .andExpect(jsonPath("$.otherDetails").value("4K plan"));
    }

    //get by non-existing Id
    @Test
    void getProductById_nonExistingId_returns404() throws Exception {
        mockMvc.perform(get(BASE_URL + "/{id}", "non-existing-uuid"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Product not found with id: non-existing-uuid")));
    }

    // create product
    @Test
    void createProduct_validRequest_returns201AndPersists() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value("Netflix"))
                .andExpect(jsonPath("$.description").value("Streaming service"))
                .andExpect(jsonPath("$.pageLink").value("https://netflix.com"))
                .andExpect(jsonPath("$.loginUsername").value("user@mail.com"))
                .andExpect(jsonPath("$.amount").value(15.99))
                .andExpect(jsonPath("$.currency").value("USD"));

        assertThat(productRepository.count()).isEqualTo(1);
    }

    // create product with missing name
    @Test
    void createProduct_missingName_returns400WithValidationError() throws Exception {
        ProductRequestDto invalidRequest = ProductRequestDto.builder()
                .description("Streaming service")
                .pageLink("https://netflix.com")
                .loginUsername("user@mail.com")
                .loginPassword("secret123")
                .amount(15.99)
                .currency("USD")
                .build();

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").value("Name is required"));
    }



    // create product with invalid page link
    @Test
    void createProduct_invalidPageLink_returns400WithValidationError() throws Exception {
        ProductRequestDto invalidRequest = ProductRequestDto.builder()
                .name("Netflix")
                .pageLink("not-a-valid-url")
                .loginUsername("user@mail.com")
                .loginPassword("secret123")
                .amount(15.99)
                .currency("USD")
                .build();

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.pageLink").value("Page link must be a valid URL starting with http:// or https://"));
    }


    // create product with invalid currency length
    @Test
    void createProduct_invalidCurrencyLength_returns400WithValidationError() throws Exception {
        ProductRequestDto invalidRequest = ProductRequestDto.builder()
                .name("Netflix")
                .pageLink("https://netflix.com")
                .loginUsername("user@mail.com")
                .loginPassword("secret123")
                .amount(15.99)
                .currency("USDD")
                .build();

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.currency").value("Currency must be a 3-letter ISO code (e.g. USD, EUR)"));
    }

    // create amount with negative amount
    @Test
    void createProduct_negativeAmount_returns400WithValidationError() throws Exception {
        ProductRequestDto invalidRequest = ProductRequestDto.builder()
                .name("Netflix")
                .pageLink("https://netflix.com")
                .loginUsername("user@mail.com")
                .loginPassword("secret123")
                .amount(-5.00)
                .currency("USD")
                .build();

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.amount").value("Amount must be a positive value"));
    }


    // create product with malformed json
    @Test
    void createProduct_malformedJson_returns400() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ this is not valid json }"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Malformed request body"));
    }

   // update existing product with id
    @Test
    void updateProduct_existingId_returns200AndUpdatesDb() throws Exception {
        ProductEntity saved = productRepository.save(productMapper.toEntity(validRequest));

        ProductRequestDto updateRequest = ProductRequestDto.builder()
                .name("Netflix Premium")
                .description("Streaming HD")
                .pageLink("https://netflix.com")
                .loginUsername("newuser@mail.com")
                .loginPassword("newSecret")
                .amount(22.99)
                .currency("EUR")
                .otherDetails("8K plan")
                .build();

        mockMvc.perform(put(BASE_URL + "/{id}", saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.name").value("Netflix Premium"))
                .andExpect(jsonPath("$.amount").value(22.99))
                .andExpect(jsonPath("$.currency").value("EUR"));

        ProductEntity updated = productRepository.findById(saved.getId()).orElseThrow();
        assertThat(updated.getName()).isEqualTo("Netflix Premium");
        assertThat(updated.getAmount()).isEqualTo(22.99);
        assertThat(updated.getCurrency()).isEqualTo("EUR");
    }

    // update non-existing product with non-existing id
    @Test
    void updateProduct_nonExistingId_returns404() throws Exception {
        mockMvc.perform(put(BASE_URL + "/{id}", "non-existing-uuid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Product not found with id: non-existing-uuid")));
    }

    // update product with invalid request
    @Test
    void updateProduct_invalidRequest_returns400() throws Exception {
        ProductEntity saved = productRepository.save(productMapper.toEntity(validRequest));

        //name cant be empty or less than 2 characters
        ProductRequestDto invalidRequest = ProductRequestDto.builder()
                .name("")
                .pageLink("https://netflix.com")
                .loginUsername("user@mail.com")
                .loginPassword("secret123")
                .amount(15.99)
                .currency("USD")
                .build();

        mockMvc.perform(put(BASE_URL + "/{id}", saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").exists());
    }


    // delete product
    @Test
    void deleteProduct_existingId_returns204AndRemovesFromDb() throws Exception {
        ProductEntity saved = productRepository.save(productMapper.toEntity(validRequest));

        mockMvc.perform(delete(BASE_URL + "/{id}", saved.getId()))
                .andExpect(status().isNoContent());

        assertThat(productRepository.findById(saved.getId())).isEmpty();
    }

    // delete non-existing product
    @Test
    void deleteProduct_nonExistingId_returns404() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/{id}", "non-existing-uuid"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Product not found with id: non-existing-uuid")));
    }

    // delete product already deleted
    @Test
    void deleteProduct_alreadyDeleted_returns404() throws Exception {
        ProductEntity saved = productRepository.save(productMapper.toEntity(validRequest));
        productRepository.deleteById(saved.getId());

        mockMvc.perform(delete(BASE_URL + "/{id}", saved.getId()))
                .andExpect(status().isNotFound());
    }
}
*/
