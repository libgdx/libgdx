#pragma once

#include <pthread.h>

/**
 * @brief A c++ wrapper around the pthread mutex.
 * 
 * @author Rob Bogie <rob.bogie@codepoke.net>
 */
class Mutex {
    friend class CondVar;
public:
    Mutex() {
        pthread_mutex_init(&mutex, NULL);
    }

    Mutex(bool recursive){
		if(recursive) {
    		pthread_mutexattr_t mta;
    		pthread_mutexattr_init(&mta);
    		pthread_mutexattr_settype(&mta, PTHREAD_MUTEX_RECURSIVE);
			pthread_mutex_init(&mutex, &mta);
		} else {
			pthread_mutex_init(&mutex, NULL);
        }
	}
    virtual ~Mutex() {
        pthread_mutex_destroy(&mutex);
    }

    int lock() {
        return pthread_mutex_lock(&mutex);
    }
    int trylock() {
        return pthread_mutex_trylock(&mutex);
    }
    int unlock() {
        return pthread_mutex_unlock(&mutex);
    }
private:
    pthread_mutex_t  mutex;
};
