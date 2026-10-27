package com.khabalita.sistemacomercial.Service;

import com.khabalita.sistemacomercial.Entities.BusinessSettings;
import com.khabalita.sistemacomercial.Repositories.BusinessSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BusinessLogoService {

    private static final long MAX_SIZE = 5 * 1024 * 1024;

    private final BusinessSettingsRepository settingsRepository;

    @Value("${app.storage.business-assets:./data/business-assets}")
    private String storageDirectory;

    @Transactional
    public void replaceLogo(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("Selecciona una imagen para el logo");
        if (file.getSize() > MAX_SIZE) throw new IllegalArgumentException("El logo no puede superar 5 MB");

        String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase(Locale.ROOT);
        if (!contentType.equals("image/jpeg") && !contentType.equals("image/png"))
            throw new IllegalArgumentException("El logo debe ser una imagen JPG o PNG");

        byte[] content = file.getBytes();
        if (ImageIO.read(new ByteArrayInputStream(content)) == null)
            throw new IllegalArgumentException("El archivo seleccionado no es una imagen valida");

        Path directory = storagePath();
        Files.createDirectories(directory);
        String extension = contentType.equals("image/png") ? ".png" : ".jpg";
        String storageKey = UUID.randomUUID() + extension;
        Path target = resolve(storageKey);
        Files.write(target, content);

        BusinessSettings settings = settingsRepository.findFirstByOrderByIdAsc()
                .orElseGet(BusinessSettings::new);
        String previousKey = settings.getLogoStorageKey();
        settings.setLogoStorageKey(storageKey);
        settings.setLogoContentType(contentType);
        try {
            settingsRepository.save(settings);
        } catch (RuntimeException exception) {
            Files.deleteIfExists(target);
            throw exception;
        }

        deleteStoredFile(previousKey);
    }

    @Transactional
    public void removeLogo() throws IOException {
        BusinessSettings settings = settingsRepository.findFirstByOrderByIdAsc().orElse(null);
        if (settings == null || settings.getLogoStorageKey() == null) return;

        String previousKey = settings.getLogoStorageKey();
        settings.setLogoStorageKey(null);
        settings.setLogoContentType(null);
        settingsRepository.save(settings);
        deleteStoredFile(previousKey);
    }

    @Transactional(readOnly = true)
    public boolean hasLogo() {
        return settingsRepository.findFirstByOrderByIdAsc()
                .map(settings -> settings.getLogoStorageKey() != null
                        && Files.isRegularFile(resolve(settings.getLogoStorageKey())))
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public LogoData loadLogo() throws IOException {
        BusinessSettings settings = settingsRepository.findFirstByOrderByIdAsc().orElse(null);
        if (settings == null || settings.getLogoStorageKey() == null) return null;

        Path file = resolve(settings.getLogoStorageKey());
        if (!Files.isRegularFile(file)) return null;
        return new LogoData(Files.readAllBytes(file), settings.getLogoContentType());
    }

    private Path storagePath() {
        return Paths.get(storageDirectory).toAbsolutePath().normalize();
    }

    private Path resolve(String storageKey) {
        Path directory = storagePath();
        Path file = directory.resolve(storageKey).normalize();
        if (!file.startsWith(directory)) throw new IllegalArgumentException("Ruta de logo invalida");
        return file;
    }

    private void deleteStoredFile(String storageKey) throws IOException {
        if (storageKey != null) Files.deleteIfExists(resolve(storageKey));
    }

    public record LogoData(byte[] content, String contentType) {
    }
}
