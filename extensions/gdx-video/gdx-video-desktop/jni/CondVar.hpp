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
