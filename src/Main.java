public class Main {

    private Map map;

    public static void main(String[] args) {
        Main main = new Main();
        main.start();
    }

    public Main() {}

    public void start() {
        this.map = new Map(123456789);
    }
}
