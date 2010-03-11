/*
 * libmad - MPEG audio decoder library
 * Copyright (C) 2000-2004 Underbit Technologies, Inc.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * $Id: decoder.c,v 1.22 2004/01/23 09:41:32 rob Exp $
 */

# ifdef HAVE_CONFIG_H
#  include "config.h"
# endif

# include "global.h"

# ifdef HAVE_SYS_TYPES_H
#  include <sys/types.h>
# endif

# ifdef HAVE_SYS_WAIT_H
#  include <sys/wait.h>
# endif

# ifdef HAVE_UNISTD_H
#  include <unistd.h>
# endif

# ifdef HAVE_FCNTL_H
#  include <fcntl.h>
# endif

# include <stdlib.h>

# ifdef HAVE_ERRNO_H
#  include <errno.h>
# endif

# include "stream.h"
# include "frame.h"
# include "synth.h"
# include "decoder.h"

/*
 * NAME:	decoder->init()
 * DESCRIPTION:	initialize a decoder object with callback routines
 */
void mad_decoder_init(struct mad_decoder *decoder, void *data,
		      enum mad_flow (*input_func)(void *,
						  struct mad_stream *),
		      enum mad_flow (*header_func)(void *,
						   struct mad_header const *),
		      enum mad_flow (*filter_func)(void *,
						   struct mad_stream const *,
						   struct mad_frame *),
		      enum mad_flow (*output_func)(void *,
						   struct mad_header const *,
						   struct mad_pcm *),
		      enum mad_flow (*error_func)(void *,
						  struct mad_stream *,
						  struct mad_frame *),
		      enum mad_flow (*message_func)(void *,
						    void *, unsigned int *))
{
  decoder->mode         = -1;

  decoder->options      = 0;

  decoder->async.pid    = 0;
  decoder->async.in     = -1;
  decoder->async.out    = -1;

  decoder->sync         = 0;

  decoder->cb_data      = data;

  decoder->input_func   = input_func;
  decoder->header_func  = header_func;
  decoder->filter_func  = filter_func;
  decoder->output_func  = output_func;
  decoder->error_func   = error_func;
  decoder->message_func = message_func;
}

int mad_decoder_finish(struct mad_decoder *decoder)
{
# if defined(USE_ASYNC)
  if (decoder->mode == MAD_DECODER_MODE_ASYNC && decoder->async.pid) {
    pid_t pid;
    int status;

    close(decoder->async.in);

    do
      pid = waitpid(decoder->async.pid, &status, 0);
    while (pid == -1 && errno == EINTR);

    decoder->mode = -1;

    close(decoder->async.out);

    decoder->async.pid = 0;
    decoder->async.in  = -1;
    decoder->async.out = -1;

    if (pid == -1)
      return -1;

    return (!WIFEXITED(status) || WEXITSTATUS(status)) ? -1 : 0;
  }
# endif

  return 0;
}

# if defined(USE_ASYNC)
static
enum mad_flow send_io(int fd, void const *data, size_t len)
{
  char const *ptr = data;
  ssize_t count;

  while (len) {
    do
      count = write(fd, ptr, len);
    while (count == -1 && errno == EINTR);

    if (count == -1)
      return MAD_FLOW_BREAK;

    len -= count;
    ptr += count;
  }

  return MAD_FLOW_CONTINUE;
}

static
enum mad_flow receive_io(int fd, void *buffer, size_t len)
{
  char *ptr = buffer;
  ssize_t count;

  while (len) {
    do
      count = read(fd, ptr, len);
    while (count == -1 && errno == EINTR);

    if (count == -1)
      return (errno == EAGAIN) ? MAD_FLOW_IGNORE : MAD_FLOW_BREAK;
    else if (count == 0)
      return MAD_FLOW_STOP;

    len -= count;
    ptr += count;
  }

  return MAD_FLOW_CONTINUE;
}

static
enum mad_flow receive_io_blocking(int fd, void *buffer, size_t len)
{
  int flags, blocking;
  enum mad_flow result;

  flags = fcntl(fd, F_GETFL);
  if (flags == -1)
    return MAD_FLOW_BREAK;

  blocking = flags & ~O_NONBLOCK;

  if (blocking != flags &&
      fcntl(fd, F_SETFL, blocking) == -1)
    return MAD_FLOW_BREAK;

  result = receive_io(fd, buffer, len);

  if (flags != blocking &&
      fcntl(fd, F_SETFL, flags) == -1)
    return MAD_FLOW_BREAK;

  return result;
}

