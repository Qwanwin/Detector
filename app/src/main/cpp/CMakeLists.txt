cmake_minimum_required(VERSION 3.22.1)
project("detector")

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

include_directories(${CMAKE_SOURCE_DIR})

add_library(detector SHARED
            detector.cpp
            dev/Qwanwin/implement/antivpn.cpp
            dev/Qwanwin/implement/antihook.cpp
            dev/Qwanwin/implement/antiroot.cpp)

find_library(log-lib log)
target_link_libraries(detector ${log-lib})