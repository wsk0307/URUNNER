package com.urunner.khweb.controller.lecture;


import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.urunner.khweb.controller.dto.lecture.*;
import com.urunner.khweb.entity.member.Member;
import com.urunner.khweb.repository.member.MemberRepository;
import com.urunner.khweb.filter.TokenUtil;
import com.urunner.khweb.service.lecture.LectureService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/lecture")
public class LectureController {

    @Value("${image.location}")
    private String imageLocation;

    @Value("${video.location}")
    private String videoLocation;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    private LectureService lectureService;

    private String thum;

    private String detail;

    private Long lectureId;

    @PreAuthorize("hasRole('ROLE_MANAGER')")
    @PostMapping("/upload/image/thumbnail")
    public @ResponseBody
    ResponseEntity<UrlResource> LectureUpload(@RequestParam("thumbnailImage") List<MultipartFile> thumbnailImage,
                                              @RequestParam("imageDetail") List<MultipartFile> imageDetail,
//                                                String말고 Long으로
                                              @RequestParam("lectureId") Long lectureId) throws IOException {

        log.info("image Name : " + lectureId);

        ClassPathResource resource1 = new ClassPathResource(imageLocation);

        System.out.println(resource1.getURL().toString());

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("토큰존재여부 확인 용 : " + authentication.getName());

        try {
            Path path = Paths.get(resource1.getURI());
            String dirPath = path.toString() + "/" + authentication.getName();
            File folder = new File(dirPath);

            System.out.println(path.toString());

            mkdirFolder(folder);

            UUID uuid = UUID.randomUUID();
            String randomString = uuid.toString() + "_";

            for (MultipartFile multipartFile : thumbnailImage) {
                String thumnailImageName = "Thum_" + randomString + multipartFile.getOriginalFilename();
                FileOutputStream writer = new FileOutputStream(dirPath + "/" + thumnailImageName);
                writer.write(multipartFile.getBytes());
                writer.close();
                log.info("name for db : " + thumnailImageName);

                this.thum = thumnailImageName;
            }

            for (MultipartFile multipartFile : imageDetail) {
                String imageDetailName = "Detail_" + randomString + multipartFile.getOriginalFilename();
                FileOutputStream writer = new FileOutputStream(dirPath + "/" + imageDetailName);
                writer.write(multipartFile.getBytes());
                writer.close();
                log.info("name for db : " + imageDetailName);

                this.detail = imageDetailName;
            }
            this.lectureId = lectureId;

            lectureService.lectureAddImage(this.thum, this.detail, this.lectureId);

        } catch (Exception e) {
            return null;
        }
//        UrlResource image = new UrlResource("classpath:" + imageLocation + "/" + authentication.getName() + "/" + this.detail);

        return null;
    }

    @GetMapping("/image/{path}/{writer}")
    public ResponseEntity<UrlResource> getThumnail(@PathVariable("path") String path, @PathVariable("writer") String writer){

        if (findNull(path, writer)) return null;

        try {
            UrlResource image = new UrlResource("classpath:" + imageLocation + "/" + writer + "/" + path);
            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                    .contentType(MediaTypeFactory.getMediaType(image).orElse(MediaType.APPLICATION_OCTET_STREAM))
                    .body(image);
        } catch (NoSuchElementException | MalformedURLException error ) {
            error.getStackTrace();
            return null;
        }
    }

    private boolean findNull(String path, String writer) {
        if (path == null || writer == null) {
            System.out.println("paht : " + path);
            System.out.println("writer : " + writer);
            return true;
        }
        return false;
    }

    @PreAuthorize("hasRole('ROLE_MANAGER')")
    @PostMapping("/newlecture")
    public String newLecture(@RequestBody LinkedHashMap test) throws JsonProcessingException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        org.json.JSONObject lectureInfo1 = LectureParse.parsingInfoWithString(test, "lectureInfo");

        String title = lectureInfo1.getString("title");
        Long price = lectureInfo1.getLong("price");
        String description = lectureInfo1.getString("description");
        String content = lectureInfo1.getString("content");
        String grade = lectureInfo1.getString("grade");
        String category = lectureInfo1.getString("category");

        String[] categoryArray = category.split(",");
        for (String s : categoryArray) {
            System.out.println(s);
        }

        lectureService.lectureRegister(authentication.getName(), title, price, description, content, grade, category);
        System.out.println(Arrays.toString(categoryArray));

