package blog.code.codeblog.controller;

import blog.code.codeblog.dto.cloudinary.ImageUploadResponseDTO;
import blog.code.codeblog.enums.FlowImageFlag;
import blog.code.codeblog.service.CloudinaryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@Slf4j
public class CloudnaryController {

    @Autowired
    CloudinaryService cloudinaryService;

    @PostMapping("/upload")
    @ResponseStatus(HttpStatus.OK)
    public ImageUploadResponseDTO uploadImage(@RequestParam("file") MultipartFile file,
                                              @RequestParam("flag") FlowImageFlag flag,
                                              @RequestParam(value = "userId", required = false) String userId,
                                              @RequestParam(value = "postId", required = false) String postId) throws IOException {
        return cloudinaryService.uploadFile(file, flag, userId, postId);
    }

    @DeleteMapping("/delete")
    @ResponseStatus(HttpStatus.OK)
    public Map<String, Object> deleteImage(@RequestParam("publicId") String publicId) throws IOException {
        return cloudinaryService.deleteFile(publicId);
    }

}