static
enum mad_flow send(int fd, void const *message, unsigned int size)
{
  enum mad_flow result;

  /* send size */

  result = send_io(fd, &size, sizeof(size));

  /* send message */

  if (result == MAD_FLOW_CONTINUE)
    result = send_io(fd, message, size);

  return result;
}

static
enum mad_flow receive(int fd, void **message, unsigned int *size)
{
  enum mad_flow result;
  unsigned int actual;

  if (*message == 0)
    *size = 0;

  /* receive size */

  result = receive_io(fd, &actual, sizeof(actual));

  /* receive message */

  if (result == MAD_FLOW_CONTINUE) {
    if (actual > *size)
      actual -= *size;
    else {
      *size  = actual;
      actual = 0;
    }

    if (*size > 0) {
      if (*message == 0) {
	*message = malloc(*size);
	if (*message == 0)
	  return MAD_FLOW_BREAK;
      }

      result = receive_io_blocking(fd, *message, *size);
    }

    /* throw away remainder of message */

    while (actual && result == MAD_FLOW_CONTINUE) {
      char sink[256];
      unsigned int len;

      len = actual > sizeof(sink) ? sizeof(sink) : actual;

      result = receive_io_blocking(fd, sink, len);

      actual -= len;
    }
  }

  return result;
}

static
enum mad_flow check_message(struct mad_decoder *decoder)
{
  enum mad_flow result;
  void *message = 0;
  unsigned int size;

  result = receive(decoder->async.in, &message, &size);

  if (result == MAD_FLOW_CONTINUE) {
    if (decoder->message_func == 0)
      size = 0;
    else {
      result = decoder->message_func(decoder->cb_data, message, &size);

      if (result == MAD_FLOW_IGNORE ||
	  result == MAD_FLOW_BREAK)
	size = 0;
    }

    if (send(decoder->async.out, message, size) != MAD_FLOW_CONTINUE)
      result = MAD_FLOW_BREAK;
  }

  if (message)
    free(message);

  return result;
}
# endif

static
enum mad_flow error_default(void *data, struct mad_stream *stream,
			    struct mad_frame *frame)
{
  int *bad_last_frame = data;

  switch (stream->error) {
  case MAD_ERROR_BADCRC:
    if (*bad_last_frame)
      mad_frame_mute(frame);
    else
      *bad_last_frame = 1;

    return MAD_FLOW_IGNORE;

  default:
    return MAD_FLOW_CONTINUE;
  }
}

static
int run_sync(struct mad_decoder *decoder)
{
  enum mad_flow (*error_func)(void *, struct mad_stream *, struct mad_frame *);
  void *error_data;
  int bad_last_frame = 0;
  struct mad_stream *stream;
  struct mad_frame *frame;
  struct mad_synth *synth;
  int result = 0;

  if (decoder->input_func == 0)
    return 0;

  if (decoder->error_func) {
    error_func = decoder->error_func;
    error_data = decoder->cb_data;
  }
  else {
    error_func = error_default;
    error_data = &bad_last_frame;
  }

  stream = &decoder->sync->stream;
  frame  = &decoder->sync->frame;
  synth  = &decoder->sync->synth;

  mad_stream_init(stream);
  mad_frame_init(frame);
  mad_synth_init(synth);

  mad_stream_options(stream, decoder->options);

  do {
    switch (decoder->input_func(decoder->cb_data, stream)) {
    case MAD_FLOW_STOP:
      goto done;
    case MAD_FLOW_BREAK:
      goto fail;
    case MAD_FLOW_IGNORE:
      continue;
    case MAD_FLOW_CONTINUE:
      break;
    }

    while (1) {
# if defined(USE_ASYNC)
      if (decoder->mode == MAD_DECODER_MODE_ASYNC) {
	switch (check_message(decoder)) {
	case MAD_FLOW_IGNORE:
	case MAD_FLOW_CONTINUE:
	  break;
	case MAD_FLOW_BREAK:
	  goto fail;
	case MAD_FLOW_STOP:
	  goto done;
	}
      }
# endif

      if (decoder->header_func) {
	if (mad_header_decode(&frame->header, stream) == -1) {
	  if (!MAD_RECOVERABLE(stream->error))
	    break;

	  switch (error_func(error_data, stream, frame)) {
	  case MAD_FLOW_STOP:
	    goto done;
	  case MAD_FLOW_BREAK:
	    goto fail;
	  case MAD_FLOW_IGNORE:
	  case MAD_FLOW_CONTINUE:
	  default:
	    continue;
	  }
	}

	switch (decoder->header_func(decoder->cb_data, &frame->header)) {
	case MAD_FLOW_STOP:
	  goto done;
	case MAD_FLOW_BREAK:
	  goto fail;
	case MAD_FLOW_IGNORE:
	  continue;
	case MAD_FLOW_CONTINUE:
	  break;
	}
      }

      if (mad_frame_decode(frame, stream) == -1) {
	if (!MAD_RECOVERABLE(stream->error))
	  break;

	switch (error_func(error_data, stream, frame)) {
	case MAD_FLOW_STOP:
	  goto done;
	case MAD_FLOW_BREAK:
	  goto fail;
	case MAD_FLOW_IGNORE:
	  break;
	case MAD_FLOW_CONTINUE:
	default:
	  continue;
	}
      }
      else
	bad_last_frame = 0;

      if (decoder->filter_func) {
	switch (decoder->filter_func(decoder->cb_data, stream, frame)) {
	case MAD_FLOW_STOP:
	  goto done;
	case MAD_FLOW_BREAK:
	  goto fail;
	case MAD_FLOW_IGNORE:
	  continue;
	case MAD_FLOW_CONTINUE:
	  break;
	}
      }

      mad_synth_frame(synth, frame);

      if (decoder->output_func) {
	switch (decoder->output_func(decoder->cb_data,
				     &frame->header, &synth->pcm)) {
	case MAD_FLOW_STOP:
	  goto done;
	case MAD_FLOW_BREAK:
	  goto fail;
	case MAD_FLOW_IGNORE:
	case MAD_FLOW_CONTINUE:
	  break;
	}
      }
    }
  }
  while (stream->error == MAD_ERROR_BUFLEN);

 fail:
  result = -1;

 done:
  mad_synth_finish(synth);
  mad_frame_finish(frame);
  mad_stream_finish(stream);

  return result;
}

