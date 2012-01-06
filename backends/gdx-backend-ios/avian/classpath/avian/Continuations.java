/* Copyright (c) 2009-2010, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package avian;

import java.util.concurrent.Callable;

/**
 * This class provides methods to capture continuations and manage
 * control flow when calling continuations.
 *
 * <p>A continuation is a snapshot of a thread's call stack which can
 * be captured via <code>callWithCurrentContinuation</code> and later
 * restored any number of times.  The program may restore this
 * snapshot by either feeding it a result (to be returned by
 * <code>callWithCurrentContinuation</code>) or feeding it an
 * exception (to be thrown by
 * <code>callWithCurrentContinuation</code>).  Continuations may be
 * used to implement features such as coroutines, generators, and
 * cooperative multitasking.
 *
 * <p>This class provides two static methods,
 * <code>callWithCurrentContinuation</code> and
 * <code>dynamicWind</code>, with similar semantics to the Scheme
 * functions <code>call-with-current-continuation</code> and
 * <code>dynamic-wind</code>, respectively.  In addition, we define
 * how continuations work with respect to native code, exceptions,
 * try/finally blocks, synchronized blocks, and multithreading.
 *
 * <h3>Continuations and Continuation Contexts</h3>
 *
 * <p>A continuation can be thought of as a singly-linked list of
 * stack frames representing the call trace, where the head of the
 * list is the frame of the method most recently called (i.e. the top
 * of the stack).  However, this trace only extends as far as the most
 * recent chain of Java frames - it ends just prior to the most recent
 * native frame in the stack.  The reason for this is that the VM
 * cannot, in general, safely capture and restore native frames.
 * Therefore, each call from native code to Java (including the
 * original invocation of <code>main(String[])</code> or
 * <code>Thread.run()</code>) represents a new continuation context in
 * which continuations may be captured, and these will only contain
 * frames from within that context.
 *
 * <p>Calling a continuation (i.e. feeding it a result or exception)
 * causes the current continuation to be replaced with the called
 * continuation.  When the last method in this new continuation
 * returns, it returns to the native frame which created the current
 * context, which may or may not be the same as the context in which
 * that continuation was created.
 *
 * <p>We define the return type of a continuation context as the
 * return type of the first method called in that context.  A
 * continuation may be called from a different context than the one in
 * which it was created, provided the return type of the latter is
 * compatible with the current context.
 *
 * <p>Given a thread executing in context "A" which wants to call a
 * continuation created in context "B", the following rules apply:
 *
 * <ul>
 *
 *   <li>If the return type of "A" is <code>void</code>, the return
 *   type of "B" may be anything, including <code>void</code></li>
 *
 *   <li>If the return type of "A" is a primitive type, the return
 *   type of "B" must match exactly</li>
 *
 *   <li>If the return type of "A" is an object type, that type must
 *   be assignable from the return type of "B" (i.e. the latter must
 *   either be the same as the former or a superclass or
 *   superinterface of it)</li>
 *
 * </ul>
 *
 * <p>A thread may call a continuation created by a different thread
 * provided the return types are compatible.  Multiple threads may
 * safely call the same continuation simultaneously without
 * synchronization.  Any attempt to call a continuation from a context
 * with an incompatible return type will throw an {@link
 * avian.IncompatibleContinuationException}.
 *
 * <h3>Winding, Unwinding, and Rewinding</h3>
 *
 * <p>Traditionally, Java provides one way to wind the execution stack
 * (method calls) and two ways to unwind it (normal returns and
 * exception unwinding).  With continuations, we add a new way to
 * rewind the stack and a new way to unwind it.
 *
 * <p>The call stack of a continuation may share frames with other
 * continuations - in which case they share a common history.  When
 * calling a continuation "B" from the current continuation "A", the
 * VM must unwind past any frames which are in "A" but not in "B" and
 * rewind past any frames in "B" but not in "A".  During this
 * unwinding and rewinding, control may pass through synchronized and
 * try/finally blocks while going down the old stack and up the new
 * stack.
 *
 * <p>However, unlike the traditional processes of winding and
 * unwinding, the VM will ignore these blocks - monitors will not be
 * released or acquired and finally blocks will not execute.  This is
 * by design.  The purpose of such a block is to acquire a resource,
 * such as a file handle or monitor, once before executing a task and
 * release it after the task is finished, regardless of how often the
 * task might temporarily yield control to other continuations.
 *
 * <p>Alternatively, one might wish to acquire and release a resource
 * each time control (re)winds to or unwinds from a continuation,
 * respectively.  In this case, one may use <code>dynamicWind</code>
 * to register functions which will run every time that frame is
 * passed, regardless of how the stack is wound or unwound.
 */
