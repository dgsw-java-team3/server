package com.todayschool.global.entity;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

@DataJpaTest
class BaseTimeEntityTest {

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("저장하면 createdAt과 updatedAt이 자동으로 채워집니다")
    void timestampsAreSetOnPersist() {
        TimeProbe probe = new TimeProbe("처음");

        entityManager.persist(probe);
        entityManager.flush();

        assertThat(probe.getCreatedAt()).isNotNull();
        assertThat(probe.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("수정해도 createdAt은 바뀌지 않고 updatedAt만 갱신됩니다")
    void onlyUpdatedAtChangesOnUpdate() throws InterruptedException {
        TimeProbe probe = new TimeProbe("처음");
        entityManager.persist(probe);
        entityManager.flush();
        LocalDateTime createdAt = probe.getCreatedAt();
        LocalDateTime firstUpdatedAt = probe.getUpdatedAt();

        // 두 시각이 같게 기록되지 않도록 잠시 기다립니다.
        Thread.sleep(10);
        probe.rename("수정");
        entityManager.flush();

        assertThat(probe.getCreatedAt()).isEqualTo(createdAt);
        assertThat(probe.getUpdatedAt()).isAfter(firstUpdatedAt);
    }

    /** 테스트용 엔티티입니다. */
    @Entity
    static class TimeProbe extends BaseTimeEntity {

        @Id
        @GeneratedValue
        private Long id;

        private String name;

        protected TimeProbe() {
        }

        TimeProbe(String name) {
            this.name = name;
        }

        void rename(String name) {
            this.name = name;
        }
    }
}
