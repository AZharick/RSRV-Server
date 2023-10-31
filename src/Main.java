import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {
   public static void main(String[] args) throws IOException {
      Server server = new Server();

      server.addHandler("GET", "/index.html", (request, responseStream) -> {
         final var filePath = Path.of(".", "public", "/index.html");
         final var mimeType = Files.probeContentType(filePath);
         final var length = Files.size(filePath);
         String response = "HTTP/1.1 200 OK\r\n" +
                 "Content-Type: " + mimeType + "\r\n" +
                 "Content-Length: " + length + "\r\n" +
                 "Connection: close\r\n" +
                 "\r\n";
         String response2 = "HTTP/1.1 200 OK\r\n" +
                 "Content-Type: text/plain\r\n" +
                 "\r\n" +
                 "Hello, World!";
         responseStream.write(response.getBytes());
         Files.copy(filePath, responseStream);
         responseStream.flush();
      });

      server.start();
   }
}