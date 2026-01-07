package com.root.meetsync.repository.event;

import com.root.meetsync.entity.Event;
import com.root.meetsync.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EventTestRepository extends JpaRepository <Event , Long>{
    List<Event> findAllByHost(User host);
    Optional<Event> findByIdAndHost(Long id, User host);
}
