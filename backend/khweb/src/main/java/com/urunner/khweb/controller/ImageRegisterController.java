package com.urunner.khweb.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileOutputStream;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/image")
public class ImageRegisterController {

    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/uploadImg_Profile")
    @ResponseBody
    public String requestUploadFile3(
            @RequestParam("fileList") List<MultipartFile> fileList,
            @RequestParam("id") String userId) {

        log.info("requestUploadFile(): " + fileList);
        log.info("**params value( id ) : " + userId);

        String cut_userId = userId.substring(0, userId.length()-4);

        try {
            for (MultipartFile multipartFile : fileList) {
                log.info("requestUploadFile(): Make File");
                FileOutputStream writer = new FileOutputStream("./images/profiles/"+ cut_userId + ".gif");
                log.info("multipartFile.getOriginalFilename : " + multipartFile.getOriginalFilename());

                writer.write(multipartFile.getBytes());
                writer.close();
            }
        } catch (Exception e) {
            return "Fail";
        }
        log.info("requestUploadFile()");

        return "Success";
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/uploadImg_Free")
    @ResponseBody
    public String requestUploadFile3(
            @RequestParam("fileList") List<MultipartFile> fileList,
            @RequestParam("id") String id,
            @RequestParam("no") String no) {

        log.info("requestUploadFile(): " + fileList);
        log.info("**params value( id ) : " + id);
        log.info("**params value( no ) : " + no);

        try {
            for (MultipartFile multipartFile : fileList) {
                log.info("requestUploadFile(): Make File");
                FileOutputStream writer = new FileOutputStream("./images/free/"+ id + "_" + no + ".gif");
                log.info("multipartFile.getOriginalFilename : " + multipartFile.getOriginalFilename());


                writer.write(multipartFile.getBytes());
                writer.close();
            }
        } catch (Exception e) {
            return "Upload Fail!!!";
        }

        log.info("requestUploadFile(): Success!!!");

        return "Upload Success!!!";
    }
}