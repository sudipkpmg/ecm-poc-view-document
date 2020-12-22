package gov.tn.dhs.ecm.service;

import com.box.sdk.BoxAPIException;
import com.box.sdk.BoxDeveloperEditionAPIConnection;
import com.box.sdk.BoxFile;
import gov.tn.dhs.ecm.model.DocumentViewRequest;
import gov.tn.dhs.ecm.model.DocumentViewResult;
import gov.tn.dhs.ecm.util.ConnectionHelper;
import gov.tn.dhs.ecm.util.JsonUtil;
import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URL;

@Service
public class DocumentViewService extends BaseService {

    private static final Logger logger = LoggerFactory.getLogger(DocumentViewService.class);

    public DocumentViewService(ConnectionHelper connectionHelper) {
        super(connectionHelper);
    }

    public void process(Exchange exchange) {
        DocumentViewRequest documentViewRequest = exchange.getIn().getBody(DocumentViewRequest.class);
        String documentId = documentViewRequest.getDocumentId();
        String appUserId = documentViewRequest.getAppUserId();
        logger.info("Document View request received with payload {}", JsonUtil.toJson(documentViewRequest));

        try {
            BoxDeveloperEditionAPIConnection api = getBoxApiConnection();
//            api.asUser(appUserId);
            BoxFile file = new BoxFile(api, documentId);
            URL previewUrl = file.getPreviewLink();
            DocumentViewResult documentViewResult = new DocumentViewResult();
            documentViewResult.setPreviewUrl(previewUrl.toString());
            logger.info("Document View success response is {}", JsonUtil.toJson(documentViewResult));
            setupResponse(exchange, "200", documentViewResult, DocumentViewResult.class);
        } catch (BoxAPIException e) {
            logger.error(e.getMessage());
            switch (e.getResponseCode()) {
                case 404: {
                    setupError("409", "Document not found");
                }
                default: {
                    setupError("500", "Document view error");
                }
            }
        }
    }

}



