#pragma once

#include "Mutex.hpp"

/**
 * @brief A c++ wrapper around the pthread conditional.
 * 
 * @author Rob Bogie <rob.bogie@codepoke.net>
 */
class CondVar {
public:
    // just initialize to defaults
    CondVar(Mutex& mutex) : lock(mutex) {
        pthread_cond_init(&cond, NULL);
    }
    virtual ~CondVar() {
        pthread_cond_destroy(&cond);
    }

    int wait() {
        return pthread_cond_wait(&cond, &(lock.mutex));
    }
    int wait(const timespec * timeout) {
        return  pthread_cond_timedwait(&cond, &(lock.mutex), timeout);
    }
    int signal() {
        return pthread_cond_signal(&cond);
    }
    int broadcast() {
        return pthread_cond_broadcast(&cond);
    }

private:
    pthread_cond_t  cond;
    Mutex&          lock;
};
