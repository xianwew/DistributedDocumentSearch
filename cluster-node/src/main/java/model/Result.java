package model;

import model.proto.SearchModel;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Result implements Serializable {
    private final Map<String, DocumentData> documentToDocumentData = new HashMap<>();

    public void addDocumentData(String document, DocumentData documentData) {
        documentToDocumentData.put(document, documentData);
    }

    public Map<String, DocumentData> getDocumentToDocumentData() {
        return Collections.unmodifiableMap(documentToDocumentData);
    }

    public static Result fromProto(SearchModel.Response response) {
        Result result = new Result();
        for (SearchModel.Response.DocumentStats docStats : response.getRelevantDocumentsList()) {
            DocumentData documentData = new DocumentData();
            documentData.putTermFrequency("score", docStats.getScore());
            documentData.putTermFrequency("documentSize", (double) docStats.getDocumentSize());
            if (docStats.hasAuthor()) {
                documentData.putTermFrequency("author", docStats.getAuthor().hashCode());
            }
            result.addDocumentData(docStats.getDocumentName(), documentData);
        }
        return result;
    }
}
