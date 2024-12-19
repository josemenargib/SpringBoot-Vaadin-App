package com.primefactorsolutions.service;

import com.primefactorsolutions.model.Document;
import com.primefactorsolutions.model.DocumentType;
import com.primefactorsolutions.model.Employee;
import com.primefactorsolutions.repositories.DocumentRepository;
import lombok.AllArgsConstructor;
import org.apache.commons.beanutils.BeanComparator;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class DocumentService {
    private final DocumentRepository documentRepository;

    public void saveDocument(final Document newDocument) {
        documentRepository.save(newDocument);
    }

    public void deleteDocument(final UUID id) {
        documentRepository.deleteById(id);
    }

    public List<Document> getAllDocuments() {
        return documentRepository.findAll();
    }

    public Document getDocument(final UUID id) {
        Optional<Document> employee = documentRepository.findById(id);
        return employee.orElse(null);
    }

    public List<Document> findDocuments(
            final int start, final int pageSize, final String sortProperty, final boolean asc) {
        List<Document> documents = documentRepository.findAll();

        int end = Math.min(start + pageSize, documents.size());
        documents.sort(new BeanComparator<>(sortProperty));

        if (!asc) {
            Collections.reverse(documents);
        }

        return documents.subList(start, end);
    }

    public List<Document> findDocuments(final int start, final int pageSize) {
        List<Document> employees = documentRepository.findAll();

        int end = Math.min(start + pageSize, employees.size());
        return employees.subList(start, end);
    }

    public List<Document> findDocumentBy(final DocumentType documentType,
                                         final Employee employee,
                                         final int start,
                                         final int pageSize) {
        List<Document> documents = documentRepository.findAll();
        if (documentType != null) {
            documents = documents.stream()
                    .filter(doc -> doc.getDocumentType().equals(documentType))
                    .collect(Collectors.toList());
        }
        if (employee != null) {
            documents = documents.stream()
                    .filter(doc -> doc.getEmployee().equals(employee))
                    .collect(Collectors.toList());
        }
        int end = Math.min(start + pageSize, documents.size());
        return documents.subList(start, end);
    }
}
