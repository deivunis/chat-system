import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.*;

public class KlientoGija extends Thread {
    Socket clientSocket = null;
    ArrayList<KlientoGija> sar = new ArrayList();
    PrintWriter out = null;
    String nickname = null;
    String role = null;
    private boolean isMuted;
    private Timer muteTimer;
    private boolean isPollActive;
    private String pollQuestion;
    private HashMap<String, Integer> pollOptions;
    private boolean isVoted;

    public KlientoGija(Socket c, ArrayList<KlientoGija> s, String r, Boolean iPA, String pQ, HashMap<String, Integer> pO) {
        clientSocket = c;
        nickname = "User" + c.getPort();
        sar = s;
        role = r;
        this.isMuted = false;
        isPollActive = iPA;
        pollQuestion = pQ;
        pollOptions = pO;
        this.isVoted = false;
    }
    @Override
    public void run() {
        super.run();
        try {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            String inputLine = null;
            String outputLine = "";
            while ((inputLine = in.readLine()) != null) {
                //outputLine = "Gavome is jusu zinute '" + inputLine + "'";
                //out.println(outputLine);

                boolean nutraukti = logikaIrSiuntimas(inputLine);
                if(nutraukti) {
                    break;
                }
            }
        } catch (Exception e) {
            System.out.println("Klaida");
        }
    }
    public void siustiZinute(String txt) {
        for(int i = 0; i < sar.size(); i++) {
            KlientoGija dabartinis = sar.get(i);
            dabartinis.out.println("[VIESA] " + nickname + ":" + txt);
        }
    }
    public void updateClient(boolean poll, String questions, HashMap<String, Integer> options) {
        for(int i = 0; i < sar.size(); i++) {
            KlientoGija dabartinis = sar.get(i);
            dabartinis.isPollActive = poll;
            dabartinis.pollQuestion = questions;
            dabartinis.pollOptions = options;
        }
    }

