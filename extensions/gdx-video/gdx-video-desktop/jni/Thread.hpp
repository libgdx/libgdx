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
 * @brief A c++ wrapper around the pthread thread.
 * 
 * @author Rob Bogie <rob.bogie@codepoke.net>
 */
class Thread {
public:
	Thread();
	virtual ~Thread();

	bool start();
	bool join();
	bool detach();
	pthread_t getId();

	bool isRunning();

    void internalRun();
	virtual void run() = 0;

private:
	pthread_t threadId;
	bool running;
	bool detached;
};
