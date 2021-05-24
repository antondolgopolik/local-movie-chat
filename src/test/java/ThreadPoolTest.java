import by.bsuir.datastructures.SynchronizedLinkedList;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadPoolTest {

    public static void main(String[] args) throws InterruptedException {
        SynchronizedLinkedList<String> list = new SynchronizedLinkedList<>();
        for (int i = 0; i < 100; i++) {
            list.add(String.valueOf(i));
        }

        ExecutorService executorService = Executors.newFixedThreadPool(5);
        Runnable task = () -> {
            while (!Thread.currentThread().isInterrupted()) {
                if (!list.isEmpty()) {
                    // Извлечение
                    String val = list.removeFirst();
                    if (val != null) {
                        // Обработка
                        System.out.println(val);
                        // Помещение
                        list.add(val + "*");
                    }
                }
            }
        };
        for (int i = 0; i < 5; i++) {
            executorService.execute(task);
        }
        Thread.sleep(5000);
        System.err.println("GOT IT!");
        executorService.shutdownNow();
    }
}
