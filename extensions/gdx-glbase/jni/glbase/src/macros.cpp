/**
 * @file macros.cpp
 * @brief Set of common usage macros and utilities
 *
 **/

#include "macros.h"
#include "glbase.h"
#include "arrays.h"

#include <string.h>

using namespace std;

vector<string> split(const string &str, const string &delim){
  vector<string> res;
  size_t current = 0, found, delimlen = delim.size();
  while((found = str.find(delim, current)) != string::npos){
    res.push_back(string(str, current, found - current));
    current = found + delimlen;
  }
  res.push_back(string(str, current, str.size() - current));
  return res;
}


char* strdup2(const char* string)
{
  char* res = new char[strlen(string)+1];
  strcpy(res, string);
  return res;
}
