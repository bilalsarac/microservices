package com.ecommerce.microservices.product.repository;

import com.ecommerce.microservices.product.model.Product;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProductRepository extends MongoRepository<Product,String> {

}
