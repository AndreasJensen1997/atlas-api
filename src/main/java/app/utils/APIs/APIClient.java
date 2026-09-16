package app.utils.APIs;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class APIClient {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static <T> T getApiAsDTO(String url, Class<T> tclass) {
        try {
            JsonNode node = objectMapper.readTree(new URI(url).toURL().openStream());
            return objectMapper.treeToValue(node, tclass);
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}