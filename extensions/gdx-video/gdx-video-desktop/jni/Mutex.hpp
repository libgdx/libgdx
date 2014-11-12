/*******************************************************************************
 * Copyright 2014 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/


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
