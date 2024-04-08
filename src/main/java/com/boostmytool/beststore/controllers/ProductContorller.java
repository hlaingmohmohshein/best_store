package com.boostmytool.beststore.controllers;

import java.io.InputStream;
import java.nio.file.*;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.boostmytool.beststore.models.Product;
import com.boostmytool.beststore.models.ProductDto;
import com.boostmytool.beststore.services.ProductRespository;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/products")
public class ProductContorller {

	@Autowired
	private ProductRespository repo;

	@GetMapping({ "", "/" })
	public String ShowProductList(Model model) {
		List<Product> products = repo.findAll(Sort.by(Sort.Direction.DESC, "id"));
		model.addAttribute("products", products);

		return "products/index";
	}

	@GetMapping("/create")
	public String showCreatePage(Model model) {
		ProductDto productDto = new ProductDto();
		model.addAttribute("productDto", productDto);
		return "products/create_product";
	}

	@PostMapping("/create")
	public String createProduct(@Valid @ModelAttribute ProductDto productDto, BindingResult result) {
		if (productDto.getImageFileName().isEmpty()) {
			result.addError(new FieldError("productionDto", "imageFileName", "The image file is required"));
		}
		if (result.hasErrors()) {
			return "products/create_product";
		}
		/* save image file */
		MultipartFile image = productDto.getImageFileName();
		Date createAt = new Date();
		String storageFileName = createAt.getTime() + "_" + image.getOriginalFilename();

		try {
			String uploadDir = "public/images/";
			Path uploadPath = Paths.get(uploadDir);
			if (!Files.exists(uploadPath)) {
				Files.createDirectories(uploadPath);
			}
			try (InputStream inputStream = image.getInputStream()) {
				Files.copy(inputStream, Paths.get(uploadDir + storageFileName), StandardCopyOption.REPLACE_EXISTING);
			}
		} catch (Exception e) {
			System.out.println("Exception: " + e.getMessage());
		}
		Product product = new Product();
		product.setName(productDto.getName());
		product.setBrand(productDto.getBrand());
		product.setCategory(productDto.getCategory());
		product.setPrice(productDto.getPrice());
		product.setDescription(productDto.getDescription());
		product.setCreateAt(createAt);
		product.setImageFileName(storageFileName);
		repo.save(product);
		return "redirect:/products";

	}

	@GetMapping("/edit")
	public String showEditPage(Model model, @RequestParam int id) {
		try {
			Product product = repo.findById(id).get();
			model.addAttribute("product", product);
			ProductDto productDto = new ProductDto();
			model.addAttribute("productDto", productDto);
			productDto.setBrand(product.getBrand());
			productDto.setName(product.getName());
			productDto.setCategory(product.getCategory());
			productDto.setPrice(product.getPrice());
			productDto.setDescription(product.getDescription());
		} catch (Exception e) {
			System.out.println("Exception: " + e.getMessage());
			return "redirect:/products";
		}
		return "products/edit_product";
	}

	@PostMapping("/edit")
	public String updateProduct(Model model, @RequestParam int id, @Valid @ModelAttribute ProductDto productDto,
			BindingResult result) {
		try {
			Product product = repo.findById(id).get();
			model.addAttribute("product", product);
			if (result.hasErrors()) {
				return "products/edit_product";
			}
			if (!productDto.getImageFileName().isEmpty()) {
				// Delete the old image

				String uploadDir = "public/images/";
				Path oldImageIUploadPath = Paths.get(uploadDir + product.getImageFileName());
				try {
					Files.delete(oldImageIUploadPath);
				} catch (Exception e) {
					System.out.println("Exceptoin: " + e.getMessage());
				}
				// Save the new image file
				MultipartFile image = productDto.getImageFileName();
				Date createAt = new Date();
				String storageFileName = createAt.getTime() + "_" + image.getOriginalFilename();
				try (InputStream inputStream = image.getInputStream()) {
					Files.copy(inputStream, Paths.get(uploadDir + storageFileName),
							StandardCopyOption.REPLACE_EXISTING);
				}
				product.setImageFileName(storageFileName);
			}
			product.setName(productDto.getName());
			product.setBrand(productDto.getBrand());
			product.setCategory(productDto.getCategory());
			product.setPrice(productDto.getPrice());
			product.setDescription(productDto.getDescription());
			repo.save(product);

		} catch (Exception e) {
			System.out.println("Exceptoin: " + e.getMessage());
		}

		return "redirect:/products";
	}

	@GetMapping("/delete")
	public String deleteProduct(@RequestParam int id) {
		try {
			Product product = repo.findById(id).get();
			// Delete product image
			Path imagePath = Paths.get("public/images/" + product.getImageFileName());
			try {
				Files.delete(imagePath);

			} catch (Exception e) {
				// TODO: handle exception
			}
			repo.delete(product);
		} catch (Exception e) {
			System.out.println("Exception :" + e.getMessage());
		}

		return "redirect:/products";
	}

}