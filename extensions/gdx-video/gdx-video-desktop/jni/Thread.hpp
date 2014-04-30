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
