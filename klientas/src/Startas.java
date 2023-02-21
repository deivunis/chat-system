import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Startas {
    public static void main(String[] args) {
        String hostName = "127.0.0.1";
        int portNumber = 1234;

        try {
            Socket echoSocket = new Socket(hostName, portNumber);
            PrintWriter out = new PrintWriter(echoSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));
            BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));

            GautiServerioInfo gijaSkaitymui = new GautiServerioInfo(in);
            gijaSkaitymui.start();

            String userInput;
            while ((userInput = stdIn.readLine()) != null) {
                out.println(userInput);
            }

        } catch(Exception e) {
            System.out.println("Nepavyko prisijungti");
            e.printStackTrace();
        }
    }
}
