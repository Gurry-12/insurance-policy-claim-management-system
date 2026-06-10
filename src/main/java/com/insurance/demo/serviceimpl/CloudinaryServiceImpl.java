package com.insurance.demo.serviceimpl;

import java.io.IOException;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.insurance.demo.service.CloudinaryService;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class CloudinaryServiceImpl implements CloudinaryService {

	private final Cloudinary cloudinary;

	@Override
	public Map<String, Object> uploadFile(MultipartFile file) throws IOException {

		return cloudinary.uploader().upload(file.getBytes(), Map.of("folder", "insurance_claims"));
	}

	@Override
	public void deleteFile(String publicId) throws IOException {
		
		 cloudinary.uploader().destroy(
	                publicId,
	                Map.of()
	        );
	}

}
