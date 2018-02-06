package com.codewalnut.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * Created by Weway-RG001 on 2018-02-05.
 */
public interface FileTaskRepository extends JpaRepository<FileTask, Integer> {
    @Query("SELECT t FROM FileTask t WHERE t.height = (SELECT MAX(t1.height) FROM FileTask t1 WHERE t1.htime IS NULL AND t1.ftime IS NULL)")
    FileTask getMaxFileTask();

    @Query("SELECT t FROM FileTask t WHERE t.height = (SELECT MIN(t1.height) FROM FileTask t1 WHERE t1.htime IS NULL AND t1.ftime IS NULL)")
    FileTask getMinFileTask();

}