        return null;
    }

    @PreAuthorize("hasRole('ROLE_MANAGER')")
    @PutMapping("/modifyLecture")
    public String modifyLecture(@RequestBody LinkedHashMap info) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        org.json.JSONObject lectureInfo1 = LectureParse.parsingInfoWithString(info, "lectureInfo");

        Long lectureId = lectureInfo1.getLong("lectureId");
        String title = lectureInfo1.getString("title");
        Long price = lectureInfo1.getLong("price");
        String description = lectureInfo1.getString("description");
        String category = lectureInfo1.getString("category");
        String content = lectureInfo1.getString("content");
        String grade = lectureInfo1.getString("grade");

        String[] categoryArray = category.split(",");
        for (String s : categoryArray) {
            System.out.println(s);
        }

        lectureService.modifyLecture(lectureId, authentication.getName(), title, price, description, content, grade, category);
        System.out.println(Arrays.toString(categoryArray));

        return null;
    }

    @PreAuthorize("hasRole('ROLE_MANAGER')")
    @PostMapping("/upload/video/lecture")
    public @ResponseBody
    String videoUpload(@RequestParam("video") List<MultipartFile> video,
                       @RequestParam("duration") Long duration,
                       @RequestParam("title") String title,
                       @RequestParam("description") String description,
                       @RequestParam("id") Long id
    ) throws IOException {

        log.info("title : " + title);
        log.info("duration : " + duration.toString());
        log.info("description : " + description.toString());
        log.info("id : " + id.toString());

        String dur = Long.toString(duration);

        ClassPathResource resource1 = new ClassPathResource(videoLocation);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("sc 객체 : " + authentication.getName());

        try {
            Path path = Paths.get(resource1.getURI());
            String dirPath = path.toString() + "/" + authentication.getName();
            File folder = new File(dirPath);

            mkdirFolder(folder);

            UUID uuid = UUID.randomUUID();
            String randomString = uuid.toString() + "_";

            for (MultipartFile multipartFile : video) {
                String lecture = "Lecture_" + randomString + multipartFile.getOriginalFilename();
                FileOutputStream writer = new FileOutputStream(dirPath + "/" + lecture);
                writer.write(multipartFile.getBytes());
                writer.close();
                log.info("name for db : " + lecture);
                lectureService.videoUpload(title, description, dur, id, lecture);
            }

        } catch (Exception e) {
            return "upload fail";
        }
        return "upload success";
    }

    @PreAuthorize("hasRole('ROLE_MANAGER')")
    @Transactional
    @PostMapping("/modify/video/lecture")
    public @ResponseBody
    DtoWrapper modifyVideoUpload(@RequestParam("video") List<MultipartFile> video,
//                         long으로 받는지 확인
                                 @RequestParam("duration") Long duration,
                                 @RequestParam("title") String title,
                                 @RequestParam("description") String description,
                                 @RequestParam("id") Long id
    ) throws IOException {

        lectureService.modifyVideoDelete(id);

        String lectureName = null;

        String dur = Long.toString(duration);

        ClassPathResource resource1 = new ClassPathResource(videoLocation);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("sc 객체 : " + authentication.getName());

        try {
            Path path = Paths.get(resource1.getURI());
            String dirPath = path.toString() + "/" + authentication.getName();
            File folder = new File(dirPath);

            System.out.println(path.toString());

            mkdirFolder(folder);

            UUID uuid = UUID.randomUUID();
            String randomString = uuid.toString() + "_";


            for (MultipartFile multipartFile : video) {
                String lecture = "Lecture_" + randomString + multipartFile.getOriginalFilename();
                FileOutputStream writer = new FileOutputStream(dirPath + "/" + lecture);
                writer.write(multipartFile.getBytes());
                writer.close();
                log.info("name for db : " + lecture);
                lectureName = lecture;
            }
        } catch (Exception e) {
            log.info("에러");
        }

        return new DtoWrapper(lectureService.modifyVideo(title, description, dur, id, lectureName));
    }

    @PreAuthorize("hasRole('ROLE_MANAGER')")
    @GetMapping("/getLectureList")
    public List<LectureDto> getLectureList() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        return lectureService.getLectureList(authentication.getName());
    }


    @GetMapping("/getBasicInfo/{lectureId}")
    public DtoWrapper getBasicInfo(@PathVariable("lectureId") Long lectureId) {

        Optional<LectureDto> basicInfo = lectureService.getBasicInfo(lectureId);
        return new DtoWrapper(basicInfo);
    }

    @PreAuthorize("hasRole('ROLE_MANAGER')")
    @DeleteMapping("/deleteThumbImg/{lectureId}")
    public ResponseEntity deleteThumbImg(@PathVariable("lectureId") Long lectureId) {

        lectureService.deleteThumbImg(lectureId);

        return new ResponseEntity(HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ROLE_MANAGER')")
    @DeleteMapping("/deleteDetailImg/{lectureId}")
    public ResponseEntity deleteDetailImg(@PathVariable("lectureId") Long lectureId) {

        lectureService.deleteDetailImg(lectureId);

        return new ResponseEntity(HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ROLE_MANAGER')")
    @DeleteMapping("/deleteLecture/{lectureId}")
    public ResponseEntity deleteLecture(@PathVariable("lectureId") Long lectureId) {

        lectureService.deleteLecture(lectureId);

        return new ResponseEntity(HttpStatus.OK);
    }


    @GetMapping("/getSectionTopic/{lectureId}")
    public DtoWrapper getSectionTopic(@PathVariable("lectureId") Long lectureId) {

        return lectureService.getSectionTopic(lectureId);
    }

    @PreAuthorize("hasRole('ROLE_MANAGER')")
    @DeleteMapping("/deleteSection/{lectureListId}")
    public ResponseEntity deleteSection(@PathVariable("lectureListId") Long lectureListId) {

        lectureService.deleteSection(lectureListId);

        return new ResponseEntity(HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ROLE_MANAGER')")
    @PutMapping("/modifySectionTopic")
    public ResponseEntity modifySectionTopic(@RequestBody LectureListDto lectureListDto) {

        lectureService.modifySectionTopic(lectureListDto);

        return new ResponseEntity(HttpStatus.OK);
    }

    @GetMapping("/getLectureVideoInfo/{videoId}")
    public DtoWrapper getLectureVideoInfo(@PathVariable("videoId") Long videoId) {

        return lectureService.getLectureVideoInfo(videoId);
    }

    @PreAuthorize("hasRole('ROLE_MANAGER')")
    @DeleteMapping("/deleteLectureVideo/{videoId}")
    public ResponseEntity deleteLectureVideo(@PathVariable("videoId") Long videoId) {

        lectureService.deleteLectureVideo(videoId);

        return new ResponseEntity(HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ROLE_MANAGER')")
    @PostMapping("/inProgressToFalse/{id}")
    public ResponseEntity inProgressToFalse(@PathVariable("id") Long id) {

        lectureService.inProgressToFalse(id);

        return new ResponseEntity(HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ROLE_MANAGER')")
    @PostMapping("/inProgressToTrue/{id}")
    public ResponseEntity inProgressToTrue(@PathVariable("id") Long id) {

        lectureService.inProgressToTrue(id);

        return new ResponseEntity(HttpStatus.OK);
    }

    @GetMapping("/getLectureDetailInfo/{lectureId}")
    public DtoWrapper2 getLectureDetailInfo(@PathVariable("lectureId") Long lectureId) {
        System.out.println(lectureId);

        return lectureService.getLectureDetailInfo(lectureId);
    }

    @GetMapping("/getAllLecture")
    public List<LectureDto> getAllLecture() {

        return lectureService.getAllLectureList();
    }

    @GetMapping("/getLectureBanner/{page}")
    public DtoWrapper getLectureBanner(@PathVariable("page") int page) {

        return lectureService.lectureBanner(page);
    }

    @GetMapping("/getVideoInfo/{videoId}")
    public DtoWrapper getVideoInfo(@PathVariable Long videoId) {

        return lectureService.getVideoInfoDetail(videoId);
    }

    @GetMapping("/videos/{lectureId}/{token}")
    public ResponseEntity<ResourceRegion> getVideo(@PathVariable Long lectureId,
                                                   @RequestHeader HttpHeaders headers,
                                                   @PathVariable String token) throws IOException {


        log.info("lectureId : "+ lectureId);
        String tokenParsing = token.substring("Bearer ".length());

        DecodedJWT decodedJWT = TokenUtil.validateToken(tokenParsing);

        String username = decodedJWT.getSubject();

        log.info("현재 유저이름 : " + username);
        Member member = memberRepository.findByEmail(username);
        member.setLatestVideoId(lectureId);
        memberRepository.save(member);
        log.info("member videoId:" + member.getLatestVideoId());
        Optional<LectureVideoInfo> videoInfo = lectureService.getVideoInfo(lectureId, username);

        log.info("getVideo");

        UrlResource video = new UrlResource("classpath:" + videoLocation + "/" + videoInfo.get().getWriter() + "/" + videoInfo.get().getPath());
        ResourceRegion region = resourceRegion(video, headers);
        return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                .contentType(MediaTypeFactory.getMediaType(video).orElse(MediaType.APPLICATION_OCTET_STREAM))
                .body(region);
    }

    private ResourceRegion resourceRegion(UrlResource video, HttpHeaders headers) throws IOException {

        final long chunkSize = 1000000L;
        long contentLength = video.contentLength();

        HttpRange httpRange = headers.getRange().stream().findFirst().get();
        if(httpRange != null) {
            long start = httpRange.getRangeStart(contentLength);
            long end = httpRange.getRangeEnd(contentLength);
            long rangeLength = Long.min(chunkSize, end - start + 1);
            return new ResourceRegion(video, start, rangeLength);
        } else {
            long rangeLength = Long.min(chunkSize, contentLength);
            return new ResourceRegion(video, 0, rangeLength);
        }
    }

    @GetMapping("/getReview/{lectureId}")
    public DtoWrapper getReview(@PathVariable("lectureId") Long id) {

        return lectureService.getReview(id);
    }

    public void mkdirFolder(File folder) {

        if (!folder.exists()) {
            try {
                folder.mkdir();
                System.out.println("폴더생성함");
            } catch (Exception e) {
                e.getStackTrace();
            }
        } else {
            System.out.println("이미 존재하는 폴더입니다.");
        }
    }
}
