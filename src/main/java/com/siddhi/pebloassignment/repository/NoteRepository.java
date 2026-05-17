package com.siddhi.pebloassignment.repository;

import com.siddhi.pebloassignment.model.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NoteRepository extends JpaRepository<Note, String> {

    // Fetch all active (non-archived) notes for a specific user, ordered by latest update
    List<Note> findByUserIdAndArchivedFalseOrderByUpdatedAtDesc(String userId);

    // Find a public note via its unique shared token
    Optional<Note> findByShareTokenAndArchivedFalse(String shareToken);

    // Full text search across title and content for a specific user's active notes
    @Query("SELECT n FROM Note n WHERE n.user.id = :userId AND n.archived = false " +
            "AND (LOWER(n.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(n.content) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Note> searchNotes(@Param("userId") String userId, @Param("keyword") String keyword);
}