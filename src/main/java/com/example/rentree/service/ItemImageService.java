package com.example.rentree.service;

import com.example.rentree.domain.ItemImage;
import com.example.rentree.repository.ItemImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ItemImageService {

    private final ItemImageRepository itemImageRepository;

    @Value("${file.upload-dir}") // application.properties에 설정
    private String uploadDir;

    public String saveImage(Long rentalItemId, MultipartFile imageFile) {
        try {
            String originalFilename = imageFile.getOriginalFilename();
            String fileName = UUID.randomUUID() + "_" + originalFilename;

            // EC2에 파일 저장
            File destination = new File(uploadDir + fileName);
            imageFile.transferTo(destination);

            // 접근 가능한 URL 생성
            String imageUrl = "/images/" + fileName;

            // DB 저장
            ItemImage image = ItemImage.builder()
                    .rentalItemId(rentalItemId)
                    .imageUrl(imageUrl)
                    .build();
            itemImageRepository.save(image);

            return imageUrl;
        } catch (IOException e) {
            throw new RuntimeException("이미지 저장 실패", e);
        }
    }

    public List<ItemImage> getImagesByRentalItemId(Long rentalItemId) {
        return itemImageRepository.findByRentalItemId(rentalItemId);
    }

    public void deleteImage(Long id) {
        ItemImage image = itemImageRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("이미지 없음"));

        String fileName = new File(image.getImageUrl()).getName();

        // EC2 로컬 파일 삭제
        File file = new File(uploadDir + fileName);
        if (file.exists()) {
            file.delete();
        }

        itemImageRepository.deleteById(id);
    }
}
