package controller.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import dto.responce.ErrorResponseDTO;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class BaseApiServlet extends HttpServlet {
    private final ObjectMapper objectMapper;

    public BaseApiServlet() {
        this.objectMapper = new ObjectMapper();
    }

    protected void writeResponse(HttpServletResponse resp, Object whatToSerialize, int status) throws IOException {
        resp.setStatus(status);
        String whatToWrite = objectMapper
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(whatToSerialize);
        resp.getWriter().write(whatToWrite);
    }

    protected void writeError(HttpServletResponse resp, String message, int status) throws IOException {
        resp.setStatus(status);
        String errorJson = objectMapper.writeValueAsString(
                new ErrorResponseDTO(message)
        );
        resp.getWriter().write(errorJson);
    }

    protected void write500Error(HttpServletResponse resp, Exception e) throws IOException {
        writeError(resp, "internal server error", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        System.err.println(e.getMessage());
        e.printStackTrace();
    }
}
