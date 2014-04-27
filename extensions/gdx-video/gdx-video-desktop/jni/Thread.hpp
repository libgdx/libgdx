#pragma once

#include <pthread.h>

class Thread {
public:
	Thread();
	virtual ~Thread();

	bool start();
	bool join();
	bool detach();
	pthread_t self();

	bool isRunning();

    void internalRun();
	virtual void run() = 0;

private:
	pthread_t threadId;
	bool running;
	bool detached;
};
