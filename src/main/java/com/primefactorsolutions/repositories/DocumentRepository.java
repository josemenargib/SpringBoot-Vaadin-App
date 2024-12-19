package com.primefactorsolutions.repositories;

import com.primefactorsolutions.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DocumentRepository  extends JpaRepository<Document, UUID> {
}
