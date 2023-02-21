import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class Paleisti {
    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(1234);
            ArrayList<KlientoGija> sarasas = new ArrayList();
            String role;
            boolean isPollActive = false;
            String pollQuestion = null;
            HashMap<String, Integer> pollOptions = new HashMap<>();
            while (true) {
                Socket clientSocket = serverSocket.accept();
                if(sarasas.isEmpty()) {
                    role = "Host";
                } else {
                    role = "Participant";
                }
                KlientoGija naujas = new KlientoGija(clientSocket, sarasas, role, isPollActive, pollQuestion, pollOptions);
                sarasas.add(naujas);
                System.out.println("Siuo metu sistemoje yra " + sarasas.size() + " klientu");
                naujas.start();
            }

        } catch (Exception e) {
            System.out.println("Klaida");
            e.printStackTrace();
        }
    }
}