# if defined(USE_ASYNC)
static
int run_async(struct mad_decoder *decoder)
{
  pid_t pid;
  int ptoc[2], ctop[2], flags;

  if (pipe(ptoc) == -1)
    return -1;

  if (pipe(ctop) == -1) {
    close(ptoc[0]);
    close(ptoc[1]);
    return -1;
  }

  flags = fcntl(ptoc[0], F_GETFL);
  if (flags == -1 ||
      fcntl(ptoc[0], F_SETFL, flags | O_NONBLOCK) == -1) {
    close(ctop[0]);
    close(ctop[1]);
    close(ptoc[0]);
    close(ptoc[1]);
    return -1;
  }

  pid = fork();
  if (pid == -1) {
    close(ctop[0]);
    close(ctop[1]);
    close(ptoc[0]);
    close(ptoc[1]);
    return -1;
  }

  decoder->async.pid = pid;

  if (pid) {
    /* parent */

    close(ptoc[0]);
    close(ctop[1]);

    decoder->async.in  = ctop[0];
    decoder->async.out = ptoc[1];

    return 0;
  }

  /* child */

  close(ptoc[1]);
  close(ctop[0]);

  decoder->async.in  = ptoc[0];
  decoder->async.out = ctop[1];

  _exit(run_sync(decoder));

  /* not reached */
  return -1;
}
# endif

/*
 * NAME:	decoder->run()
 * DESCRIPTION:	run the decoder thread either synchronously or asynchronously
 */
int mad_decoder_run(struct mad_decoder *decoder, enum mad_decoder_mode mode)
{
  int result;
  int (*run)(struct mad_decoder *) = 0;

  switch (decoder->mode = mode) {
  case MAD_DECODER_MODE_SYNC:
    run = run_sync;
    break;

  case MAD_DECODER_MODE_ASYNC:
# if defined(USE_ASYNC)
    run = run_async;
# endif
    break;
  }

  if (run == 0)
    return -1;

  decoder->sync = malloc(sizeof(*decoder->sync));
  if (decoder->sync == 0)
    return -1;

  result = run(decoder);

  free(decoder->sync);
  decoder->sync = 0;

  return result;
}

/*
 * NAME:	decoder->message()
 * DESCRIPTION:	send a message to and receive a reply from the decoder process
 */
int mad_decoder_message(struct mad_decoder *decoder,
			void *message, unsigned int *len)
{
# if defined(USE_ASYNC)
  if (decoder->mode != MAD_DECODER_MODE_ASYNC ||
      send(decoder->async.out, message, *len) != MAD_FLOW_CONTINUE ||
      receive(decoder->async.in, &message, len) != MAD_FLOW_CONTINUE)
    return -1;

  return 0;
# else
  return -1;
# endif
}
