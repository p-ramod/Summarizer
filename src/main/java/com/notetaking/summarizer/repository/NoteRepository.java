package com.notetaking.summarizer.repository;

import com.notetaking.summarizer.entity.NoteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NoteRepository extends JpaRepository<NoteEntity, Long> {

    List<NoteEntity> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<NoteEntity> findByIdAndUserId(Long id, Long userId);
}