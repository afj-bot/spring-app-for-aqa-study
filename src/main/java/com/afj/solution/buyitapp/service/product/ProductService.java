package com.afj.solution.buyitapp.service.product;

import java.io.IOException;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import com.afj.solution.buyitapp.model.Product;
import com.afj.solution.buyitapp.payload.request.CreateProductRequest;
import com.afj.solution.buyitapp.payload.response.ProductResponse;

/**
 * @author Tomash Gombosh
 */
public interface ProductService {

    Page<ProductResponse> getProducts(Pageable pageable);

    Product save(CreateProductRequest createProductRequest);

    ProductResponse addImageToProduct(UUID id, MultipartFile file) throws IOException;

    byte[] getImageByProductId(UUID id);

    Product findById(UUID id);
}