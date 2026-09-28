package com.khabalita.sistemacomercial.Service;

import com.khabalita.sistemacomercial.Entities.Product;
import com.khabalita.sistemacomercial.Entities.ProductImage;
import com.khabalita.sistemacomercial.Repositories.ProductImageRepository;
import com.khabalita.sistemacomercial.Repositories.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductImageService {

    private static final long MAX_SIZE = 5 * 1024 * 1024;
    private final ProductRepository productRepository;
    private final ProductImageRepository imageRepository;

    @Value("${app.storage.product-images:data/product-images}")
    private String storageDirectory;

    @Transactional
    public void replace(Long productId, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("La imagen esta vacia");
        if (file.getSize() > MAX_SIZE) throw new IllegalArgumentException("La imagen no puede superar 5 MB");
        String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase(Locale.ROOT);
        if (!(contentType.equals("image/jpeg") || contentType.equals("image/png")))
            throw new IllegalArgumentException("Solo se permiten imagenes JPG o PNG");
        if (ImageIO.read(file.getInputStream()) == null)
            throw new IllegalArgumentException("El archivo no es una imagen valida");

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found: " + productId));
        Path directory = Paths.get(storageDirectory).toAbsolutePath().normalize();
        Files.createDirectories(directory);
        ProductImage previous = imageRepository.findFirstByProduct_IdAndPrimaryImageTrue(productId).orElse(null);
        String extension = contentType.equals("image/png") ? ".png" : ".jpg";
        String storageKey = UUID.randomUUID() + extension;
        Path target = directory.resolve(storageKey).normalize();
        if (!target.startsWith(directory)) throw new IllegalArgumentException("Ruta de imagen invalida");
        Files.write(target, file.getBytes());

        if (previous != null) {
            deleteFile(directory, previous.getStorageKey());
            imageRepository.delete(previous);
        }
        imageRepository.save(ProductImage.builder().product(product).storageKey(storageKey)
                .originalFilename(file.getOriginalFilename()).contentType(contentType)
                .fileSize(file.getSize()).primaryImage(true).build());
    }

    @Transactional(readOnly = true)
    public ImageData loadPrimary(Long productId) throws IOException {
        ProductImage image = imageRepository.findFirstByProduct_IdAndPrimaryImageTrue(productId).orElse(null);
        if (image == null) return null;
        Path file = Paths.get(storageDirectory).toAbsolutePath().normalize().resolve(image.getStorageKey()).normalize();
        if (!file.startsWith(Paths.get(storageDirectory).toAbsolutePath().normalize()) || !Files.exists(file)) return null;
        return new ImageData(Files.readAllBytes(file), image.getContentType());
    }

    @Transactional
    public void delete(Long productId) throws IOException {
        ProductImage image = imageRepository.findFirstByProduct_IdAndPrimaryImageTrue(productId).orElse(null);
        if (image == null) return;
        Path directory = Paths.get(storageDirectory).toAbsolutePath().normalize();
        deleteFile(directory, image.getStorageKey());
        imageRepository.delete(image);
    }

    private void deleteFile(Path directory, String storageKey) throws IOException {
        Path file = directory.resolve(storageKey).normalize();
        if (file.startsWith(directory)) Files.deleteIfExists(file);
    }

    public record ImageData(byte[] content, String contentType) {
    }
}
