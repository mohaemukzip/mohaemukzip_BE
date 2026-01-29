package com.mohaemukzip.mohaemukzip_be.domain.ingredient.service;

import com.mohaemukzip.mohaemukzip_be.domain.member.repository.MemberRepository;
import com.mohaemukzip.mohaemukzip_be.global.exception.BusinessException;
import com.mohaemukzip.mohaemukzip_be.global.response.code.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecentSearchServiceImpl implements RecentSearchService {

    private final MemberRepository memberRepository;
    private final RedisTemplate<String,String> redisTemplate;

    private static final int MAX_RECENT_SEARCH_COUNT = 10;
    private static final String RECENT_SEARCH_KEY = "recent_search";

    private String getRecentSearchKey(Long memberId) {
        return RECENT_SEARCH_KEY + memberId;
    }

    // 최근 검색어 저장
    @Override
    public void saveRecentSearch(Long memberId, String keyword) {
        System.out.println("============== [Redis 저장 로직 시작] ==============");
        System.out.println("1. 입력된 memberId: " + memberId);
        System.out.println("2. 입력된 keyword: " + keyword);
        if(keyword == null || keyword.isBlank()){
            System.out.println("❌ 실패: 키워드가 비어있어서 저장을 안 하고 종료합니다.");
            return;
        }

        if (!memberRepository.existsById(memberId)) {
            System.out.println("❌ 실패: DB에서 멤버를 찾을 수 없습니다. (ID: " + memberId + ")");
            // 예외를 던지면 500 에러가 나니까, 일단 로그만 보고 리턴할게요.
            return;
        }
        String recentSearchKey = getRecentSearchKey(memberId);
        System.out.println("3. 생성된 Redis Key: " + recentSearchKey);

        try {
            System.out.println("🚀 Redis에 데이터 넣는 중...");
            redisTemplate.opsForZSet().add(recentSearchKey, keyword, System.currentTimeMillis());
            System.out.println("✅ Redis 저장 명령 성공!");

            Long size = redisTemplate.opsForZSet().zCard(recentSearchKey);

            if (size != null && size >= MAX_RECENT_SEARCH_COUNT) {
                redisTemplate.opsForZSet().remove(recentSearchKey, 0, size - MAX_RECENT_SEARCH_COUNT - 1);
            }
        }
        catch (Exception e) {
            log.error("Redis 작업 중 오류 발생! 원인: {}", e.getMessage());
        }
    }
    //최근 검색어 목록 조회
    @Override
    public List<String> getRecentSearches(Long memberId) {

        if (!memberRepository.existsById(memberId)) {
            throw new BusinessException(ErrorStatus.MEMBER_NOT_FOUND);
        }

        String recentSearchKey = getRecentSearchKey(memberId);

        try {
            Set<ZSetOperations.TypedTuple<String>> typedTuples =
                    redisTemplate.opsForZSet().reverseRangeWithScores(recentSearchKey, 0, -1);

            if (typedTuples == null || typedTuples.isEmpty()) {
                return List.of();
            }

            return typedTuples.stream()
                    .map(ZSetOperations.TypedTuple::getValue)
                    .collect(Collectors.toList());
        }catch (Exception e) {
            throw new BusinessException(ErrorStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 최근 검색어 삭제
    @Override
    public void deleteRecentSearch(Long memberId, String keyword) {

        if (!memberRepository.existsById(memberId)) {
            throw new BusinessException(ErrorStatus.MEMBER_NOT_FOUND);
        }

        String recentSearchKey = getRecentSearchKey(memberId);

        try {
            Long removedCount = redisTemplate.opsForZSet().remove(recentSearchKey, keyword);

            if (removedCount == null || removedCount == 0) {
                throw new BusinessException(ErrorStatus.SEARCH_NOT_FOUND);
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(ErrorStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
