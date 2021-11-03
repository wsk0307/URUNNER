package com.urunner.khweb.service.board;

import com.urunner.khweb.controller.dto.StudyRequest;
import com.urunner.khweb.entity.board.Study;
import com.urunner.khweb.repository.board.study.StudyBoardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;


@Service
public class StudyBoardServiceImpl implements StudyBoardService {

    @Autowired
    private StudyBoardRepository repository;

    @Override
    public Study register(StudyRequest studyRequest) throws Exception {

        Study postEntity = new Study(studyRequest.getTitle(), studyRequest.getContent(), studyRequest.getWriter(), studyRequest.getName());

        return repository.save(postEntity);
    }

    public List<Study> findAll(){
        return repository.findAll();
    }

    public Optional<Study> findByBoardNo(Long boardNo){
        return repository.findByBoardNo(boardNo);
    }

    public void updatePost(StudyRequest studyRequest){

        repository.updatePost(studyRequest.getTitle(), studyRequest.getContent(), studyRequest.getBoardNo());
    }


    public void delete(Long boardNo) throws Exception {
        Study study = new Study();
        study.setBoardNo(boardNo);
        repository.delete(study);
    }
}