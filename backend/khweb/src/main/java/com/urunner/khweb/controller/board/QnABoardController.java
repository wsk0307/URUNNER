package com.urunner.khweb.controller.board;

import com.urunner.khweb.controller.dto.QnARequest;
import com.urunner.khweb.controller.dto.StudyRequest;
import com.urunner.khweb.entity.board.QnA;
import com.urunner.khweb.entity.board.QnAMember;
import com.urunner.khweb.entity.board.Study;
import com.urunner.khweb.entity.board.StudyMember;
import com.urunner.khweb.service.board.QnABoardService;
import com.urunner.khweb.service.board.StudyBoardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Slf4j
@Controller
@RequestMapping("/qnaboard")
@CrossOrigin(origins = "http://localhost:8080", allowedHeaders = "*")
public class QnABoardController {

    @Autowired
    private QnABoardService service;

    @PostMapping("/register")
    public ResponseEntity<QnA> register(
            @Validated @RequestBody QnARequest qnARequest) throws Exception {
        log.info(":::: post register request from vue");
        log.info(":::: RequestBody value : " + qnARequest);

        return new ResponseEntity<>(service.register(qnARequest), HttpStatus.OK);
    }

    @GetMapping("/lists")
    public ResponseEntity<List<QnA>> getLists () throws Exception {
        log.info("getStudyLists() ");

        return new ResponseEntity<>(service.findAll(), HttpStatus.OK);
    }

    @GetMapping("/{boardNo}")
    public ResponseEntity<Optional<QnA>> read(@PathVariable("boardNo") Long boardNo) throws Exception {

        Optional<QnA> board = service.findByBoardNo(boardNo);

        return new ResponseEntity<>(board, HttpStatus.OK);
    }

    @GetMapping("/memberList/{boardNo}") // study에 지원한 memberList 요청
    public ResponseEntity<List<QnAMember>> memberList(@PathVariable("boardNo") Long boardNo){

        log.info("****");
        System.out.println(boardNo);
        log.info("****");
        log.info("****");
        log.info("****");
        log.info("****");
        List<QnAMember> member = service.selectStudyBoardNo(boardNo);
        log.info("****");
        log.info("****");
        log.info("study memberList 값은 : " + member);
        log.info("****");
        log.info("****");

        return new ResponseEntity<>(member, HttpStatus.OK);
    }

    @PutMapping("/apply/{boardNo}") // study에 지원한 member 등록
    public ResponseEntity<String> apply(@PathVariable("boardNo") Long boardNo,
                                        @Validated @RequestBody QnAMember qnAMember) throws Exception {
        System.out.println("######## Controller run success");
        qnAMember.setBoardNo(boardNo);
        System.out.println("######## applyMember ready");
        service.applyMember(qnAMember); //일단 memberList에 추가하고(중복체크까지)
        System.out.println("######## applyMember success");

        System.out.println("######## checkLength ready");
        Long num = service.checkLength(boardNo);// boardNO 값을 가지는 테이블의 length를 구해서 num으로 주고
        System.out.println("######## checkLength success and num value is" + num);
        num += 1;

        QnARequest qnARequest = new QnARequest();
        qnARequest.setCurrentNum(num);
        qnARequest.setBoardNo(boardNo);
        System.out.println("######## studyRequest value :" + qnARequest);
        service.updateCurrentNum(qnARequest); // post 값 갱신

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping ("/{boardNo}")
    public ResponseEntity<QnA> modify(@PathVariable("boardNo") Long boardNo,
                                       @Validated @RequestBody QnARequest qnARequest) throws Exception {
        log.info(":::: post modify request from vue");
        log.info(":::: RequestBody value : " + qnARequest);

        qnARequest.setBoardNo(boardNo);
        service.updatePost(qnARequest);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/{boardNo}")
    public ResponseEntity<Void> remove(@PathVariable("boardNo") Long boardNo) throws Exception {
        service.delete(boardNo);

        return new ResponseEntity<>(HttpStatus.OK);
    }
}