package eu.thog92.dramagen.task;

import eu.thog92.dramagen.TasksManager;

public abstract class ScheduledTask implements ITask<Boolean>, Runnable {

    protected boolean isCancelled;
    protected int delay;
    protected TasksManager manager;

    public ScheduledTask(TasksManager manager) {
        this.manager = manager;
    }

    @Override
    public void run() {
        //long startTime = System.currentTimeMillis();
        try {
            if (!this.execute()) {
                this.cancel();
            }
            this.manager.onFinishTask(this);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
        //System.out.println("Finish task " + this.getName() + " in " + (System.currentTimeMillis() - startTime) + "ms.");

    }

    public boolean isCancelled() {
        return isCancelled;
    }

    protected void cancel() {
        this.isCancelled = true;
    }

    public int getDelay() {
        return delay;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    public String getName() {

        return this.getClass().getSimpleName();
    }
}
