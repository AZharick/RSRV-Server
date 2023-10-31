import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Calendar;
import java.util.Date;

public class Server {
   private static final int PORT = 9999;
   ServerSocket serverSocket;
   Socket clientSocket;
   private Map<String, Handler> getHandlers;
   private Map<String, Handler> postHandlers;

   public Server() {
      getHandlers = new HashMap<>();
      postHandlers = new HashMap<>();
   }

   public void addHandler(String method, String path, Handler handler) {
      if (method.equals("GET")) {
         getHandlers.put(path, handler);
         System.out.println(getDateAndTime() + "> GET-handler added");
      }
   }

   public void start() throws IOException {
      serverSocket = new ServerSocket(PORT);
      System.out.println(getDateAndTime() + "Server started at port " + PORT);

      while (true) {
         clientSocket = serverSocket.accept();
         System.out.println(getDateAndTime() + "Client connected: " + clientSocket.getInetAddress().getHostAddress());
         handleRequest(clientSocket);
      }
   }

   private void handleRequest(Socket socket) throws IOException {
      while (true) {
         try (
                 final var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 final var out = new BufferedOutputStream(socket.getOutputStream());
         ) {
            System.out.println(getDateAndTime() + "> reading request...");
            String requestLine = in.readLine();
            // Разбираем запрос для получения HTTP-метода и пути
            String[] parts = requestLine.split(" ");
            String method = parts[0];
            String path = parts[1];
            Request currentRequest = new Request();
            currentRequest.setMethod(method);
            currentRequest.setPath(path);
            System.out.println("m: " + method + "; path: " + path);

//            int c;
//            StringBuilder wholeRequest = new StringBuilder();
//            while((c=in.read()) != -1) {
//               wholeRequest.append((char) c);
//            }
//            System.out.println(getDateAndTime() + "> wholeRequest:\n\n" + wholeRequest);
//
//            Request currentRequest = parseHttpRequest(wholeRequest.toString());

            if (currentRequest.getMethod().equals("GET") && getHandlers.containsKey(currentRequest.getPath())
            ) {
               Handler handler = getHandlers.get(currentRequest.getPath());
               handler.handle(currentRequest, out);

            } else {
               System.out.println(getDateAndTime() + "> 404");
               out.write(("HTTP/1.1 404 Not Found\r\n" +
                       "Content-Length: 0\r\n" +
                       "Connection: close\r\n" +
                       "\r\n").getBytes());
            }
            out.flush();
            return;        //PADLA!!!!
         }//try-with-res
      }//while
   }//handleRequest

   public static Request parseHttpRequest(String httpRequest) {
      System.out.println(getDateAndTime() + "> parsing req started...");
      Request request = new Request();
      String[] lines = httpRequest.split("\n");

      // Method, path
      String[] firstLineTokens = lines[0].split(" ");
      request.setMethod(firstLineTokens[0]);
      request.setPath(firstLineTokens[1]);

      // Headers
      Map<String, String> headers = new HashMap<>();
      for (int i = 1; i < lines.length; i++) {
         String line = lines[i];

         // Body
         if (line.isEmpty()) {
            StringBuilder body = new StringBuilder();
            for (int j = i + 1; j < lines.length; j++) {
               body.append(lines[j]);
            }
            request.setBody(body.toString());
            break;
         } else {
            String[] headerTokens = line.split(": ");
            headers.put(headerTokens[0], headerTokens[1]);
         }
      }
      request.setHeaders(headers);
      return request;
   }

   private static String getDateAndTime() {
      String datePattern = "[HH:mm:ss] ";
      DateFormat d = new SimpleDateFormat(datePattern);
      Date today = Calendar.getInstance().getTime();
      String str = d.format(today);
      return str;
   }

}//Server