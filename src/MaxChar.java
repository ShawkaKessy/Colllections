import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class MaxChar {

    private static final int TEXT_COUNT = 10_000;
    private static final int TEXT_LENGTH = 100_000;
    private static final int QUEUE_CAPACITY = 100;

    private static final BlockingQueue<String> queueA = new ArrayBlockingQueue<>(QUEUE_CAPACITY);
    private static final BlockingQueue<String> queueB = new ArrayBlockingQueue<>(QUEUE_CAPACITY);
    private static final BlockingQueue<String> queueC = new ArrayBlockingQueue<>(QUEUE_CAPACITY);

    private static String maxTextA = "";
    private static String maxTextB = "";
    private static String maxTextC = "";
    private static int maxCountA = 0;
    private static int maxCountB = 0;
    private static int maxCountC = 0;

    public static void main(String[] args) throws InterruptedException {
        Thread producer = new Thread(() -> {
            try {
                for (int i = 0; i < TEXT_COUNT; i++) {
                    String text = generateText("abc", TEXT_LENGTH);
                    queueA.put(text);
                    queueB.put(text);
                    queueC.put(text);
                }
                queueA.put("END");
                queueB.put("END");
                queueC.put("END");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        Thread consumerA = new Thread(() -> processQueue(queueA, 'a'));
        Thread consumerB = new Thread(() -> processQueue(queueB, 'b'));
        Thread consumerC = new Thread(() -> processQueue(queueC, 'c'));

        producer.start();
        consumerA.start();
        consumerB.start();
        consumerC.start();

        producer.join();
        consumerA.join();
        consumerB.join();
        consumerC.join();

        System.out.printf("Строка с максимальным количеством 'a': %d символов\n", maxCountA);
        System.out.printf("Строка с максимальным количеством 'b': %d символов\n", maxCountB);
        System.out.printf("Строка с максимальным количеством 'c': %d символов\n", maxCountC);
    }

    public static String generateText(String letters, int length) {
        Random random = new Random();
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < length; i++) {
            text.append(letters.charAt(random.nextInt(letters.length())));
        }
        return text.toString();
    }

    private static void processQueue(BlockingQueue<String> queue, char targetChar) {
        try {
            while (true) {
                String text = queue.take();
                if ("END".equals(text)) {
                    break;
                }
                int count = countChar(text, targetChar);
                synchronized (MaxChar.class) {
                    if (targetChar == 'a' && count > maxCountA) {
                        maxCountA = count;
                        maxTextA = text;
                    } else if (targetChar == 'b' && count > maxCountB) {
                        maxCountB = count;
                        maxTextB = text;
                    } else if (targetChar == 'c' && count > maxCountC) {
                        maxCountC = count;
                        maxTextC = text;
                    }
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static int countChar(String text, char targetChar) {
        int count = 0;
        for (char c : text.toCharArray()) {
            if (c == targetChar) {
                count++;
            }
        }
        return count;
    }
}
