package dk.statsbiblioteket.bitrepository.commandline.testutil;

import dk.statsbiblioteket.bitrepository.commandline.action.ClientAction;


/**
 * Helper class to abstract away the complexity of running an action and 
 * waiting for its completion 
 */
public class ActionRunner implements Runnable {
    private final Object finishLock = new Object();
    boolean finished = false;
    ClientAction action;
    
    public ActionRunner(ClientAction action) {
        this.action = action;
    }
    
    public boolean getFinished() {
        return finished;
    }
    
    private void finish() {
        synchronized (finishLock) {
            finished = true;
            finishLock.notifyAll();
        }
    }
    
    public void waitForFinish(long timeout) throws InterruptedException {
        synchronized (finishLock) {
            if(finished == false) {
                finishLock.wait(timeout);
            }
        }
    }
    
    public void run() {
        try {
            action.performAction();
        } catch (Exception e) {
            // Err, not sure if we want to do anything?
        }
        finish();
    }
}
