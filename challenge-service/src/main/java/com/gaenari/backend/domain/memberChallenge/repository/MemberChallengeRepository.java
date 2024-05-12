package com.gaenari.backend.domain.memberChallenge.repository;

import com.gaenari.backend.domain.memberChallenge.entity.MemberChallenge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemberChallengeRepository extends JpaRepository<MemberChallenge, Long> {

    // 회원 아이디로 도전과제 리스트 조회
    List<MemberChallenge> findByMemberId(String memberId);

    // 회원 아이디로 해당 회원이 달성한 도전 과제 아이디만 조회
    List<MemberChallenge> findByMemberIdAndIsAchievedIsTrue(String memberId);

    // 회원 아이디와 도전 과제 아이디로 해당 회원의 도전 과제 정보를 조회
    MemberChallenge findByMemberIdAndChallengeId(String memberId, Integer challengeId);

    // 회원 아이디와 일치하고, Obtainable이 i 보다 큰 회원의 도전과제 리스트 조회
    List<MemberChallenge> findByMemberIdAndObtainableGreaterThan(String memberId, int i);

}