package com.dcom.intranet.archive.repository;

import com.dcom.intranet.archive.domain.Archive;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ArchiveRepository extends JpaRepository<Archive, Long> {

    // Optional을 쓰는 이유 : 조회 결과가 없을 수도 있는 단일(1개) 객체를 안전하게 다루기 위해
    // 없으면 Optional.empty() 반환
    // 용도 : 족보 등록 시 기존 과목/교수 폴더가 있는지 확인용(보류)
    // "확률 및 랜덤변수", "확률및랜덤변수" 등의 여러 형태로 폴더를 생성할려는 경우가 생길 수 있으니 있는지 조회를 할 때는 띄어쓰기 없이 검색
    Optional<Archive> findBySubjectNameAndProfessorName(String subjectName, String professorName);
    // 과목명 또는 교수명으로 찾기
    Page<Archive> findBySubjectNameContainingOrProfessorNameContaining(
            String subjectName,
            String professorName,
            Pageable pageable
    );

    //findAll(Pageable pageable)은 JpaRepository에 이미 있어서 작성 X
}
