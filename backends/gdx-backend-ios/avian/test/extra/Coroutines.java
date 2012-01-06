package extra;

import static avian.Continuations.callWithCurrentContinuation;

import avian.CallbackReceiver;
import avian.Callback;

public class Coroutines {
  private static void expect(boolean v) {
    if (! v) throw new RuntimeException();
  }

  private static void produce(Consumer<Character> consumer) throws Exception {
    System.out.println("produce \"a\"");
    consumer.consume('a');

    System.out.println("produce \"b\"");
    consumer.consume('b');

    System.out.println("produce \"c\"");
    consumer.consume('c');
  }

  private static void consume(Producer<Character> producer) throws Exception {
    char v = producer.produce();
    System.out.println("consume \"" + v + "\"");
    expect(v == 'a');

    v = producer.produce();
    System.out.println("consume \"" + v + "\"");
    expect(v == 'b');

    v = producer.produce();
    System.out.println("consume \"" + v + "\"");
    expect(v == 'c');
  }

  public static void main(String[] args) throws Exception {
    final CoroutineState<Character> state = new CoroutineState<Character>();

    final Consumer<Character> consumer = new Consumer<Character>() {
      public void consume(final Character c) throws Exception {
        callWithCurrentContinuation(new CallbackReceiver() {
          public Object receive(Callback continuation) {
            state.produceNext = continuation;

            state.consumeNext.handleResult(c);

            throw new AssertionError();
          }
        });
      }
    };

    final Producer<Character> producer = new Producer<Character>() {
      final CallbackReceiver<Character> receiver
      = new CallbackReceiver<Character>() {
        public Character receive(Callback<Character> continuation)
        throws Exception
        {
          state.consumeNext = continuation;
          
          if (state.produceNext == null) {
            Coroutines.produce(consumer);
          } else {
            state.produceNext.handleResult(null);
          }
          
          throw new AssertionError();
        }
      };

      public Character produce() throws Exception {
        return callWithCurrentContinuation(receiver);
      }
    };

    consume(producer);
  }

  private static class CoroutineState<T> {
    public Callback produceNext;
    public Callback<T> consumeNext;
  }

  private interface Producer<T> {
    public T produce() throws Exception;
  }

  private interface Consumer<T> {
    public void consume(T value) throws Exception;
  }
}
