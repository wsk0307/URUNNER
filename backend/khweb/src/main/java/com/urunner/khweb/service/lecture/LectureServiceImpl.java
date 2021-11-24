package com.urunner.khweb.service.lecture;

import com.urunner.khweb.controller.dto.lecture.*;
import com.urunner.khweb.entity.lecture.*;
//import com.urunner.khweb.entity.lecture.LectureImage;
import com.urunner.khweb.entity.member.Member;
import com.urunner.khweb.entity.mypage.Cart;

import com.urunner.khweb.entity.mypage.WishList;
import com.urunner.khweb.entity.sort.Category;
import com.urunner.khweb.entity.sort.CategoryLecture;
import com.urunner.khweb.repository.lecture.*;
import com.urunner.khweb.repository.member.MemberRepository;
import com.urunner.khweb.utility.LectureUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Transactional
@Service
public class LectureServiceImpl implements LectureService {


    @Autowired
    private LectureVideoRepository lectureVideoRepository;

    @Autowired
    private LectureListRepository lectureListRepository;

    @Autowired
    private LectureRepository lectureRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private LectureUtil lectureUtil;


//    @Override
//    public void lectureVideo(LectureVideo lectureVideo, EnrollLectureVideoDto enrollLectureVideoDto) {
//
//        LectureList lectureList = lectureListRepository.enrollLectureList(enrollLectureVideoDto.writer, enrollLectureVideoDto.topic);
//
//        LectureVideo saveVideo = LectureVideo.builder()
//                .title(lectureVideo.getTitle())
//                .description(lectureVideo.getDescription())
//                .sequence(lectureVideo.getSequence())
//                .duration(lectureVideo.getDuration())
//                .build();
//
//        saveVideo.setLectureList(lectureList);
//
//        lectureVideoRepository.save(saveVideo);
//    }

    @Override
    public void lectureEnroll(Lecture lecture) {

        lectureRepository.save(lecture);
    }



    @Override
    public void lectureAddImage(String thum, String detail, Long id) {

        Lecture lecture = em.find(Lecture.class, id);

//        LectureImage lectureImage = LectureImage.builder()
//                .thumb_path(thum)
//                .detail_path(detail)
//                .build();
//
//        lectureImage.setLecture(lecture);
//
//        System.out.println(lectureImage.getThumb_path());
        try {
            if (lecture.getThumb_path() != null && lecture.getDetail_path() != null) {
                lectureUtil.deleteUtil("image", lecture.getThumb_path());
                lectureUtil.deleteUtil("image", lecture.getDetail_path());
            }
        } finally {
            lecture.setLectureThumb(thum);
            lecture.setLectureDetail(detail);
        }

        lectureRepository.save(lecture);
    }

    @Override
    public void lectureRegister(String writer, String title, Long price, String desc, String content, String grade, String categoryArray) {

        String[] category = categoryArray.split(",");

        List<String> cateList = new ArrayList();

        Lecture lecture = Lecture.builder()
                .writer(writer)
                .title(title)
                .price(price)
                .description(desc)
                .content(content)
                .grade(grade)
                .build();

        Review review = Review.builder()
                .rating(0L)
                .content(null)
                .writer(null)
                .build();

        review.setLecture(lecture);

        lectureRepository.save(lecture);

        reviewRepository.save(review);

        for (String s : category) {
            cateList.add(s);
        }

        for (int i = 0; i < category.length; i++) {
            Category getCategory = categoryRepository.findByCategoryName(cateList.get(i));
            if (getCategory != null) {
                CategoryLecture categoryLecture = new CategoryLecture();
                categoryLecture.setLecture(lecture);
                categoryLecture.setCategory(getCategory);
                em.persist(categoryLecture);
            }
        }
    }

