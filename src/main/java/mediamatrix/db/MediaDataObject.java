package mediamatrix.db;

import java.io.File;

public class MediaDataObject {

    private String name;
    private String id;
    private String entityID;

    public MediaDataObject(String id) {
        this.id = id;
        this.entityID = id;
        if (new File(id).exists()) {
            this.name = new File(id).getName();
        }
    }

    public MediaDataObject(String id, String entityID) {
        this.id = id;
        this.name = entityID;
        this.entityID = entityID;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEntityID() {
        return entityID;
    }

    public void setEntityID(String entityID) {
        this.entityID = entityID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MediaDataObject other = (MediaDataObject) obj;
        if (this.id != other.id && (this.id == null || !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 61 * hash + (this.id != null ? this.id.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return name;
    }
}
