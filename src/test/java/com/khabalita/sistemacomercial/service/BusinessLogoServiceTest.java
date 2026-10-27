package com.khabalita.sistemacomercial.service;

import com.khabalita.sistemacomercial.Entities.BusinessSettings;
import com.khabalita.sistemacomercial.Repositories.BusinessSettingsRepository;
import com.khabalita.sistemacomercial.Service.BusinessLogoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

class BusinessLogoServiceTest {

    @TempDir
    Path temporaryDirectory;

    private final BusinessSettingsRepository settingsRepository = mock(BusinessSettingsRepository.class);

    @Test
    void replaceLogo_storesImageAndMetadata() throws Exception {
        when(settingsRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.empty());
        BusinessLogoService service = service();
        MockMultipartFile upload = pngUpload();

        service.replaceLogo(upload);

        var captor = org.mockito.ArgumentCaptor.forClass(BusinessSettings.class);
        verify(settingsRepository).save(captor.capture());
        BusinessSettings saved = captor.getValue();
        assertEquals("image/png", saved.getLogoContentType());
        assertTrue(saved.getLogoStorageKey().endsWith(".png"));
        assertTrue(Files.exists(temporaryDirectory.resolve(saved.getLogoStorageKey())));

        when(settingsRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(saved));
        assertTrue(service.hasLogo());
        BusinessLogoService.LogoData loaded = service.loadLogo();
        assertNotNull(loaded);
        assertEquals("image/png", loaded.contentType());
        assertTrue(loaded.content().length > 0);
    }

    @Test
    void replaceLogo_rejectsInvalidImageWithoutSaving() {
        BusinessLogoService service = service();
        MockMultipartFile upload = new MockMultipartFile("logo", "logo.png", "image/png", "not an image".getBytes());

        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                () -> service.replaceLogo(upload));

        verify(settingsRepository, never()).save(any(BusinessSettings.class));
    }

    @Test
    void removeLogo_clearsMetadataAndDeletesFile() throws Exception {
        BusinessSettings settings = BusinessSettings.builder()
                .logoStorageKey("current.png")
                .logoContentType("image/png")
                .build();
        Files.write(temporaryDirectory.resolve("current.png"), pngBytes());
        when(settingsRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(settings));
        BusinessLogoService service = service();

        service.removeLogo();

        assertNull(settings.getLogoStorageKey());
        assertNull(settings.getLogoContentType());
        assertFalse(Files.exists(temporaryDirectory.resolve("current.png")));
        verify(settingsRepository).save(settings);
    }

    private BusinessLogoService service() {
        BusinessLogoService service = new BusinessLogoService(settingsRepository);
        ReflectionTestUtils.setField(service, "storageDirectory", temporaryDirectory.toString());
        return service;
    }

    private MockMultipartFile pngUpload() throws Exception {
        return new MockMultipartFile("logo", "business.png", "image/png", pngBytes());
    }

    private byte[] pngBytes() throws Exception {
        BufferedImage image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(image, "png", output);
        return output.toByteArray();
    }
}
