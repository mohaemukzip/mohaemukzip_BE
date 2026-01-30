package com.mohaemukzip.mohaemukzip_be.global.scheduler;

import com.mohaemukzip.mohaemukzip_be.domain.ingredient.repository.MemberIngredientRepository;
import com.mohaemukzip.mohaemukzip_be.domain.member.entity.Member;
import com.mohaemukzip.mohaemukzip_be.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class FridgeScoreScheduler {

    private final MemberRepository memberRepository;
    private final MemberIngredientRepository memberIngredientRepository;

    @Scheduled(cron = "0 0 0 * * *") // 매일 자정
    @Transactional
    public void updateFridgeScores() {
        log.info("냉장고 점수 업데이트 시작");

        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        LocalDate sevenDaysAgo = today.minusDays(7);

        List<Member> members = memberRepository.findAll();
        int updatedCount = 0;

        for (Member member : members) {
            int delta = 0;

            // 벌점: 어제 만료된 재료 × -2
            int expiredCount = memberIngredientRepository.countExpiredYesterday(member.getId(), yesterday);
            delta -= expiredCount * 2;

            // 상점: 최근 7일간 만료 없으면 +1
            int recentExpired = memberIngredientRepository.countExpiredBetween(
                    member.getId(), sevenDaysAgo, yesterday);
            if (recentExpired == 0) {
                delta += 1;
            }

            if (delta != 0) {
                member.updateFridgeScore(delta);
                updatedCount++;
                log.debug("사용자 {} 냉장고 점수 변경: {} (현재: {})", member.getId(), delta, member.getFridgeScore());
            }
        }

        log.info("냉장고 점수 업데이트 완료. {}명의 사용자 업데이트", updatedCount);
    }
}
