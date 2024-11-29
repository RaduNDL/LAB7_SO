import java.io.*;
import java.util.concurrent.*;
public class ProcessAccess {
    // Semafor pentru controlul accesului între procesele albe si negre
    private static final Semaphore mutex = new Semaphore(1);
    private static final Semaphore whiteQueue = new Semaphore(1);
    private static final Semaphore blackQueue = new Semaphore(1);
    private static boolean lastWasWhite = false;
    // Funcția pentru a lansa un proces alb extern
    public static void runWhiteProcess() throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder("java", "-cp", ".", "WhiteProcess");
        Process process = processBuilder.start();
        process.waitFor();  // Asteptam ca procesul alb sa termine
    }
    // Funcția pentru a lansa un proces negru extern
    public static void runBlackProcess() throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder("java", "-cp", ".", "BlackProcess");
        Process process = processBuilder.start();
        process.waitFor();  // Asteptam ca procesul negru sa termine
    }
    // Accesarea resursei de catre fire alb si negru
    public static void accessResource(String threadType) throws InterruptedException, IOException {
        if ("white".equals(threadType)) {
            whiteQueue.acquire();
            if (!lastWasWhite) {
                mutex.acquire();
            }
            // Crearea si rularea procesului alb
            System.out.println("Firul alb acceseaza resursa.");
            runWhiteProcess();  // Rulam procesul alb
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
            runBlackProcess();  // Rulam procesul negru
            System.out.println("Firul negru a terminat utilizarea resursei.");

            lastWasWhite = false;
            mutex.release();
            blackQueue.release();
        }
    }
    public static void main(String[] args) throws InterruptedException, IOException {
        ProcessAccess processAccess = new ProcessAccess();
        Thread whiteThread1 = new Thread(() -> {
            try {
                processAccess.accessResource("white");
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        });
        Thread whiteThread2 = new Thread(() -> {
            try {
                processAccess.accessResource("white");
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        });
        Thread blackThread1 = new Thread(() -> {
            try {
                processAccess.accessResource("black");
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        });
        Thread blackThread2 = new Thread(() -> {
            try {
                processAccess.accessResource("black");
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        });
        whiteThread1.start();
        whiteThread2.start();
        blackThread1.start();
        blackThread2.start();
        try {
            whiteThread1.join();
            whiteThread2.join();
            blackThread1.join();
            blackThread2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
