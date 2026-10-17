package com.mohaemukzip.mohaemukzip_be.domain.member.event;

import com.mohaemukzip.mohaemukzip_be.domain.member.repository.MemberRepository;
import com.mohaemukzip.mohaemukzip_be.global.client.DiscordMemberNotificationClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
@RequiredArgsConstructor
public class MemberNotificationListener {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private static final int GREEN = 0x57F287;
    private static final int RED = 0xED4245;

    private final MemberRepository memberRepository;
    private final DiscordMemberNotificationClient discordMemberNotificationClient;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleSignUp(MemberSignedUpEvent event) {
        try {
            sendNotification(
                    "🎉 신규 회원 가입",
                    GREEN,
                    event.nickname(),
                    event.loginType().toString(),
                    event.memberId()
            );
        } catch (Exception e) {
            log.error("회원가입 Discord 알림 실패 - memberId: {}",
                    event.memberId(), e);
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleWithdrawal(MemberWithdrawnEvent event) {
        try {
            sendNotification(
                    "👋 회원 탈퇴",
                    RED,
                    event.nickname(),
                    event.loginType().toString(),
                    event.memberId()
            );
        } catch (Exception e) {
            log.error("회원탈퇴 Discord 알림 실패 - memberId: {}",
                    event.memberId(), e);
        }
    }

    private void sendNotification(
            String title,
            int color,
            String nickname,
            String loginType,
            Long memberId
    ) {
        long totalMembers = memberRepository.count();

        String time = LocalDateTime.now(KST).format(FORMATTER);

        discordMemberNotificationClient.sendMemberNotification(
                title,
                color,
                nickname,
                loginType,
                memberId,
                totalMembers,
                time
        );
    }
}
