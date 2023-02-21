public class Main {
    public static void main(String[] args) {
        Gija g = new Gija();
        g.start();
        Gija g2 = new Gija();
        g2.start();
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {

        }
        g.interrupt();
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {

        }
        g2.interrupt();
    }
}