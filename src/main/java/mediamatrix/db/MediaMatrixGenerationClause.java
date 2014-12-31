package mediamatrix.db;

import java.io.IOException;

public class MediaMatrixGenerationClause {

    private final String name;

    public MediaMatrixGenerationClause(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public MediaMatrix eval(MediaDataObject obj, PrimitiveEngine pe) throws IOException, MediaMatrixException {
        if (name.equals("NULL")) {
            return pe.any();
        } else {
            return pe.open(obj, name);
        }
    }

    @Override
    public String toString() {
        return "Generation(" + name + ")";
    }
}
