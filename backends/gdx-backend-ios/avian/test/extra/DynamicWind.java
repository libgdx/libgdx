package extra;

import static avian.Continuations.callWithCurrentContinuation;
import static avian.Continuations.dynamicWind;

import avian.CallbackReceiver;
import avian.Callback;

import java.util.concurrent.Callable;

public class DynamicWind {
  private int before;
  private int task;
  private int after;
  private int continuationCount;
  private Callback<Integer> continuationReference;

  private static void expect(boolean v) {
    if (! v) throw new RuntimeException();
  }

  private void unwindTest(final Callable<Integer> unwind) throws Exception {
    System.out.println("unwindTest enter");
            
    try {
      expect(dynamicWind(new Runnable() {
          public void run() {
            System.out.println("unwindTest before");

            expect(before == 0);
            expect(task == 0);
            expect(after == 0);

            before = 1;
          }
        }, new Callable<Integer>() {
            public Integer call() throws Exception {
              System.out.println("unwindTest thunk");
            
              expect(before == 1);
              expect(task == 0);
              expect(after == 0);
              
              task = 1;

              return unwind.call();
            }
          },
          new Runnable() {
            public void run() {
              System.out.println("unwindTest after");
            
              expect(before == 1);
              expect(task == 1);
              expect(after == 0);

              after = 1;
            }
          }) == 42);
    } catch (MyException e) {
      e.printStackTrace();
    }

    System.out.println("unwindTest expect");
            
    expect(before == 1);
    expect(task == 1);
    expect(after == 1);

    System.out.println("unwindTest exit");
  }

  private void normalUnwind() throws Exception {
    unwindTest(new Callable<Integer>() {
        public Integer call() {
          return 42;
        }
      });
  }

  private void exceptionUnwind() throws Exception {
    unwindTest(new Callable<Integer>() {
        public Integer call() throws Exception {
          throw new MyException();
        }
      });
  }

  private void continuationUnwindTest(final CallbackReceiver<Integer> receiver)
    throws Exception
  {
    System.out.println("continuationUnwindTest enter");
            
    try {
      expect(callWithCurrentContinuation(new CallbackReceiver<Integer>() {
            public Integer receive(final Callback<Integer> continuation)
              throws Exception
            {
              unwindTest(new Callable<Integer>() {
                  public Integer call() throws Exception {
                    return receiver.receive(continuation);
                  }
                });
              throw new AssertionError();
            }
          }) == 42);
    } catch (MyException e) {
      e.printStackTrace();
    }
    
    System.out.println("continuationUnwindTest expect");

    expect(before == 1);
    expect(task == 1);
    expect(after == 1);

    System.out.println("continuationUnwindTest exit");
  }

  private void continuationResultUnwind() throws Exception {
    continuationUnwindTest(new CallbackReceiver<Integer>() {
          public Integer receive(final Callback<Integer> continuation) {
            continuation.handleResult(42);
            throw new AssertionError();
          }
      });
  }

  private void continuationExceptionUnwind() throws Exception {
    continuationUnwindTest(new CallbackReceiver<Integer>() {
          public Integer receive(final Callback<Integer> continuation) {
            continuation.handleException(new MyException());
            throw new AssertionError();
          }
      });
  }

