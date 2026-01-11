package com.mohaemukzip.mohaemukzip_be.domain.Mission.repository;

import com.mohaemukzip.mohaemukzip_be.domain.Mission.entity.Mission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MissionRepository extends JpaRepository<Mission,Long> {
}
