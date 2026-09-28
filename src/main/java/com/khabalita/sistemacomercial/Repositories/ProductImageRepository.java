package com.khabalita.sistemacomercial.Repositories;

import com.khabalita.sistemacomercial.Entities.ProductImage;

import java.util.Optional;

public interface ProductImageRepository extends BaseRepository<ProductImage, Long> {

    Optional<ProductImage> findFirstByProduct_IdAndPrimaryImageTrue(Long productId);
}
