#include "Thread.hpp"

static void* runThread(void* arg) {
    ((Thread*) arg)->internalRun();
    return NULL;
}

Thread::Thread() {
    threadId = 0;
    running = false;
    detached = false;
}

Thread::~Thread() {
    if (running && !detached) {
        pthread_detach(threadId);
    }
    if (running) {
        pthread_cancel(threadId);
    }
}

bool Thread::start() {
    int result = pthread_create(&threadId, NULL, runThread, this);
    if (result == 0) {
        running = true;
    }
    return result == 0;
}

bool Thread::detach() {
    int result = -1;
    if (running && !detached) {
        result = pthread_detach(threadId);
        if (result == 0) {
            detached = true;
        }
    }
    return result == 0;
}

bool Thread::join() {
    int result = -1;
    if (running && !detached) {
        result = pthread_join(threadId, NULL);
        if (result == 0) {
            detached = true;
        }
    }
    return result == 0;
}

bool Thread::isRunning() {
    return running;
}

pthread_t Thread::getId() {
    return threadId;
}

void Thread::internalRun() {
    this->run();
    running = false;
}
