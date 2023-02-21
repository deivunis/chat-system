public class Gija extends Thread {

    @Override
    public void run() {
        super.run();

        int i = 0;
        try {
            while (true) {
                System.out.println("Skaicius " + this.getName() + " " + i);
                i++;
                long laikas = (long) (Math.random() * 3000);
                Thread.sleep(laikas);
            }
        } catch (Exception e) {
            System.out.println("Klaida");
        }
    }
    public static void labas() {
        System.out.println("Labas");
    }
}