  private void rewindTest(final Callable<Integer> unwind, Runnable rewind)
    throws Exception
  {
    System.out.println("rewindTest enter");

    int value;
    try {
      value = dynamicWind(new Runnable() {
          public void run() {
            System.out.println("rewindTest before");
    
            expect(before == continuationCount);
            expect(task == continuationCount);
            expect(after == continuationCount);
          
            ++ before;
          }
        }, new Callable<Integer>() {
          public Integer call() throws Exception {
            System.out.println("rewindTest thunk");
    
            expect(before == 1);
            expect(task == 0);
            expect(after == 0);
          
            task = 1;
        
            return callWithCurrentContinuation
            (new CallbackReceiver<Integer>() {
              public Integer receive(final Callback<Integer> continuation)
                throws Exception
              {
                continuationReference = continuation;
                return unwind.call();
              }
            });
          }
        }, new Runnable() {
            public void run() {
              System.out.println("rewindTest after");
    
              expect(before == continuationCount + 1);
              expect(task == 1);
              expect(after == continuationCount);
            
              ++ after;
            }
          });
    } catch (MyException e) {
      value = e.value;
    }

    System.out.println("rewindTest expect");
    
    expect(value == continuationCount);
    
    if (value == 0) {
      System.out.println("rewindTest expect 0");
    
      expect(before == 1);
      expect(task == 1);
      expect(after == 1);

      continuationCount = 1;
      rewind.run();
      throw new AssertionError();
    } else {
      System.out.println("rewindTest expect 1");
    
      expect(value == 1);
      expect(before == 2);
      expect(task == 1);
      expect(after == 2);
    }

    System.out.println("rewindTest exit");
  }

  private void continuationResultRewind() throws Exception {
    rewindTest(new Callable<Integer>() {
        public Integer call() {
          return 0;
        }
      }, new Runnable() {
          public void run() {
            continuationReference.handleResult(1);
          }
        });
  }

  private void continuationExceptionRewind() throws Exception {
    rewindTest(new Callable<Integer>() {
        public Integer call() throws Exception {
          throw new MyException(0);
        }
      }, new Runnable() {
          public void run() {
            continuationReference.handleException(new MyException(1));
          }
        });
  }

  private void continuationResultUnwindAndRewind() throws Exception {
    rewindTest(new Callable<Integer>() {
        public Integer call() {
          return 0;
        }
      }, new Runnable() {
          public void run() {
            try {
              new DynamicWind().unwindTest(new Callable<Integer>() {
                  public Integer call() {
                    continuationReference.handleResult(1);
                    throw new AssertionError();
                  }
              });
            } catch (Exception e) {
              throw new RuntimeException(e);
            }
          }
        });
  }

  private void continuationExceptionUnwindAndRewind() throws Exception {
    rewindTest(new Callable<Integer>() {
        public Integer call() throws Exception {
          throw new MyException(0);
        }
      }, new Runnable() {
          public void run() {
            try {
              new DynamicWind().unwindTest(new Callable<Integer>() {
                  public Integer call() {
                    continuationReference.handleException(new MyException(1));
                    throw new AssertionError();
                  }
              });
            } catch (Exception e) {
              throw new RuntimeException(e);
            }
          }
        });
  }

  private void continuationResultUnwindAndRewindWithShared() throws Exception {
    unwindTest(new Callable<Integer>() {
        public Integer call() throws Exception {
          new DynamicWind().continuationResultUnwindAndRewind();
          return 42;
        }
      });
  }

  private void continuationExceptionUnwindAndRewindWithShared()
    throws Exception
  {
    unwindTest(new Callable<Integer>() {
        public Integer call() throws Exception {
          new DynamicWind().continuationExceptionUnwindAndRewind();
          return 42;
        }
      });
  }

  public static void main(String[] args) throws Exception {
    new DynamicWind().normalUnwind();

    new DynamicWind().exceptionUnwind();

    new DynamicWind().continuationResultUnwind();

    new DynamicWind().continuationExceptionUnwind();

    new DynamicWind().continuationResultRewind();

    new DynamicWind().continuationExceptionRewind();

    new DynamicWind().continuationResultUnwindAndRewind();

    new DynamicWind().continuationExceptionUnwindAndRewind();

    new DynamicWind().continuationResultUnwindAndRewindWithShared();

    new DynamicWind().continuationExceptionUnwindAndRewindWithShared();
  }

  private static class MyException extends Exception { 
    public final int value;

    public MyException() {
      this(0);
    }

    public MyException(int value) {
      this.value = value;
    }
  }
}
