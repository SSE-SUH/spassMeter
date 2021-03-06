// Copyright 2008, Google Inc.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are met:
//
//  1. Redistributions of source code must retain the above copyright notice,
//     this list of conditions and the following disclaimer.
//  2. Redistributions in binary form must reproduce the above copyright notice,
//     this list of conditions and the following disclaimer in the documentation
//     and/or other materials provided with the distribution.
//  3. Neither the name of Google Inc. nor the names of its contributors may be
//     used to endorse or promote products derived from this software without
//     specific prior written permission.
//
// THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
// WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
// MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
// EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
// SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
// PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
// OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
// WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
// OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
// ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

#ifndef GEARS_BASE_COMMON_THREAD_POSIX_H__
#define GEARS_BASE_COMMON_THREAD_POSIX_H__

// POSIX systems.
#if defined(LINUX) || defined(OS_MACOSX) || defined(OS_ANDROID)

#include <pthread.h>
#include "gears/base/common/thread.h"

// POSIX implementation of ThreadInternal.
class Thread::ThreadInternal {
 public:
  ThreadInternal();
  ~ThreadInternal();
  
  // Start a new thread. On success, the child thread will invoke
  // thread->ThreadMain() and return true. Returns false on failure.
  bool Start(Thread *thread);
  // Wait for thread termination and clean up thread resources. In the
  // case of POSIX, this will most importantly free its stack. Must be
  // called before destruction if a thread was successfully created.
  void Join();

 private:
  // POSIX thread handle.
  pthread_t handle_;

  // Function called on the child thread. This will invoke
  // thread->ThreadMain().
  static void *ThreadRun(void *data);
};

#endif // defined(LINUX) || defined(OS_MACOSX) || defined(OS_ANDROID)

#endif  // GEARS_BASE_COMMON_THREAD_POSIX_H__
