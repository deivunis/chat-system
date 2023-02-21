import java.io.BufferedReader;

public class GautiServerioInfo extends Thread {
    BufferedReader in;
    public GautiServerioInfo(BufferedReader sk) {
        in = sk;
    }

    @Override
    public void run() {
        super.run();

        try {
            while (true) {

                System.out.println(in.readLine());
            }
        } catch (Exception e){
            System.out.println("Serverio klaida!");
        }
    }
}
