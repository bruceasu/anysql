package com.asql.core.io;

class ConsoleEraser extends Thread {
  private volatile boolean running = true;

  public void run() {
    while (this.running) {
      System.out.print("\b ");
      try {
        sleep(5L);
      } catch (InterruptedException localInterruptedException) {
      }
    }
  }

  public synchronized void halt() {
    this.running = false;
  }
}
