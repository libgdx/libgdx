package org.jbox2d.particle;


public class StackQueue<T> {

  private T[] m_buffer;
  private int m_front;
  private int m_back;
  private int m_end;

  public StackQueue() {}

  public void reset(T[] buffer) {
    m_buffer = buffer;
    m_front = 0;
    m_back = 0;
    m_end = buffer.length;
  }

  public void push(T task) {
    if (m_back >= m_end) {
      System.arraycopy(m_buffer, m_front, m_buffer, 0, m_back - m_front);
      m_back -= m_front;
      m_front = 0;
      if (m_back >= m_end) {
        return;
      }
    }
    m_buffer[m_back++] = task;
  }

  public T pop() {
    assert (m_front < m_back);
    return m_buffer[m_front++];
  }

  public boolean empty() {
    return m_front >= m_back;
  }

  public T front() {
    return m_buffer[m_front];
  }
}