public class Continuations {
  private Continuations() { }

  /**
   * Captures the current continuation, passing a reference to the
   * specified receiver.
   *
   * <p>This method will either return the result returned by
   * <code>receiver.receive(Callback)</code>, propagate the exception
   * thrown by that method, return the result passed to the
   * handleResult(T) method of the continuation, or throw the
   * exception passed to the handleException(Throwable) method of the
   * continuation.
   */
  public static native <T> T callWithCurrentContinuation
    (CallbackReceiver<T> receiver) throws Exception;

  /**
   * Calls the specified "before" and "after" tasks each time a
   * continuation containing the call is wound or unwound,
   * respectively.
   *
   * <p>This method first calls <code>before.run()</code>, then
   * <code>thunk.call()</code>, and finally <code>after.run()</code>,
   * returning the result of the second call.  If
   * <code>before.run()</code> does not return normally, the second
   * and third calls will not happen.  If <code>thunk.call()</code>
   * throws an exception, <code>after.run()</code>, will be called
   * before the exception is propagated.
   *
   * <p>If <code>thunk.call()</code> calls a continuation (directly or
   * via a subroutine) which does not include the current call to
   * <code>dynamicWind</code>, <code>after.run()</code> will be called
   * before control passes to that continuation.  If this call throws
   * an exception, the exception will propagate to the current caller
   * of <code>dynamicWind</code>.
   *
   * <p>If <code>thunk.call()</code> creates a continuation which is
   * later called from a continuation which does not include the
   * current call to <code>dynamicWind</code>,
   * <code>before.run()</code> will be called before control passes to
   * that continuation.  As above, if this call throws an exception,
   * the exception will propagate to the current caller of
   * <code>dynamicWind</code>.
   */
  public static <T> T dynamicWind(Runnable before,
                                  Callable<T> thunk,
                                  Runnable after)
    throws Exception
  {
    UnwindResult result = dynamicWind2(before, thunk, after);
    if (result.continuation != null) {
      after.run();
      if (result.exception != null) {
        result.continuation.handleException(result.exception);
      } else {
        result.continuation.handleResult(result.result);
      }
      throw new AssertionError();
    } else {
      return (T) result.result;
    }
  }

  private static native UnwindResult dynamicWind2(Runnable before,
                                                  Callable thunk,
                                                  Runnable after)
    throws Exception;

  private static UnwindResult wind(Runnable before,
                                   Callable thunk,
                                   Runnable after)
    throws Exception
  {
    before.run();

    try {
      return new UnwindResult(null, thunk.call(), null);
    } finally {
      after.run();
    }
  }

  private static void rewind(Runnable before,
                             Callback continuation,
                             Object result,
                             Throwable exception)
    throws Exception
  {
    before.run();
    
    if (exception != null) {
      continuation.handleException(exception);
    } else {
      continuation.handleResult(result);
    }

    throw new AssertionError();
  }

  private static class Continuation<T> implements Callback<T> {
    public native void handleResult(T result);
    public native void handleException(Throwable exception);
  }

  private static class UnwindResult {
    public final Callback continuation;
    public final Object result;
    public final Throwable exception;

    public UnwindResult(Callback continuation, Object result,
                        Throwable exception)
    {
      this.continuation = continuation;
      this.result = result;
      this.exception = exception;
    }
  }
}
