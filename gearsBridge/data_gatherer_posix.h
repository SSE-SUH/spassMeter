
#ifndef DATA_GATHERER_POSIX_H_
#define DATA_GATHERER_POSIX_H_

#define PROC_PID_STAT "/proc/%i/stat"
#define PROC_PID_NET  "/proc/%i/net"
#define PROC_NET_DEV  "/proc/net/dev"

#ifdef VAR_ARBITRARY_PROCESS_DATA
FILE* getProcFd(const char* namePattern, pid_t pid);
#endif

#endif /* DATA_GATHERER_POSIX_H_ */