    public boolean logikaIrSiuntimas(String komanda) {
        //logai
        rasytiIFaila(" <- " + nickname + ": " + komanda);

        //komandos
        if(komanda.charAt(0) == '/' && komanda.indexOf('/') == komanda.lastIndexOf('/')) {
            if(komanda.equals("/leave")) {
                leave();
                return true;
            }

            else if(komanda.equals("/help"))
                help();

            else if(komanda.equals("/timestamp"))
                timestamp();

            else if(komanda.equals("/list"))
                list();

            else if(komanda.contains("/private ")) {
                if(!isMuted)
                    privateMsg(komanda);
                else
                    out.println("Jus esate uztildytas!");
            }

            else if(komanda.contains("/nickname "))
                changeNickname(komanda);

            else if(komanda.contains("/kick ")) {
                if(role.equals("Host")) kickUser(komanda);
                else out.println("Vartotojus ismesti gali tik Host!");
            }

            else if(komanda.contains("/mute ")) {
                if(role.equals("Host")) muteUser(komanda);
                else out.println("Vartotojus uztildyti gali tik Host!");
            }

            else if (komanda.startsWith("/createpoll:")) {
                if(!isPollActive){
                    if(role.equals("Host")) createPoll(komanda);
                    else out.println("Tik Host gali kurti apklausas!");
                } else out.println("Siuo metu yra aktyvi apklausa!");
            }

            else if (komanda.startsWith("/vote")) {
                if(isPollActive)
                    voteForPoll(komanda);
                    else out.println("Siuo metu nera aktyvios apklausos!");
            }

            else if (komanda.equals("/pollresults")) {
                if(isPollActive)
                    pollResults();
                else out.println("Siuo metu nera aktyvios apklausos!");
            }

            else if(komanda.equals("/closepoll")) {
                if(isPollActive) {
                    if(role.equals("Host")) {
                        closePoll();
                    } else
                        out.println("Tik Host gali uzdaryti apklausa!");
                } else
                    out.println("Nera atidarytu apklausu!");

            }

            else out.println("Blogai ivesta komanda! Daugiau informacijos - /help");
        } else {
            //pagrindinis komunikavimas
            if(!isMuted)
                siustiZinute(komanda);
            else
                out.println("Jus esate uztildytas!");
        }
        return false;
    }
    public KlientoGija gautiKlienta(String nick) {
        for(int i=0; i< sar.size(); i++) {
            if(sar.get(i).nickname.equals(nick)) {
                return sar.get(i);
            }
        }
        return null;
    }
    public KlientoGija gautiNaujaHost(String nick) {
        for(int i=0; i< sar.size(); i++) {
            if(sar.get(i).nickname.equals(nick)) {
                i++;
                return sar.get(i);
            }
        }
        return null;
    }
    public synchronized void rasytiIFaila(String txt) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("log.txt", true));
            LocalDateTime myObj = LocalDateTime.now();
            System.out.println(myObj);
            writer.write(myObj + ": " + txt + "\n");
            writer.close();
        } catch (Exception e) {
            System.out.println("Klaida rasant i faila!");
        }
    }
    //komandos
    public void leave() {
        if(role.equals("Host")) {
            KlientoGija kamPerleisti = gautiNaujaHost(nickname);
            kamPerleisti.role = "Host";
        }
        sar.remove(this);
        System.out.println("Isejo vartotojas. Siuo metu sistemoje yra " + sar.size() + " klientu");
        siustiZinute( " atsijunge.");
        rasytiIFaila(nickname + "ATSIJUNGE");
    }
    public void help() {
        out.println("Galimu komandu sarasas: \n" +
                "/help - atspausdinti komandu sarasa;\n" +
                "/leave - atsijungti;\n" +
                "/list - atspausdinti prisijungusiu vartotoju sarasa;\n" +
                "/timestamp - ijungti laiko rodyma prie zinuciu;\n" +
                "/private [Vartotojo vardas] [Zinute] - issiusti privacia zinute;\n" +
                "/nickname [Naujas slapyvardis] - pakeisti savo vartotojo varda;\n" +
                "/kick [Vartotojo vardas] - ismesti vartotoja is pokalbiu;\n" +
                "/mute [Vartotojo vardas] [Sekundes] - uztildyti vartotoja;\n" +
                "/createpoll:klausimas:pasirinkimas1:pasirinkimas2 - sukurti apklausa;\n" +
                "/vote [pasirinkimas] - prabalsuoti;\n" +
                "/pollresults - parodyti apklausos rezultatus;\n" +
                "/closepoll - uzdaryti apklausa.");
    }
    public void timestamp() {
        LocalDateTime myObj = LocalDateTime.now();
        out.println(myObj);
    }
    public void list() {
        String bendras = "";
        for(int i=0; i<sar.size(); i++) {
            bendras += " [" + sar.get(i).role + "] " + sar.get(i).nickname + ";";
        }
        out.println("Prisijungusiu sarasas:" + bendras);
        rasytiIFaila(" -> " + nickname + ": Vartotoju sarasas: " + bendras);
    }
    public void privateMsg(String input) {
        String[] daly = input.split(" ", 3);
        String user = daly[1];
        String msg = daly[2];
        KlientoGija kamSiusti = gautiKlienta(user);
        if(kamSiusti != null) {
            kamSiusti.out.println("[PRIVATE] " + nickname + ": " + msg);
        } else {
            out.println("Tokio vartotojo nera arba jis ka tik atsijunge!");
        }
    }
    public void changeNickname(String input) {
        String[] daly = input.split(" ");
        String user = daly[1];
        KlientoGija kamSiusti = gautiKlienta(user);
        if(kamSiusti != null) {
            out.println("Toks vartotojo vardas jau yra! \nPrasome pasirinkti kita.");
        } else {
            nickname = user;
            out.println("Jusu vartotojo vardas sekmingai pakeistas.");
        }
    }
    public void kickUser(String input) {
        String[] daly = input.split(" ");
        String user = daly[1];
        KlientoGija kamSiusti = gautiKlienta(user);
        if(kamSiusti != null) {
            siustiZinute(" ismete vartotoja '" + user + "' is pokalbiu kambario!");
            sar.remove(kamSiusti);
            System.out.println("Ismestas vartotojas. Siuo metu sistemoje yra " + sar.size() + " klientu");
            rasytiIFaila(nickname + "ismestas");
        } else {
            out.println("Tokio vartotojo nera arba jis ka tik atsijunge!");
        }
    }
    public void muteUser(String input) {
        String[] daly = input.split(" ", 3);
        String user = daly[1];
        int muteTimeInSeconds = Integer.parseInt(daly[2]);
        KlientoGija kamSiusti = gautiKlienta(user);

        if(kamSiusti != null) {
            if (muteTimer != null) {
                muteTimer.cancel();
            }
            kamSiusti.isMuted = true;
            kamSiusti.out.println("[PRIVATE] Jus uztilde " + nickname + ": " + muteTimeInSeconds + " sekundziu laikotarpiui!");
            siustiZinute(" uztilde vartotoja '" + user + "' " + muteTimeInSeconds + " sekundziu.");

            muteTimer = new Timer();
            muteTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    kamSiusti.isMuted = false;
                    kamSiusti.out.println("Jus buvote atitildytas.");
                }
            }, muteTimeInSeconds * 1000);
        } else {
            out.println("Tokio vartotojo nera arba jis ka tik atsijunge!");
        }
    }
    public void createPoll(String input) {
        String[] pollArgs = input.split(":");
        if (pollArgs.length >= 3) {
            pollQuestion = pollArgs[1];
            for (int i = 2; i < pollArgs.length; i++) {
                pollOptions.put(pollArgs[i], 0);
            }
            isPollActive = true;
            siustiZinute("Apklausa sukurta: " + pollQuestion);
            siustiZinute("Apklausos pasirinkimai:");
            for (String option : pollOptions.keySet()) {
                siustiZinute("- " + option);
            }
            updateClient(isPollActive, pollQuestion, pollOptions);
        } else {
            siustiZinute("Netinkamas formatas! Naudojimas: /createpoll:klausimas:pasirinkimas1:pasirinkimas2..");
        }
    }
    public void voteForPoll(String input) {
        if(!isVoted) {
            String[] voteArgs = input.split(" ");
            if (voteArgs.length == 2 && pollOptions.containsKey(voteArgs[1])) {
                pollOptions.put(voteArgs[1], pollOptions.get(voteArgs[1]) + 1);
                siustiZinute("prabalsavo: " + voteArgs[1]);
                updateClient(isPollActive, pollQuestion, pollOptions);
                this.isVoted = true;
            } else {
                siustiZinute("Netinkamas apklausos pasirinkimas!");
            }
        } else
            out.println("Jau balsavote.");

    }
    public void pollResults(){
        out.println("Apklausos rezultatai:");
        for (String option : pollOptions.keySet()) {
            out.println("- " + option + ": " + pollOptions.get(option));
        }
    }
    public void closePoll() {
        siustiZinute("Apklausa uzdaryta");
        pollResults();
        isPollActive = false;
        pollQuestion = "";
        pollOptions = new HashMap<String, Integer>();
        updateClient(isPollActive, pollQuestion, pollOptions);
    }

}
