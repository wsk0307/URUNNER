package com.urunner.khweb.service.board;

import com.urunner.khweb.controller.dto.StudyRequest;
import com.urunner.khweb.entity.board.Study;
import com.urunner.khweb.entity.board.StudyMember;

import java.util.List;
import java.util.Optional;

public interface StudyBoardService {
    Study register(StudyRequest studyRequest) throws Exception;
    List<Study> findAll() throws Exception;
    Optional<Study> findByBoardNo(Long boardNo) throws Exception;
    void updatePost(StudyRequest studyRequest) throws Exception;
    void updateCurrentNum(StudyRequest studyRequest) throws Exception;
    void delete(Long boardNo) throws Exception;
    List<StudyMember> selectStudyBoardNo(Long boardNo);
    StudyMember applyMember(StudyMember studyMember);
    Long checkLength(Long boardNo);
}