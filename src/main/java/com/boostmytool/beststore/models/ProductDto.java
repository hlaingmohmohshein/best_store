package com.boostmytool.beststore.models;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
@Data
public class ProductDto {
	@NotEmpty(message = "The name is required")
	private String name;
	@NotEmpty(message="The category is required")
	private String category;
	@Min(0)
	private double price;
	@NotEmpty(message="The brand name is required")
	private String brand;
	@Size(min = 10, message = "The description should be at least 10 characters")
	@Size(max = 200, message = "The description cannot exceed 2000 characters")
	private String description;
	private MultipartFile imageFileName;
}
