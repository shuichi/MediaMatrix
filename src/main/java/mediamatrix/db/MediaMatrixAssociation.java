package mediamatrix.db;

public class MediaMatrixAssociation {

    public double offset;
    public String id;

    public MediaMatrixAssociation(double offset, String id) {
        this.offset = offset;
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getOffset() {
        return offset;
    }

    public void setOffset(double offset) {
        this.offset = offset;
    }
}
