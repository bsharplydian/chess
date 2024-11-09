import chess.*;
import client.REPL;

public class Main {
    public static void main(String[] args) {

        System.out.println("♕ 240 Chess Client");
        REPL repl = new REPL("http://localhost:8080");

        repl.run();
    }
}