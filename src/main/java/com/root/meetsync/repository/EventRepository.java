
package com.root.meetsync.repository;


import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EventRepository extends JpaRepository <Event, Long>{

    Optional<Event> findByShareLink(String shareLink);
}
