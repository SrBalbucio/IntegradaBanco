package balbucio.banco.listener;

import balbucio.responsivescheduler.event.Listener;
import balbucio.responsivescheduler.event.impl.*;

public class TaskListener implements Listener {
    @Override
    public void asyncTaskStarted(AsyncTaskStartedEvent asyncTaskStartedEvent) {

    }

    @Override
    public void asyncTaskFinished(AsyncTaskFinishedEvent asyncTaskFinishedEvent) {

    }

    @Override
    public void taskStatedEvent(TaskStartedEvent taskStartedEvent) {

    }

    @Override
    public void taskFinishedEvent(TaskFinishedEvent taskFinishedEvent) {
        taskFinishedEvent.rerun();
        System.out.println("Alguma task parou!");
    }

    @Override
    public void taskProblemEvent(TaskProblemEvent taskProblemEvent) {
        System.out.println(taskProblemEvent.getProblemID());
    }

    @Override
    public void scheduledTask(ScheduledTaskEvent scheduledTaskEvent) {

    }
}
