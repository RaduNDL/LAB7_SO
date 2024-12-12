import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

public class ProcessAccess {
    // Semafor pentru controlul accesului intre procesele albe si negre
    private static final Semaphore mutex = new Semaphore(1);
    private static final Semaphore whiteQueue = new Semaphore(1);
    private static final Semaphore blackQueue = new Semaphore(1);
    private static boolean lastWasWhite = false;

    // Functia pentru a lansa un proces alb extern
    public static void runWhiteProcess() throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder("java", "-cp", ".", "WhiteProcess");
        Process process = processBuilder.start();
        process.waitFor(); // Asteptam ca procesul alb sa termine
    }

    // Functia pentru a lansa un proces negru extern
    public static void runBlackProcess() throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder("java", "-cp", ".", "BlackProcess");
        Process process = processBuilder.start();
        process.waitFor(); // Asteptam ca procesul negru sa termine
    }

    // Accesarea resursei de catre firele albe si negre
    public static void accessResource(String threadType) throws InterruptedException, IOException {
        if ("white".equals(threadType)) {
            whiteQueue.acquire();
            if (!lastWasWhite) {
                mutex.acquire();
            }
            // Crearea si rularea procesului alb
            System.out.println("Firul alb acceseaza resursa.");
            runWhiteProcess();
            System.out.println("Firul alb a terminat utilizarea resursei.");
            lastWasWhite = true;
            mutex.release();
            whiteQueue.release();
        } else if ("black".equals(threadType)) {
            blackQueue.acquire();
            if (lastWasWhite) {
                mutex.acquire();
            }
            // Crearea si rularea procesului negru
            System.out.println("Firul negru acceseaza resursa.");
            runBlackProcess();
            System.out.println("Firul negru a terminat utilizarea resursei.");
            lastWasWhite = false;
            mutex.release();
            blackQueue.release();
        }
    }

    public static void main(String[] args) throws InterruptedException, IOException {
        List<Thread> threads = new ArrayList<>();

        // Crearea si adaugarea firelor in lista
        for (int i = 0; i < 2; i++) { // cate doua fire pentru fiecare culoare
            threads.add(new Thread(() -> {
                try {
                    ProcessAccess.accessResource("white");
                } catch (InterruptedException | IOException e) {
                    e.printStackTrace();
                }
            }));
            threads.add(new Thread(() -> {
                try {
                    ProcessAccess.accessResource("black");
                } catch (InterruptedException | IOException e) {
                    e.printStackTrace();
                }
            }));
        }
        for (Thread thread : threads) {
            thread.start();
        }
        for (Thread thread : threads) {
            thread.join();
        }
    }
}