    @Transactional
    @Override
    public void modifyLecture(Long lectureId, String writer, String title, Long price, String desc, String content, String grade, String categoryArray) {

        String[] category = categoryArray.split(",");

        List<String> cateList = new ArrayList();

        Lecture lecture = em.find(Lecture.class, lectureId);

        for (String s : category) {
            cateList.add(s);
        }

        lecture.setTitle(title);
        lecture.setPrice(price);
        lecture.setDescription(desc);
        lecture.setGrade(grade);
        lecture.setContent(content);
        lectureRepository.save(lecture);

        String query = "delete from CategoryLecture where Lecture_id = :Lecture_id";

        em.createQuery(query)
                .setParameter("Lecture_id", lectureId)
                .executeUpdate();

        for (int i = 0; i < category.length; i++) {
            Category getCategory = categoryRepository.findByCategoryName(cateList.get(i));
            if (getCategory != null) {
                CategoryLecture categoryLecture = new CategoryLecture();
                categoryLecture.setLecture(lecture);
                categoryLecture.setCategory(getCategory);
                em.persist(categoryLecture);
            }
        }

    }

    @Override
    public void saveLectureSection(Long lectureId, String topic) {

        try {
            Lecture lecture = em.find(Lecture.class, lectureId);

            lecture.exist(lecture);

//            중복 나중에 한번에 하기로..
//            중복검증은 리스트 아이디, 토픽 두개를 가지로 체크 필요
//            if (lectureListRepository.findByTopic(topic).isPresent()) {
//                log.info("토픽이 중복이다..");
//                throw new Exception("중복 토픽입니다.");
//            }

            LectureList lectureList = LectureList.builder()
                    .topic(topic)
                    .build();

            lectureList.setLecture(lecture);
            lectureListRepository.save(lectureList);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Page<LectureListDto> findAllLectureSection(Long lectureId) {

//       jpa페이징은 쿼리 -1개
        Lecture lecture = em.find(Lecture.class, lectureId);
//      나중에 페이징 처리하도록 둘것
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<LectureList> lecturePaging = lectureListRepository.findByLecture(lecture, pageRequest);

        Page<LectureListDto> pageDto = lecturePaging.map(m -> new LectureListDto(m.getLectureList_id(), m.getTopic(), m.getSection()));

        return pageDto;
    }

    @Override
    public void videoUpload(String title, String desc, String dur, Long id, String path) {

        LectureList lectureList = em.find(LectureList.class, id);

        System.out.println(lectureList.getLecture().getLecture_id());
        LectureVideo lectureVideo = LectureVideo.builder()
                .title(title)
                .description(desc)
                .duration(dur)
                .videoPath(path)
                .build();

        lectureVideo.setLectureList(lectureList);

        lectureVideoRepository.save(lectureVideo);
    }

    @Override
    public Optional<LectureVideoDto> modifyVideo(String title, String desc, String duration, Long id, String path) {

        Optional<LectureVideo> lectureVideo = lectureVideoRepository.findById(id);

        lectureVideo.orElseThrow(() -> new NoSuchElementException());

        lectureVideo.ifPresent(
                l-> {
                    l.setTitle(title);
                    l.setDescription(desc);
                    l.setDuration(duration);
                    l.setVideoPath(path);
                    lectureVideoRepository.save(l);
                }
        );
        return lectureVideo.stream().findAny().map(l ->
                new LectureVideoDto(l.getId(), l.getTitle(), l.getDescription(), l.getSequence(), l.getDuration(), l.getVideoPath())
        );

    }

    @Override
    public void modifyVideoDelete(Long id) {

        Optional<LectureVideo> lectureVideo = lectureVideoRepository.findById(id);

        lectureVideo.orElseThrow(() -> new NoSuchElementException());

        lectureUtil.deleteUtil("video", lectureVideo.get().getVideoPath());

    }

    @Transactional(readOnly = true)
    @Override
    public DtoWrapper2 getLectureDetailInfo(Long lectureId) {

        Optional<Lecture> lecture = lectureRepository.findById(lectureId);

        String query = "select c from Category c join CategoryLecture cl on cl.category = c " +
                "join Lecture l on l = cl.lecture where l.id = :lectureId";

        //      점수 1~10으로 받고 반 나누기
        String query4 = "select new com.urunner.khweb.service.lecture.GetReviewDto(avg(r.rating), count(r.rating)) from Review r where r.lecture.lecture_id in :id";

        Optional<LectureDto> lectureDto = lecture.stream().findAny().map(l ->
                new LectureDto(l.getLecture_id(), l.getWriter(), l.getTitle(),
                        l.getDescription(), l.getPrice(), l.isInProgress(),
                        l.isDiscounted(), l.getThumb_path(), l.getDetail_path(), l.getContent(), l.getGrade(),
                        em.createQuery(query, Category.class)
                                .setParameter("lectureId", l.getLecture_id()).
                                getResultList(),
                        em.createQuery(query4, GetReviewDto.class)
                                .setParameter("id", l.getLecture_id())
                                .getSingleResult()
                ));

        List<LectureList> lectureLists = lectureListRepository.lectureList(lectureId);

        String query2 = "select v from LectureVideo v where v.lectureList.lectureList_id = :id";

        List<LectureListDto2> list = lectureLists.stream().map(l ->
                new LectureListDto2(l.getLectureList_id(), l.getSection(), l.getTopic(),
                        em.createQuery(query2, LectureVideo.class)
                                .setParameter("id", l.getLectureList_id())
                                .getResultList()
                )).collect(Collectors.toList());

        String query3 = "select count(w.wishListId) from WishList w where w.lecture.lecture_id = :id";

        Long wishListCount = em.createQuery(query3, Long.class)
                .setParameter("id", lecture.get().getLecture_id())
                .getSingleResult();



        String username = authentication();
        if (username.equals("anonymousUser")) {
            log.info("로그인 되있지않은 사용자");
        } else {
            Member member = memberRepository.findByEmail(username);

//            시간되면 fetch join으로 가져오기
            List<Cart> carts = new ArrayList<>(member.getMyPage().getCartList());

            List<WishList> wishLists = new ArrayList<>(member.getMyPage().getWishLists());

            if (wishLists.size() != 0) {
                for (int j = 0; j < wishLists.size(); j++) {
                    boolean exist = lecture.get().getLecture_id().equals(wishLists.get(j).getLecture().getLecture_id());
                    System.out.println("매칭 여부 확인 : " + exist);
                    if (exist) {
                        lectureDto.ifPresent(l -> l.setWishList(true));
                    }
                }
            }

            if (carts.size() != 0) {
                for (int j = 0; j < carts.size(); j++) {
                    boolean exist = lecture.get().getLecture_id().equals(carts.get(j).getLecture().getLecture_id());
                    System.out.println("매칭 여부 확인 : " + exist);
                    if (exist) {
                        lectureDto.ifPresent(l -> l.setCart(true));
                    }
                }
            }
        }


//      현재는 4방쿼리
//        한방쿼리만드는법
//       1. 네이티브쿼리로 dsl로
//       2. OneToMany부분 fetch 조인 set으로 바꾸기
//       3. queryDsl쓰기...
        DtoWrapper2 dtoWrapper = new DtoWrapper2(lectureDto, Optional.of(list));
        dtoWrapper.setWishListCount(wishListCount);
        return dtoWrapper;
    }

    @Override
    public Optional<LectureVideoInfo> getVideoInfo(Long lectureId) {

        Optional<LectureVideo> videoInfo = lectureVideoRepository.findById(lectureId);

        Optional<LectureVideoInfo> lectureVideoInfo = videoInfo.stream().findFirst().map(l ->
                new LectureVideoInfo(l.getLectureList().getLecture().getWriter(), l.getVideoPath())
        );

        return lectureVideoInfo;
    }

    @Override
    public DtoWrapper getVideoInfoDetail(Long id) {

        Optional<LectureVideo> videoInfo = lectureVideoRepository.findById(id);

        return new DtoWrapper(videoInfo.map(l -> new LectureVideoDto(l.getTitle(), l.getDescription(), l.getDuration())));
    }

    @Override
    public DtoWrapper mainCartList(int page) {

        String query = "select l from Lecture l join l.cartList cl join cl.myPage mp join mp.member m " +
                "where m.email = :email";

        List<Lecture> email = em.createQuery(query, Lecture.class)
                .setParameter("email", authentication())
                .setFirstResult(page)
                .setMaxResults(5)
                .getResultList();

        List<LectureDto> lectureDtos = email.stream().map(l ->
                new LectureDto(l.getLecture_id(), l.getWriter(), l.getTitle(), l.getPrice(), l.isDiscounted(), l.getThumb_path())).collect(Collectors.toList());

        return new DtoWrapper(lectureDtos);
    }

    @Override
    public DtoWrapper mainWishList(int page) {

        String query = "select l from Lecture l join l.wishList cl join cl.myPage mp join mp.member m " +
                "where m.email = :email";

        List<Lecture> email = em.createQuery(query, Lecture.class)
                .setParameter("email", authentication())
                .setFirstResult(page)
                .setMaxResults(5)
                .getResultList();

        List<LectureDto> lectureDtos = email.stream().map(l ->
                new LectureDto(l.getLecture_id(), l.getWriter(), l.getTitle(), l.getPrice(), l.isDiscounted(), l.getThumb_path())).collect(Collectors.toList());

        return new DtoWrapper(lectureDtos);
    }

    @Override
    public List<LectureDto> getAllLectureList() {
        List<Lecture> findAllLectureList = lectureRepository.findAll();

        String query = "select c from Category c join CategoryLecture cl on cl.category = c " +
                "join Lecture l on l = cl.lecture where l.id = :lectureId";

        return findAllLectureList.stream().map(l ->
                new LectureDto(l.getLecture_id(), l.getWriter(), l.getTitle(),
                        l.getDescription(), l.getPrice(), l.isInProgress(),
                        l.isDiscounted(), l.getThumb_path(), l.getDetail_path(), l.getContent(), l.getGrade(),
                        em.createQuery(query, Category.class)
                                .setParameter("lectureId", l.getLecture_id()).
                                getResultList()
                )).collect(Collectors.toList());
    }

    @Override
    public List<GetReviewDto> reviewList(Page<LectureSearchDto> lectureSearchDtos) {

        String query = "select new com.urunner.khweb.service.lecture.GetReviewDto(avg(r.rating), count(r.rating)) from Review r where r.lecture.lecture_id in :id";

        return lectureSearchDtos.stream().map(l ->
                em.createQuery(query, GetReviewDto.class)
                        .setParameter("id", l.getId())
                        .getSingleResult()).collect(Collectors.toList());
    }

    //  옳게 된 쿼리
    @Override
    public DtoWrapper lectureBanner(int page) {
        try {

            PageRequest pageRequest = PageRequest.of(page, 4);

            Page<Lecture> findAllLecture = lectureRepository.findByInProgressTrue(true, pageRequest);

//      4번 추가 쿼리 feth쓸거면 lecture등록시 review 하나 추가해주기..
//      점수 1~10으로 받고 반 나누기
            String query = "select new com.urunner.khweb.service.lecture.GetReviewDto(avg(r.rating), count(r.rating)) from Review r where r.lecture.lecture_id in :id";

//        System.out.println("Review 사이즈 : " +findAllLecture.getContent().get(0).getReviews().size());
            Page<LectureDto> lectureDtos = findAllLecture.map(l ->
                    new LectureDto(l.getLecture_id(), l.getWriter(), l.getTitle(),
                            l.getDescription(), l.getPrice(), l.isInProgress(),
                            l.isDiscounted(), l.getThumb_path(), l.getDetail_path(), l.getContent(), l.getGrade(),
                            l.getCategoryList().stream().map(CategoryLecture::getCategory).collect(Collectors.toList()),
                            em.createQuery(query, GetReviewDto.class)
                                    .setParameter("id", l.getLecture_id())
                                    .getSingleResult()
                    ));

            String username = authentication();
            if (username.equals("anonymousUser")) {
                log.info("로그인 되있지않은 사용자");
            } else {
                Member member = memberRepository.findByEmail(username);

//            시간되면 fetch join으로 가져오기
                List<Cart> carts = new ArrayList<>(member.getMyPage().getCartList());

                List<WishList> wishLists = new ArrayList<>(member.getMyPage().getWishLists());

                if (wishLists.size() != 0) {
                    for (int i = 0; i < lectureDtos.getContent().size(); i++) {
                        for (int j = 0; j < wishLists.size(); j++) {
                            boolean exist = lectureDtos.getContent().get(i).getId().equals(wishLists.get(j).getLecture().getLecture_id());
                            System.out.println("매칭 여부 확인 : " + exist);
                            if (exist) {
                                lectureDtos.getContent().get(i).setWishList(true);
                            }
                        }
                    }
                }

                if (carts.size() != 0) {
                    for (int i = 0; i < lectureDtos.getContent().size(); i++) {
                        for (int j = 0; j < carts.size(); j++) {
                            boolean exist = lectureDtos.getContent().get(i).getId().equals(carts.get(j).getLecture().getLecture_id());
                            System.out.println("매칭 여부 확인 : " + exist);
                            if (exist) {
                                lectureDtos.getContent().get(i).setCart(true);
                            }
                        }
                    }
                }


            }
            return new DtoWrapper(lectureDtos);
        } catch (NoSuchElementException noSuchElementException) {
            log.info("등록된 강의가 없습니다.");
            return null;
        }

    }

    @Autowired
    private ReviewRepository reviewRepository;

    @Override
    public Boolean regStudentComment(ReviewDto reviewDto) {

//        구매한사람인지 체크하는 메서드 ( 구입 테이블에서 로드하는 로직)
        Lecture lecture = em.find(Lecture.class, reviewDto.getLectureId());

        Review review = Review.builder()
                .rating(reviewDto.getRating())
                .content(reviewDto.getContent())
                .writer(authentication())
                .build();

        review.setLecture(lecture);

        reviewRepository.save(review);

        return true;
    }

    @Override
    public DtoWrapper getCartList() {

        String query = "select l from Lecture l join l.cartList cl join cl.myPage mp join mp.member m " +
                "where m.email = :email";

        List<Lecture> lectureList = em.createQuery(query, Lecture.class)
                .setParameter("email", authentication())
                .getResultList();

        List<LectureDto> lectureDtos = lectureList.stream().map(l ->
                new LectureDto(l.getLecture_id(), l.getWriter(), l.getTitle(), l.getPrice(), l.isDiscounted(), l.getThumb_path())).collect(Collectors.toList());

        return new DtoWrapper(lectureDtos);
    }

    @Override
    public DtoWrapper getWishList() {
        String query = "select l from Lecture l join l.wishList cl join cl.myPage mp join mp.member m " +
                "where m.email = :email";

        List<Lecture> lectureList = em.createQuery(query, Lecture.class)
                .setParameter("email", authentication())
                .getResultList();

        List<LectureDto> lectureDtos = lectureList.stream().map(l ->
                new LectureDto(l.getLecture_id(), l.getWriter(), l.getTitle(), l.getPrice(), l.isDiscounted(), l.getThumb_path())).collect(Collectors.toList());

        return new DtoWrapper(lectureDtos);
    }

    //   심각하게 잘못된 쿼리
    @Transactional(readOnly = true)
    @Override
    public List<LectureDto> getLectureList(String writer) {

        List<Lecture> findAllLecture = lectureRepository.findByWriter(writer);
//      부적합한 쿼리 N + 1
        String query = "select c from Category c join CategoryLecture cl on cl.category = c " +
                "join Lecture l on l = cl.lecture where l.id = :lectureId";
//      부적합한 쿼리 N + 1
//        String query = "select c from Category c join fetch c.lectureList cl " +
//                "join fetch cl.lecture l where l.id = :lectureId";

//        fetch join을 위에서 사용해야됨

        return findAllLecture.stream().map(l ->
                new LectureDto(l.getLecture_id(), l.getWriter(), l.getTitle(),
                        l.getDescription(), l.getPrice(), l.isInProgress(),
                        l.isDiscounted(), l.getThumb_path(), l.getDetail_path(), l.getContent(), l.getGrade(),
                        em.createQuery(query, Category.class)
                                .setParameter("lectureId", l.getLecture_id()).
                                getResultList()
                )).collect(Collectors.toList());
    }

    @Override
    public List<LectureVideoDto> findAllLectureVideo(Long lectureListId) {

        LectureList lectureList = em.find(LectureList.class, lectureListId);

        List<LectureVideo> findAllLectureVideo = lectureVideoRepository.findByLectureList(lectureList);

        return findAllLectureVideo.stream().map(l ->
                new LectureVideoDto(l.getId(), l.getTitle(), l.getDescription(), l.getSequence(), l.getDuration(),
                        l.getVideoPath())).collect(Collectors.toList());
    }

    @Override
    public Optional<LectureDto> getBasicInfo(Long lectureId) {

        Optional<Lecture> lecture = lectureRepository.findById(lectureId);

        Lecture lecture1 = em.find(Lecture.class, lectureId);

        String query = "select c from Category c join CategoryLecture cl on cl.category = c " +
                "join Lecture l on l = cl.lecture where l.id = :lectureId";

        return lecture.stream().map(l ->
                new LectureDto(l.getLecture_id(), l.getWriter(), l.getTitle(),
                        l.getDescription(), l.getPrice(), l.isInProgress(), l.isDiscounted(),
                        l.getThumb_path(), l.getDetail_path(),
                        l.getContent(), l.getGrade(),
                        em.createQuery(query, Category.class)
                                .setParameter("lectureId", l.getLecture_id()).
                                getResultList())).findAny();
    }

    @Override
    public void deleteThumbImg(Long lectureId) {

        Optional<Lecture> lecture = lectureRepository.findById(lectureId);

        lecture.orElseThrow(() -> new NoSuchElementException());

        lectureUtil.deleteUtil("image", lecture.get().getThumb_path());

        lecture.filter(l -> authentication().equals(l.getWriter()))
                .ifPresent(l -> {
                    l.setLectureThumb(null);
                    lectureRepository.save(l);
                });
//        테스트용 토큰없는경우(X)
//        lecture.ifPresent(l -> {
//                    l.setLectureThumb(null);
//                    lectureRepository.save(l);
//                });

//        파일 삭제 아직 미구현


    }



    @Override
    public void deleteDetailImg(Long lectureId) {

        Optional<Lecture> lecture = lectureRepository.findById(lectureId);

        lecture.orElseThrow(() -> new NoSuchElementException());
//        ispresnet ifpresent 구분 주의


        lectureUtil.deleteUtil("image", lecture.get().getDetail_path());

        lecture.filter(l -> authentication().equals(l.getWriter()))
                .ifPresent(l -> {
                    l.setLectureDetail(null);
                    lectureRepository.save(l);
                });
//        파일 삭제 아직 미구현
    }

    @Override
    public void deleteLecture(Long lectureId) {

        try {
            String query = "select l from Lecture l join l.lectureLists li join li.lectureVideos where l.lecture_id = :id";

            Lecture lecture1 = em.createQuery(query, Lecture.class)
                    .setParameter("id", lectureId)
                    .getSingleResult();

            List<LectureList> lectureLists = lecture1.getLectureLists();

            if (lecture1.getThumb_path() != null && lecture1.getDetail_path() != null) {
                lectureUtil.deleteUtil("image", lecture1.getThumb_path());
                lectureUtil.deleteUtil("image", lecture1.getDetail_path());
            }

            for (int i = 0; i < lectureLists.size(); i++) {
                for (int j = 0; j < lectureLists.get(i).getLectureVideos().size(); j++) {
                    lectureUtil.deleteUtil("video", lectureLists.get(i).getLectureVideos().get(j).getVideoPath());
                }
            }

        } catch (NoResultException re) {
            log.info("error");
        } finally {

            Optional<Lecture> lecture = lectureRepository.findById(lectureId);

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            lecture.filter(l -> authentication.getName().equals(l.getWriter()))
                    .ifPresent(l -> {
                        l.setLectureDetail(null);
                        lectureRepository.deleteById(l.getLecture_id());
                    });
        }
    }

    @Override
    public void modifyLecture(LectureDto lectureDto) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Optional<Lecture> lecture = lectureRepository.findById(lectureDto.getId());

        lecture.filter(l -> authentication.getName().equals(l.getWriter()))
                .ifPresent(l -> {
                    l.setTitle(lectureDto.getTitle());
                    l.setPrice(lectureDto.getPrice());
                    l.setDescription(lectureDto.getDesc());
                    lectureRepository.save(l);
                });
    }

    @Override
    public DtoWrapper getSectionTopic(Long lectureListId) {

        Optional<LectureList> lectureList = lectureListRepository.findById(lectureListId);

//        인증인가 처리는 get쪽에서  필요가 전혀 없을듯 수정,삭제만 인증 인가
        Optional<LectureListDto> lectureListDto = lectureList.stream().findAny().map(l -> new LectureListDto(l.getLectureList_id(), l.getTopic(), l.getSection()));

        return new DtoWrapper(lectureListDto);

    }

    @Override
    public void deleteSection(Long lectureListId) {

        Optional<LectureList> lectureList = lectureListRepository.findById(lectureListId);

        lectureList.filter(l -> authentication().equals(l.getLecture().getWriter()))
                .ifPresent(l -> lectureListRepository.deleteById(l.getLectureList_id()));

    }

    @Override
    public void modifySectionTopic(LectureListDto lectureListDto) {

        Optional<LectureList> lectureList = lectureListRepository.findById(lectureListDto.lectureList_id);

        lectureList.filter(l -> authentication().equals(l.getLecture().getWriter()))
                .ifPresent(l -> {
                    l.setTopic(lectureListDto.topic);
                    lectureListRepository.save(l);
                });
    }

    @Override
    public DtoWrapper getLectureVideoInfo(Long videoId) {

        Optional<LectureVideo> lectureVideo = lectureVideoRepository.findById(videoId);

        Optional<LectureVideoDto> lectureVideoDto = lectureVideo.stream().findAny().map(l ->
                new LectureVideoDto(l.getId(), l.getTitle(), l.getDescription(), l.getSequence(), l.getDuration(), l.getVideoPath()));

//        Stream<LectureVideoDto> lectureVideoDtoStream = lectureVideo.stream().map(l ->
//                new LectureVideoDto(l.getId(), l.getTitle(), l.getDescription(), l.getSequence(), l.getDuration(), l.getVideoPath())
//        );

        return new DtoWrapper(lectureVideoDto);
    }

    @Override
    public void deleteLectureVideo(Long videoId) {

        Optional<LectureVideo> videoInfo = lectureVideoRepository.findById(videoId);

        lectureUtil.deleteUtil("video", videoInfo.get().getVideoPath());


        videoInfo.filter(l -> authentication().equals(l.getLectureList().getLecture().getWriter()))
                .ifPresent(l -> lectureVideoRepository.deleteById(videoId));
    }

    @Override
    public void inProgressToFalse(Long id) {
        lectureUtil.isProgress(id, false);
    }

    @Override
    public void inProgressToTrue(Long id) {
        lectureUtil.isProgress(id, true);
    }



    private String authentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }
}