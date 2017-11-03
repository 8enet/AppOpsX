cmake_minimum_required(VERSION 3.4.1)

project(opsxstart)

set(CMAKE_C_STANDARD 99)

set(CMAKE_RUNTIME_OUTPUT_DIRECTORY ${CMAKE_LIBRARY_OUTPUT_DIRECTORY})

set(SOURCE_FILES start_daemon.c)

add_executable(libopsxstart.so
                ${SOURCE_FILES})