package it.uniparthenope.GribDroid;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Scheduler implements Runnable {

    private BlockingQueue queue = new ArrayBlockingQueue(32);
    private Thread thread;

    private static Scheduler scheduler=null;

    public static Scheduler getInstance() {
        if (scheduler==null) {
            scheduler=new Scheduler();
        }
        return scheduler;
    }

    private Scheduler() {
        thread=new Thread(this);
        thread.start();
    }

    public void submit(Worker worker) throws InterruptedException {
        queue.put(worker);
    }

    @Override
    public void run() {
        while (true) {
            while (queue.size()>0) {
                try {
                    Worker worker=(Worker) queue.take();
                    worker.start();
                } catch (InterruptedException e) {

                }

            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {

            }
        }
    }
}
