package com.primefactorsolutions.model;

import jakarta.persistence.*;

import java.util.UUID;

@MappedSuperclass
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Version
    private int version;

    public UUID getId() {
        return id;
    }

    public void setId(final UUID id) {
        this.id = id;
    }

    public int getVersion() {
        return version;
    }

    @Override
    public int hashCode() {
        if (getId() != null) {
            return getId().hashCode();
        }
        return super.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof BaseEntity that)) {
            return false; // null or not an AbstractEntity class
        }
        if (getId() != null) {
            return getId().equals(that.getId());
        }
        return super.equals(that);
    }
}
