# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.
cmake_minimum_required(VERSION 3.4.1)

#设置生成的so动态库最后输出的路径
set(CMAKE_VERBOSE_MAKEFILE on)
set(CMAKE_LIBRARY_OUTPUT_DIRECTORY ${CMAKE_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI})
set(LOCAL_LDLIBS  "-llog -lGLESv1_CM -ldl -lz")
set(LAME_LIBMP3_DIR ${CMAKE_SOURCE_DIR}/src/main/cpp/libmp3lame)
# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.
#动态方式加载 STATIC：表示静态的.a的库 SHARED：表示.so的库。
add_library(mp3lame

        SHARED

        ${LAME_LIBMP3_DIR}/bitstream.c
        ${LAME_LIBMP3_DIR}/encoder.c
        ${LAME_LIBMP3_DIR}/fft.c
        ${LAME_LIBMP3_DIR}/gain_analysis.c
        ${LAME_LIBMP3_DIR}/id3tag.c
        ${LAME_LIBMP3_DIR}/lame.c
        ${LAME_LIBMP3_DIR}/mpglib_interface.c
        ${LAME_LIBMP3_DIR}/newmdct.c
        ${LAME_LIBMP3_DIR}/presets.c
        ${LAME_LIBMP3_DIR}/psymodel.c
        ${LAME_LIBMP3_DIR}/quantize.c
        ${LAME_LIBMP3_DIR}/quantize_pvt.c
        ${LAME_LIBMP3_DIR}/reservoir.c
        ${LAME_LIBMP3_DIR}/set_get.c
        ${LAME_LIBMP3_DIR}/tables.c
        ${LAME_LIBMP3_DIR}/takehiro.c
        ${LAME_LIBMP3_DIR}/util.c
        ${LAME_LIBMP3_DIR}/vbrquantize.c
        ${LAME_LIBMP3_DIR}/VbrTag.c
        ${LAME_LIBMP3_DIR}/version.c
        )

add_library(
        mp3encode
        SHARED
        ${CMAKE_SOURCE_DIR}/src/main/cpp/mp3encode.cpp
)

#设置要连接的so的相对路径 ${CMAKE_SOURCE_DIR}：表示CMake.txt的当前文件夹路径 ${ANDROID_ABI}：编译时会自动根据CPU架构去选择相应的库
#set_target_properties(qrdatadecode PROPERTIES IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI}/libqrdatadecode.so)
#set_target_properties(avcodec PROPERTIES IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/libs/${ANDROID_ABI}/libavcodec.so)
#set_target_properties(avfilter PROPERTIES IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/libs/${ANDROID_ABI}/libavfilter.so)
#set_target_properties(avformat PROPERTIES IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/libs/${ANDROID_ABI}/libavformat.so)
#set_target_properties(avutil PROPERTIES IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/libs/${ANDROID_ABI}/libavutil.so)
#set_target_properties(swresample PROPERTIES IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/libs/${ANDROID_ABI}/libswresample.so)
#set_target_properties(swscale PROPERTIES IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/libs/${ANDROID_ABI}/libswscale.so)

#添加第三方头文件
target_include_directories(mp3lame PRIVATE ${CMAKE_SOURCE_DIR}/src/main/cpp/libmp3lame)
target_include_directories(mp3encode PRIVATE ${CMAKE_SOURCE_DIR}/src/main/cpp/include)
# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
        mp3encode
        android

        mp3lame
        # Links the target library to the log library
        # included in the NDK.
        ${log-lib})