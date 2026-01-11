package com.mohaemukzip.mohaemukzip_be.domain.Mission.repository;

import com.mohaemukzip.mohaemukzip_be.domain.Mission.entity.Member_Mission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberMissionRepository extends JpaRepository<Member_Mission,Long> {
}
