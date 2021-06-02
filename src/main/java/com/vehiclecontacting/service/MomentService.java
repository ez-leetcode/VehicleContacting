package com.vehiclecontacting.service;

import org.springframework.web.multipart.MultipartFile;

public interface MomentService {

    String momentPhotoUpload(MultipartFile file,Long id);

    String generateMoment(Long id,String description,String photo1,String photo2,String photo3);

    String deleteMoment(Long id,Long number);

    String momentLike(Long id,Long number);
}
