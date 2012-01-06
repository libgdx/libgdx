#define JNI_OnLoad net_JNI_OnLoad
#include "net_util.c"

#ifdef _WIN32

#undef  IN6_SET_ADDR_UNSPECIFIED
#define IN6_SET_ADDR_UNSPECIFIED(a) \
  memset((a)->s6_bytes,0,sizeof(struct in6_addr))

void
IN6ADDR_SETANY(struct sockaddr_in6 *a)
{
  a->sin6_family = AF_INET6;
  a->sin6_port = 0;
  a->sin6_flowinfo = 0;
  IN6_SET_ADDR_UNSPECIFIED(&a->sin6_addr);
  a->sin6_scope_id = 0;
}

#endif
