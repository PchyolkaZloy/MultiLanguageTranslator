package en.pchz.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import en.pchz.common.*;
import en.pchz.exception.TranslationApiException;
import en.pchz.service.TranslationService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@AutoConfigureMockMvc
@ActiveProfiles("test")
@SpringBootTest
public class TranslationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TranslationService translationService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testTranslate_ValidData_Success() throws Exception {
        // Arrange
        TranslationRequest request = new TranslationRequest("en", "es", "hello world");
        TranslationResponse expectedResponse = new TranslationResponse("hola mundo");

        Mockito.when(translationService.translate(any(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn("hola mundo");

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.post("/translator/translate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(expectedResponse)));
    }

    @Test
    void testTranslate_MissingData_BadRequest() throws Exception {
        // Arrange
        TranslationRequest request = new TranslationRequest(null, "es", "hello world");

        SystemResponse expectedResponse = new SystemResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Invalid request: sourceCode, targetCode, and text must not be null"
        );

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.post("/translator/translate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(objectMapper.writeValueAsString(expectedResponse)));
    }

    @Test
    void testTranslate_ApiError_TranslationApiException() throws Exception {
        // Arrange
        TranslationRequest request = new TranslationRequest("en", "es", "hello world");
        Mockito.when(translationService.translate(any(), anyString(), anyString(), anyString(), anyString()))
                .thenThrow(new TranslationApiException("API error", HttpStatus.INTERNAL_SERVER_ERROR.value()));

        SystemResponse expectedResponse = new SystemResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "API error"
        );

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.post("/translator/translate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().json(objectMapper.writeValueAsString(expectedResponse)));
    }

    @Test
    void testGetLanguages_ValidData_Success() throws Exception {
        // Arrange
        List<Language> languages = List.of(new Language("en", "English"), new Language("es", "Spanish"));
        LanguageResponse expectedResponse = new LanguageResponse(languages);

        Mockito.when(translationService.getAllSupportedLanguage()).thenReturn(languages);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.get("/translator/languages")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(expectedResponse)));
    }

    @Test
    void testGetLanguages_ApiError_TranslationApiException() throws Exception {
        // Arrange
        Mockito.when(translationService.getAllSupportedLanguage())
                .thenThrow(new TranslationApiException("API error", HttpStatus.INTERNAL_SERVER_ERROR.value()));

        SystemResponse expectedResponse = new SystemResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "API error"
        );

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.get("/translator/languages")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().json(objectMapper.writeValueAsString(expectedResponse)));
    }
}

