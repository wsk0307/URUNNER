package com.urunner.khweb.controller.payment;


import com.urunner.khweb.controller.dto.lecture.DtoWrapper;
import com.urunner.khweb.controller.dto.payment.PaymentLectureInfoDto;
import com.urunner.khweb.controller.dto.payment.PaymentDto;
import com.urunner.khweb.service.payment.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/payment")
public class PaymentController {

    private List<String> lectureList;


    @Autowired
    PaymentService paymentService;

    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/pay-ready")
    public void payReady(@RequestBody PaymentLectureInfoDto lectureInfoDto) {
        log.info("pay-ready!");
        this.lectureList = lectureInfoDto.getLectureList();
        log.info("lectureSet!");
        lectureList.forEach(System.out::println);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        log.info("memberName : " + authentication.getName());

    }


    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/success")
    public ResponseEntity<String> paymentSuccessCheck(@RequestBody PaymentDto paymentDto) throws Exception {
        log.info("paymentSuccessCheck()");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String memberEmail = authentication.getName();
        log.info(memberEmail);
        String result = paymentService.successCheck(paymentDto, lectureList, memberEmail);

        if (result.equals("success")) {
            log.info("success");
            return new ResponseEntity<>("success", HttpStatus.OK);

        } else {
            log.info("fail");
            return new ResponseEntity<>("fail", HttpStatus.OK);
        }
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/getPurchasedLecture")
    public DtoWrapper getPurchasedLecture() {

        return paymentService.getPurchasedLecture();
    }
}
