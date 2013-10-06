#!/usr/bin/python

__author__ = "Jon Renner"

"""
This script will find all source files with the specified extensions (see the variable 'source_extension')
that do not contain the Apache license header.  If you wish, it will then insert the header at the beginning.
Please be aware that the HEADER as specified in the variable below requires a language that accepts C style
comment formatting (/* --> */), and as such would not work in languages such as Python without further
changes to this script.
"""

import os

current_dir = os.getcwd()

source_extensions = ['java']
apache_test_string = 'Licensed under the Apache License, Version 2.0 (the "License")'

HEADER = """/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
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

 """

def main():
    answer = raw_input("find all source files without Apache license header in directory '%s'?: " % current_dir)
    if not is_answer_yes(answer):
        print "exit"
        return
    with_header = 0
    without_header = 0
    without_files = []
    for dirpath, dirnames, filenames in os.walk(current_dir):
        for filename in filenames:
            if not is_source_file(filename):
                continue
            match_path = os.path.join(dirpath, filename)
            has_header = check_for_apache_header(match_path)
            if not has_header:
                print "file without header: %s (%s)" % (filename, match_path)
                without_header += 1
                without_files.append(match_path)
            else:
                with_header +=1
    print "found %d files with the header" % with_header
    print "found %d files without the header" % without_header

    answer = raw_input("Insert header into files without header (suggest git commit or new branch first!) ?: ")
    if is_answer_yes(answer):
        for without_file in without_files:
            insert_header(without_file)

def is_answer_yes(answer):
    answer = answer.lower()
    if len(answer) > 0 and answer[0] == 'y':
        return True
    return False


def check_for_apache_header(match_path):
    with open(match_path, "r") as checkfile:
        data = checkfile.read()
        if data.find(apache_test_string) == -1:
            return False
    return True

def is_source_file(filename):
    pieces = filename.split(".")
    if len(pieces) < 2:
        return False
    if pieces[1] in source_extensions:
        return True
    return False

def insert_header(filename):
    print ("inserting header into: " + filename)
    with open(filename, "r+") as insert_file:
        data = insert_file.read()
        new_data = HEADER + data
        insert_file.seek(0)
        insert_file.write(new_data)


if __name__ == "__main__":
    main()